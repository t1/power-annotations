package test.indexed;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static test.indexed.TestTools.buildAnnotationsLoader;

public class JandexBehavior {
    AnnotationsLoaderImpl TheAnnotations = buildAnnotationsLoader();

    @Nested class FailingLoad {
        @Test void shouldSilentlySkipUnknownIndexResource() {
            AnnotationsLoaderImpl loader = new AnnotationsLoaderImpl();

            then(loader).isNotNull();
        }

        // implementation detail
        @Test void shouldFailToLoadInvalidIndexInputStream() throws Exception {
            FileInputStream inputStream = new FileInputStream("pom.xml");

            Throwable throwable = catchThrowable(() -> buildAnnotationsLoader(inputStream));

            then(throwable)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage("Not a jandex index");
        }
    }


    @SomeAnnotation("interface")
    @SomeAnnotationWithDefaultValue
    interface SomeInterface {
        @SomeAnnotation("interface-method")
        void foo(@Deprecated String x);
    }

    @Nested class ClassAnnotations {
        @Test void shouldGetClassAnnotation() {
            @SomeAnnotation("local-class")
            class SomeClass {}
            Annotations annotations = TheAnnotations.onType(SomeClass.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "local-class")
                .isNotSameAs(SomeClass.class.getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetInterfaceAnnotation() {
            Annotations annotations = TheAnnotations.onType(SomeInterface.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "interface")
                .isNotSameAs(SomeInterface.class.getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetNoClassAnnotation() {
            class SomeClass {}

            Annotations annotations = TheAnnotations.onType(SomeClass.class);

            thenEmpty(annotations);
        }

        @Test void shouldGetDefaultValueOfClassAnnotation() {
            Annotations annotations = TheAnnotations.onType(SomeInterface.class);

            Optional<SomeAnnotationWithDefaultValue> annotation = annotations.get(SomeAnnotationWithDefaultValue.class);

            assert annotation.isPresent();
            SomeAnnotationWithDefaultValue someAnnotation = annotation.get();
            then(someAnnotation.annotationType()).isEqualTo(SomeAnnotationWithDefaultValue.class);
            then(someAnnotation.valueWithDefault()).isEqualTo("default-value");
            then(someAnnotation).isNotSameAs(SomeInterface.class.getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetAllClassAnnotations() {
            Annotations annotations = TheAnnotations.onType(SomeInterface.class);

            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + SomeAnnotationWithDefaultValue.class.getName() + " on " + SomeInterface.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"interface\") on " + SomeInterface.class.getName());
        }
    }


    @Nested class FieldAnnotations {
        // implementation detail
        @Test void shouldFailToGetUnknownFieldAnnotation() {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-field")
                String foo;
            }

            Throwable throwable = catchThrowable(() -> TheAnnotations.onField(SomeClass.class, "bar"));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no field 'bar' in " + SomeClass.class);
        }

        @Test void shouldGetFieldAnnotation() throws NoSuchFieldException {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-field")
                private String foo;
            }
            Annotations annotations = TheAnnotations.onField(SomeClass.class, "foo");

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "local-class-field")
                .isNotSameAs(SomeClass.class.getDeclaredField("foo").getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetNoFieldAnnotation() {
            @SuppressWarnings("unused")
            class SomeClass {
                String foo;
            }

            Annotations annotations = TheAnnotations.onField(SomeClass.class, "foo");

            thenEmpty(annotations);
        }
    }


    @Nested class MethodAnnotations {
        @Test void shouldGetMethodAnnotation() throws NoSuchMethodException {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-method")
                void foo(String x) {}
            }
            Annotations annotations = TheAnnotations.onMethod(SomeClass.class, "foo", String.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "local-class-method")
                .isNotSameAs(SomeClass.class.getDeclaredMethod("foo", String.class).getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetInterfaceMethodAnnotation() throws NoSuchMethodException {
            Annotations annotations = TheAnnotations.onMethod(SomeInterface.class, "foo", String.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "interface-method")
                .isNotSameAs(fooMethod().getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetAllMethodAnnotations() throws NoSuchMethodException {
            Annotations annotations = TheAnnotations.onMethod(SomeInterface.class, "foo", String.class);

            List<Annotation> all = annotations.all();

            then(fooMethod().getParameterAnnotations()[0][0].toString())
                .startsWith("@" + Deprecated.class.getName()); // `since` and `forRemoval` are JDK 9+
            then(all.stream().map(Object::toString)).containsOnly(
                // the parameter annotation @Deprecated must not be visible as method annotation
                "@" + SomeAnnotation.class.getName() + "(value = \"interface-method\") on " + SomeInterface.class.getName() + ".foo");
        }

        private Method fooMethod() throws NoSuchMethodException {
            return SomeInterface.class.getDeclaredMethod("foo", String.class);
        }

        // implementation detail
        @Test void shouldFailToGetAnnotationsFromUnknownMethodName() {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-method")
                void foo(String x) {}
            }

            Throwable throwable = catchThrowable(() -> TheAnnotations.onMethod(SomeClass.class, "bar", String.class));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method bar(String) in " + SomeClass.class);
        }

        // implementation detail
        @Test void shouldFailToGetAnnotationsFromMethodWithTooManyArguments() {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-method")
                void foo(String x) {}
            }

            Throwable throwable = catchThrowable(() -> TheAnnotations.onMethod(SomeClass.class, "foo"));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method foo() in " + SomeClass.class);
        }

        // implementation detail
        @Test void shouldFailToGetAnnotationsFromMethodWithTooFewArguments() {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-method")
                void foo(String x) {}
            }

            Throwable throwable = catchThrowable(() -> TheAnnotations.onMethod(SomeClass.class, "foo", String.class, int.class));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method foo(String, int) in " + SomeClass.class);
        }

        // implementation detail
        @Test void shouldFailToGetAnnotationsFromMethodWithWrongArgumentType() {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-method")
                void foo(int x) {}
            }

            Throwable throwable = catchThrowable(() -> TheAnnotations.onMethod(SomeClass.class, "foo", String.class));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method foo(String) in " + SomeClass.class);
        }

        @Test void shouldGetNoMethodAnnotation() {
            @SuppressWarnings("unused")
            class SomeClass {
                void foo(String x) {}
            }

            Annotations annotations = TheAnnotations.onMethod(SomeClass.class, "foo", String.class);

            thenEmpty(annotations);
        }
    }

    void thenEmpty(Annotations annotations) {
        then(annotations.all()).isEmpty();
        then(annotations.get(SomeAnnotation.class)).isEmpty();
    }

    ObjectAssert<SomeAnnotation> thenIsSomeAnnotation(
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<SomeAnnotation> annotation,
        String expectedValue) {
        assert annotation.isPresent();
        SomeAnnotation someAnnotation = annotation.get();
        then(someAnnotation.annotationType()).isEqualTo(SomeAnnotation.class);
        then(someAnnotation.value()).isEqualTo(expectedValue);
        return then(someAnnotation);
    }
}
