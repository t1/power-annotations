package test.plain;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import org.junit.jupiter.api.Test;
import test.jandexed.RepeatableAnnotation;
import test.jandexed.SomeAnnotation;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

/**
 * If the Jandex index file is not available, silently build the index at runtime.
 * This test also covers the {@link Annotations API} and {@link com.github.t1.annotations.AnnotationsLoader SPI}
 */
public class DynamicJandexBehavior {

    @Test void shouldGetSingleClassAnnotation() {
        Optional<SomeAnnotation> annotation = Annotations.on(SomeReflectionClass.class).get(SomeAnnotation.class);

        assert annotation.isPresent();
        SomeAnnotation someAnnotation = annotation.get();
        then(someAnnotation.annotationType()).isEqualTo(SomeAnnotation.class);
        then(someAnnotation.value()).isEqualTo("some-reflection-class");
    }

    @Test void shouldGetAllClassAnnotations() {
        List<Annotation> annotations = Annotations.on(SomeReflectionClass.class).all();

        then(annotations.stream().map(Objects::toString)).containsOnly(
            "@" + SomeAnnotation.class.getName() + "(value = \"some-reflection-class\")",
            "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
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

    @Test void shouldFailWithSingleAccess() {
        Annotations annotations = Annotations.on(SomeReflectionClass.class);

        Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

        then(throwable)
            .isInstanceOf(AmbiguousAnnotationResolutionException.class)
            .hasMessage("The annotation " + RepeatableAnnotation.class.getName() + " is ambiguous on "
                + ". You should query it with `all` not `get`.");
    }

    @Test void shouldGetTypedAll() {
        Annotations annotations = Annotations.on(SomeReflectionClass.class);

        Stream<RepeatableAnnotation> someAnnotations = annotations.all(RepeatableAnnotation.class);

        then(someAnnotations.map(RepeatableAnnotation::value)).containsExactly(1, 2);
    }
}
