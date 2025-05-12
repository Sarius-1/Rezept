package com.example.meinkochbuch.util;

import org.jetbrains.annotations.NotNull;

/**
 * Implemented by classes that hold an ID which is later received because it is i.e. loaded from a
 * sqlite database. Therefore, the method {@link #deployID(T)} should then be called.
 * @author KleeSup
 */
public interface IDReceiver<T> {

    /**
     * Deploys the id to the implementing class.
     * @param id The id to deploy.
     */
    void deployID(@NotNull T id);

}
