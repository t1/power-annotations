package test;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.MixinFor;
import com.github.t1.annotations.tck.MixinClasses.FieldAnnotationMixinClasses.SomeClassWithFieldWithVariousAnnotations;
import com.github.t1.annotations.tck.MixinClasses.FieldAnnotationMixinClasses.TargetFieldClassWithTwoMixins;
import com.github.t1.annotations.tck.MixinClasses.FieldAnnotationMixinClasses.TargetFieldClassWithTwoNonRepeatableMixins;
import com.github.t1.annotations.tck.MixinClasses.FieldAnnotationMixinClasses.TargetFieldClassWithTwoRepeatableMixins;
import com.github.t1.annotations.tck.MixinClasses.MethodAnnotationMixinClasses.MixinForSomeClassWithMethodWithVariousAnnotations;
import com.github.t1.annotations.tck.MixinClasses.MethodAnnotationMixinClasses.SomeClassWithMethodWithVariousAnnotations;
import com.github.t1.annotations.tck.MixinClasses.MethodAnnotationMixinClasses.TargetMethodClassWithTwoMixins;
import com.github.t1.annotations.tck.MixinClasses.MethodAnnotationMixinClasses.TargetMethodClassWithTwoNonRepeatableMixins;
import com.github.t1.annotations.tck.MixinClasses.MethodAnnotationMixinClasses.TargetMethodClassWithTwoRepeatableMixins;
import com.github.t1.annotations.tck.MixinClasses.TypeAnnotationMixinClasses.MixinForAnnotation;
import com.github.t1.annotations.tck.MixinClasses.TypeAnnotationMixinClasses.MixinForSomeClassWithVariousAnnotations;
import com.github.t1.annotations.tck.MixinClasses.TypeAnnotationMixinClasses.OriginalAnnotatedTarget;
import com.github.t1.annotations.tck.MixinClasses.TypeAnnotationMixinClasses.SomeAnnotationTargetedByMixin;
import com.github.t1.annotations.tck.MixinClasses.TypeAnnotationMixinClasses.SomeAnnotationWithoutValue;
import com.github.t1.annotations.tck.MixinClasses.TypeAnnotationMixinClasses.SomeClassWithAnnotationTargetedByMixin;
import com.github.t1.annotations.tck.MixinClasses.TypeAnnotationMixinClasses.SomeClassWithVariousAnnotations;
import com.github.t1.annotations.tck.MixinClasses.TypeAnnotationMixinClasses.TargetClassWithTwoMixins;
import com.github.t1.annotations.tck.MixinClasses.TypeAnnotationMixinClasses.TargetClassWithTwoNonRepeatableMixins;
import com.github.t1.annotations.tck.MixinClasses.TypeAnnotationMixinClasses.TargetClassWithTwoRepeatableMixins;
import com.github.t1.annotations.tck.RepeatableAnnotation;
import com.github.t1.annotations.tck.SomeAnnotation;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

public class MixinBehavior {

    @Nested class ClassAnnotations {
        Annotations annotations = Annotations.on(SomeClassWithVariousAnnotations.class);

        @Test void shouldGetTargetClassAnnotation() {
            Optional<SomeAnnotationWithoutValue> annotation = annotations.get(SomeAnnotationWithoutValue.class);

            then(annotation).isPresent();
        }

        @Test void shouldGetMixinClassAnnotation() {
            Optional<Deprecated> annotation = annotations.get(Deprecated.class);

            then(annotation).isPresent();
        }

        @Test void shouldGetReplacedClassAnnotation() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            BDDAssertions.then(someAnnotation.get().value()).isEqualTo("replacing");
        }

        @Test void shouldGetReplacedRepeatableClassAnnotation() {
            Optional<RepeatableAnnotation> repeatableAnnotation = annotations.get(RepeatableAnnotation.class);

            assert repeatableAnnotation.isPresent();
            then(repeatableAnnotation.get().value()).isEqualTo(1);
        }

