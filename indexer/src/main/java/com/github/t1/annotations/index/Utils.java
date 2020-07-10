package com.github.t1.annotations.index;

import org.jboss.jandex.DotName;

public class Utils {
    static DotName toDotName(Class<?> type) {
        return DotName.createSimple(type.getName());
    }
}
