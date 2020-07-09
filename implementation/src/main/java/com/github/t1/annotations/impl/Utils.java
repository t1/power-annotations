package com.github.t1.annotations.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

class Utils {
    /**
     * Collect to an {@link Optional}: if the Stream is empty return an empty Optional.
     * If it contains one element, return an Optional with that element. If the stream
     * contains more than one element get an exception from the supplier and throw it.
     */
    static <T> Collector<T, ?, Optional<T>> toOptionalOrThrow(Function<List<T>, RuntimeException> throwableSupplier) {
        return Collector.of(
            (Supplier<List<T>>) ArrayList::new,
            List::add,
            (left, right) -> {
                left.addAll(right);
                return left;
            },
            list -> {
                switch (list.size()) {
                    case 0:
                        return Optional.empty();
                    case 1:
                        return Optional.of(list.get(0));
                    default:
                        throw throwableSupplier.apply(list);
                }
            });
    }

    /** Like JDK 9 <code>Optional::stream</code> */
    static <T> Stream<T> toStream(Optional<T> optional) {
        return optional.map(Stream::of).orElseGet(Stream::empty);
    }

    /** Like JDK 9 <code>Optional::or</code> */
    static <T> Optional<T> or(Optional<T> optional, Supplier<Optional<T>> alternative) {
        return optional.isPresent() ? optional : alternative.get();
    }

    static <T extends Enum<T>> Enum<T> enumValue(Class<?> type, String value) {
        @SuppressWarnings("unchecked")
        Class<T> enumType = (Class<T>) type;
        return Enum.valueOf(enumType, value);
    }
}
