package org.iswib.iswibexplorer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The DatabaseHelper class is a singleton class that helps access
 * the local android database
 *
 * @author Jovan
 * @version 1.1
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Singleton instance to use with other classes
    private static DatabaseHelper instance;

    // Make it a singleton
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    // Constructor made private so that no class can instantiate it on its own
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // create statement for news database
    private static final String SQL_CREATE_NEWS =
            "CREATE TABLE " + NewsClass.TABLE_NAME + " (" +
                    NewsClass._ID       + " INTEGER PRIMARY KEY," +
                    NewsClass.ID        + " INT UNIQUE," +
                    NewsClass.TITLE     + " TEXT," +
                    NewsClass.TEXT      + " TEXT," +
                    NewsClass.IMAGE     + " TEXT," +
                    NewsClass.DATE      + " TEXT" +
            " )";

    // create statement for calendar database
    private static final String SQL_CREATE_CALENDAR =
            "CREATE TABLE " + CalendarClass.TABLE_NAME + " (" +
                    CalendarClass._ID       + " INTEGER PRIMARY KEY," +
                    CalendarClass.ID        + " INT UNIQUE," +
                    CalendarClass.VERSION   + " TEXT," +
                    CalendarClass.DATE   + " TEXT," +
                    CalendarClass.FIRST_TITLE     + " TEXT," +
                    CalendarClass.FIRST_TEXT      + " TEXT," +
                    CalendarClass.FIRST_IMAGE     + " TEXT," +
                    CalendarClass.FIRST_TIME      + " TEXT," +
                    CalendarClass.SECOND_TITLE     + " TEXT," +
                    CalendarClass.SECOND_TEXT      + " TEXT," +
                    CalendarClass.SECOND_IMAGE     + " TEXT," +
                    CalendarClass.SECOND_TIME      + " TEXT," +
                    CalendarClass.THIRD_TITLE     + " TEXT," +
                    CalendarClass.THIRD_TEXT      + " TEXT," +
                    CalendarClass.THIRD_IMAGE     + " TEXT," +
                    CalendarClass.THIRD_TIME      + " TEXT," +
                    CalendarClass.BREAKFAST      + " TEXT," +
                    CalendarClass.LUNCH      + " TEXT," +
                    CalendarClass.DINNER      + " TEXT," +
                    CalendarClass.WORKSHOPS      + " TEXT" +
                    " )";

    // create statement for workshops database
    private static final String SQL_CREATE_WORKSHOPS =
            "CREATE TABLE " + WorkshopsClass.TABLE_NAME + " (" +
                    WorkshopsClass._ID       + " INTEGER PRIMARY KEY," +
                    WorkshopsClass.ID        + " INT UNIQUE," +
                    WorkshopsClass.VERSION   + " TEXT," +
                    WorkshopsClass.TITLE     + " TEXT," +
                    WorkshopsClass.TEXT      + " TEXT," +
                    WorkshopsClass.IMAGE     + " TEXT" +
                    " )";

    // delete statement for news database
    private static final String SQL_DELETE_NEWS =
            "DROP TABLE IF EXISTS " + NewsClass.TABLE_NAME;

    // delete statement for calendar database
    private static final String SQL_DELETE_CALENDAR =
            "DROP TABLE IF EXISTS " + CalendarClass.TABLE_NAME;

    // delete statement for calendar database
    private static final String SQL_DELETE_WORKSHOPS =
            "DROP TABLE IF EXISTS " + WorkshopsClass.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "iswib.db";

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NEWS);
        db.execSQL(SQL_CREATE_CALENDAR);
        db.execSQL(SQL_CREATE_WORKSHOPS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over

        // DESCOPED - We don't want this in production
        //db.execSQL(SQL_DELETE_NEWS);
        //db.execSQL(SQL_DELETE_CALENDAR);
        //db.execSQL(SQL_DELETE_WORKSHOPS);
        //onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Same as onUpgrade, start over
        onUpgrade(db, oldVersion, newVersion);
    }
}
