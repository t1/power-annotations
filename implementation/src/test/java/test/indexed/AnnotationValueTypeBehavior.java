package test.indexed;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.impl.AnnotationsLoaderImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import static org.assertj.core.api.BDDAssertions.then;
import static test.indexed.TestTools.buildAnnotationsLoader;

@Nested class AnnotationValueTypeBehavior {
    AnnotationsLoaderImpl TheAnnotations = buildAnnotationsLoader();

    @Retention(RUNTIME)
    public @interface DifferentValueTypesAnnotation {
        boolean booleanValue() default false;

        byte byteValue() default 0;

        char charValue() default 0;

        short shortValue() default 0;

        int intValue() default 0;

        long longValue() default 0;

        float floatValue() default 0;

        double doubleValue() default 0;

        String stringValue() default "";

        RetentionPolicy enumValue() default SOURCE;

        Class<?> classValue() default Void.class;

        SomeAnnotation annotationValue() default @SomeAnnotation("");


        boolean[] booleanArrayValue() default false;

        byte[] byteArrayValue() default 0;

        char[] charArrayValue() default 0;

        short[] shortArrayValue() default 0;

        int[] intArrayValue() default 0;

        long[] longArrayValue() default 0;

        double[] doubleArrayValue() default 0;

        float[] floatArrayValue() default 0;

        String[] stringArrayValue() default "";

        RetentionPolicy[] enumArrayValue() default SOURCE;

        Class<?>[] classArrayValue() default Void.class;

        SomeAnnotation[] annotationArrayValue() default @SomeAnnotation("annotation-array-value");
    }

    @Test void shouldGetBooleanAnnotationValue() {
        @DifferentValueTypesAnnotation(booleanValue = true)
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.booleanValue()).isTrue();
    }

    @Test void shouldGetByteAnnotationValue() {
        @DifferentValueTypesAnnotation(byteValue = 1)
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.byteValue()).isEqualTo((byte) 1);
    }

    @Test void shouldGetCharAnnotationValue() {
        @DifferentValueTypesAnnotation(charValue = 'a')
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.charValue()).isEqualTo('a');
    }

    @Test void shouldGetShortAnnotationValue() {
        @DifferentValueTypesAnnotation(shortValue = 1234)
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.shortValue()).isEqualTo((short) 1234);
    }

    @Test void shouldGetIntAnnotationValue() {
        @DifferentValueTypesAnnotation(intValue = 42)
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.intValue()).isEqualTo(42);
    }

    @Test void shouldGetLongAnnotationValue() {
        @DifferentValueTypesAnnotation(longValue = 44L)
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.longValue()).isEqualTo(44L);
    }

    @Test void shouldGetFloatAnnotationValue() {
        @DifferentValueTypesAnnotation(floatValue = 1.2F)
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.floatValue()).isEqualTo(1.2F);
    }

    @Test void shouldGetDoubleAnnotationValue() {
        @DifferentValueTypesAnnotation(doubleValue = 12.34D)
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.doubleValue()).isEqualTo(12.34);
    }

    @Test void shouldGetStringAnnotationValue() {
        @DifferentValueTypesAnnotation(stringValue = "foo")
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.stringValue()).isEqualTo("foo");
    }

    @Test void shouldGetEnumAnnotationValue() {
        @DifferentValueTypesAnnotation(enumValue = RUNTIME)
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.enumValue()).isEqualTo(RUNTIME);
    }

    @Test void shouldGetClassAnnotationValue() {
        @DifferentValueTypesAnnotation(classValue = String.class)
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.classValue()).isEqualTo(String.class);
    }

    @Test void shouldGetAnnotationAnnotationValue() {
        @DifferentValueTypesAnnotation(annotationValue = @SomeAnnotation("annotation-value"))
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.annotationValue().value()).isEqualTo("annotation-value");
    }


    @Test void shouldGetBooleanArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(booleanArrayValue = {true, false})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.booleanArrayValue()).contains(true, false);
    }

    @Test void shouldGetByteArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(byteArrayValue = {1, 2})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.byteArrayValue()).containsExactly((byte) 1, (byte) 2);
    }

    @Test void shouldGetCharArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(charArrayValue = {'a', 'b'})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.charArrayValue()).containsExactly('a', 'b');
    }

    @Test void shouldGetShortArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(shortArrayValue = {1234, 1235})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.shortArrayValue()).containsExactly((short) 1234, (short) 1235);
    }

    @Test void shouldGetIntArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(intArrayValue = {42, 43})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.intArrayValue()).containsExactly(42, 43);
    }

    @Test void shouldGetLongArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(longArrayValue = {44L, 45L})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.longArrayValue()).containsExactly(44L, 45L);
    }

    @Test void shouldGetFloatArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(floatArrayValue = {1.2F, 1.3F})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.floatArrayValue()).containsExactly(1.2F, 1.3F);
    }

    @Test void shouldGetDoubleArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(doubleArrayValue = {12.34D, 12.35D})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.doubleArrayValue()).containsExactly(12.34, 12.35);
    }

    @Test void shouldGetStringArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(stringArrayValue = {"foo", "bar"})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.stringArrayValue()).containsExactly("foo", "bar");
    }

    @Test void shouldGetEnumArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(enumArrayValue = {RUNTIME, CLASS})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.enumArrayValue()).containsExactly(RUNTIME, CLASS);
    }

    @Test void shouldGetClassArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(classArrayValue = {String.class, Integer.class})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(annotation.classArrayValue()).containsExactly(String.class, Integer.class);
    }

    @Test void shouldGetAnnotationArrayAnnotationValue() {
        @DifferentValueTypesAnnotation(annotationArrayValue = {@SomeAnnotation("annotation-value1"), @SomeAnnotation("annotation-value2")})
        class SomeClass {}

        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(SomeClass.class);

        then(Stream.of(annotation.annotationArrayValue()).map(SomeAnnotation::value))
            .containsExactly("annotation-value1", "annotation-value2");
    }


    private DifferentValueTypesAnnotation getDifferentValueTypesAnnotation(Class<?> type) {
        Annotations annotations = TheAnnotations.onType(type);

        Optional<DifferentValueTypesAnnotation> annotation = annotations.get(DifferentValueTypesAnnotation.class);

        assert annotation.isPresent();
        DifferentValueTypesAnnotation someAnnotation = annotation.get();
        then(someAnnotation.annotationType()).isEqualTo(DifferentValueTypesAnnotation.class);
        return someAnnotation;
    }
}
