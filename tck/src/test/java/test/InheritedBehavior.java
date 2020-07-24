package test;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.tck.InheritedAnnotationClass.InheritingClass;
import com.github.t1.annotations.tck.InheritedAnnotationClass.InheritingInterface;
import com.github.t1.annotations.tck.RepeatableAnnotation;
import com.github.t1.annotations.tck.SomeAnnotation;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

public class InheritedBehavior {

    @Test void shouldGetAllOnInterface() {
        Annotations annotations = Annotations.on(InheritingInterface.class);

        Stream<Annotation> all = annotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
            "@" + SomeAnnotation.class.getName() + "(value = \"1\")",
            "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 6)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 8)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 9)");
    }

    @Test void shouldGetAllOnInterfaceMethod() {
        Annotations annotations = Annotations.onMethod(InheritingInterface.class, "method");

        Stream<Annotation> all = annotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
            "@" + SomeAnnotation.class.getName() + "(value = \"7\")",
            "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 6)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 7)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 8)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 9)");
    }

    @Test void shouldGetAllOnClass() {
        Annotations annotations = Annotations.on(InheritingClass.class);

        Stream<Annotation> all = annotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
            "@" + SomeAnnotation.class.getName() + "(value = \"2\")",
            "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 3)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 6)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 8)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 9)");
    }

    @Test void shouldGetAllOnField() {
        Annotations annotations = Annotations.onField(InheritingClass.class, "field");

        Stream<Annotation> all = annotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
            "@" + SomeAnnotation.class.getName() + "(value = \"4\")",
            "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 3)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 4)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 6)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 8)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 9)");
    }

    @Test void shouldGetAllOnClassMethod() {
        Annotations annotations = Annotations.onMethod(InheritingClass.class, "method");

        Stream<Annotation> all = annotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
            "@" + SomeAnnotation.class.getName() + "(value = \"5\")",
            "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 3)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 5)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 6)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 7)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 8)",
            "@" + RepeatableAnnotation.class.getName() + "(value = 9)");
    }
}
