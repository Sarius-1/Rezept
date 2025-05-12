package com.example.meinkochbuch.util;

import java.util.NoSuchElementException;

public interface OptionalLong {

    static OptionalLong empty(){
        return new OptionalLong() {
            @Override
            public boolean isPresent() {
                return false;
            }

            @Override
            public long get() {
                throw new NoSuchElementException("OptionalLong is empty!");
            }
        };
    }

    static OptionalLong of(long l){
        return new OptionalLong() {
            @Override
            public boolean isPresent() {
                return true;
            }

            @Override
            public long get() {
                return l;
            }
        };
    }

    boolean isPresent();
    long get();

}
