package test.indexed;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.MixinFor;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Incubating;
import org.mockito.NotExtensible;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static test.indexed.TestTools.buildAnnotationsLoader;

public class MixinBehavior {
    AnnotationsLoaderImpl TheAnnotations = buildAnnotationsLoader();

    @Nested class ClassAnnotations {
        @Test void shouldGetClassAnnotationFromMultipleMixins() {
            class TargetClassWithTwoMixins {}

            @MixinFor(TargetClassWithTwoMixins.class)
            @SomeAnnotation("one")
            class MixinClass1 {}

            @MixinFor(TargetClassWithTwoMixins.class)
            @RepeatableAnnotation(2)
            class MixinClass2 {}

            Optional<SomeAnnotation> someAnnotation = TheAnnotations.onType(TargetClassWithTwoMixins.class)
                .get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("one");
        }

        @Test void shouldFailToGetDuplicateNonRepeatableClassAnnotationFromMultipleMixins() {
            class TargetClassWithTwoNonRepeatableMixins {}

            @MixinFor(TargetClassWithTwoNonRepeatableMixins.class)
            @SomeAnnotation("one")
            class MixinClass1 {}

            @MixinFor(TargetClassWithTwoNonRepeatableMixins.class)
            @SomeAnnotation("one")
            class MixinClass2 {}

            Annotations annotations = TheAnnotations.onType(TargetClassWithTwoNonRepeatableMixins.class);

            Throwable throwable = catchThrowable(() -> annotations.get(SomeAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        @Test void shouldFailToGetDuplicateRepeatableClassAnnotationFromMultipleMixins() {
            class TargetClassWithTwoRepeatableMixins {}

            @MixinFor(TargetClassWithTwoRepeatableMixins.class)
            @RepeatableAnnotation(1)
            class MixinClass1 {}

            @MixinFor(TargetClassWithTwoRepeatableMixins.class)
            @RepeatableAnnotation(2)
            class MixinClass2 {}

            Annotations annotations = TheAnnotations.onType(TargetClassWithTwoRepeatableMixins.class);

            Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        @Incubating
        @SomeAnnotation("to-be-replaced")
        @RepeatableAnnotation(2)
        class TargetClass {}

        @MixinFor(TargetClass.class)
        @SuppressWarnings("DeprecatedIsStillUsed")
        @Deprecated
        @SomeAnnotation("replacing")
        @RepeatableAnnotation(1)
        class MixinClass {}

        Annotations annotations = TheAnnotations.onType(TargetClass.class);

        @Test void shouldGetTargetClassAnnotation() {
            Optional<Incubating> annotation = annotations.get(Incubating.class);

            then(annotation).isPresent();
        }

        @Test void shouldGetMixinClassAnnotation() {
            Optional<Deprecated> annotation = annotations.get(Deprecated.class);

            then(annotation).isPresent();
        }

        @Test void shouldGetReplacedClassAnnotation() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("replacing");
        }

        @Test void shouldGetReplacedRepeatableClassAnnotation() {
            Optional<RepeatableAnnotation> repeatableAnnotation = annotations.get(RepeatableAnnotation.class);

            assert repeatableAnnotation.isPresent();
            then(repeatableAnnotation.get().value()).isEqualTo(1);
        }

        @Test void shouldGetAllClassAnnotations() {
            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + MixinClass.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"replacing\") on " + MixinClass.class.getName(),
                "@" + Deprecated.class.getName() + " on " + MixinClass.class.getName(),
                "@" + MixinFor.class.getName() + "(value = " + TargetClass.class.getName() + ") on " + MixinClass.class.getName(),
                "@" + Incubating.class.getName() + " on " + TargetClass.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"to-be-replaced\") on " + TargetClass.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + TargetClass.class.getName());
        }

        @Test void shouldGetAllRepeatableClassAnnotations() {
            Stream<RepeatableAnnotation> list = annotations.all(RepeatableAnnotation.class);

            then(list.map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + MixinClass.class.getName(),
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + TargetClass.class.getName());
        }


        @NotExtensible
        public class NotExtensibleTarget {}

        @MixinFor(NotExtensible.class)
        @SomeAnnotation("annotation-mixin")
        public class NotExtensibleMixin {}

        @Test void shouldGetMixedInAnnotation() {
            Annotations annotations = TheAnnotations.onType(NotExtensibleTarget.class);

            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("annotation-mixin");
        }

        @Test void shouldGetAllRepeatedMixedInAnnotation() {
            Annotations annotations = TheAnnotations.onType(NotExtensibleTarget.class);

            Stream<SomeAnnotation> someAnnotation = annotations.all(SomeAnnotation.class);

            then(someAnnotation.map(Object::toString)).containsOnly(
                "@" + SomeAnnotation.class.getName() + "(value = \"annotation-mixin\") on " + NotExtensibleMixin.class.getName());
        }

        @Test void shouldGetAllMixedInAnnotation() {
            Annotations annotations = TheAnnotations.onType(NotExtensibleTarget.class);

            List<Annotation> all = annotations.all();

            then(all.stream().map(Object::toString)).containsOnly(
                "@" + NotExtensible.class.getName() + " on " + NotExtensibleTarget.class.getName(),
                "@" + MixinFor.class.getName() + "(value = " + NotExtensible.class.getName() + ") " +
                    "on " + NotExtensibleMixin.class.getName());
        }


        @NotExtensible
        @SomeAnnotation("original")
        public class OriginalAnnotatedTarget {}

        @Test void shouldGetOriginalInsteadOfMixedInAnnotation() {
            Annotations annotations = TheAnnotations.onType(OriginalAnnotatedTarget.class);

            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("original");
        }
    }

    @Nested class FieldAnnotations {
        @Test void shouldGetClassAnnotationFromMultipleMixins() {
            class TargetFieldClassWithTwoMixins {
                @SuppressWarnings("unused") String foo;
            }

            @MixinFor(TargetFieldClassWithTwoMixins.class)
            class MixinClass1 {
                @SomeAnnotation("one")
                @SuppressWarnings("unused") String foo;
            }

            @MixinFor(TargetFieldClassWithTwoMixins.class)
            class MixinClass2 {
                @RepeatableAnnotation(2)
                @SuppressWarnings("unused") String foo;
            }

            Optional<SomeAnnotation> someAnnotation = TheAnnotations.onField(TargetFieldClassWithTwoMixins.class, "foo")
                .get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("one");
        }

        @Test void shouldFailToGetDuplicateNonRepeatableClassAnnotationFromMultipleMixins() {
            class TargetFieldClassWithTwoNonRepeatableMixins {
                @SuppressWarnings("unused") String foo;
            }

            @MixinFor(TargetFieldClassWithTwoNonRepeatableMixins.class)
            class MixinClass1 {
                @SomeAnnotation("one")
                @SuppressWarnings("unused") String foo;
            }

            @MixinFor(TargetFieldClassWithTwoNonRepeatableMixins.class)
            class MixinClass2 {
                @SomeAnnotation("one")
                @SuppressWarnings("unused") String foo;
            }

            Annotations annotations = TheAnnotations.onField(TargetFieldClassWithTwoNonRepeatableMixins.class, "foo");

            Throwable throwable = catchThrowable(() -> annotations.get(SomeAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        @Test void shouldFailToGetDuplicateRepeatableClassAnnotationFromMultipleMixins() {
            class TargetFieldClassWithTwoRepeatableMixins {
                @SuppressWarnings("unused") String foo;
            }

            @MixinFor(TargetFieldClassWithTwoRepeatableMixins.class)
            class MixinClass1 {
                @RepeatableAnnotation(1)
                @SuppressWarnings("unused") String foo;
            }

            @MixinFor(TargetFieldClassWithTwoRepeatableMixins.class)
            class MixinClass2 {
                @RepeatableAnnotation(2)
                @SuppressWarnings("unused") String foo;
            }

            Annotations annotations = TheAnnotations.onField(TargetFieldClassWithTwoRepeatableMixins.class, "foo");

            Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        class TargetFieldClass {
            @SuppressWarnings("unused")
            @Incubating
            @SomeAnnotation("to-be-replaced")
            @RepeatableAnnotation(2)
            String foo;

            @SuppressWarnings("unused")
            String bar;
        }

        @MixinFor(TargetFieldClass.class)
        class MixinClass {
            @SuppressWarnings("unused")
            @Deprecated
            @SomeAnnotation("replacing")
            @RepeatableAnnotation(1)
            String foo;
        }

        Annotations annotations = TheAnnotations.onField(TargetFieldClass.class, "foo");

        @Test void shouldSkipUndefinedMixinFieldAnnotation() {
            Annotations annotations = TheAnnotations.onField(TargetFieldClass.class, "bar");

            Optional<Incubating> deprecated = annotations.get(Incubating.class);

            then(deprecated).isNotPresent();
        }

        @Test void shouldGetTargetFieldAnnotation() {
            Optional<Incubating> deprecated = annotations.get(Incubating.class);

            then(deprecated).isPresent();
        }

        @Test void shouldGetMixinFieldAnnotation() {
            Optional<Deprecated> deprecated = annotations.get(Deprecated.class);

            then(deprecated).isPresent();
        }

        @Test void shouldGetReplacedFieldAnnotation() {
            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            assert annotation.isPresent();
            then(annotation.get().value()).isEqualTo("replacing");
        }

        @Test void shouldFailToGetRepeatableFieldAnnotation() {
            Optional<RepeatableAnnotation> repeatableAnnotation = annotations.get(RepeatableAnnotation.class);

            assert repeatableAnnotation.isPresent();
            then(repeatableAnnotation.get().value()).isEqualTo(1);
        }

        @Test void shouldGetAllFieldAnnotations() {
            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + Deprecated.class.getName() + " on " + MixinClass.class.getName() + ".foo",
                "@" + Incubating.class.getName() + " on " + TargetFieldClass.class.getName() + ".foo",
                "@" + SomeAnnotation.class.getName() + "(value = \"to-be-replaced\") on " + TargetFieldClass.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + MixinClass.class.getName() + ".foo",
                "@" + SomeAnnotation.class.getName() + "(value = \"replacing\") on " + MixinClass.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + TargetFieldClass.class.getName() + ".foo");
        }

        @Test void shouldGetAllRepeatableFieldAnnotations() {
            Stream<RepeatableAnnotation> list = annotations.all(RepeatableAnnotation.class);

            then(list.map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + MixinClass.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + TargetFieldClass.class.getName() + ".foo");
        }
    }

    @Nested class MethodAnnotations {
        @Test void shouldGetClassAnnotationFromMultipleMixins() {
            class TargetMethodClassWithTwoMixins {
                @SuppressWarnings("unused") String foo() { return "foo"; }
            }

            @MixinFor(TargetMethodClassWithTwoMixins.class)
            class MixinClass1 {
                @SomeAnnotation("one")
                @SuppressWarnings("unused") String foo() { return "foo"; }
            }

            @MixinFor(TargetMethodClassWithTwoMixins.class)
            class MixinClass2 {
                @RepeatableAnnotation(2)
                @SuppressWarnings("unused") String foo() { return "foo"; }
            }

            Optional<SomeAnnotation> someAnnotation = TheAnnotations.onMethod(TargetMethodClassWithTwoMixins.class, "foo")
                .get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("one");
        }

        @Test void shouldFailToGetDuplicateNonRepeatableClassAnnotationFromMultipleMixins() {
            class TargetMethodClassWithTwoNonRepeatableMixins {
                @SuppressWarnings("unused") String foo() { return "foo"; }
            }

            @MixinFor(TargetMethodClassWithTwoNonRepeatableMixins.class)
            class MixinClass1 {
                @SomeAnnotation("one")
                @SuppressWarnings("unused") String foo() { return "foo"; }
            }

            @MixinFor(TargetMethodClassWithTwoNonRepeatableMixins.class)
            class MixinClass2 {
                @SomeAnnotation("one")
                @SuppressWarnings("unused") String foo() { return "foo"; }
            }

            Annotations annotations = TheAnnotations.onMethod(TargetMethodClassWithTwoNonRepeatableMixins.class, "foo");

            Throwable throwable = catchThrowable(() -> annotations.get(SomeAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        @Test void shouldFailToGetDuplicateRepeatableClassAnnotationFromMultipleMixins() {
            class TargetMethodClassWithTwoRepeatableMixins {
                @SuppressWarnings("unused") String foo() { return "foo"; }
            }

            @MixinFor(TargetMethodClassWithTwoRepeatableMixins.class)
            class MixinClass1 {
                @RepeatableAnnotation(1)
                @SuppressWarnings("unused") String foo() { return "foo"; }
            }

            @MixinFor(TargetMethodClassWithTwoRepeatableMixins.class)
            class MixinClass2 {
                @RepeatableAnnotation(2)
                @SuppressWarnings("unused") String foo() { return "foo"; }
            }

            Annotations annotations = TheAnnotations.onMethod(TargetMethodClassWithTwoRepeatableMixins.class, "foo");

            Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        class TargetMethodClass {
            @SuppressWarnings("unused")
            @Incubating
            @SomeAnnotation("to-be-replaced")
            @RepeatableAnnotation(2)
            String foo() { return "foo"; }

            @SuppressWarnings("unused")
            String bar() { return "bar"; }
        }

        @MixinFor(TargetMethodClass.class)
        class MixinClass {
            @SuppressWarnings("unused")
            @Deprecated
            @SomeAnnotation("replacing")
            @RepeatableAnnotation(1)
            String foo() { return "foo"; }
        }

        Annotations annotations = TheAnnotations.onMethod(TargetMethodClass.class, "foo");

        @Test void shouldSkipUndefinedMixinMethodAnnotation() {
            Annotations annotations = TheAnnotations.onMethod(TargetMethodClass.class, "bar");

            Optional<Incubating> deprecated = annotations.get(Incubating.class);

            then(deprecated).isNotPresent();
        }

        @Test void shouldGetTargetMethodAnnotation() {
            Optional<Incubating> deprecated = annotations.get(Incubating.class);

            then(deprecated).isPresent();
        }

        @Test void shouldGetMixinMethodAnnotation() {
            Optional<Deprecated> deprecated = annotations.get(Deprecated.class);

            then(deprecated).isPresent();
        }

        @Test void shouldGetReplacedMethodAnnotation() {
            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            assert annotation.isPresent();
            then(annotation.get().value()).isEqualTo("replacing");
        }

        @Test void shouldFailToGetRepeatableMethodAnnotation() {
            Optional<RepeatableAnnotation> repeatableAnnotation = annotations.get(RepeatableAnnotation.class);

            assert repeatableAnnotation.isPresent();
            then(repeatableAnnotation.get().value()).isEqualTo(1);
        }

        @Test void shouldGetAllMethodAnnotations() {
            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + Deprecated.class.getName() + " on " + MixinClass.class.getName() + ".foo",
                "@" + Incubating.class.getName() + " on " + TargetMethodClass.class.getName() + ".foo",
                "@" + SomeAnnotation.class.getName() + "(value = \"to-be-replaced\") on " + TargetMethodClass.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + MixinClass.class.getName() + ".foo",
                "@" + SomeAnnotation.class.getName() + "(value = \"replacing\") on " + MixinClass.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + TargetMethodClass.class.getName() + ".foo");
        }

        @Test void shouldGetAllRepeatableMethodAnnotations() {
            Stream<RepeatableAnnotation> list = annotations.all(RepeatableAnnotation.class);

            then(list.map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1) on " + MixinClass.class.getName() + ".foo",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2) on " + TargetMethodClass.class.getName() + ".foo");
        }
    }
}
