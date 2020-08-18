package test;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.tck.CombinedAnnotationClasses.SomeInheritingInterface;
import com.github.t1.annotations.tck.CombinedAnnotationClasses.SomeStereotypedClass;
import com.github.t1.annotations.tck.CombinedAnnotationClasses.SomeStereotypedInterface;
import com.github.t1.annotations.tck.SomeAnnotation;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import static org.assertj.core.api.BDDAssertions.then;

public class CombinedBehavior {
    @Test void shouldResolveInterfaceStereotypesBeforeTypeToMember() {
        Annotations fooAnnotations = Annotations.onMethod(SomeStereotypedInterface.class, "foo");

        Stream<Annotation> all = fooAnnotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
            "@" + SomeAnnotation.class.getName() + "(value = \"from-stereotype\")");
    }

    @Test void shouldResolveClassStereotypesBeforeTypeToMember() {
        Annotations fooAnnotations = Annotations.onMethod(SomeStereotypedClass.class, "foo");

        Stream<Annotation> all = fooAnnotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
            "@" + SomeAnnotation.class.getName() + "(value = \"from-stereotype\")");
    }

    @Test void shouldResolveInterfaceInheritedBeforeTypeToMember() {
        Annotations fooAnnotations = Annotations.onMethod(SomeInheritingInterface.class, "foo");

        Stream<Annotation> all = fooAnnotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
            "@" + SomeAnnotation.class.getName() + "(value = \"from-sub-interface\")");
    }
}
