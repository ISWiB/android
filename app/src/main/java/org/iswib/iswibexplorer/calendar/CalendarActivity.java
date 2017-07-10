package org.iswib.iswibexplorer.calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.iswib.iswibexplorer.MainActivity;
import org.iswib.iswibexplorer.dialogs.DialogDays;
import org.iswib.iswibexplorer.R;
import org.iswib.iswibexplorer.database.CalendarClass;
import org.iswib.iswibexplorer.map.MapsActivity;
import org.iswib.iswibexplorer.news.NewsActivity;
import org.iswib.iswibexplorer.settings.SettingsActivity;
import org.iswib.iswibexplorer.web.Downloader;
import org.iswib.iswibexplorer.workshops.WorkshopsActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * The CalendarActivity displays days of the festival and corresponding schedule
 * one at a time, and lets you pick different days
 *
 * @author ISWiB IT&D
 * @version 1.1
 */
public class CalendarActivity extends AppCompatActivity {

    // Tag
    public static String CALENDAR_DAY = "id";

    // Dialog for day picking
    private DialogDays dialog = new DialogDays();


    // make a static instance of activity that can be passed to async tasks
    public static CalendarActivity activity;

    // getter for the activity instance singletone
    public static Activity getActivity() { return activity; }

    private ArrayList<Integer> listOfCalendarIds = new ArrayList<>();

    private ArrayList<View> pub_calendar_items = new ArrayList<>();

    public TextView message_empty;    // This will display database empty message
    private Integer id = null;          // This will tell what day is selected

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Log.i("tag", "ovde1");

        // This will display message if database is empty
        message_empty = new TextView(this);

        // If started from notification get day id and load it
        Intent intent = getIntent();
        String calendar_day = intent.getStringExtra(CALENDAR_DAY);
        if(calendar_day != null)
            id = Integer.parseInt(calendar_day) - 1;

