package org.iswib.iswibexplorer.database;

import android.provider.BaseColumns;


/**
 * The NewsClass is abstract class presentation of database news values
 * This class has fields for all columns that local android database has
 *
 * @author Jovan
 * @version 1.1
 */
public abstract class NewsClass implements BaseColumns {

    // define table properties
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String TEXT = "text";
    public static final String DATE = "date";
    public static final String PREFIX = "news";

    // To prevent someone from accidentally instantiating the article class,
    // give it an empty constructor.
    public NewsClass() {}
}


