package com.eggtive.spm.common.enums;

public enum StorageType {
    LOCAL("local"),
    S3("s3");

    private final String value;

    StorageType(String value) {
        this.value = value;
    }

    /** The value stored in the database and used in config. */
    public String value() {
        return value;
    }
}