        // check if menu is present
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // set the back and home buttons
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            // hide the title
            actionBar.setDisplayShowTitleEnabled(false);
        }

        final RelativeLayout calendar_update = (RelativeLayout) findViewById(R.id.calendar_update);

        // Get the date position ranging from -1 to total amount of festival days TODO
        int position = 9;

        // Update the calendar in background
        if(Downloader.checkPermission(this)) {
            Log.i("tag", "ovde2");
            executeCalendarDownloaderTask(position);
            position++;
            // loadMoreNoView();
//        TODO Fina porukica ako nema konekcije. :/
        }



        // If finished
        if (calendar_update != null) {
            // Hide the calendar update info
            calendar_update.setVisibility(View.GONE);
        }
        // Load the data from the local database
        //loadCalendar(id);


    }

    public void executeCalendarDownloaderTask(int position) {
        //LinearLayout container = (LinearLayout) findViewById(R.id.calendar_container);
        LinearLayout calendar_item_container = (LinearLayout) findViewById(R.id.calendar_item_container);
        WeakReference<LinearLayout> weakReference = new WeakReference<>(calendar_item_container);
        CalendarDownloaderTask task = new CalendarDownloaderTask(weakReference, position);
        task.execute();
    }

    /**
     * This method will clear the calendar activity
     *
     */
    public void clearCalendar() {
        // get the initial view
        LinearLayout calendar_item_container = (LinearLayout) findViewById(R.id.calendar_item_container);
        if (calendar_item_container != null) {
            calendar_item_container.removeAllViews();
        }
    }

    /**
     * This method will show day picker dialog
     *
     */
    public void showDays(View view) {
        dialog.show(getSupportFragmentManager(), "");
        getSupportFragmentManager().executePendingTransactions();
        dialog.getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    /**
     * This method returns the difference in days between to dates
     */
    public static int getDayPosition(Date now_date, Date start_date) {
        Calendar now = getDatePart(now_date);
        Calendar start = getDatePart(start_date);

        // Keep count, as there is no need to count more than festival days
        int counter = 0;

        int days_between = 0;
        if(now.before(start)) {
            // Festival not yet started
            return -1;
        }

        // If festival started
        while (start.before(now)) {
            // Start the count
            start.add(Calendar.DAY_OF_MONTH, 1);
            days_between++;
            // Check if breached
            counter++;
            if (counter > CalendarClass.DAYS)
                break;
        }


        // Return negative
        return days_between;
    }

    /**
     * This method clears all the info of the date, except the days
     */
    public static Calendar getDatePart(Date date){
        Calendar cal = Calendar.getInstance();       // get calendar instance
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        cal.set(Calendar.MINUTE, 0);                 // set minute in hour
        cal.set(Calendar.SECOND, 0);                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second

        return cal;                                  // return the date part
    }

    /**
     * This method loads the calendar with a new day
     */
    public void pickDay(View view) {
        // Get the selected tag
        int pick = Integer.parseInt(view.getTag().toString());

        // If not already on the same day
        if (pick == CalendarClass.DAYS && pick != id) {
            clearCalendar();
            executeCalendarDownloaderTask(0);
        } else if(pick != id) {
            clearCalendar();
            executeCalendarDownloaderTask(pick);
        }

        // Close the dialog
        dialog.dismiss();
    }


    private class CalendarDownloaderTask extends AsyncTask<String, Void, ArrayList<View>> {

        private WeakReference<LinearLayout> weakReference;
        private int position;

        private CalendarDownloaderTask(WeakReference<LinearLayout> weakReference, int position) {
            this.weakReference = weakReference;
            this.position = position;
        }

        @Override
        protected /*synchronized*/ ArrayList<View> doInBackground(String... params) {

            MainActivity.calendarFlag = false;

            // Escape early if cancel() is called
            if(isCancelled()) {
                return null;
            }

            for(int i = 9; i < 18; i++) {
//                listOfCalendarIds.add(i);
                loadCalendarDay(i);
            }
            // call the API script that will return all calendar ids
            //String result = Downloader.getString("http://iswib.org/api/getCalendar.php", this);
            //loadCalendarDay(position);

            //return result;

            return pub_calendar_items;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<View> calendar_items) {
            calendar_items = pub_calendar_items;
            super.onPostExecute(calendar_items);
            MainActivity.calendarFlag = true;
            // Mark that the update is finished
            //MainActivity.calendarFlag = true;
            if (weakReference.get() != null) {
                for(View calendar_item : pub_calendar_items) {
                    // as the last view is a button to add more news, add the news item before the button
                    weakReference.get().addView(calendar_item);
                }
//            TODO Proveriti da li nam je ovo potrebno, ako ne skloniti
//            loaded++;
            }

            // Get and show the schedule
            LinearLayout calendar_schedule = (LinearLayout) findViewById(R.id.calendar_schedule);
            if (calendar_schedule != null) {
                calendar_schedule.setVisibility(View.VISIBLE);
            }

            TextView calendar_header = (TextView) findViewById(R.id.calendar_header);
            calendar_header.setVisibility(View.VISIBLE);

            Log.i("CalendarUpdate", "Finished");
        }

        private ArrayList<View> loadCalendarDay(int id) {

            if(id < 9 || id > 17) {
                Log.i("Calendar Activity", "Loading JSON ID is less than 9 or greater than 17");
                return null;
            }

            // Get all rows for calendar with this id
            String result = Downloader.getString("http://iswib.org/api/getCalendar.php?id=" + id);

            // Parse the result and put every value into variable
            try {
                // this will create json array
                JSONArray arr = new JSONArray(result);
                // no need to loop as only one row will be returned
                JSONObject json = arr.getJSONObject(0);
                // get all fields from json object
                //String version = json.getString(CalendarClass.VERSION); TODO
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

                // Download "first" data
                Bitmap img1 = null;
                if (!first_image.equals("null")) {
                     img1 = Downloader.getImage("http://iswib.org/" + first_image);
                }
                // Download "second" data
                Bitmap img2 = null;
                if (!second_image.equals("null")) {
                    img2 = Downloader.getImage("http://iswib.org/" + second_image);
                }

                // Download "third" data
                Bitmap img3 = null;
                if (!third_image.equals("null")) {
                    img3 = Downloader.getImage("http://iswib.org/" + third_image);
                }

                // Set the day picker
                TextView calendar_header = (TextView) findViewById(R.id.calendar_header);
                if (calendar_header != null) {
                    switch (id) {
                        case 0:
                            calendar_header.setText(R.string.day_1);
                            break;
                        case 1:
                            calendar_header.setText(R.string.day_2);
                            break;
                        case 2:
                            calendar_header.setText(R.string.day_3);
                            break;
                        case 3:
                            calendar_header.setText(R.string.day_4);
                            break;
                        case 4:
                            calendar_header.setText(R.string.day_5);
                            break;
                        case 5:
                            calendar_header.setText(R.string.day_6);
                            break;
                        case 6:
                            calendar_header.setText(R.string.day_7);
                            break;
                        case 7:
                            calendar_header.setText(R.string.day_8);
                            break;
                    }
                }

                // Show date
                TextView calendar_date = (TextView) findViewById(R.id.calendar_date);
                if (calendar_date != null) {
                    // Append a date and arrow down sign that users know it can be clicked
                    String str = "\u25be " + date.substring(0, 6);
                    calendar_date.setText(str);
                }

                // Populate schedule
                //final LinearLayout calendar_item_container = (LinearLayout) findViewById(R.id.calendar_item_container);

                // Find the views to populate data with
                TextView calendar_schedule_left = (TextView) findViewById(R.id.calendar_schedule_left);
                TextView calendar_schedule_right = (TextView) findViewById(R.id.calendar_schedule_right);

                // Add data to views
                if(calendar_schedule_left != null) {
                    // Remove any previous text
                    calendar_schedule_left.setText("");
                    // Add new text
                    if(!breakfast.equals("null"))
                        // Add breakfast
                        calendar_schedule_left.append(getString(R.string.schedule_breakfast) + ": " + breakfast);
                    if(!lunch.equals("null")) {
                        // If breakfast was added add a new line separator as well
                        if(!breakfast.equals("null"))
                            calendar_schedule_left.append("\n");
                        // Add lunch
                        calendar_schedule_left.append(getString(R.string.schedule_lunch) + ": " + lunch);
                    }
                    if(!dinner.equals("null")) {
                        // If lunch was added add a new line separator as well
                        if(!breakfast.equals("null"))
                            calendar_schedule_left.append("\n");
                        // Add dinner
                        calendar_schedule_left.append(getString(R.string.schedule_dinner) + ": " + dinner);
                    }
                }

                if(calendar_schedule_right != null) {
                    // Remove any previous text
                    calendar_schedule_right.setText("");
                    // Hide work icon
                    ImageView calendar_schedule_work = (ImageView) findViewById(R.id.calendar_schedule_work);
                    if (calendar_schedule_work != null) {
                        calendar_schedule_work.setVisibility(View.INVISIBLE);
                    }
                    // Add new test
                    if(!workshops.equals("null")) {
                        // Add workshops
                        calendar_schedule_right.append(getString(R.string.schedule_workshops) + ": " + workshops);
                        if (calendar_schedule_work != null) {
                            // Show work icon
                            calendar_schedule_work.setVisibility(View.VISIBLE);
                        }
                    }
                }

                // Add "first", "second" and "third" data
                for(int i = 1; i <= 3; i++) {
                    String title;
                    String text;
                    String image;
                    String time;
                    Bitmap img;

                    if(i == 3) {
                        title = first_title;
                        text = first_text;
                        image = first_image;
                        time = first_time;
                        img = img1;
                    } else if(i == 2) {
                        title = second_title;
                        text = second_text;
                        image = second_image;
                        time = second_time;
                        img = img2;
                    } else {
                        title = third_title;
                        text = third_text;
                        image = third_image;
                        time = third_time;
                        img = img3;
                    }

                    if (!image.equals("null")) {

                        // add calendar item
                        final View calendar_item = getLayoutInflater().inflate(R.layout.calendar_item, null, false); // TODO

                        // load the image
                        ImageView item_image = (ImageView) calendar_item.findViewById(R.id.calendar_item_image);
                        item_image.setImageBitmap(img);

                        // load the title // TODO
                        TextView item_title_left = (TextView) calendar_item.findViewById(R.id.calendar_item_title_left);
//                        if (title.contains(CalendarClass.SEPARATOR)) {
//                            String titles[] = title.split(CalendarClass.SEPARATOR);
//                            TextView item_title_right = (TextView) calendar_item.findViewById(R.id.calendar_item_title_right);
//                            item_title_left.setTextSize(18);
//                            item_title_right.setTextSize(18);
//                            item_title_left.setText(titles[0]);
//                            item_title_right.setText(titles[1]);
//                        } else {
                            item_title_left.setTextSize(25);
                            item_title_left.setText(title);
                       // }

                        // load the date
                        TextView item_date_left = (TextView) calendar_item.findViewById(R.id.calendar_item_date_left);
//                        if (time.contains(CalendarClass.SEPARATOR)) {
//                            String dates[] = time.split(CalendarClass.SEPARATOR);
//                            TextView item_date_right = (TextView) calendar_item.findViewById(R.id.calendar_item_date_right);
//                            item_date_left.setText(dates[0]);
//                            item_date_right.setText(dates[1]);
//                        } else {
                            item_date_left.setText(time);
                       // }

                        // load the text
                        TextView item_text = (TextView) calendar_item.findViewById(R.id.calendar_item_text);
                        item_text.setTypeface(Typeface.createFromAsset(getAssets(), "roboto.ttf"));
                        text = text.replaceAll("<li>", "<br>&#149;&nbsp;");
                        item_text.setLinkTextColor(Color.BLUE);
                        item_text.setText(Html.fromHtml(text));

                        // attach completed view to container
                        pub_calendar_items.add(calendar_item);
                    }
                }

                return pub_calendar_items;

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.getItem(1);
        item.setIcon(R.drawable.calendar_active);
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
