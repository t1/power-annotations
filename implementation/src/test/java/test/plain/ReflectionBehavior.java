package test.plain;

import com.github.t1.annotations.Annotations;
import org.junit.jupiter.api.Test;
import test.jandexed.SomeAnnotation;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

/**
 * If Jandex is not available, silently fall back to using reflection.
 * This test also covers the {@link Annotations API} and {@link com.github.t1.annotations.AnnotationsLoader SPI}
 */
public class ReflectionBehavior {

    @Test void shouldGetSingleClassAnnotation() {
        Optional<SomeAnnotation> annotation = Annotations.on(SomeReflectionClass.class).get(SomeAnnotation.class);

        assert annotation.isPresent();
        SomeAnnotation someAnnotation = annotation.get();
        then(someAnnotation.annotationType()).isEqualTo(SomeAnnotation.class);
        then(someAnnotation.value()).isEqualTo("some-reflection-class");
        then(someAnnotation).isSameAs(SomeReflectionClass.class.getAnnotation(SomeAnnotation.class));
    }

    @Test void shouldGetAllClassAnnotations() {
        List<Annotation> annotations = Annotations.on(SomeReflectionClass.class).all();

        then(annotations).hasSize(1);
        then(annotations.get(0).annotationType()).isEqualTo(SomeAnnotation.class);
        SomeAnnotation someAnnotation = (SomeAnnotation) annotations.get(0);
        then(someAnnotation.value()).isEqualTo("some-reflection-class");
    }

    @Test void shouldGetSingleFieldAnnotation() {
        Annotations fieldAnnotations = Annotations.onField(SomeReflectionClass.class, "bar");

        Optional<SomeAnnotation> annotation = fieldAnnotations.get(SomeAnnotation.class);

        assert annotation.isPresent();
        SomeAnnotation someAnnotation = annotation.get();
        then(someAnnotation.annotationType()).isEqualTo(SomeAnnotation.class);
        then(someAnnotation.value()).isEqualTo("some-reflection-field");
    }

    @Test void shouldFailToGetUnknownFieldAnnotation() {
        Throwable throwable = catchThrowable(() -> Annotations.onField(SomeReflectionClass.class, "unknown"));

        then(throwable).isInstanceOf(RuntimeException.class)
            .hasMessage("no field 'unknown' in " + SomeReflectionClass.class);
    }

    @Test void shouldGetSingleMethodAnnotation() {
        Annotations methodAnnotations = Annotations.onMethod(SomeReflectionClass.class, "foo", String.class);

        Optional<SomeAnnotation> annotation = methodAnnotations.get(SomeAnnotation.class);

        assert annotation.isPresent();
        SomeAnnotation someAnnotation = annotation.get();
        then(someAnnotation.annotationType()).isEqualTo(SomeAnnotation.class);
        then(someAnnotation.value()).isEqualTo("some-reflection-method");
    }

    @Test void shouldFailToGetUnknownMethodAnnotation() {
        Throwable throwable = catchThrowable(() -> Annotations.onMethod(SomeReflectionClass.class, "unknown", String.class));

        then(throwable).isInstanceOf(RuntimeException.class)
            .hasMessage("no method unknown(String) in " + SomeReflectionClass.class);
    }
}
