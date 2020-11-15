package test;

import com.github.t1.annotations.plugin.tck.MixedInAnnotation;
import com.github.t1.annotations.plugin.tck.SomeStereotype;
import com.github.t1.annotations.plugin.tck.StereotypedAnnotation;
import com.github.t1.annotations.plugin.tck.TargetClass;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.newInputStream;
import static org.assertj.core.api.BDDAssertions.then;

class AnnotationBehavior {
    @Test void shouldAddAnnotation() {
        Index index = loadIndex();

        ClassInfo classInfo = index.getClassByName(TARGET_CLASS);

        then(classInfo).describedAs("class not not index").isNotNull();
        thenClassAnnotations(classInfo);
        thenFieldAnnotations(classInfo);
        thenMethodAnnotations(classInfo);
        // TODO parameter annotations
        // TODO type annotations
    }

    private void thenClassAnnotations(ClassInfo classInfo) {
        then(classInfo.classAnnotations()).hasSize(3);
        then(classInfo.classAnnotation(SOME_STEREOTYPE)).isNotNull();
        then(classInfo.classAnnotation(MIXED_IN_ANNOTATION).value().asString()).isEqualTo("mixed-in-class");
        then(classInfo.classAnnotation(STEREOTYPED_ANNOTATION).value().asString()).isEqualTo("stereotyped");
    }

    private void thenFieldAnnotations(ClassInfo classInfo) {
        FieldInfo fieldInfo = classInfo.field("bar");
        then(fieldInfo).describedAs("field not in index").isNotNull();
        then(fieldInfo.annotations()).hasSize(3);
        then(fieldInfo.annotation(SOME_STEREOTYPE)).isNotNull();
        then(fieldInfo.annotation(MIXED_IN_ANNOTATION).value().asString()).isEqualTo("mixed-in-field");
        then(fieldInfo.annotation(STEREOTYPED_ANNOTATION).value().asString()).isEqualTo("stereotyped");
    }

    private void thenMethodAnnotations(ClassInfo classInfo) {
        MethodInfo methodInfo = classInfo.method("foo");
        then(methodInfo).describedAs("method not in index").isNotNull();
        then(methodInfo.annotations()).hasSize(3);
        then(methodInfo.annotation(SOME_STEREOTYPE)).isNotNull();
        then(methodInfo.annotation(MIXED_IN_ANNOTATION).value().asString()).isEqualTo("mixed-in-method");
        then(methodInfo.annotation(STEREOTYPED_ANNOTATION).value().asString()).isEqualTo("stereotyped");
    }

    private Index loadIndex() {
        Path path = Paths.get("target/classes/META-INF/jandex.idx");
        try {
            InputStream indexStream = newInputStream(path);
            Index result = new IndexReader(indexStream).read();
            indexStream.close();
            return result;
        } catch (IOException e) {
            throw new RuntimeException("can't load index: " + path, e);
        }
    }

    private static final DotName TARGET_CLASS = DotName.createSimple(TargetClass.class.getName());
    private static final DotName MIXED_IN_ANNOTATION = DotName.createSimple(MixedInAnnotation.class.getName());
    private static final DotName SOME_STEREOTYPE = DotName.createSimple(SomeStereotype.class.getName());
    private static final DotName STEREOTYPED_ANNOTATION = DotName.createSimple(StereotypedAnnotation.class.getName());
}
