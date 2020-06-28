package test.jandexed;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.MixinFor;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Incubating;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static test.jandexed.TestTools.buildAnnotationsLoader;

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

            then(throwable).isExactlyInstanceOf(AmbiguousAnnotationResolutionException.class);
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

            then(throwable).isExactlyInstanceOf(AmbiguousAnnotationResolutionException.class);
        }

        @Incubating
        @SomeAnnotation("to-be-replaced")
        @RepeatableAnnotation(2)
        class TargetClass {}

        @MixinFor(TargetClass.class)
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

        @Test void shouldFailToGetRepeatableClassAnnotation() {
            Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class)
                .hasMessage("The annotation " + RepeatableAnnotation.class.getName() + " is ambiguous on "
                    + ". You should query it with `all` not `get`."); // the message is an implementation detail
        }

        @Test void shouldGetAllClassAnnotations() {
            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                "@" + SomeAnnotation.class.getName() + "(value = \"replacing\")",
                "@" + Deprecated.class.getName(),
                "@" + MixinFor.class.getName() + "(value = " + TargetClass.class.getName() + ")",
                "@" + Incubating.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"to-be-replaced\")",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
        }

        @Test void shouldGetAllRepeatableClassAnnotations() {
            Stream<RepeatableAnnotation> list = annotations.all(RepeatableAnnotation.class);

            then(list.map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
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

            then(throwable).isExactlyInstanceOf(AmbiguousAnnotationResolutionException.class);
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

            then(throwable).isExactlyInstanceOf(AmbiguousAnnotationResolutionException.class);
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
            Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class)
                .hasMessage("The annotation " + RepeatableAnnotation.class.getName() + " is ambiguous on "
                    + ". You should query it with `all` not `get`."); // the message is an implementation detail
        }

        @Test void shouldGetAllFieldAnnotations() {
            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + Deprecated.class.getName(),
                "@" + Incubating.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"to-be-replaced\")",
                "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                "@" + SomeAnnotation.class.getName() + "(value = \"replacing\")",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
        }

        @Test void shouldGetAllRepeatableFieldAnnotations() {
            Stream<RepeatableAnnotation> list = annotations.all(RepeatableAnnotation.class);

            then(list.map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
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

            then(throwable).isExactlyInstanceOf(AmbiguousAnnotationResolutionException.class);
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

            then(throwable).isExactlyInstanceOf(AmbiguousAnnotationResolutionException.class);
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
            Throwable throwable = catchThrowable(() -> annotations.get(RepeatableAnnotation.class));

            then(throwable).isInstanceOf(AmbiguousAnnotationResolutionException.class)
                .hasMessage("The annotation " + RepeatableAnnotation.class.getName() + " is ambiguous on "
                    + ". You should query it with `all` not `get`."); // the message is an implementation detail
        }

        @Test void shouldGetAllMethodAnnotations() {
            List<Annotation> list = annotations.all();

            then(list.stream().map(Object::toString)).containsOnly(
                "@" + Deprecated.class.getName(),
                "@" + Incubating.class.getName(),
                "@" + SomeAnnotation.class.getName() + "(value = \"to-be-replaced\")",
                "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                "@" + SomeAnnotation.class.getName() + "(value = \"replacing\")",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
        }

        @Test void shouldGetAllRepeatableMethodAnnotations() {
            Stream<RepeatableAnnotation> list = annotations.all(RepeatableAnnotation.class);

            then(list.map(Object::toString)).containsOnly(
                "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
        }
    }
}
