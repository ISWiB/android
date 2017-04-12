package org.iswib.iswibexplorer.news;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
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

import com.google.android.gms.vision.text.Line;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    private int lastIndexOfLoadedNews = 1;

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
        //final Button news_button = (Button) findViewById(R.id.news_button);
        //final RelativeLayout news_update = (RelativeLayout) findViewById(R.id.news_update);

        // get the initial view with button
        LinearLayout container = (LinearLayout) findViewById(R.id.news_container);
        WeakReference<LinearLayout> weakReference = new WeakReference<>(container);


        if(Downloader.checkPermission(this)) {
            NewsDownloaderTask task = new NewsDownloaderTask(weakReference);
            task.execute();
           // loadMoreNoView();
//        TODO Fina porukica ako nema konekcije. :/
        }
    }

//      Uzima index od poslednje vesti koja je ucitana
//      Ucitava sledecih x vesti, na primer 5.
    public void loadMore(View view) {
        // TODO another asyncTask
        //loadMoreNoView();
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








    private class NewsDownloaderTask extends AsyncTask<String, Void, ArrayList<View>> {

        private WeakReference<LinearLayout> weakReference;

        public NewsDownloaderTask(WeakReference<LinearLayout> weakReference) {
            this.weakReference = weakReference;
        }

        @Override
        protected ArrayList<View> doInBackground(String... urls) {
            ArrayList<View> news_items = new ArrayList<>();
            // Escape early if cancel() is called
            if (isCancelled()) {
                return null;
            }
            news_items = loadMoreNoView();
            //return news_item;
            return news_items;
        }

        protected void onPostExecute(ArrayList<View> news_items) {
//            TODO spreman view i onda cemo ovde samo da ga ispisemo na nasoj aktivnosti
            //View news_item = null;

             //attach completed view to container
            if (weakReference.get() != null) {
                for(View news_item : news_items) {
                    // as the last view is a button to add more news, add the news item before the button
                    weakReference.get().addView(news_item);
                }
//            TODO Proveriti da li nam je ovo potrebno, ako ne skloniti
//            loaded++;
            }
        }

        ArrayList<View> loadMoreNoView() {
            ArrayList<View> news_items = new ArrayList<>();
            for(int i = lastIndexOfLoadedNews; i < loadNewsAmount; i++) {
                news_items.add(loadNews(i));
            }

            lastIndexOfLoadedNews += loadNewsAmount;

            return news_items;
        }

        //    Uzima konkretan ID i load-uje image i load-uje title.
//    Loaduje konkretnu vest da se prikaze u news bar-u.
//    Da se napravi da load-uje samo TITLE, sliku i datum.
        private View loadNews(int id) {

            // get all column for news with this id
            String result = Downloader.getString("http://iswib.org/api/getNews.php?id=" + id);

            String title = "";
            String image;
            String date = "";
            Bitmap bitmapImage = null;

            // parse the result and put every value into variable
            try {
                // this will create json array
                JSONArray arr = new JSONArray(result);
                // no need to loop as only one row will be returned
                JSONObject json = arr.getJSONObject(0);
                // get all fields from json object
                title = json.getString("title");
                image = json.getString("image");
                date = json.getString("date");

                // this will download the image
                bitmapImage = Downloader.getImage("http://iswib.org/" + image);
                image = image.substring(image.lastIndexOf("/") + 1);
                FileOutputStream out;
//TODO               PROVERITI DA LI OVO SLJAKA, jer je bio CONTEXT, a ne THIS.
                out = NewsActivity.this.openFileOutput(NewsClass.PREFIX + image, Context.MODE_PRIVATE);
                if (bitmapImage != null) {
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                }
                out.close();

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            // get the total number of rows returned
//        int rows = cursor.getCount();
//        int count = 0; // Count how many news are added

            // get the initial view with button
           // LinearLayout container = (LinearLayout) weakReference.get().findViewById(R.id.news_container);
            Button button = (Button)findViewById(R.id.news_button);


//        TODO Proveriti u nekom ucitavanju vesti da li je ucitana poslednja vest.
            // if less then load_total news are loaded that means there are no more news
//        if(rows < load_total) {
//            // show no more news text on button
//            if (button != null) {
//                button.setText(R.string.news_no_more);
//                button.setEnabled(false);
//            }
//        }

            // add news item
            final View news_item = getLayoutInflater().inflate(R.layout.news_item, weakReference.get(), false);
            news_item.setId(id);
            news_item.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    openArticle(news_item);
                }
            });

            // load the image
            ImageView item_image = (ImageView) news_item.findViewById(R.id.news_item_image);
            item_image.setImageBitmap(bitmapImage);

            // load the title
            TextView item_title = (TextView) news_item.findViewById(R.id.news_item_title);
            SpannableString span = new SpannableString(title);
            span.setSpan(new AbsoluteSizeSpan(35), 0, title.length(), 0); // set size
            span.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0);
            item_title.setText(span);
            item_title.setTypeface(Typeface.createFromAsset(getAssets(), "roboto.ttf"));

            // load the date
            TextView item_date = (TextView) news_item.findViewById(R.id.news_item_date);
            item_date.setText(date);

            return news_item;

//            // attach completed view to container
//            if (weakReference.get() != null) {
//                // as the last view is a button to add more news, add the news item before the button
//                weakReference.get().addView(news_item);
////            TODO Proveriti da li nam je ovo potrebno, ako ne skloniti
////            loaded++;
//            }

            // return news_item;
        }

    }


}
