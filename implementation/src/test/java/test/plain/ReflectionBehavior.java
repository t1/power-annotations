package test.plain;

import com.github.t1.annotations.Annotations;
import org.junit.jupiter.api.Test;
import test.jandexed.DummyAnnotation;

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
        Optional<DummyAnnotation> annotation = Annotations.on(ReflectionDummyClass.class).get(DummyAnnotation.class);

        assert annotation.isPresent();
        DummyAnnotation dummyAnnotation = annotation.get();
        then(dummyAnnotation.annotationType()).isEqualTo(DummyAnnotation.class);
        then(dummyAnnotation.value()).isEqualTo("reflection-dummy-class");
        then(dummyAnnotation).isSameAs(ReflectionDummyClass.class.getAnnotation(DummyAnnotation.class));
    }

    @Test void shouldGetAllClassAnnotations() {
        List<Annotation> annotations = Annotations.on(ReflectionDummyClass.class).all();

        then(annotations).hasSize(1);
        then(annotations.get(0).annotationType()).isEqualTo(DummyAnnotation.class);
        DummyAnnotation dummyAnnotation = (DummyAnnotation) annotations.get(0);
        then(dummyAnnotation.value()).isEqualTo("reflection-dummy-class");
    }

    @Test void shouldGetSingleFieldAnnotation() {
        Annotations fieldAnnotations = Annotations.onField(ReflectionDummyClass.class, "bar");

        Optional<DummyAnnotation> annotation = fieldAnnotations.get(DummyAnnotation.class);

        assert annotation.isPresent();
        DummyAnnotation dummyAnnotation = annotation.get();
        then(dummyAnnotation.annotationType()).isEqualTo(DummyAnnotation.class);
        then(dummyAnnotation.value()).isEqualTo("reflection-dummy-field");
    }

    @Test void shouldFailToGetUnknownFieldAnnotation() {
        Throwable throwable = catchThrowable(() -> Annotations.onField(ReflectionDummyClass.class, "unknown"));

        then(throwable).isInstanceOf(RuntimeException.class)
            .hasMessage("no field 'unknown' in " + ReflectionDummyClass.class);
    }

    @Test void shouldGetSingleMethodAnnotation() {
        Annotations methodAnnotations = Annotations.onMethod(ReflectionDummyClass.class, "foo", String.class);

        Optional<DummyAnnotation> annotation = methodAnnotations.get(DummyAnnotation.class);

        assert annotation.isPresent();
        DummyAnnotation dummyAnnotation = annotation.get();
        then(dummyAnnotation.annotationType()).isEqualTo(DummyAnnotation.class);
        then(dummyAnnotation.value()).isEqualTo("reflection-dummy-method");
    }

    @Test void shouldFailToGetUnknownMethodAnnotation() {
        Throwable throwable = catchThrowable(() -> Annotations.onMethod(ReflectionDummyClass.class, "unknown", String.class));

        then(throwable).isInstanceOf(RuntimeException.class)
            .hasMessage("no method unknown(String) in " + ReflectionDummyClass.class);
    }
}
