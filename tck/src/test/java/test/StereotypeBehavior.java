package test;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.Stereotype;
import com.github.t1.annotations.tck.RepeatableAnnotation;
import com.github.t1.annotations.tck.SomeAnnotation;
import com.github.t1.annotations.tck.StereotypeClasses.AnotherStereotype;
import com.github.t1.annotations.tck.StereotypeClasses.ClassWithStereotypedField;
import com.github.t1.annotations.tck.StereotypeClasses.ClassWithStereotypedMethod;
import com.github.t1.annotations.tck.StereotypeClasses.DoubleStereotypedClass;
import com.github.t1.annotations.tck.StereotypeClasses.SomeStereotype;
import com.github.t1.annotations.tck.StereotypeClasses.StereotypedClass;
import com.github.t1.annotations.tck.StereotypeClasses.StereotypedClassWithSomeAnnotation;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

public class StereotypeBehavior {

    @Nested class StereotypedClasses {
        Annotations annotations = Annotations.on(StereotypedClass.class);

        @Test void shouldGetAnnotationFromClassStereotype() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            BDDAssertions.then(someAnnotation.get().value()).isEqualTo("stereotype");
        }

        @Test void shouldGetAllAnnotationsFromClassStereotype() {
            List<Annotation> someAnnotation = annotations.all();

            then(someAnnotation.stream().map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 5) on " + StereotypedClass.class.getName(),
                "@" + SomeStereotype.class.getName() + " on " + StereotypedClass.class.getName(),
                "@" + Stereotype.class.getName() + " on " + SomeStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName(),
                "@" + Retention.class.getName() + "(value = RUNTIME) on " + SomeStereotype.class.getName());
        }

        @Test void shouldGetAllNonRepeatableAnnotationsFromClassStereotype() {
            Stream<SomeAnnotation> someAnnotation = annotations.all(SomeAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsOnly(
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName()
            );
        }

        @Test void shouldGetAllRepeatableAnnotationFromClassStereotype() {
            Stream<RepeatableAnnotation> someAnnotation = annotations.all(RepeatableAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 5) on " + StereotypedClass.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName()
            );
        }

        // TODO test indirect stereotypes

        @Test void shouldNotReplaceExistingClassAnnotation() {
            Annotations annotations = Annotations.on(StereotypedClassWithSomeAnnotation.class);

            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            BDDAssertions.then(someAnnotation.get().value()).isEqualTo("on-class");
        }
    }

    @Nested class DoubleStereotypedClasses {
        Annotations annotations = Annotations.on(DoubleStereotypedClass.class);

        @Test void shouldFailToGetAmbiguousAnnotationFromTwoStereotypes() {
            Throwable throwable = catchThrowable(() -> annotations.get(SomeAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        @Test void shouldGetAllNonRepeatableAnnotationsFromTwoStereotypes() {
            Stream<SomeAnnotation> someAnnotations = annotations.all(SomeAnnotation.class);

            then(someAnnotations.map(Objects::toString)).containsOnly(
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"another-stereotype\") on " + AnotherStereotype.class.getName()
            );
        }

        @Test void shouldGetAllRepeatableAnnotationsFromTwoStereotypes() {
            Stream<RepeatableAnnotation> repeatableAnnotations = annotations.all(RepeatableAnnotation.class);

            then(repeatableAnnotations.map(Objects::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 6) on " + DoubleStereotypedClass.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 3) on " + AnotherStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 4) on " + AnotherStereotype.class.getName()
            );
        }

        @Test void shouldGetAllAnnotationsFromTwoStereotypes() {
            List<Annotation> all = annotations.all();

            then(all.stream().map(Objects::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 6) on " + DoubleStereotypedClass.class.getName(),
                "@" + SomeStereotype.class.getName() + " on " + DoubleStereotypedClass.class.getName(),
                "@" + AnotherStereotype.class.getName() + " on " + DoubleStereotypedClass.class.getName(),
                "@" + Stereotype.class.getName() + " on " + SomeStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName(),
                "@" + Retention.class.getName() + "(value = RUNTIME) on " + SomeStereotype.class.getName(),
                "@" + Stereotype.class.getName() + " on " + AnotherStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"another-stereotype\") on " + AnotherStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 3) on " + AnotherStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 4) on " + AnotherStereotype.class.getName(),
                "@" + Retention.class.getName() + "(value = RUNTIME) on " + AnotherStereotype.class.getName()
            );
        }
    }

    @Nested class StereotypedFields {
        Annotations annotations = Annotations.onField(ClassWithStereotypedField.class, "foo");

        @Test void shouldGetAnnotationFromFieldStereotype() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            BDDAssertions.then(someAnnotation.get().value()).isEqualTo("stereotype");
        }

        @Test void shouldGetAllAnnotationsFromFieldStereotype() {
            List<Annotation> someAnnotation = annotations.all();

            then(someAnnotation.stream().map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 7) on " + ClassWithStereotypedField.class.getName() + ".foo",
                "@" + SomeStereotype.class.getName() + " on " + ClassWithStereotypedField.class.getName() + ".foo",
                "@" + Stereotype.class.getName() + " on " + SomeStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName(),
                "@" + Retention.class.getName() + "(value = RUNTIME) on " + SomeStereotype.class.getName());
        }

        @Test void shouldGetAllAnnotationNonRepeatableTypedFromFieldStereotype() {
            Stream<SomeAnnotation> someAnnotation = annotations.all(SomeAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsOnly(
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName()
            );
        }
    }

    @Nested class StereotypedMethods {
        Annotations annotations = Annotations.onMethod(ClassWithStereotypedMethod.class, "foo");

        @Test void shouldGetAnnotationFromMethodStereotype() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            BDDAssertions.then(someAnnotation.get().value()).isEqualTo("stereotype");
        }

        @Test void shouldGetAllAnnotationsFromMethodStereotype() {
            List<Annotation> someAnnotation = annotations.all();

            then(someAnnotation.stream().map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 7) on " + ClassWithStereotypedMethod.class.getName() + ".foo",
                "@" + SomeStereotype.class.getName() + " on " + ClassWithStereotypedMethod.class.getName() + ".foo",
                "@" + Stereotype.class.getName() + " on " + SomeStereotype.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + SomeStereotype.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeStereotype.class.getName(),
                "@" + Retention.class.getName() + "(value = RUNTIME) on " + SomeStereotype.class.getName());
        }

        @Test void shouldGetAllAnnotationNonRepeatableTypedFromMethodStereotype() {
            Stream<SomeAnnotation> someAnnotation = annotations.all(SomeAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsOnly(
                "@" + SomeAnnotation.class.getName() + "(value = \"stereotype\") on " + SomeStereotype.class.getName()
            );
        }
    }
}
