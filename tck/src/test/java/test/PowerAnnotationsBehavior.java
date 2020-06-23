package test;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.tck.SomeAnnotation;
import com.github.t1.annotations.tck.SomeClass;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.BDDAssertions.then;

public class PowerAnnotationsBehavior {
    @Test void shouldGetClassAnnotation() {
        Annotations annotations = Annotations.on(SomeClass.class);

        Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

        assert someAnnotation.isPresent();
        then(someAnnotation.get().value()).isEqualTo("class-annotation");
    }
}
