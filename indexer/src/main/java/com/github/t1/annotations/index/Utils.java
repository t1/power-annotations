package com.github.t1.annotations.index;

import java.util.stream.Stream;

public class Utils {
    /** Like JDK 9 <code>Stream::ofNullable</code> */
    static <T> Stream<T> streamOfNullable(T value) {
        return (value == null) ? Stream.empty() : Stream.of(value);
    }
}
