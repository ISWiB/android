package org.iswib.iswibexplorer.database;

import android.provider.BaseColumns;


/**
 * The CalendarClass is abstract class presentation of database calendar values
 * This class has fields for all columns that local android database has
 *
 * @author Jovan
 * @version 1.1
 */
public abstract class CalendarClass implements BaseColumns {

    // define table properties
    public static final String TABLE_NAME = "iswib_calendar";
    public static final String ID = "id";
    public static final String VERSION = "version";
    public static final String DATE = "date";

    public static final String FIRST_IMAGE = "first_image";
    public static final String FIRST_TITLE = "first_title";
    public static final String FIRST_TIME = "first_time";
    public static final String FIRST_TEXT = "first_text";

    public static final String SECOND_IMAGE = "second_image";
    public static final String SECOND_TITLE = "second_title";
    public static final String SECOND_TIME = "second_time";
    public static final String SECOND_TEXT = "second_text";

    public static final String THIRD_IMAGE = "third_image";
    public static final String THIRD_TITLE = "third_title";
    public static final String THIRD_TIME = "third_time";
    public static final String THIRD_TEXT = "third_text";

    public static final String BREAKFAST = "breakfast";
    public static final String LUNCH = "lunch";
    public static final String DINNER = "dinner";
    public static final String WORKSHOPS = "workshops";

    public static final String SEPARATOR = "&";
    public static final String PREFIX = "calendar";
    public static final int DAYS = 18;

    // To prevent someone from accidentally instantiating the article class,
    // give it an empty constructor.
    public CalendarClass() {}
}


