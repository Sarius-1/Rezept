package com.example.meinkochbuch.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class with various methods to verify parameters.
 * @author KleeSup
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Verify {

    public static boolean isInvalidText(String s){
        return s == null || s.isEmpty();
    }

    public static boolean notSameClass(Object obj, Class<?> clazz){
        return obj == null || !obj.getClass().equals(clazz);
    }

}
