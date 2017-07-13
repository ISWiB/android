package org.iswib.iswibexplorer.workshops;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import org.iswib.iswibexplorer.database.WorkshopsClass;
import org.iswib.iswibexplorer.map.MapsActivity;
import org.iswib.iswibexplorer.news.NewsActivity;
import org.iswib.iswibexplorer.settings.SettingsActivity;
import org.iswib.iswibexplorer.web.Downloader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * The WorkshopsActivity displays workshops of the festival and lets
 * you open each of them to read more.
 *
 * @author ISWiB IT&D
 * @version 1.1
 */
public class WorkshopsActivity extends AppCompatActivity {

    // constructor
    public WorkshopsActivity() {

    }

    private ArrayList<Integer> listOfWorkshopsIds = new ArrayList<>();

    protected TextView message_empty;    // This will display database empty message

    // Tag
    public static String WORKSHOPS_ID = "id";

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

        if(Downloader.checkPermission(this)) {
            executeWorkshopsDownloaderTask(null);
        }
        else {
            // no connection
            ((TextView)workshops_update.getChildAt(0)).setText(R.string.no_permission);
            workshops_update.getChildAt(1).setVisibility(View.GONE);
        }

    }

    public void executeWorkshopsDownloaderTask(View view) {
        LinearLayout container = (LinearLayout) findViewById(R.id.workshops_container);
        WeakReference<LinearLayout> weakReference = new WeakReference<>(container);
        WorkshopsDownloaderTask task = new WorkshopsDownloaderTask(weakReference);
        task.execute();
    }

    private class WorkshopsDownloaderTask extends AsyncTask<String, Void, ArrayList<View>> {

        private WeakReference<LinearLayout> weakReference;

        private WorkshopsDownloaderTask(WeakReference<LinearLayout> wr) {
            this.weakReference = wr;
        }

        @Override
        protected ArrayList<View> doInBackground(String... params) {
            ArrayList<View> workshops_items;

            if (isCancelled()) {
                return null;
            }

            for(int i = 9; i < 19; i ++) {
                listOfWorkshopsIds.add(i);
            }

            workshops_items = loadWorkshops();

            return workshops_items;

        }

        protected void onPreExecute() {
            findViewById(R.id.workshops_update).setVisibility(View.VISIBLE);
        }

        protected void onPostExecute(ArrayList<View> workshops_items) {
//            TODO spreman view i onda cemo ovde samo da ga ispisemo na nasoj aktivnosti
            findViewById(R.id.workshops_update).setVisibility(View.INVISIBLE);

            //attach completed view to container
            if (weakReference.get() != null) {
                for(View workshops_item : workshops_items) {
                    // as the last view is a button to add more news, add the news item before the button
                    weakReference.get().addView(workshops_item);
                }
            }
        }

        /**
         * This method will update the workshops activity with data from database
         *
         */
        private ArrayList<View> loadWorkshops() {

            ArrayList<View> workshop_items = new ArrayList<>();

            for (int i = 0; i < listOfWorkshopsIds.size(); i++) {
                workshop_items.add(loadWorkshop(listOfWorkshopsIds.get(i)));
            }


            return workshop_items;
        }

        private View loadWorkshop(int id) {

            String result = Downloader.getString("http://iswib.org/api/getWorkshops.php?id=" + id);

            String title = "";
            String image;
            String text = "";
            Bitmap bitmapImage = null;
            byte[] bytes = null;


            try {

                JSONArray array = new JSONArray(result);

                JSONObject json = array.getJSONObject(0);

                title = json.getString("title");
                text = json.getString("text");
                image = json.getString("workshop");

                bitmapImage = Downloader.getImage("http://www.iswib.org/images/workshops/" + image);
                image = image.substring(image.lastIndexOf("/") + 1);

                FileOutputStream out;
                out = WorkshopsActivity.this.openFileOutput(WorkshopsClass.PREFIX + image, Context.MODE_PRIVATE);
                if(bitmapImage != null) {
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                }

                out.close();

                //Ovo sam ja ubacivao dodatno zbog WorkshopsArticle i NewsArticle, radi prebacivanja slike
                //U drugu klasu... Mada nije urodilo plodom
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, stream);

                bytes = stream.toByteArray();
                stream.close();


            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            final View workshops_item = getLayoutInflater().inflate(R.layout.workshops_item, weakReference.get(), false);
            workshops_item.setId(id);

            // load the image
            ImageView item_image = (ImageView) workshops_item.findViewById(R.id.workshops_item_image);
//          Scale image to be smaller so it could fit into 4096x4096 if it doesn't.
            int nh = (int) (bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);
            item_image.setImageBitmap(scaled);

            // load the title
            TextView item_title = (TextView) workshops_item.findViewById(R.id.workshops_item_title);
            item_title.setText(title);
            item_title.setTypeface(Typeface.createFromAsset(getAssets(), "roboto.ttf"));

            final String ftitle = title;
            final String ftext = text;
            final byte[] fbytes = bytes;

            workshops_item.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    openArticle(workshops_item, ftitle, ftext, fbytes);
                }
            });

            return workshops_item;
        }

    }


    // open touched article
    public void openArticle(View workshops_item, String title, String text, byte[] bytes) {

        int id = workshops_item.getId();

        // create a new intent
        Intent intent = new Intent(this, WorkshopsArticle.class);

        // pass the id to the intent
        intent.putExtra(WORKSHOPS_ID, id);
        intent.putExtra(WorkshopsClass.TITLE, title);
        intent.putExtra(WorkshopsClass.TEXT, text);
        intent.putExtra("Bytes", bytes);

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
