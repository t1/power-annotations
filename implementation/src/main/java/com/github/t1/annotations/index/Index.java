package com.github.t1.annotations.index;

import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import java.util.Optional;
import java.util.stream.Stream;

import static com.github.t1.annotations.index.AnnotationInstance.resolveRepeatables;
import static java.util.Objects.requireNonNull;

public class Index {
    public static Index load() { return Indexer.init(); }

    final IndexView jandex;

    /** visible for testing: we need to load different index files */
    public Index(IndexView jandex) { this.jandex = requireNonNull(jandex); }

    public Optional<ClassInfo> classInfo(Class<?> type) {
        DotName name = DotName.createSimple(type.getName());
        return Optional.ofNullable(jandex.getClassByName(name))
            .map(classInfo -> new ClassInfo(this, classInfo));
    }

    public Stream<AnnotationInstance> annotations(Class<?> type) {
        return jandex.getAnnotations(DotName.createSimple(type.getName())).stream()
            .flatMap(instance -> resolveRepeatables(this, instance));
    }
}
