package org.iswib.iswibexplorer.web;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import org.iswib.iswibexplorer.MainActivity;
import org.iswib.iswibexplorer.database.DatabaseHelper;
import org.iswib.iswibexplorer.database.WorkshopsClass;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * The WorkshopsUpdater updates the workshops from the internet and stores it to
 * the local android database only if version of the workshop is not the same
 *
 * @author Jovan
 * @version 1.1
 */
public class WorkshopsUpdater extends AsyncTask<String, Void, String> {

    private ArrayList<String> idRemote = new ArrayList<>(); // holds the workshop id fetched from the web
    private ArrayList<String> idLocal = new ArrayList<>(); // holds the workshop id from android local database
    private ArrayList<String> versionRemote = new ArrayList<>(); // holds the workshop version from the web
    private ArrayList<String> versionLocal = new ArrayList<>(); // holds the workshop version from android local database
    private Context context;

    // public constructor that will receive application context
    public WorkshopsUpdater(Context context){
        this.context=context;
    }


    @Override
    protected synchronized String doInBackground(String... params) {

        /*
         * get the list of workshop ids from internet
         */

        // call the API script that will return all workshop ids
        String result = Downloader.getString("http://iswib.org/api/getWorkshops.php", this);

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
                    String id = json.getString(WorkshopsClass.ID);
                    String version = json.getString(WorkshopsClass.VERSION);
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
                    WorkshopsClass.ID,
                    WorkshopsClass.VERSION
            };

            // sorting order
            String sortOrder = WorkshopsClass.ID + " ASC";

            Cursor cursor = db.query(
                    WorkshopsClass.TABLE_NAME,           // table
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
                // database empty, create workshops in local database
                for (String id : idRemote) {
                    updateWorkshops(id, db);
                }
            } else {
                // get ids from local database
                while (cursor.moveToNext()) {
                    idLocal.add(cursor.getString(cursor.getColumnIndex(WorkshopsClass.ID)));
                    versionLocal.add(cursor.getString(cursor.getColumnIndex(WorkshopsClass.VERSION)));
                }
                // Compare web to local ids and update if needed
                for (String id : idRemote) {
                    if (idLocal.contains(id)) {
                        // get local version and update if necessary
                        String localVer = versionLocal.get(idLocal.indexOf(id));
                        String remoteVer = versionRemote.get(idRemote.indexOf(id));
                        if (!localVer.equals(remoteVer)) {
                            updateWorkshops(id, db);
                        }
                    } else {
                        // This means there's no workshop with that id, so add one
                        updateWorkshops(id, db);
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
        MainActivity.workshopsFlag = true;

        Log.i("WorkshopsUpdate", "Finished");
    }

    private void updateWorkshops(String id, SQLiteDatabase db) {
        // get all rows for workshop with this id
        String result = Downloader.getString("http://iswib.org/api/getWorkshops.php?id=" + id, this);

        // parse the result and put every value into variable
        try {
            // this will create json array
            JSONArray arr = new JSONArray(result);
            // no need to loop as only one row will be returned
            JSONObject json = arr.getJSONObject(0);
            // get all fields from json object
            String version = json.getString(WorkshopsClass.VERSION);
            String image = json.getString(WorkshopsClass.IMAGE);
            String title = json.getString(WorkshopsClass.TITLE);
            String text = json.getString(WorkshopsClass.TEXT);

            // this will download the image
            Bitmap img = Downloader.getImage("http://iswib.org/" + image, this);
            image = image.substring(image.lastIndexOf("/") + 1);
            FileOutputStream out;
            try {
                out = context.openFileOutput(WorkshopsClass.PREFIX + image, Context.MODE_PRIVATE);
                if (img != null) {
                    img.compress(Bitmap.CompressFormat.JPEG, 90, out);
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // update workshops in the local database
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(WorkshopsClass.ID, id);
            values.put(WorkshopsClass.VERSION, version);
            values.put(WorkshopsClass.IMAGE, WorkshopsClass.PREFIX + image);
            values.put(WorkshopsClass.TITLE, title);
            values.put(WorkshopsClass.TEXT, text);

            // Try to delete the row if present
            db.delete(
                    WorkshopsClass.TABLE_NAME,
                    "id=" + id,
                    null);

            // Insert the new row, returning the primary key value of the new row
            db.insert(
                    WorkshopsClass.TABLE_NAME,
                    null,
                    values);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}