        @SuppressWarnings("deprecation")
        @Test void shouldGetAllClassAnnotations() {
            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + MixinForSomeClassWithVariousAnnotations.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"replacing\") on " + MixinForSomeClassWithVariousAnnotations.class.getName(),
                "@" + Deprecated.class.getName() + " on " + MixinForSomeClassWithVariousAnnotations.class.getName(),
                "@" + MixinFor.class.getName() + "(value = " + SomeClassWithVariousAnnotations.class.getName() + ") on " + MixinForSomeClassWithVariousAnnotations.class.getName(),
                "@" + SomeAnnotationWithoutValue.class.getName() + " on " + SomeClassWithVariousAnnotations.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"to-be-replaced\") on " + SomeClassWithVariousAnnotations.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeClassWithVariousAnnotations.class.getName());
        }

        @SuppressWarnings("deprecation")
        @Test void shouldGetAllRepeatableClassAnnotations() {
            Stream<RepeatableAnnotation> list = annotations.all(RepeatableAnnotation.class);

            then(list.map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + MixinForSomeClassWithVariousAnnotations.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeClassWithVariousAnnotations.class.getName());
        }


        Annotations annotationsFromAnnotationTargetedByMixin = Annotations.on(SomeClassWithAnnotationTargetedByMixin.class);

        @Test void shouldGetMixedInAnnotation() {
            Optional<SomeAnnotation> someAnnotation = annotationsFromAnnotationTargetedByMixin.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            BDDAssertions.then(someAnnotation.get().value()).isEqualTo("annotation-mixin");
        }

        @Test void shouldGetAllRepeatedMixedInAnnotation() {
            Stream<SomeAnnotation> someAnnotation = annotationsFromAnnotationTargetedByMixin.all(SomeAnnotation.class);

            BDDAssertions.then(someAnnotation.map(Object::toString)).containsOnly(
                "@" + SomeAnnotation.class.getName() + "(value = \"annotation-mixin\") on " + MixinForAnnotation.class.getName());
        }

        @Test void shouldGetAllMixedInAnnotation() {
            List<Annotation> all = annotationsFromAnnotationTargetedByMixin.all();

            then(all.stream().map(Object::toString)).containsOnly(
                "@" + SomeAnnotationTargetedByMixin.class.getName() + " on " + SomeClassWithAnnotationTargetedByMixin.class.getName(),
                "@" + MixinFor.class.getName() + "(value = " + SomeAnnotationTargetedByMixin.class.getName() + ") " +
                    "on " + MixinForAnnotation.class.getName());
        }


        @Test void shouldGetNonMixedInOriginalInsteadOfOtherMixedInAnnotation() {
            Annotations annotations = Annotations.on(OriginalAnnotatedTarget.class);

            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            BDDAssertions.then(someAnnotation.get().value()).isEqualTo("original");
        }


        @Test void shouldGetClassAnnotationFromMultipleMixins() {
            Annotations annotations = Annotations.on(TargetClassWithTwoMixins.class);

            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            BDDAssertions.then(someAnnotation.get().value()).isEqualTo("one");
        }

        @Test void shouldFailToGetDuplicateNonRepeatableClassAnnotationFromMultipleMixins() {
            Annotations annotations = Annotations.on(TargetClassWithTwoNonRepeatableMixins.class);

            Throwable throwable = catchThrowable(() -> annotations.get(SomeAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        @Test void shouldFailToGetDuplicateRepeatableClassAnnotationFromMultipleMixins() {
            Annotations annotations = Annotations.on(TargetClassWithTwoRepeatableMixins.class);

            Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }
    }

    @Nested class FieldAnnotations {
        Annotations annotations = Annotations.onField(SomeClassWithFieldWithVariousAnnotations.class, "foo");

        @Test void shouldSkipUndefinedMixinFieldAnnotation() {
            Annotations annotations = Annotations.onField(SomeClassWithFieldWithVariousAnnotations.class, "bar");

            Optional<SomeAnnotationWithoutValue> deprecated = annotations.get(SomeAnnotationWithoutValue.class);

            then(deprecated).isNotPresent();
        }

        @Test void shouldGetTargetFieldAnnotation() {
            Optional<SomeAnnotationWithoutValue> deprecated = annotations.get(SomeAnnotationWithoutValue.class);

            then(deprecated).isPresent();
        }

        @Test void shouldGetMixinFieldAnnotation() {
            Optional<Deprecated> deprecated = annotations.get(Deprecated.class);

            then(deprecated).isPresent();
        }

        @Test void shouldGetReplacedFieldAnnotation() {
            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            assert annotation.isPresent();
            BDDAssertions.then(annotation.get().value()).isEqualTo("replacing");
        }

        @Test void shouldFailToGetRepeatableFieldAnnotation() {
            Optional<RepeatableAnnotation> repeatableAnnotation = annotations.get(RepeatableAnnotation.class);

            assert repeatableAnnotation.isPresent();
            then(repeatableAnnotation.get().value()).isEqualTo(1);
        }

        @Test void shouldGetAllFieldAnnotations() {
            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + Deprecated.class.getName() + " on " + com.github.t1.annotations.tck.MixinClasses.FieldAnnotationMixinClasses.MixinForSomeClassWithFieldWithVariousAnnotations.class.getName() + ".foo",
                "@" + SomeAnnotationWithoutValue.class.getName() + " on " + SomeClassWithFieldWithVariousAnnotations.class.getName() + ".foo",
                "@" + SomeAnnotation.class.getName() + "(value = \"to-be-replaced\") on " + SomeClassWithFieldWithVariousAnnotations.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + com.github.t1.annotations.tck.MixinClasses.FieldAnnotationMixinClasses.MixinForSomeClassWithFieldWithVariousAnnotations.class.getName() + ".foo",
                "@" + SomeAnnotation.class.getName() + "(value = \"replacing\") on " + com.github.t1.annotations.tck.MixinClasses.FieldAnnotationMixinClasses.MixinForSomeClassWithFieldWithVariousAnnotations.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeClassWithFieldWithVariousAnnotations.class.getName() + ".foo");
        }

        @Test void shouldGetAllRepeatableFieldAnnotations() {
            Stream<RepeatableAnnotation> list = annotations.all(RepeatableAnnotation.class);

            then(list.map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + com.github.t1.annotations.tck.MixinClasses.FieldAnnotationMixinClasses.MixinForSomeClassWithFieldWithVariousAnnotations.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeClassWithFieldWithVariousAnnotations.class.getName() + ".foo");
        }


        @Test void shouldGetClassAnnotationFromMultipleMixins() {
            Annotations annotations = Annotations.onField(TargetFieldClassWithTwoMixins.class, "foo");

            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            BDDAssertions.then(someAnnotation.get().value()).isEqualTo("one");
        }

        @Test void shouldFailToGetDuplicateNonRepeatableClassAnnotationFromMultipleMixins() {
            Annotations annotations = Annotations.onField(TargetFieldClassWithTwoNonRepeatableMixins.class, "foo");

            Throwable throwable = catchThrowable(() -> annotations.get(SomeAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        @Test void shouldFailToGetDuplicateRepeatableClassAnnotationFromMultipleMixins() {
            Annotations annotations = Annotations.onField(TargetFieldClassWithTwoRepeatableMixins.class, "foo");

            Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }
    }

    @Nested class MethodAnnotations {
        Annotations annotations = Annotations.onMethod(SomeClassWithMethodWithVariousAnnotations.class, "foo");

        @Test void shouldSkipUndefinedMixinMethodAnnotation() {
            Annotations annotations = Annotations.onMethod(SomeClassWithMethodWithVariousAnnotations.class, "bar");

            Optional<SomeAnnotationWithoutValue> deprecated = annotations.get(SomeAnnotationWithoutValue.class);

            then(deprecated).isNotPresent();
        }

        @Test void shouldGetTargetMethodAnnotation() {
            Optional<SomeAnnotationWithoutValue> deprecated = annotations.get(SomeAnnotationWithoutValue.class);

            then(deprecated).isPresent();
        }

        @Test void shouldGetMixinMethodAnnotation() {
            Optional<Deprecated> deprecated = annotations.get(Deprecated.class);

            then(deprecated).isPresent();
        }

        @Test void shouldGetReplacedMethodAnnotation() {
            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            assert annotation.isPresent();
            BDDAssertions.then(annotation.get().value()).isEqualTo("replacing");
        }

        @Test void shouldFailToGetRepeatableMethodAnnotation() {
            Optional<RepeatableAnnotation> repeatableAnnotation = annotations.get(RepeatableAnnotation.class);

            assert repeatableAnnotation.isPresent();
            then(repeatableAnnotation.get().value()).isEqualTo(1);
        }

        @Test void shouldGetAllMethodAnnotations() {
            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + Deprecated.class.getName() + " on " + MixinForSomeClassWithMethodWithVariousAnnotations.class.getName() + ".foo",
                "@" + SomeAnnotationWithoutValue.class.getName() + " on " + SomeClassWithMethodWithVariousAnnotations.class.getName() + ".foo",
                "@" + SomeAnnotation.class.getName() + "(value = \"to-be-replaced\") on " + SomeClassWithMethodWithVariousAnnotations.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + MixinForSomeClassWithMethodWithVariousAnnotations.class.getName() + ".foo",
                "@" + SomeAnnotation.class.getName() + "(value = \"replacing\") on " + MixinForSomeClassWithMethodWithVariousAnnotations.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeClassWithMethodWithVariousAnnotations.class.getName() + ".foo");
        }

        @Test void shouldGetAllRepeatableMethodAnnotations() {
            Stream<RepeatableAnnotation> list = annotations.all(RepeatableAnnotation.class);

            then(list.map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + MixinForSomeClassWithMethodWithVariousAnnotations.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + SomeClassWithMethodWithVariousAnnotations.class.getName() + ".foo");
        }


        @Test void shouldGetClassAnnotationFromMultipleMixins() {
            Annotations annotations = Annotations.onMethod(TargetMethodClassWithTwoMixins.class, "foo");

            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            BDDAssertions.then(someAnnotation.get().value()).isEqualTo("one");
        }

        @Test void shouldFailToGetDuplicateNonRepeatableClassAnnotationFromMultipleMixins() {
            Annotations annotations = Annotations.onMethod(TargetMethodClassWithTwoNonRepeatableMixins.class, "foo");

            Throwable throwable = catchThrowable(() -> annotations.get(SomeAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        @Test void shouldFailToGetDuplicateRepeatableClassAnnotationFromMultipleMixins() {
            Annotations annotations = Annotations.onMethod(TargetMethodClassWithTwoRepeatableMixins.class, "foo");

            Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }
    }
}
