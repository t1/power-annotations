package com.github.t1.annotations.index;

import org.jboss.jandex.DotName;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;

public class Utils {

    static DotName toDotName(Class<?> type) {
        return toDotName(type.getName());
    }

    static DotName toDotName(String typeName) {
        return DotName.createSimple(typeName);
    }

    public static <T> Collector<T, List<T>, T[]> toArray(Class<T> componentType) {
        return new Collector<T, List<T>, T[]>() {
            @Override
            public Supplier<List<T>> supplier() { return ArrayList::new; }

            @Override
            public BiConsumer<List<T>, T> accumulator() { return List::add; }

            @Override
            public BinaryOperator<List<T>> combiner() {
                return (a, b) -> {
                    a.addAll(b);
                    return a;
                };
            }

            @Override
            public Function<List<T>, T[]> finisher() {
                return list -> {
                    @SuppressWarnings("unchecked")
                    T[] array = (T[]) Array.newInstance(componentType, 0);
                    return list.toArray(array);
                };
            }

            @Override
            public Set<Characteristics> characteristics() { return emptySet(); }
        };
    }
}
