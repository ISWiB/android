package org.iswib.iswibexplorer.workshops;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.iswib.iswibexplorer.MainActivity;
import org.iswib.iswibexplorer.R;
import org.iswib.iswibexplorer.calendar.CalendarActivity;
import org.iswib.iswibexplorer.database.DatabaseHelper;
import org.iswib.iswibexplorer.database.WorkshopsClass;
import org.iswib.iswibexplorer.map.MapsActivity;
import org.iswib.iswibexplorer.news.NewsActivity;
import org.iswib.iswibexplorer.settings.SettingsActivity;
import org.iswib.iswibexplorer.web.Downloader;

import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The WorkshopsActivity displays workshops of the festival and lets
 * you open each of them to read more.
 *
 * @author ISWiB IT&D
 * @version 1.1
 */
public class WorkshopsActivity extends AppCompatActivity {

    // make a static instance of activity that can be passed to async tasks
    public static WorkshopsActivity activity;

    TextView message_empty;    // This will display database empty message

    // Tag
    public static String WORKSHOPS_ID = "id";

    // Getter for the activity instance
    public static Activity getActivity() {
        return activity;
    }

    // constructor
    public WorkshopsActivity() {
        activity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workshops);

        message_empty = new TextView(this);

        // check if menu is present
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // set the back and home buttons
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            // hide the title
            actionBar.setDisplayShowTitleEnabled(false);
        }

        final RelativeLayout workshops_update = (RelativeLayout) findViewById(R.id.workshops_update);

        // Check if the update has started
        if(MainActivity.updating) {
            // Check if the update is finished
            if (MainActivity.workshopsFlag) {
                // If finished
                if (workshops_update != null) {
                    // Hide the workshops update info
                    workshops_update.setVisibility(View.GONE);
                }
                // Load the data from the local database
                loadWorkshops();
            } else {
                // Check every x seconds if update is finished and load the workshops
                final Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (MainActivity.workshopsFlag) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    message_empty.setVisibility(TextView.GONE);
                                    if (workshops_update != null) {
                                        workshops_update.setVisibility(View.GONE);
                                    }
                                    loadWorkshops();
                                }
                            });
                            timer.cancel();
                        }
                    }
                }, 0, Downloader.TIMEOUT);
            }
        } else {
            // if update was not started at all
            if (workshops_update != null) {
                // Hide the workshops update info
                workshops_update.setVisibility(View.GONE);
            }
            // Load the data from the local database
            loadWorkshops();
        }
    }

    /**
     * This method will update the workshops activity with data from database
     *
     */
    public void loadWorkshops() {
        // get the database instance
        DatabaseHelper daHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = daHelper.getReadableDatabase();

        // Select what columns to return
        String[] tableColumns = {
                WorkshopsClass.ID,
                WorkshopsClass.TITLE,
                WorkshopsClass.IMAGE,
        };

        // sorting order
        String sortOrder = WorkshopsClass.ID + " ASC";

        Cursor cursor = db.query(
                WorkshopsClass.TABLE_NAME, // table
                tableColumns,             // columns
                null,                     // selection
                null,                     // selection arguments
                null,                     // group by
                null,                     // having
                sortOrder                 // order by
        );

        // get the total number of rows returned
        int rows = cursor.getCount();

        // get the initial view
        LinearLayout container = (LinearLayout) findViewById(R.id.workshops_container);

        if(rows == 0) {
            // Database is empty, show message
            message_empty.setText(R.string.workshops_empty);
            message_empty.setTextSize(20);
            message_empty.setGravity(Gravity.CENTER);
            message_empty.setPadding(0, 40, 0, 0);
            if (container != null) {
                container.addView(message_empty);
            }

        } else {

            // for each workshop
            while(cursor.moveToNext()) {
                // Read from database
                String id = cursor.getString(cursor.getColumnIndex(WorkshopsClass.ID));
                String title = cursor.getString(cursor.getColumnIndex(WorkshopsClass.TITLE));
                String image = cursor.getString(cursor.getColumnIndex(WorkshopsClass.IMAGE));

                // add workshops item
                final View workshops_item = getLayoutInflater().inflate(R.layout.workshops_item, container, false);

                // Set onClick listener and apply the method call
                workshops_item.setId(Integer.parseInt(id));
                workshops_item.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        openArticle(workshops_item);
                    }
                });

                // load the image
                ImageView item_image = (ImageView)workshops_item.findViewById(R.id.workshops_item_image);
                Bitmap bitmap = null;
                try{
                    // Load the file
                    FileInputStream stream = this.openFileInput(image);

                    // Set the lower quality of images for better performance
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;

                    // Load the image with options for lower quality
                    bitmap = BitmapFactory.decodeStream(stream, null, options);

                    // Close the stream
                    stream.close();
                } catch(Exception e){
                    e.printStackTrace();
                }
                item_image.setImageBitmap(bitmap);

                // load the title
                TextView item_title = (TextView)workshops_item.findViewById(R.id.workshops_item_title);
                item_title.setText(title);

                // attach completed view to container
                if (container != null) {
                    container.addView(workshops_item);
                }
            }
        }

        // release the resources
        cursor.close();
    }

    // open touched article
    public void openArticle(View view) {
        // get the touched article id
        int id = view.getId();

        // create a new intent
        Intent intent = new Intent(this, WorkshopsArticle.class);

        // pass the id to the intent
        intent.putExtra(WORKSHOPS_ID, id);

        // open the article
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.getItem(2);
        item.setIcon(R.drawable.workshops_active);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar (main menu) item clicks. Get the clicked id
        int id = item.getItemId();

        // Open requested page
        if (id == R.id.menu_news) {
            Intent intent = new Intent(this, NewsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_calendar) {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_workshops) {
            return true;
        } else if (id == R.id.menu_maps) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
