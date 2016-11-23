package com.fingolfintek.lmdb;

import javaslang.collection.Stream;
import javaslang.collection.Stream.Empty;

public class LambdaUtils {
    
    // TODO remove when Javaslang 2.1.0 is released
    public static <T> Stream<T> create(java.util.Iterator<? extends T> iterator) {
        return iterator.hasNext() ? Stream.cons(iterator.next(), () -> create(iterator)) : Empty.instance();
    }
}
