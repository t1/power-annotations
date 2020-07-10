package test.indexed;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.BDDAssertions.then;
import static test.indexed.TestTools.buildAnnotationsLoader;

public class ResolveFromClassBehavior {
    AnnotationsLoaderImpl TheAnnotations = buildAnnotationsLoader();

    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface ClassOnlyAnnotation {}

    @Retention(RUNTIME)
    public @interface EverywhereAnnotation {}

    @Nested class FieldAnnotations {
        @Test void shouldGetFieldAnnotationFromClass() {
            @SomeAnnotation("class-annotation")
            class SomeClass {
                @SuppressWarnings("unused")
                String someField;
            }
            Annotations annotations = TheAnnotations.onField(SomeClass.class, "someField");

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            assert annotation.isPresent();
            then(annotation.get().value()).isEqualTo("class-annotation");
        }

        @Test void shouldGetRepeatableFieldAnnotationFromClass() {
            @RepeatableAnnotation(1)
            @RepeatableAnnotation(2)
            class SomeClass {
                @SuppressWarnings("unused")
                String someField;
            }
            Annotations annotations = TheAnnotations.onField(SomeClass.class, "someField");

            Stream<RepeatableAnnotation> annotation = annotations.all(RepeatableAnnotation.class);

            then(annotation.map(RepeatableAnnotation::value)).containsExactly(1, 2);
        }

        @Test void shouldNotGetMoreRepeatableFieldAnnotationsFromClass() {
            @RepeatableAnnotation(1)
            class SomeClass {
                @RepeatableAnnotation(2)
                @SuppressWarnings("unused")
                String someField;
            }
            Annotations annotations = TheAnnotations.onField(SomeClass.class, "someField");

            Stream<RepeatableAnnotation> annotation = annotations.all(RepeatableAnnotation.class);

            then(annotation.map(RepeatableAnnotation::value)).containsExactly(2);
        }

        @Test void shouldOnlyGetAllFieldAnnotationAndNotFromClass() {
            @SomeAnnotation("class-annotation")
            class SomeClass {
                @SuppressWarnings("unused")
                @RepeatableAnnotation(1)
                String someField;
            }
            Annotations annotations = TheAnnotations.onField(SomeClass.class, "someField");

            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeClass.class.getName() + ".someField");
        }
    }

    @Nested class MethodAnnotations {
        @Test void shouldGetMethodAnnotationFromClass() {
            @SomeAnnotation("class-annotation")
            class SomeClass {
                @SuppressWarnings("unused")
                void someMethod() {}
            }
            Annotations annotations = TheAnnotations.onMethod(SomeClass.class, "someMethod");

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            assert annotation.isPresent();
            then(annotation.get().value()).isEqualTo("class-annotation");
        }

        @Test void shouldGetRepeatableMethodAnnotationFromClass() {
            @RepeatableAnnotation(1)
            @RepeatableAnnotation(2)
            class SomeClass {
                @SuppressWarnings("unused")
                void someMethod() {}
            }
            Annotations annotations = TheAnnotations.onMethod(SomeClass.class, "someMethod");

            Stream<RepeatableAnnotation> annotation = annotations.all(RepeatableAnnotation.class);

            then(annotation.map(RepeatableAnnotation::value)).containsExactly(1, 2);
        }

        @Test void shouldNotGetMoreRepeatableMethodAnnotationsFromClass() {
            @RepeatableAnnotation(1)
            class SomeClass {
                @RepeatableAnnotation(2)
                @SuppressWarnings("unused")
                void someMethod() {}
            }
            Annotations annotations = TheAnnotations.onMethod(SomeClass.class, "someMethod");

            Stream<RepeatableAnnotation> annotation = annotations.all(RepeatableAnnotation.class);

            then(annotation.map(RepeatableAnnotation::value)).containsExactly(2);
        }
    }
}
