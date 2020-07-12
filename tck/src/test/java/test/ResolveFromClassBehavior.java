package test;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.tck.RepeatableAnnotation;
import com.github.t1.annotations.tck.ResolveFromClassClasses.ClassWithAnnotationsOnClassAndField;
import com.github.t1.annotations.tck.ResolveFromClassClasses.ClassWithAnnotationsOnClassAndMethod;
import com.github.t1.annotations.tck.ResolveFromClassClasses.ClassWithField;
import com.github.t1.annotations.tck.ResolveFromClassClasses.ClassWithMethod;
import com.github.t1.annotations.tck.ResolveFromClassClasses.ClassWithRepeatableAnnotationOnClassAndField;
import com.github.t1.annotations.tck.ResolveFromClassClasses.ClassWithRepeatableAnnotationOnClassAndMethod;
import com.github.t1.annotations.tck.ResolveFromClassClasses.ClassWithRepeatedAnnotationsForField;
import com.github.t1.annotations.tck.ResolveFromClassClasses.ClassWithRepeatedAnnotationsForMethod;
import com.github.t1.annotations.tck.SomeAnnotation;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

public class ResolveFromClassBehavior {
    @Nested class FieldAnnotations {
        Annotations classWithFieldAnnotations = Annotations.onField(ClassWithField.class, "someField");

        @Test void shouldGetFieldAnnotationFromClass() {
            Optional<SomeAnnotation> annotation = classWithFieldAnnotations.get(SomeAnnotation.class);

            assert annotation.isPresent();
            BDDAssertions.then(annotation.get().value()).isEqualTo("class-annotation");
        }

        @Test void shouldNotGetAllFieldAnnotationFromClass() {
            Optional<SomeAnnotation> annotation = classWithFieldAnnotations.get(SomeAnnotation.class);

            assert annotation.isPresent();
            BDDAssertions.then(annotation.get().value()).isEqualTo("class-annotation");
        }

        @Test void shouldGetRepeatableFieldAnnotationFromClass() {
            Annotations annotations = Annotations.onField(ClassWithRepeatedAnnotationsForField.class, "someField");

            Stream<RepeatableAnnotation> annotation = annotations.all(RepeatableAnnotation.class);

            then(annotation.map(RepeatableAnnotation::value)).containsExactly(1, 2);
        }

        @Test void shouldGetMoreRepeatableFieldAnnotationsFromClass() {
            Annotations annotations = Annotations.onField(ClassWithRepeatableAnnotationOnClassAndField.class, "someField");

            Stream<RepeatableAnnotation> annotation = annotations.all(RepeatableAnnotation.class);

            then(annotation.map(RepeatableAnnotation::value)).containsExactly(1, 2);
        }

        @Test void shouldOnlyGetAllFieldAnnotationAndNotFromClass() {
            Annotations annotations = Annotations.onField(ClassWithAnnotationsOnClassAndField.class, "someField");

            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + ClassWithAnnotationsOnClassAndField.class.getName() + ".someField");
        }
    }

    @Nested class MethodAnnotations {
        Annotations classWithMethodAnnotations = Annotations.onMethod(ClassWithMethod.class, "someMethod");

        @Test void shouldGetMethodAnnotationFromClass() {
            Optional<SomeAnnotation> annotation = classWithMethodAnnotations.get(SomeAnnotation.class);

            assert annotation.isPresent();
            BDDAssertions.then(annotation.get().value()).isEqualTo("class-annotation");
        }

        @Test void shouldNotGetAllMethodAnnotationFromClass() {
            Optional<SomeAnnotation> annotation = classWithMethodAnnotations.get(SomeAnnotation.class);

            assert annotation.isPresent();
            BDDAssertions.then(annotation.get().value()).isEqualTo("class-annotation");
        }

        @Test void shouldGetRepeatableMethodAnnotationFromClass() {
            Annotations annotations = Annotations.onMethod(ClassWithRepeatedAnnotationsForMethod.class, "someMethod");

            Stream<RepeatableAnnotation> annotation = annotations.all(RepeatableAnnotation.class);

            then(annotation.map(RepeatableAnnotation::value)).containsExactly(1, 2);
        }

        @Test void shouldGetMoreRepeatableMethodAnnotationsFromClass() {
            Annotations annotations = Annotations.onMethod(ClassWithRepeatableAnnotationOnClassAndMethod.class, "someMethod");

            Stream<RepeatableAnnotation> annotation = annotations.all(RepeatableAnnotation.class);

            then(annotation.map(RepeatableAnnotation::value)).containsExactly(1, 2);
        }

        @Test void shouldOnlyGetAllMethodAnnotationAndNotFromClass() {
            Annotations annotations = Annotations.onMethod(ClassWithAnnotationsOnClassAndMethod.class, "someMethod");

            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + ClassWithAnnotationsOnClassAndMethod.class.getName() + ".someMethod");
        }
    }
}
