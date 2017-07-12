package org.iswib.iswibexplorer.web;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.iswib.iswibexplorer.settings.SettingsActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * The Downloader sends requests to server and returns the response
 * It also decides if the download should be started at all
 *
 * @author Jovan
 * @version 1.1
 */
public class Downloader {

    // Fields used while updating/refreshing
    public static int TIMEOUT = 2000;  // How many milliseconds to wait between updates
    public static int LOOP = 25;    // How many times to try to update

    /**
     * This method will check configuration to see if download can be started
     *
     * @return true if update is permitted, false otherwise
     */
    public static boolean checkPermission(Context context) {
        // Check the connection and start the update
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        // If connected to the internet
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {

            // Check the connectivity type, WIFI or MOBILE (and if it's roaming)
            String connection_type = activeNetworkInfo.getTypeName();

            boolean roaming = activeNetworkInfo.isRoaming();

            // Check the synchronization type: 0 means WIFI, 1 means MOBILE, 2 means MOBILE with roaming
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            int sync_type = Integer.parseInt(sharedPref.getString(SettingsActivity.SYNC_TYPE, "0"));

            Log.i("mobilni", "r: " + roaming + "; s: " + sync_type);

            if (roaming && sync_type != 2) {
                return false;
            }

            if (connection_type.equals("MOBILE") && sync_type != 1) {
                return false;
            }

            // alles gute
            return true;
        }
        return false;
    }

    /**
     * This method will send a request to server and return the response
     *
     * @param request url that will be executed
     * @param task Instance of the AsyncTask itself
     * @return response that is read from the server
     */
    public static String getString(String request, AsyncTask task) {

        Log.i("tag", "ASDFGH");
        Log.d("tag", "IIASDFGH");
        Log.v("tag", "VVASDFGH");


        // result that will be returned
        StringBuilder result = new StringBuilder();
        // stuff for connection
        URL url;
        HttpURLConnection con;

        try {
            // form the url and open connection
            url = new URL(request);
            con = (HttpURLConnection)url.openConnection();

            // check if the update is canceled
            if(task.isCancelled())
                return null;

            // stuff to read the response
            InputStream in = con.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);

            // read response character by character and write it to result
            int data = reader.read();
            while(data != -1) {
                char current = (char)data;
                result.append(current);
                data = reader.read();
            }

            return result.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This method will send a request to server and return the response
     *
     * @param request url that will be executed
     * @return response that is read from the server
     */
    public static String getString(String request) {

        Log.i("QWERTY", request);
        // result that will be returned
        StringBuilder result = new StringBuilder();
        // stuff for connection
        URL url;
        HttpURLConnection con;

        try {
            // form the url and open connection
            url = new URL(request);
            con = (HttpURLConnection)url.openConnection();

            // stuff to read the response
            InputStream in =
                    con.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);

            // read response character by character and write it to result
            int data = reader.read();
            while(data != -1) {
                char current = (char)data;
                result.append(current);
                data = reader.read();
            }

            return result.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }



    /**
     * This method will download an image from the internet and return it as a result
     *
     * @param request utl to the image
     * @return image that was downloaded
     */
    public static Bitmap getImage(String request, AsyncTask task) {

        // stuff for connection
        URL url;
        HttpURLConnection con;

        try {
            // form the url and open connection
            url = new URL(request);
            con = (HttpURLConnection)url.openConnection();

            // check if the update was canceled
            if(task.isCancelled())
                return null;

            // stuff to read the response
            InputStream in = con.getInputStream();

            // return an image parsed from the response
            return BitmapFactory.decodeStream(in);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This method will download an image from the internet and return it as a result
     *
     * @param request utl to the image
     * @return image that was downloaded
     */
    public static Bitmap getImage(String request) {

        // stuff for connection
        URL url;
        HttpURLConnection con;

        try {
            // form the url and open connection
            url = new URL(request);
            con = (HttpURLConnection)url.openConnection();

            // stuff to read the response
            InputStream in = con.getInputStream();

            // return an image parsed from the response
            return BitmapFactory.decodeStream(in);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }




}
