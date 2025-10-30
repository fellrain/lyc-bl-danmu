package com.rain.danmu.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * 集合工具类
 */
public class CollUtil {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return null == map || map.isEmpty();
    }

    public static boolean isEmpty(Iterator<?> Iterator) {
        return null == Iterator || !Iterator.hasNext();
    }

    public static boolean isEmpty(Iterable<?> iterable) {
        return null == iterable || isEmpty(iterable.iterator());
    }
}
