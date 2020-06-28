package test.jandexed;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static test.jandexed.TestTools.buildAnnotationsLoader;

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

            then(throwable).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not a jandex index");
        }
    }


    @SomeAnnotation("nested-interface")
    interface SomeInterface {
        @SomeAnnotation("nested-interface-method")
        void foo(String x);
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

            thenIsSomeAnnotation(annotation, "nested-interface")
                .isNotSameAs(SomeInterface.class.getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetNoClassAnnotation() {
            class SomeClass {}

            Annotations annotations = TheAnnotations.onType(SomeClass.class);

            thenEmpty(annotations);
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

            thenIsSomeAnnotation(annotation, "nested-interface-method")
                .isNotSameAs(SomeInterface.class.getDeclaredMethod("foo", String.class).getAnnotation(SomeAnnotation.class));
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
