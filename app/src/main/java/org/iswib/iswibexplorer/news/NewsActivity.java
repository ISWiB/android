package org.iswib.iswibexplorer.news;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.iswib.iswibexplorer.MainActivity;
import org.iswib.iswibexplorer.R;
import org.iswib.iswibexplorer.calendar.CalendarActivity;
import org.iswib.iswibexplorer.database.NewsClass;
import org.iswib.iswibexplorer.database.DatabaseHelper;
import org.iswib.iswibexplorer.map.MapsActivity;
import org.iswib.iswibexplorer.settings.SettingsActivity;
import org.iswib.iswibexplorer.web.Downloader;
import org.iswib.iswibexplorer.web.NewsUpdater;
import org.iswib.iswibexplorer.workshops.WorkshopsActivity;

import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The NewsActivity will display all the news from
 * the local android database. Each news article
 * can be accessed by tapping on it.
 *
 * @author Jovan
 * @version 1.1
 */
public class NewsActivity extends AppCompatActivity {

    public static String NEWS_ID = "id";

    // Fields for managing news loading
    public static int load_total = 5;   // How many news articles to display
    private String last = null;         // Store the last loaded news id
    private int loaded = 0;

    // make a static instance of activity that can be passed to async tasks
    public static NewsActivity activity;

    // getter for the activity instance
    public static Activity getActivity() {
        return activity;
    }

