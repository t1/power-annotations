package com.github.t1.annotations.index;

import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.t1.annotations.index.AnnotationInstance.resolveRepeatables;
import static com.github.t1.annotations.index.Utils.toDotName;
import static java.util.Objects.requireNonNull;

public class Index {
    public static Index load() {
        return new Index(Indexer.loadFromIndexFile());
    }

    public static Index from(InputStream inputStream) {
        return new Index(Indexer.loadFrom(inputStream));
    }

    public static Index fromClassPath() {
        return new Index(new Indexer().build());
    }

    final IndexView jandex;

    Index(IndexView jandex) { this.jandex = requireNonNull(jandex); }

    /** abstraction leak */
    @Deprecated public IndexView getJandex() { return jandex; }

    public ClassInfo classInfo(Class<?> type) {
        return new ClassInfo(this, type);
    }

    Optional<org.jboss.jandex.ClassInfo> getClassByName(DotName name) {
        return Optional.ofNullable(this.jandex.getClassByName(name));
    }

    public Stream<AnnotationInstance> annotations(Class<?> type) {
        return jandex.getAnnotations(toDotName(type)).stream()
            .flatMap(instance -> resolveRepeatables(this, instance));
    }

    public boolean isRepeatable(Class<? extends Annotation> type) {
        return AnnotationInstance.isRepeatable(jandex.getClassByName(toDotName(type)));
    }
}
