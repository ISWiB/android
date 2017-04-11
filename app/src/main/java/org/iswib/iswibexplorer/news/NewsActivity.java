package org.iswib.iswibexplorer.news;

import android.app.Activity;
import android.content.Context;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    /**
     * Amount of news that will be loaded in news activity when refresh is clicked.
     */
    private static final int loadNewsAmount = 5;

    private int lastIndexOfLoadedNews = 0;

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

        loadMoreNoView();
    }

//      Uzima index od poslednje vesti koja je ucitana
//      Ucitava sledecih x vesti, na primer 5.
    public void loadMore(View view) {
        loadMoreNoView();
    }

    public void loadMoreNoView() {

        for(int i = lastIndexOfLoadedNews; i < loadNewsAmount; i++) {
            loadNews(i);
        }

        lastIndexOfLoadedNews += loadNewsAmount;
    }

//    Uzima konkretan ID i load-uje image i load-uje title.
//    Loaduje konkretnu vest da se prikaze u news bar-u.
//    Da se napravi da load-uje samo TITLE, sliku i datum.
    private void loadNews(int id) {

        // get all rows for news with this id
        String result = Downloader.getString("http://iswib.org/api/getNews.php?id=" + id, this);
        String title = "";
        String text = "";
        String image = "";
        String date = "";
        Bitmap img;

        // parse the result and put every value into variable
        try {
            // this will create json array
            JSONArray arr = new JSONArray(result);
            // no need to loop as only one row will be returned
            JSONObject json = arr.getJSONObject(0);
            // get all fields from json object
            title = json.getString("title");
            text = json.getString("text");
            image = json.getString("image");
            date = json.getString("date");

            // this will download the image
            img = Downloader.getImage("http://iswib.org/" + image, this);
            image = image.substring(image.lastIndexOf("/") + 1);
            FileOutputStream out;
            try {
//               PROVERITI DA LI OVO SLJAKA, jer je bio CONTEXT, a ne THIS.
                out = this.openFileOutput(NewsClass.PREFIX + image, Context.MODE_PRIVATE);
                if (img != null) {
                    img.compress(Bitmap.CompressFormat.JPEG, 90, out);
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        // get the total number of rows returned
//        int rows = cursor.getCount();
//        int count = 0; // Count how many news are added

        // get the initial view with button
        LinearLayout container = (LinearLayout) findViewById(R.id.news_container);
        Button button = (Button)findViewById(R.id.news_button);

        if(rows == 0) {
            // **** FINA PODESAVANJA
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

            // for each news OVO JE BITNO
            while(cursor.moveToNext()) {
                // Read from database OVDE KORISTIMO GORE DEFINISANE STVARI
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
