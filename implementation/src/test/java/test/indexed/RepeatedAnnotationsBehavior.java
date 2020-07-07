package test.indexed;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static test.indexed.TestTools.buildAnnotationsLoader;

public class RepeatedAnnotationsBehavior {
    AnnotationsLoaderImpl TheAnnotations = buildAnnotationsLoader();

    @Test void shouldGetSingleRepeatedAnnotation() {
        @RepeatableAnnotation(1)
        class SomeClass {}

        Optional<RepeatableAnnotation> annotation = TheAnnotations.onType(SomeClass.class)
            .get(RepeatableAnnotation.class);

        assert annotation.isPresent();
        then(annotation.get().value()).isEqualTo(1);
    }

    @Test void shouldFailToGetRepeatingAnnotation() {
        @RepeatableAnnotation(1)
        @RepeatableAnnotation(2)
        class SomeClass {}
        Annotations annotations = TheAnnotations.onType(SomeClass.class);

        Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

        then(throwable)
            .isInstanceOf(AmbiguousAnnotationResolutionException.class)
            // TODO message detail about the target .hasMessageContaining(SomeClass.class.getName())
            .hasMessageContaining(RepeatableAnnotation.class.getName());
    }

    @Test void shouldGetAll() {
        @RepeatableAnnotation(1)
        @RepeatableAnnotation(2)
        class SomeClass {}

        List<Annotation> all = TheAnnotations.onType(SomeClass.class).all();

        then(all.stream().map(Object::toString)).containsOnly(
            "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeClass.class.getName(),
            "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeClass.class.getName());
    }

    @Test void shouldGetTypedAll() {
        @RepeatableAnnotation(1)
        @RepeatableAnnotation(2)
        class SomeClass {}

        Stream<RepeatableAnnotation> someAnnotations = TheAnnotations.onType(SomeClass.class)
            .all(RepeatableAnnotation.class);

        then(someAnnotations.map(RepeatableAnnotation::value)).containsOnly(1, 2);
    }
}
