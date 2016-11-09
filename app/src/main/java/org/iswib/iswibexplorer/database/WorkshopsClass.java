package org.iswib.iswibexplorer.database;

import android.provider.BaseColumns;


/**
 * The WorkshopsClass is abstract class presentation of database workshop values
 * This class has fields for all columns that local android database has
 *
 * @author Jovan
 * @version 1.1
 */
public abstract class WorkshopsClass implements BaseColumns {

    // define table properties
    public static final String TABLE_NAME = "iswib_workshops";
    public static final String ID = "id";
    public static final String VERSION = "version";
    public static final String IMAGE = "image";
    public static final String TITLE = "title";
    public static final String TEXT = "text";
    public static final String PREFIX = "workshops";

    // To prevent someone from accidentally instantiating the article class,
    // give it an empty constructor.
    public WorkshopsClass() {}
}


