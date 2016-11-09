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
import org.iswib.iswibexplorer.database.NewsClass;
import org.iswib.iswibexplorer.news.NewsActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * The NewsUpdater downloads the news from the internet and stores them to
 * local android database only if the news are not already present
 *
 * @author Jovan
 * @version 1.1
 */
public class NewsUpdater extends AsyncTask<String, Void, String> {

    private ArrayList<String> idRemote = new ArrayList<>(); // holds the news id fetched from the web
    private ArrayList<String> idLocal = new ArrayList<>(); // holds the news id from android local database
    private Context context;

    private int index; // news will be downloaded starting from this index (1 means latest news with highest id)

    /**
     * Public constructor that will receive application context and load index
     *
     * @param index index that the update will start from, 1 meaning the latest news
     * @param context context of the activity
     */
    public NewsUpdater(int index, Context context){
        this.context= context;
        this.index = index;
    }


    @Override
    protected synchronized String doInBackground(String... params) {

        // Set the flag to false
        MainActivity.newsFlag = false;
        /*
         * get the list of news id from internet
         */

        // call the API script that will return all news ids
        String result = Downloader.getString("http://iswib.org/getNews.php", this);

        if(result != null) {
            // parse the ids and put them in a list
            try {
                // this will create json array: [{"key":"value","key2:"value2"},{"key":"value"}]
                JSONArray arr = new JSONArray(result);
                ArrayList<String> temp_array = new ArrayList<>(); // temporary holds the news ids fetched from the web
                // for every item in an array
                for (int i = 0; i < arr.length(); i++) {
                    // create a json object like: {"key":"value"}
                    JSONObject json = arr.getJSONObject(i);
                    // get the id from an json object
                    String id = json.getString(NewsClass.ID);
                    // add it to the list
                    temp_array.add(id);
                }
                // Take only ids that were requested
                int size = temp_array.size();
                int count = 0;
                // Starting from the end of the array
                for(int i = size - index; i >= 0; i--) {
                    // Get the id from the array
                    idRemote.add(temp_array.get(i));
                    count++;
                    if(count == NewsActivity.load_total)
                        break;
                }

                Log.i("ID", idRemote.toString());

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
                    NewsClass.ID
            };

            // sorting order
            String sortOrder = NewsClass.ID + " ASC";

            Cursor cursor = db.query(
                    NewsClass.TABLE_NAME,               // table
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

                // database empty, insert all news into local database
                for(String id : idRemote) {
                    addNews(id, db);
                }

            } else {

                // get ids from local database
                while (cursor.moveToNext()) {
                    idLocal.add(cursor.getString(cursor.getColumnIndex(NewsClass.ID)));
                }

                // Compare web to local ids and update if needed
                int array_index = 0;

                // If there is no web id in local, add it
                for(int i = array_index; i < idRemote.size(); i++) {
                    if(!idLocal.contains(idRemote.get(array_index))) {
                        addNews(idRemote.get(array_index), db);
                    }
                    array_index++;
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
        MainActivity.newsFlag = true;

        Log.i("NewsUpdate", "Finished");
    }

    private void addNews(String id, SQLiteDatabase db) {
        // get all rows for news with this id
        String result = Downloader.getString("http://iswib.org/getNews.php?id=" + id, this);

        // parse the result and put every value into variable
        try {
            // this will create json array
            JSONArray arr = new JSONArray(result);
            // no need to loop as only one row will be returned
            JSONObject json = arr.getJSONObject(0);
            // get all fields from json object
            String title = json.getString("title");
            String text = json.getString("text");
            String image = json.getString("image");
            String date = json.getString("date");

            // this will download the image
            Bitmap img = Downloader.getImage("http://iswib.org/" + image, this);
            image = image.substring(image.lastIndexOf("/") + 1);
            FileOutputStream out;
            try {
                out = context.openFileOutput(NewsClass.PREFIX + image, Context.MODE_PRIVATE);
                if (img != null) {
                    img.compress(Bitmap.CompressFormat.JPEG, 90, out);
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // add news to local database
            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(NewsClass.ID, id);
            values.put(NewsClass.TITLE, title);
            values.put(NewsClass.TEXT, text);
            values.put(NewsClass.IMAGE, NewsClass.PREFIX + image);
            values.put(NewsClass.DATE, date);
            // Insert the new row, returning the primary key value of the new row
            db.insert(
                NewsClass.TABLE_NAME,
                null,
                values);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}