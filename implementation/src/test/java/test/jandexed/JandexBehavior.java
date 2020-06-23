package test.jandexed;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import test.plain.SomeReflectionClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static test.jandexed.TestTools.buildAnnotationsLoader;

public class JandexBehavior {
    AnnotationsLoaderImpl loader = buildAnnotationsLoader();

    @Nested class FailingLoad {
        @Test void shouldFailToLoadUnknownIndexResource() {
            Throwable throwable = catchThrowable(() -> new AnnotationsLoaderImpl("unknown-index-file"));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("can't read unknown-index-file")
                .hasCauseInstanceOf(IOException.class);
        }

        @Test void shouldFailToLoadInvalidIndexInputStream() throws Exception {
            FileInputStream inputStream = new FileInputStream("pom.xml");

            Throwable throwable = catchThrowable(() -> new AnnotationsLoaderImpl(inputStream));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("can't read Jandex input stream")
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasRootCauseMessage("Not a jandex index");
        }
    }


    @Nested class ReflectionFallback {
        @Test void shouldGetFallbackClassAnnotation() {
            Annotations annotations = loader.onType(SomeReflectionClass.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "some-reflection-class")
                .isSameAs(SomeReflectionClass.class.getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetFallbackFieldAnnotation() throws NoSuchFieldException {
            Annotations annotations = loader.onField(SomeReflectionClass.class, "bar");

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "some-reflection-field")
                .isSameAs(SomeReflectionClass.class.getDeclaredField("bar").getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetFallbackMethodAnnotation() throws NoSuchMethodException {
            Annotations annotations = loader.onMethod(SomeReflectionClass.class, "foo", String.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "some-reflection-method")
                .isSameAs(SomeReflectionClass.class.getDeclaredMethod("foo", String.class).getAnnotation(SomeAnnotation.class));
        }
    }


    @Nested class WithoutReflectionFallback {
        @BeforeEach void setUp() {
            loader.disableReflectionFallback();
        }

        @Test void shouldGetNoClassAnnotationsWithoutIndexAndDisabledReflection() {
            Annotations annotations = loader.onType(SomeReflectionClass.class);

            thenEmpty(annotations);
        }

        @Test void shouldGetNoFieldAnnotationsWithoutIndexAndDisabledReflection() {
            Annotations annotations = loader.onField(SomeReflectionClass.class, "bar");

            thenEmpty(annotations);
        }

        @Test void shouldGetNoMethodAnnotationsWithoutIndexAndDisabledReflection() {
            Annotations annotations = loader.onMethod(SomeReflectionClass.class, "foo", String.class);

            thenEmpty(annotations);
        }


        @Nested class ObjectMethodsOnAnnotationProxy {
            @SomeAnnotation("nested-class")
            class SomeClass {}

            private SomeAnnotation someAnnotation;

            @BeforeEach
            void setUp() {
                this.someAnnotation = loader.onType(SomeClass.class).get(SomeAnnotation.class)
                    .orElseThrow(() -> new IllegalStateException("unreachable"));
                assert Proxy.isProxyClass(someAnnotation.getClass());
            }

            @Test void shouldCallToStringOnAnnotationProxy() {
                String toString = someAnnotation.toString();

                then(toString.replace(" ", "")).isEqualTo("@test.jandexed.SomeAnnotation(value=\"nested-class\")");
            }

            @Test void annotationProxyShouldBeEqualToSelf() {
                @SuppressWarnings("EqualsWithItself")
                boolean equal = someAnnotation.equals(someAnnotation);

                //noinspection ConstantConditions
                then(equal).isTrue();
            }

            @Test void annotationProxyShouldNotBeEqualToReflection() {
                boolean equals = someAnnotation.equals(SomeClass.class.getAnnotation(SomeAnnotation.class));

                then(equals).isFalse();
            }

            @Test void shouldFailToCallHashCodeOnAnnotationProxy() {
                int hashCode = someAnnotation.hashCode();

                then(hashCode).isEqualTo(someAnnotation.toString().hashCode());
            }
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
            Annotations annotations = loader.onType(SomeClass.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "local-class")
                .isNotSameAs(SomeClass.class.getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetInterfaceAnnotation() {
            Annotations annotations = loader.onType(SomeInterface.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "nested-interface")
                .isNotSameAs(SomeInterface.class.getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetNoClassAnnotation() {
            class SomeClass {}

            Annotations annotations = loader.onType(SomeClass.class);

            thenEmpty(annotations);
        }
    }


    @Nested class FieldAnnotations {
        @Test void shouldFailToGetUnknownFieldAnnotation() {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-field")
                String foo;
            }

            Throwable throwable = catchThrowable(() -> loader.onField(SomeClass.class, "bar"));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no field 'bar' in " + SomeClass.class);
        }

        @Test void shouldGetFieldAnnotation() throws NoSuchFieldException {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-field")
                private String foo;
            }
            Annotations annotations = loader.onField(SomeClass.class, "foo");

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "local-class-field")
                .isNotSameAs(SomeClass.class.getDeclaredField("foo").getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetNoFieldAnnotation() {
            @SuppressWarnings("unused")
            class SomeClass {
                String foo;
            }

            Annotations annotations = loader.onField(SomeClass.class, "foo");

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
            Annotations annotations = loader.onMethod(SomeClass.class, "foo", String.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "local-class-method")
                .isNotSameAs(SomeClass.class.getDeclaredMethod("foo", String.class).getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldGetInterfaceMethodAnnotation() throws NoSuchMethodException {
            Annotations annotations = loader.onMethod(SomeInterface.class, "foo", String.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "nested-interface-method")
                .isNotSameAs(SomeInterface.class.getDeclaredMethod("foo", String.class).getAnnotation(SomeAnnotation.class));
        }

        @Test void shouldFailToGetAnnotationsFromUnknownMethodName() {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-method")
                void foo(String x) {}
            }

            Throwable throwable = catchThrowable(() -> loader.onMethod(SomeClass.class, "bar", String.class));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method bar(String) in " + SomeClass.class);
        }

        @Test void shouldFailToGetAnnotationsFromMethodWithTooManyArguments() {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-method")
                void foo(String x) {}
            }

            Throwable throwable = catchThrowable(() -> loader.onMethod(SomeClass.class, "foo"));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method foo() in " + SomeClass.class);
        }

        @Test void shouldFailToGetAnnotationsFromMethodWithTooFewArguments() {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-method")
                void foo(String x) {}
            }

            Throwable throwable = catchThrowable(() -> loader.onMethod(SomeClass.class, "foo", String.class, int.class));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method foo(String, int) in " + SomeClass.class);
        }


        @Test void shouldFailToGetAnnotationsFromMethodWithWrongArgumentType() {
            @SuppressWarnings("unused")
            class SomeClass {
                @SomeAnnotation("local-class-method")
                void foo(int x) {}
            }

            Throwable throwable = catchThrowable(() -> loader.onMethod(SomeClass.class, "foo", String.class));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method foo(String) in " + SomeClass.class);
        }

        @Test void shouldGetNoMethodAnnotation() {
            @SuppressWarnings("unused")
            class SomeClass {
                void foo(String x) {}
            }

            Annotations annotations = loader.onMethod(SomeClass.class, "foo", String.class);

            thenEmpty(annotations);
        }
    }

    private void thenEmpty(Annotations annotations) {
        then(annotations.all()).isEmpty();
        then(annotations.get(SomeAnnotation.class)).isEmpty();
    }

    private ObjectAssert<SomeAnnotation> thenIsSomeAnnotation(Optional<SomeAnnotation> annotation, String expectedValue) {
        assert annotation.isPresent();
        SomeAnnotation someAnnotation = annotation.get();
        then(someAnnotation.annotationType()).isEqualTo(SomeAnnotation.class);
        then(someAnnotation.value()).isEqualTo(expectedValue);
        return then(someAnnotation);
    }
}
