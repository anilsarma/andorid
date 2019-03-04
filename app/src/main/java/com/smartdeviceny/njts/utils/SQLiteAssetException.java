package com.smartdeviceny.njts.utils;

import android.database.sqlite.SQLiteException;

/**
 * An exception that indicates there was an error with SQLite asset retrieval or parsing.
 */
@SuppressWarnings("serial")
public class SQLiteAssetException extends SQLiteException {

    public SQLiteAssetException() {}

    public SQLiteAssetException(String error) {
        super(error);
    }
}