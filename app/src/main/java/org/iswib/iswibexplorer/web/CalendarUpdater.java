package org.iswib.iswibexplorer.web;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.iswib.iswibexplorer.MainActivity;
import org.iswib.iswibexplorer.database.CalendarClass;
import org.iswib.iswibexplorer.database.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * The CalendarUpdater updates the calendar from the internet and stores it to
 * the local android database only if version of the calendar day is not the same
 *
 * @author Jovan
 * @version 1.1
 */
public class CalendarUpdater extends AsyncTask<String, Void, String> {

    private ArrayList<String> idRemote = new ArrayList<>(); // holds the day id fetched from the web
    private ArrayList<String> idLocal = new ArrayList<>(); // holds the day id from android local database
    private ArrayList<String> versionRemote = new ArrayList<>(); // holds the day version from the web
    private ArrayList<String> versionLocal = new ArrayList<>(); // holds the day version from android local database
    private Context context;

    // public constructor that will receive application context
    public CalendarUpdater(Context context){
        this.context=context;
    }


    @Override
    protected synchronized String doInBackground(String... params) {

        // Set the flag to false
        MainActivity.calendarFlag = false;

        /*
         * get the list of calendar day ids from internet
         */

        // call the API script that will return all calendar ids
        String result = Downloader.getString("http://iswib.org/getCalendar.php", this);

        if(result != null) {
            // parse the ids and put them in a list
            try {
                // this will create json array: [{"key":"value","key2:"value2"},{"key":"value"}]
                JSONArray arr = new JSONArray(result);
                // for every item in an array
                for (int i = 0; i < arr.length(); i++) {
                    // create a json object like: {"key":"value"}
                    JSONObject json = arr.getJSONObject(i);
                    // get the id from an json object
                    String id = json.getString(CalendarClass.ID);
                    String version = json.getString(CalendarClass.VERSION);
                    // add it to the list
                    idRemote.add(id);
                    versionRemote.add(version);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /*
             * get ids from local android database
             */

            // get database instance
            DatabaseHelper daHelper = DatabaseHelper.getInstance(context);
            SQLiteDatabase db = daHelper.getReadableDatabase();

            // this represents the columns that will be returned
            String[] tableColumns = {
                    CalendarClass.ID,
                    CalendarClass.VERSION
            };

            // sorting order
            String sortOrder = CalendarClass.ID + " ASC";

            Cursor cursor = db.query(
                    CalendarClass.TABLE_NAME,           // table
                    tableColumns,                       // columns
                    null,                               // selection
                    null,                               // selection arguments
                    null,                               // group by
                    null,                               // having
                    sortOrder                           // order by
            );

            // get the number of rows returned
            int count = cursor.getCount();

            /*
             * compare and update local database where needed
             */

            if (count == 0) {
                // database empty, create calendar in local database
                for (String id : idRemote) {
                    updateCalendar(id, db);
                }
            } else {
                // get ids from local database
                while (cursor.moveToNext()) {
                    idLocal.add(cursor.getString(cursor.getColumnIndex(CalendarClass.ID)));
                    versionLocal.add(cursor.getString(cursor.getColumnIndex(CalendarClass.VERSION)));
                }
                // Compare web to local ids and update if needed
                for (String id : idRemote) {
                    if (idLocal.contains(id)) {

                        // get local version
                        String localVer = versionLocal.get(idLocal.indexOf(id));
                        String remoteVer = versionRemote.get(idRemote.indexOf(id));
                        if (!localVer.equals(remoteVer)) {
                            updateCalendar(id, db);
                        }
                    } else {
                        // This means there's no day with that id, so add one
                        updateCalendar(id, db);
                    }
                }
            }

            // release the cursor
            cursor.close();

            return result;
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // Mark that the update is finished
        MainActivity.calendarFlag = true;

        Log.i("CalendarUpdate", "Finished");
    }

    private void updateCalendar(String id, SQLiteDatabase db) {

        // Get all rows for calendar with this id
        String result = Downloader.getString("http://iswib.org/getCalendar.php?id=" + id, this);

        // Parse the result and put every value into variable
        try {
            // this will create json array
            JSONArray arr = new JSONArray(result);
            // no need to loop as only one row will be returned
            JSONObject json = arr.getJSONObject(0);
            // get all fields from json object
            String version = json.getString(CalendarClass.VERSION);
            String date = json.getString(CalendarClass.DATE);
            String breakfast = json.getString(CalendarClass.BREAKFAST);
            String lunch = json.getString(CalendarClass.LUNCH);
            String dinner = json.getString(CalendarClass.DINNER);
            String workshops = json.getString(CalendarClass.WORKSHOPS);

            String first_image = json.getString(CalendarClass.FIRST_IMAGE);
            String first_title = json.getString(CalendarClass.FIRST_TITLE);
            String first_text = json.getString(CalendarClass.FIRST_TEXT);
            String first_time = json.getString(CalendarClass.FIRST_TIME);

            String second_image = json.getString(CalendarClass.SECOND_IMAGE);
            String second_title = json.getString(CalendarClass.SECOND_TITLE);
            String second_text = json.getString(CalendarClass.SECOND_TEXT);
            String second_time = json.getString(CalendarClass.SECOND_TIME);

            String third_image = json.getString(CalendarClass.THIRD_IMAGE);
            String third_title = json.getString(CalendarClass.THIRD_TITLE);
            String third_text = json.getString(CalendarClass.THIRD_TEXT);
            String third_time = json.getString(CalendarClass.THIRD_TIME);

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(CalendarClass.ID, id);
            values.put(CalendarClass.VERSION, version);
            values.put(CalendarClass.DATE, date);
            values.put(CalendarClass.BREAKFAST, breakfast);
            values.put(CalendarClass.LUNCH, lunch);
            values.put(CalendarClass.DINNER, dinner);
            values.put(CalendarClass.WORKSHOPS, workshops);

            // Download "first" data
            if(!first_image.equals("null")) {
                Bitmap img = Downloader.getImage("http://iswib.org/" + first_image, this);
                first_image = first_image.substring(first_image.lastIndexOf("/") + 1);
                FileOutputStream out;
                try {
                    out = context.openFileOutput(CalendarClass.PREFIX + first_image, Context.MODE_PRIVATE);
                    if (img != null) {
                        img.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    }
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                values.put(CalendarClass.FIRST_IMAGE, CalendarClass.PREFIX + first_image);
            } else {
                values.put(CalendarClass.FIRST_IMAGE, "null");
            }
            values.put(CalendarClass.FIRST_TITLE, first_title);
            values.put(CalendarClass.FIRST_TEXT, first_text);
            values.put(CalendarClass.FIRST_TIME, first_time);

            // Download "second" data
            if(!second_image.equals("null")) {
                Bitmap img = Downloader.getImage("http://iswib.org/" + second_image, this);
                second_image = second_image.substring(second_image.lastIndexOf("/") + 1);
                FileOutputStream out;
                try {
                    out = context.openFileOutput(CalendarClass.PREFIX + second_image, Context.MODE_PRIVATE);
                    if (img != null) {
                        img.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    }
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                values.put(CalendarClass.SECOND_IMAGE, CalendarClass.PREFIX + second_image);
            } else {
                values.put(CalendarClass.SECOND_IMAGE, "null");
            }
            values.put(CalendarClass.SECOND_TITLE, second_title);
            values.put(CalendarClass.SECOND_TEXT, second_text);
            values.put(CalendarClass.SECOND_TIME, second_time);

            // Download "third" data
            if(!third_image.equals("null")) {
                Bitmap img = Downloader.getImage("http://iswib.org/" + third_image, this);
                third_image = third_image.substring(third_image.lastIndexOf("/") + 1);
                FileOutputStream out;
                try {
                    out = context.openFileOutput(CalendarClass.PREFIX + third_image, Context.MODE_PRIVATE);
                    if (img != null) {
                        img.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    }
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                values.put(CalendarClass.THIRD_IMAGE, CalendarClass.PREFIX + third_image);
            } else {
                values.put(CalendarClass.THIRD_IMAGE, "null");
            }
            values.put(CalendarClass.THIRD_TITLE, third_title);
            values.put(CalendarClass.THIRD_TEXT, third_text);
            values.put(CalendarClass.THIRD_TIME, third_time);

            // Try to delete the row if present
            db.delete(
                    CalendarClass.TABLE_NAME,
                    "id=" + id,
                    null);

            // Insert the new row, returning the primary key value of the new row
            db.insert(
                    CalendarClass.TABLE_NAME,
                    null,
                    values);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}