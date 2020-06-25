package test.jandexed;

import test.jandexed.RepeatableAnnotation.RepeatableAnnotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Repeatable(RepeatableAnnotations.class)
public @interface RepeatableAnnotation {
    int value();

    @Retention(RUNTIME) @interface RepeatableAnnotations {
        RepeatableAnnotation[] value();
    }
}
