package com.doak.common.extension;

public interface LoadingStrategy {

    String directory();

    default boolean preferExtensionClassLoader() {
        return false;
    }

    default String[] excludedPackages() {
        return null;
    }

    /**
     * Indicates current {@link LoadingStrategy} supports overriding other lower prioritized instances or not.
     *
     * @return if supports, return <code>true</code>, or <code>false</code>
     * @since 2.7.7
     */
    default boolean overridden() {
        return false;
    }

}