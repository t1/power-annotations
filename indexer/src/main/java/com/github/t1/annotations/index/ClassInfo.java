package com.github.t1.annotations.index;

import org.jboss.jandex.ClassType;
import org.jboss.jandex.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

import static com.github.t1.annotations.index.MethodInfo.signature;
import static com.github.t1.annotations.index.Utils.toDotName;
import static java.lang.annotation.ElementType.TYPE;
import static java.util.Objects.requireNonNull;

public class ClassInfo extends AnnotationTarget {
    public static Class<?> toClass(Object value) {
        String className = ((ClassType) value).name().toString();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            // TODO does this work in Quarkus?
            return Class.forName(className, true, loader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("class not found '" + className + "'", e);
        }
    }


    private final org.jboss.jandex.ClassInfo delegate;
    private final Map<String, FieldInfo> fields = new TreeMap<>();
    private final Map<String, MethodInfo> methods = new TreeMap<>();

    ClassInfo(Index index, org.jboss.jandex.ClassInfo delegate) {
        super(index);
        this.delegate = requireNonNull(delegate);
    }

    @Override public String toString() { return delegate.toString(); }

    @Override public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        ClassInfo that = (ClassInfo) other;
        return delegate.name().equals(that.delegate.name());
    }

    @Override public int hashCode() { return delegate.name().hashCode(); }


    @Override public ElementType elementType() { return TYPE; }

    @Override public String name() { return delegate.name().toString(); }

    public String simpleName() { return delegate.simpleName(); }

    @Override protected Stream<org.jboss.jandex.AnnotationInstance> rawAnnotations() {
        return delegate.classAnnotations().stream();
    }

    public boolean isAnnotationType() {
        return implementsInterface(Annotation.class.getName());
    }

    public boolean implementsInterface(String typeName) {
        return delegate.interfaceNames().contains(toDotName(typeName));
    }

    public boolean isImplicitlyAllowedOn(ElementType elementType) {
        return isAllowedOn(elementType, true);
    }

    public boolean isExplicitlyAllowedOn(ElementType elementType) {
        return isAllowedOn(elementType, false);
    }

    private boolean isAllowedOn(ElementType elementType, boolean defaultValue) {
        String targetTypeName = elementType.name();
        return annotations(Target.class.getName())
            .findAny()
            .map(targetAnnotation -> {
                // TODO resolve jandex AnnotationValues in our own AnnotationValue
                org.jboss.jandex.AnnotationValue[] allowedTargets = (org.jboss.jandex.AnnotationValue[])
                    targetAnnotation.value("value").value();
                return Stream.of(allowedTargets)
                    .map(org.jboss.jandex.AnnotationValue::value)
                    .anyMatch(targetTypeName::equals);
            }).orElse(defaultValue);
    }

    public Stream<FieldInfo> fields() {
        return delegate.fields().stream().map(this::fieldInfo);
    }

    public Optional<FieldInfo> field(String fieldName) {
        return Optional.ofNullable(delegate.field(fieldName)).map(this::fieldInfo);
    }

    private FieldInfo fieldInfo(org.jboss.jandex.FieldInfo fieldInfo) {
        return fields.computeIfAbsent(fieldInfo.name(), f -> new FieldInfo(index, fieldInfo));
    }

    public Stream<MethodInfo> methods() {
        return delegate.methods().stream().map(this::methodInfo);
    }

    public Optional<MethodInfo> method(String methodName, Class<?>... argTypes) {
        return method(methodName, Stream.of(argTypes).map(Class::getName).collect(Utils.toArray(String.class)));
    }

    public Optional<MethodInfo> method(String methodName, String... argTypeNames) {
        return delegate.methods().stream()
            .filter(methodInfo -> methodInfo.name().equals(methodName))
            .filter(methodInfo -> methodInfo.parameters().size() == argTypeNames.length)
            .filter(methodInfo -> matchTypes(methodInfo.parameters(), argTypeNames))
            .findFirst()
            .map(this::methodInfo);
    }

    private static boolean matchTypes(List<Type> parameters, String[] argTypeNames) {
        for (int i = 0; i < parameters.size(); i++) {
            Type paramType = parameters.get(i);
            String argTypeName = argTypeNames[i];
            if (!paramType.name().toString().equals(argTypeName))
                return false;
        }
        return true;
    }

    private MethodInfo methodInfo(org.jboss.jandex.MethodInfo methodInfo) {
        return methods.computeIfAbsent(signature(methodInfo), s -> new MethodInfo(index, methodInfo));
    }

    public boolean isRepeatableAnnotation() {
        return AnnotationInstance.isRepeatable(delegate);
    }
}
