package test.jandexed;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.MixinFor;
import com.github.t1.annotations.RepeatableAnnotationAccessedWithGetException;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Incubating;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static test.jandexed.TestTools.buildAnnotationsLoader;

public class MixinBehavior {
    AnnotationsLoaderImpl loader = buildAnnotationsLoader();


    @Test void shouldFailToGetClassAnnotationFromMultipleMixins() {
        class MixedClass {}

        @MixinFor(MixedClass.class)
        class MixinClass1 {}

        @MixinFor(MixedClass.class)
        class MixinClass2 {}

        Throwable throwable = catchThrowable(() -> loader.onType(MixedClass.class));

        then(throwable)
            .isExactlyInstanceOf(RuntimeException.class)
            .hasMessageStartingWith("multiple mixins for " + MixedClass.class + ": [")
            .hasMessageContaining(MixinClass1.class.getName()) // the order may be inverted
            .hasMessageContaining(MixinClass2.class.getName())
            .hasMessageEndingWith("]");
    }

    @Nested class ClassAnnotations {
        @Incubating
        @SomeAnnotation("to-be-replaced")
        @RepeatableAnnotation(2)
        class TargetClass {}

        @MixinFor(TargetClass.class)
        @Deprecated
        @SomeAnnotation("replacing")
        @RepeatableAnnotation(1)
        class MixinClass {}

        Annotations annotations = loader.onType(TargetClass.class);

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

            then(throwable).isExactlyInstanceOf(RepeatableAnnotationAccessedWithGetException.class);
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
    }

    @Nested class FieldAnnotations {
        class TargetClass {
            @SuppressWarnings("unused")
            @Incubating
            @SomeAnnotation("to-be-replaced")
            @RepeatableAnnotation(2)
            String foo;

            @SuppressWarnings("unused")
            String bar;
        }

        @MixinFor(TargetClass.class)
        class MixinClass {
            @SuppressWarnings("unused")
            @Deprecated
            @SomeAnnotation("replacing")
            @RepeatableAnnotation(1)
            String foo;
        }

        Annotations annotations = loader.onField(TargetClass.class, "foo");

        @Test void shouldNotGetUndefinedFieldAnnotation() {
            Annotations annotations = loader.onField(TargetClass.class, "bar");

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

            then(throwable).isExactlyInstanceOf(RepeatableAnnotationAccessedWithGetException.class);
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
    }

    @Nested class MethodAnnotations {
        class TargetClass {
            @SuppressWarnings("unused")
            @Incubating
            @SomeAnnotation("to-be-replaced")
            @RepeatableAnnotation(2)
            String foo() { return "foo"; }

            @SuppressWarnings("unused")
            String bar() { return "bar"; }
        }

        @MixinFor(TargetClass.class)
        class MixinClass {
            @SuppressWarnings("unused")
            @Deprecated
            @SomeAnnotation("replacing")
            @RepeatableAnnotation(1)
            String foo() { return "foo"; }
        }

        Annotations annotations = loader.onMethod(TargetClass.class, "foo");

        @Test void shouldNotGetUndefinedMethodAnnotation() {
            Annotations annotations = loader.onMethod(TargetClass.class, "bar");

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

            then(throwable).isExactlyInstanceOf(RepeatableAnnotationAccessedWithGetException.class);
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
    }
}
