package test.jandexed;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import test.plain.ReflectionDummyClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

public class JandexBehavior {

    AnnotationsLoaderImpl loader;

    @BeforeEach void setUp() throws IOException {
        try (InputStream inputStream = new FileInputStream("target/test-classes/test/jandexed/META-INF/jandex.idx")) {
            loader = new AnnotationsLoaderImpl(inputStream);
        }
    }


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
            Annotations annotations = loader.onType(ReflectionDummyClass.class);

            Optional<DummyAnnotation> annotation = annotations.get(DummyAnnotation.class);

            thenIsDummyAnnotation(annotation, "reflection-dummy-class")
                .isSameAs(ReflectionDummyClass.class.getAnnotation(DummyAnnotation.class));
        }

        @Test void shouldGetFallbackFieldAnnotation() throws NoSuchFieldException {
            Annotations annotations = loader.onField(ReflectionDummyClass.class, "bar");

            Optional<DummyAnnotation> annotation = annotations.get(DummyAnnotation.class);

            thenIsDummyAnnotation(annotation, "reflection-dummy-field")
                .isSameAs(ReflectionDummyClass.class.getDeclaredField("bar").getAnnotation(DummyAnnotation.class));
        }

        @Test void shouldGetFallbackMethodAnnotation() throws NoSuchMethodException {
            Annotations annotations = loader.onMethod(ReflectionDummyClass.class, "foo", String.class);

            Optional<DummyAnnotation> annotation = annotations.get(DummyAnnotation.class);

            thenIsDummyAnnotation(annotation, "reflection-dummy-method")
                .isSameAs(ReflectionDummyClass.class.getDeclaredMethod("foo", String.class).getAnnotation(DummyAnnotation.class));
        }
    }


    @Nested class WithoutReflectionFallback {
        @BeforeEach void setUp() {
            loader.disableReflectionFallback();
        }

        @Test void shouldGetNoClassAnnotationsWithoutIndexAndDisabledReflection() {
            Annotations annotations = loader.onType(ReflectionDummyClass.class);

            thenEmpty(annotations);
        }

        @Test void shouldGetNoFieldAnnotationsWithoutIndexAndDisabledReflection() {
            Annotations annotations = loader.onField(ReflectionDummyClass.class, "bar");

            thenEmpty(annotations);
        }

        @Test void shouldGetNoMethodAnnotationsWithoutIndexAndDisabledReflection() {
            Annotations annotations = loader.onMethod(ReflectionDummyClass.class, "foo", String.class);

            thenEmpty(annotations);
        }


        @Nested class ObjectMethodsOnAnnotationProxy {
            @DummyAnnotation("nested-class")
            class Dummy {}

            private DummyAnnotation dummyAnnotation;

            @BeforeEach
            void setUp() {
                this.dummyAnnotation = loader.onType(Dummy.class).get(DummyAnnotation.class)
                    .orElseThrow(() -> new IllegalStateException("unreachable"));
                assert Proxy.isProxyClass(dummyAnnotation.getClass());
            }

            @Test void shouldCallToStringOnAnnotationProxy() {
                String toString = dummyAnnotation.toString();

                then(toString.replace(" ", "")).isEqualTo("@test.jandexed.DummyAnnotation(value=\"nested-class\")");
            }

            @Test void annotationProxyShouldBeEqualToSelf() {
                @SuppressWarnings("EqualsWithItself")
                boolean equal = dummyAnnotation.equals(dummyAnnotation);

                //noinspection ConstantConditions
                then(equal).isTrue();
            }

            @Test void annotationProxyShouldNotBeEqualToReflection() {
                boolean equals = dummyAnnotation.equals(Dummy.class.getAnnotation(DummyAnnotation.class));

                then(equals).isFalse();
            }

            @Test void shouldFailToCallHashCodeOnAnnotationProxy() {
                int hashCode = dummyAnnotation.hashCode();

                then(hashCode).isEqualTo(dummyAnnotation.toString().hashCode());
            }
        }
    }


    @Nested class ClassAnnotations {
        @Test void shouldGetClassAnnotation() {
            @DummyAnnotation("local-class")
            class Dummy {}
            Annotations annotations = loader.onType(Dummy.class);

            Optional<DummyAnnotation> annotation = annotations.get(DummyAnnotation.class);

            thenIsDummyAnnotation(annotation, "local-class")
                .isNotSameAs(Dummy.class.getAnnotation(DummyAnnotation.class));
        }

        @Test void shouldGetNoClassAnnotation() {
            class Dummy {}

            Annotations annotations = loader.onType(Dummy.class);

            thenEmpty(annotations);
        }
    }


    @Nested class FieldAnnotations {
        @Test void shouldFailToGetUnknownFieldAnnotation() {
            @SuppressWarnings("unused")
            class Dummy {
                @DummyAnnotation("local-class-field")
                String foo;
            }

            Throwable throwable = catchThrowable(() -> loader.onField(Dummy.class, "bar"));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no field 'bar' in " + Dummy.class);
        }

        @Test void shouldGetFieldAnnotation() throws NoSuchFieldException {
            class Dummy {
                @DummyAnnotation("local-class-field")
                String foo;
            }
            Annotations annotations = loader.onField(Dummy.class, "foo");

            Optional<DummyAnnotation> annotation = annotations.get(DummyAnnotation.class);

            thenIsDummyAnnotation(annotation, "local-class-field")
                .isNotSameAs(Dummy.class.getDeclaredField("foo").getAnnotation(DummyAnnotation.class));
        }

        @Test void shouldGetNoFieldAnnotation() {
            @SuppressWarnings("unused")
            class Dummy {
                String foo;
            }

            Annotations annotations = loader.onField(Dummy.class, "foo");

            thenEmpty(annotations);
        }
    }


    @Nested class MethodAnnotations {
        @Test void shouldGetMethodAnnotation() throws NoSuchMethodException {
            @SuppressWarnings("unused")
            class Dummy {
                @DummyAnnotation("local-class-method")
                void foo(String x) {}
            }
            Annotations annotations = loader.onMethod(Dummy.class, "foo", String.class);

            Optional<DummyAnnotation> annotation = annotations.get(DummyAnnotation.class);

            thenIsDummyAnnotation(annotation, "local-class-method")
                .isNotSameAs(Dummy.class.getDeclaredMethod("foo", String.class).getAnnotation(DummyAnnotation.class));
        }

        @Test void shouldFailToGetAnnotationsFromUnknownMethodName() {
            @SuppressWarnings("unused")
            class Dummy {
                @DummyAnnotation("local-class-method")
                void foo(String x) {}
            }

            Throwable throwable = catchThrowable(() -> loader.onMethod(Dummy.class, "bar", String.class));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method bar(String) in " + Dummy.class);
        }

        @Test void shouldFailToGetAnnotationsFromMethodWithTooManyArguments() {
            @SuppressWarnings("unused")
            class Dummy {
                @DummyAnnotation("local-class-method")
                void foo(String x) {}
            }

            Throwable throwable = catchThrowable(() -> loader.onMethod(Dummy.class, "foo"));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method foo() in " + Dummy.class);
        }

        @Test void shouldFailToGetAnnotationsFromMethodWithTooFewArguments() {
            @SuppressWarnings("unused")
            class Dummy {
                @DummyAnnotation("local-class-method")
                void foo(String x) {}
            }

            Throwable throwable = catchThrowable(() -> loader.onMethod(Dummy.class, "foo", String.class, int.class));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method foo(String, int) in " + Dummy.class);
        }


        @Test void shouldFailToGetAnnotationsFromMethodWithWrongArgumentType() {
            @SuppressWarnings("unused")
            class Dummy {
                @DummyAnnotation("local-class-method")
                void foo(int x) {}
            }

            Throwable throwable = catchThrowable(() -> loader.onMethod(Dummy.class, "foo", String.class));

            then(throwable).isInstanceOf(RuntimeException.class)
                .hasMessage("no method foo(String) in " + Dummy.class);
        }

        @Test void shouldGetNoMethodAnnotation() {
            @SuppressWarnings("unused")
            class Dummy {
                void foo(String x) {}
            }

            Annotations annotations = loader.onMethod(Dummy.class, "foo", String.class);

            thenEmpty(annotations);
        }
    }

    private void thenEmpty(Annotations annotations) {
        then(annotations.all()).isEmpty();
        then(annotations.get(DummyAnnotation.class)).isEmpty();
    }

    private ObjectAssert<DummyAnnotation> thenIsDummyAnnotation(Optional<DummyAnnotation> annotation, String expectedValue) {
        assert annotation.isPresent();
        DummyAnnotation dummyAnnotation = annotation.get();
        then(dummyAnnotation.annotationType()).isEqualTo(DummyAnnotation.class);
        then(dummyAnnotation.value()).isEqualTo(expectedValue);
        return then(dummyAnnotation);
    }
}