    // constructor
    public NewsActivity() {
        activity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        // check if menu is present
        if (getSupportActionBar() != null) {
            // set the back and home buttons
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // hide the title
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Button will be shown after the update is complete
        final Button news_button = (Button) findViewById(R.id.news_button);
        final RelativeLayout news_update = (RelativeLayout) findViewById(R.id.news_update);

        // Check if the update has started
        if(MainActivity.updating) {
            // Check if the update is finished
            if(MainActivity.newsFlag) {
                if (news_update != null) {
                    // Hide the news update info
                    news_update.setVisibility(View.GONE);
                }
                // Load the data from the local database
                loadNews();
                // Button that will be displayed once news are loaded
                if (news_button != null) {
                    news_button.setVisibility(Button.VISIBLE);
                }
            } else {
                // Check every x seconds if update is finished and load the news
                final Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask(){
                    private int loop = 0;
                    @Override
                    public void run(){
                        if(MainActivity.newsFlag) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (news_button != null) {
                                        news_button.setVisibility(Button.VISIBLE);
                                    }
                                    if (news_update != null) {
                                        news_update.setVisibility(View.GONE);
                                    }
                                    loadNews();
                                }
                            });
                            timer.cancel();
                        } else {
                            if(loop >= Downloader.LOOP) {
                                timer.cancel();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LinearLayout container = (LinearLayout) findViewById(R.id.news_container);
                                        TextView message = new TextView(activity);
                                        message.setText(R.string.download_error);
                                        message.setTextSize(20);
                                        message.setGravity(Gravity.CENTER);
                                        message.setPadding(0, 40, 0, 0);
                                        if (container != null) {
                                            container.addView(message, 0);
                                        }
                                        if (news_update != null) {
                                            news_update.setVisibility(View.GONE);
                                        }
                                    }
                                });
                            }
                            loop++;
                        }
                    }
                },0, Downloader.TIMEOUT);
            }
        } else {
            // if update was not started at all
            if (news_update != null) {
                // Hide the news update info
                news_update.setVisibility(View.GONE);
            }
            // Load the data from the local database
            if (news_button != null) {
                news_button.setVisibility(Button.VISIBLE);
            }
            loadNews();
        }
    }

    public void loadMore(View view) {
        // Set the flag to false
        MainActivity.newsFlag = false;

        // Hide the button and show the update items
        final Button news_button = (Button) findViewById(R.id.news_button);
        final RelativeLayout news_update = (RelativeLayout) findViewById(R.id.news_update);
        if (news_button != null) {
            news_button.setVisibility(Button.INVISIBLE);
        }
        if (news_update != null) {
            news_update.setVisibility(View.VISIBLE);
        }

        // Start download
        if(Downloader.checkPermission(this)) {
            // Update the news in background
            NewsUpdater updaterN = new NewsUpdater(loaded + 1, this);
            updaterN.execute();
            // Check every x seconds if update is finished and load the news
            final Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask(){
                @Override
                public void run(){
                    if(MainActivity.newsFlag) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (news_button != null) {
                                    news_button.setVisibility(Button.VISIBLE);
                                }
                                if (news_update != null) {
                                    news_update.setVisibility(View.GONE);
                                }
                                loadNews();
                            }
                        });
                        timer.cancel();
                    }
                }
            },0, Downloader.TIMEOUT);
        } else {
            if (news_button != null) {
                news_button.setVisibility(Button.VISIBLE);
            }
            if (news_update != null) {
                news_update.setVisibility(View.GONE);
            }
            loadNews();
        }
    }

    private void loadNews() {
        // get the database instance
        DatabaseHelper daHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = daHelper.getReadableDatabase();

        // Select what columns to return
        String[] tableColumns = {
                NewsClass.ID,
                NewsClass.TITLE,
                NewsClass.TEXT,
                NewsClass.IMAGE,
                NewsClass.DATE
        };

        // sorting order
        String sortOrder = NewsClass.ID + " DESC";

        String selection;
        if(last != null) {
            selection = NewsClass.ID + "<" + last;
        } else {
            selection = null;
        }

        Cursor cursor = db.query(
                NewsClass.TABLE_NAME,     // table
                tableColumns,             // columns
                selection,                // selection
                null,                     // selection arguments
                null,                     // group by
                null,                     // having
                sortOrder                 // order by
        );

        // get the total number of rows returned
        int rows = cursor.getCount();
        int count = 0; // Count how many news are added

        // get the initial view with button
        LinearLayout container = (LinearLayout) findViewById(R.id.news_container);
        Button button = (Button)findViewById(R.id.news_button);

        if(rows == 0) {
            // Database is empty
            if (last == null) {
                // Database is empty, show message
                TextView message = new TextView(this);
                message.setText(R.string.news_empty);
                message.setTextSize(20);
                message.setGravity(Gravity.CENTER);
                message.setPadding(0, 40, 0, 0);
                if (button != null) {
                    button.setVisibility(View.GONE);
                }
                if (container != null) {
                    container.addView(message, 0);
                }
            } else {
                // show no more news text on button
                if (button != null) {
                    button.setText(R.string.news_no_more);
                    button.setEnabled(false);
                }
            }
        } else {

            // if less then load_total news are loaded that means there are no more news
            if(rows < load_total) {
                // show no more news text on button
                if (button != null) {
                    button.setText(R.string.news_no_more);
                    button.setEnabled(false);
                }
            }

            // for each news
            while(cursor.moveToNext()) {
                // Read from database
                String id = cursor.getString(cursor.getColumnIndex(NewsClass.ID));
                String title = cursor.getString(cursor.getColumnIndex(NewsClass.TITLE));
                String text = cursor.getString(cursor.getColumnIndex(NewsClass.TEXT));
                String image = cursor.getString(cursor.getColumnIndex(NewsClass.IMAGE));
                String date = cursor.getString(cursor.getColumnIndex(NewsClass.DATE));

                // add news item
                final View news_item = getLayoutInflater().inflate(R.layout.news_item, container, false);
                news_item.setId(Integer.parseInt(id));
                news_item.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        openArticle(news_item);
                    }
                });

                // load the image
                ImageView item_image = (ImageView)news_item.findViewById(R.id.news_item_image);
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
                TextView item_title = (TextView)news_item.findViewById(R.id.news_item_title);
                SpannableString span =  new SpannableString(title);
                span.setSpan(new AbsoluteSizeSpan(35), 0, title.length(), 0); // set size
                span.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0);
                item_title.setText(span);

                // load the date
                TextView item_date = (TextView)news_item.findViewById(R.id.news_item_date);
                item_date.setText(date);

                // load the text
                text = text.replaceAll("\n","").substring(0, 140);
                text = text.substring(0, text.lastIndexOf(" ")) + "...";
                text = text.replaceAll("<li>", "<br>&#149;&nbsp;");
                item_title.setTypeface(Typeface.createFromAsset(getAssets(), "roboto.ttf"));
                item_title.append(Html.fromHtml("<br>" + text));

                // attach completed view to container
                if (container != null) {
                    // as the last view is a button to add more news, add the news item before the button
                    container.addView(news_item);
                    count++;
                    loaded++;
                    last = id;
                }

                if(count >= load_total)
                    break;
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
        Intent intent = new Intent(this, NewsArticle.class);

        // pass the id to the intent
        intent.putExtra(NEWS_ID, id);

        // open the article
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.getItem(0);
        item.setIcon(R.drawable.news_active);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar (main menu) item clicks. Get the clicked id
        int id = item.getItemId();

        // Open requested page
        if (id == R.id.menu_news) {
            return true;
        } else if (id == R.id.menu_calendar) {
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_workshops) {
            Intent intent = new Intent(this, WorkshopsActivity.class);
            startActivity(intent);
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
