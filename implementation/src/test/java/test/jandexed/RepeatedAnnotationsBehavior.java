package test.jandexed;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.RepeatableAnnotationAccessedWithGetException;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static test.jandexed.TestTools.buildAnnotationsLoader;

public class RepeatedAnnotationsBehavior {
    AnnotationsLoaderImpl loader = buildAnnotationsLoader();

    @Test void shouldFailWithSingleAccess() {
        @RepeatableAnnotation(1)
        @RepeatableAnnotation(2)
        class SomeClass {}

        Annotations annotations = loader.onType(SomeClass.class);

        Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

        then(throwable)
            .isInstanceOf(RepeatableAnnotationAccessedWithGetException.class)
            .hasMessage("The annotation " + RepeatableAnnotation.class.getName() + " is repeatable, " +
                "so it should be queried with `all` not `get`.");
    }

    @Disabled("implement repeatable annotation-resolution based on Jandex")
    @Test void shouldGetAll() {
        @RepeatableAnnotation(1)
        @RepeatableAnnotation(2)
        class SomeClass {}

        Annotations annotations = loader.onType(SomeClass.class);

        List<Annotation> all = annotations.all();

        then(all.stream().map(Object::toString)).containsExactly(
            "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
    }

    // @Test void shouldGetTypedAll() {
    //     @RepeatableAnnotation(1)
    //     @RepeatableAnnotation(2)
    //     class SomeClass {}
    //
    //     Annotations annotations = loader.onType(SomeClass.class);
    //
    // Stream<RepeatableAnnotation> someAnnotations = annotations.all(RepeatableAnnotation.class);
    //
    // then(someAnnotations.map(RepeatableAnnotation::value)).containsExactly(1, 2);
    // }
}
