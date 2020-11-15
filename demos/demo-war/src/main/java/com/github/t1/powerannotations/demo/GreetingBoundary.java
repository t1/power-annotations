package com.github.t1.powerannotations.demo;

import org.eclipse.microprofile.graphql.Query;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@GraphQlBoundary
public class GreetingBoundary {
    private static final Logger LOG = Logger.getLogger(GreetingBoundary.class.getName());

    @Query
    public String hello() {
        String mixedInValue = Annotations.on(GreetingBoundary.class)
            .get(SomeAnnotation.class)
            .map(SomeAnnotation::value)
            .orElse("nope");
        String stereotypedValue = Annotations.on(GreetingBoundary.class)
            .get(SomeOtherAnnotation.class)
            .map(SomeOtherAnnotation::value)
            .orElse("nope");
        String self = Annotations.on(GreetingBoundary.class).all()
            .map(Objects::toString)
            .collect(Collectors.joining("\n"));
        LOG.info("hello -> " + mixedInValue + "/" + stereotypedValue + "\n" + self);
        return "mixed-in-annotation:" + mixedInValue + "\n"
            + "stereotyped-annotation:" + stereotypedValue;
    }

    private static class Annotations {
        private static final Index jandex = loadJandex();

        private static Index loadJandex() {
            try (InputStream inputStream = Annotations.class.getResourceAsStream("/META-INF/jandex.idx")) {
                return new IndexReader(inputStream).read();
            } catch (IOException e) {
                throw new RuntimeException("can't read jandex", e);
            }
        }

        public static Annotations on(Class<?> type) {
            return new Annotations(type);
        }


        private final ClassInfo classInfo;

        public Annotations(Class<?> type) {
            this.classInfo = jandex.getClassByName(toDotName(type));
        }

        public <T extends Annotation> Optional<T> get(Class<T> annotationType) {
            return Optional.ofNullable(classInfo.classAnnotation(toDotName(annotationType)))
                .map(this::proxy);
        }

        public <T> Stream<T> all() {
            return classInfo.classAnnotations().stream().map(this::proxy);
        }

        private <T> T proxy(AnnotationInstance annotationInstance) {
            //noinspection unchecked
            return (T) AnnotationProxy.proxy(annotationInstance);
        }

        private DotName toDotName(Class<?> annotationType) {
            return DotName.createSimple(annotationType.getName());
        }
    }
}
