package org.iswib.iswibexplorer.calendar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.iswib.iswibexplorer.dialogs.DialogDays;
import org.iswib.iswibexplorer.MainActivity;
import org.iswib.iswibexplorer.R;
import org.iswib.iswibexplorer.database.CalendarClass;
import org.iswib.iswibexplorer.database.DatabaseHelper;
import org.iswib.iswibexplorer.map.MapsActivity;
import org.iswib.iswibexplorer.news.NewsActivity;
import org.iswib.iswibexplorer.settings.SettingsActivity;
import org.iswib.iswibexplorer.web.Downloader;
import org.iswib.iswibexplorer.workshops.WorkshopsActivity;

import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The CalendarActivity displays days of the festival and corresponding schedule
 * one at a time, and lets you pick different days
 *
 * @author Jovan
 * @version 1.1
 */
public class CalendarActivity extends AppCompatActivity {

    // make a static instance of activity that can be passed to async tasks
    public static CalendarActivity activity;

    // Tag
    public static String CALENDAR_DAY = "id";

    // Dialog for day picking
    private DialogDays dialog = new DialogDays();

    public TextView message_empty;    // This will display database empty message
    private Integer id = null;          // This will tell what day is selected

    // Getter for the activity instance
    public static Activity getActivity() {
        return activity;
    }

    // constructor
    public CalendarActivity() {
        activity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

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

        // Check if the update has started
        if(MainActivity.updating) {
            // Check if the update is finished
            if (MainActivity.calendarFlag) {
                // If finished
                if (calendar_update != null) {
                    // Hide the calendar update info
                    calendar_update.setVisibility(View.GONE);
                }
                // Load the data from the local database
                loadCalendar(id);
            } else {
                // Check every x seconds if update is finished and load the calendar using handler
                final Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (MainActivity.calendarFlag) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    message_empty.setVisibility(TextView.GONE);
                                    if (calendar_update != null) {
                                        calendar_update.setVisibility(View.GONE);
                                    }
                                    loadCalendar(id);
                                }
                            });
                            timer.cancel();
                        }
                    }
                }, 0, Downloader.TIMEOUT);
            }
        } else {
            // if update was not started at all
            if (calendar_update != null) {
                // Hide the calendar update info
                calendar_update.setVisibility(View.GONE);
            }
            // Load the data from the local database
            loadCalendar(id);
        }
    }

    /**
     * This method will update the calendar activity with data from database
     *
     */
    public void loadCalendar(Integer id) {
        // get the database instance
        DatabaseHelper daHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = daHelper.getReadableDatabase();

        // Select what columns to return
        String[] tableColumns = {
                CalendarClass.ID,
                CalendarClass.VERSION,
                CalendarClass.DATE,
                CalendarClass.FIRST_TITLE,
                CalendarClass.FIRST_TEXT,
                CalendarClass.FIRST_IMAGE,
                CalendarClass.FIRST_TIME,
                CalendarClass.SECOND_TITLE,
                CalendarClass.SECOND_TEXT,
                CalendarClass.SECOND_IMAGE,
                CalendarClass.SECOND_TIME,
                CalendarClass.THIRD_TITLE,
                CalendarClass.THIRD_TEXT,
                CalendarClass.THIRD_IMAGE,
                CalendarClass.THIRD_TIME,
                CalendarClass.BREAKFAST,
                CalendarClass.LUNCH,
                CalendarClass.DINNER,
                CalendarClass.WORKSHOPS
        };

        // sorting order
        String sortOrder = CalendarClass.ID + " ASC";

        Cursor cursor = db.query(
                CalendarClass.TABLE_NAME,       // table
                tableColumns,                   // columns
                CalendarClass.ID,               // selection
                null,                           // selection arguments
                null,                           // group by
                null,                           // having
                sortOrder                       // order by
        );

        // get the total number of rows returned
        int rows = cursor.getCount();

        // get the initial view
        final LinearLayout calendar_item_container = (LinearLayout) findViewById(R.id.calendar_item_container);

        if(rows == 0) {
            // Database is empty, show message
            message_empty.setText(R.string.calendar_database_empty);
            message_empty.setTextSize(20);
            message_empty.setGravity(Gravity.CENTER);
            message_empty.setPadding(0, 40, 0, 0);
            if (calendar_item_container != null) {
                calendar_item_container.addView(message_empty);
            }

        } else {
            // Get the id if not provided
            if(id == null) {
                // Go to first day to get the date
                cursor.moveToNext();

                // Get the id of calendar that is closest to today
                TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

                // Time now
                Date now = Calendar.getInstance().getTime();

                // First day of the festival
                String date_string = cursor.getString(cursor.getColumnIndex(CalendarClass.DATE));

                // Set the format that will be expected
                SimpleDateFormat date_format = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);

                // Parse the date
                Date first_day = null;
                try {
                    first_day = date_format.parse(date_string);
                    //now = date_format.parse("28-JUL-2016");//TODO
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // If first_day was parsed correctly
                if (first_day != null) {

                    // Get the date position ranging from -1 to total amount of festival days
                    int position = getDayPosition(now, first_day);

                    // Check boundaries
                    if (position > -1 && position <= CalendarClass.DAYS) {
                        // Position is valid
                        id = position;
                    } else {
                        id = 0;
                    }
                } else {
                    // Error parsing, so load a first day
                    id = 0;
                }
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
                calendar_header.setVisibility(View.VISIBLE);
            }

            // Go to zero-based position and read the calendar
            cursor.moveToPosition(id);

            // Mark the selected day
            this.id = id;

            // Show date
            TextView calendar_date = (TextView) findViewById(R.id.calendar_date);
            String date = cursor.getString(cursor.getColumnIndex(CalendarClass.DATE));
            if (calendar_date != null) {
                // Append a date and arrow down sign that users know it can be clicked
                String str = "\u25be " + date.substring(0, 6);
                calendar_date.setText(str);
            }

            // Read schedule from database
            String breakfast = cursor.getString(cursor.getColumnIndex(CalendarClass.BREAKFAST));
            String lunch = cursor.getString(cursor.getColumnIndex(CalendarClass.LUNCH));
            String dinner = cursor.getString(cursor.getColumnIndex(CalendarClass.DINNER));
            String workshops = cursor.getString(cursor.getColumnIndex(CalendarClass.WORKSHOPS));

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

                if(i == 3) {
                    title = cursor.getString(cursor.getColumnIndex(CalendarClass.THIRD_TITLE));
                    text = cursor.getString(cursor.getColumnIndex(CalendarClass.THIRD_TEXT));
                    image = cursor.getString(cursor.getColumnIndex(CalendarClass.THIRD_IMAGE));
                    time = cursor.getString(cursor.getColumnIndex(CalendarClass.THIRD_TIME));
                } else if(i == 2) {
                    title = cursor.getString(cursor.getColumnIndex(CalendarClass.SECOND_TITLE));
                    text = cursor.getString(cursor.getColumnIndex(CalendarClass.SECOND_TEXT));
                    image = cursor.getString(cursor.getColumnIndex(CalendarClass.SECOND_IMAGE));
                    time = cursor.getString(cursor.getColumnIndex(CalendarClass.SECOND_TIME));
                } else {
                    title = cursor.getString(cursor.getColumnIndex(CalendarClass.FIRST_TITLE));
                    text = cursor.getString(cursor.getColumnIndex(CalendarClass.FIRST_TEXT));
                    image = cursor.getString(cursor.getColumnIndex(CalendarClass.FIRST_IMAGE));
                    time = cursor.getString(cursor.getColumnIndex(CalendarClass.FIRST_TIME));
                }

                if (!image.equals("null")) {

                    // add calendar item
                    final View calendar_item = getLayoutInflater().inflate(R.layout.calendar_item, calendar_item_container, false);

                    // load the image
                    ImageView item_image = (ImageView) calendar_item.findViewById(R.id.calendar_item_image);
                    Bitmap bitmap = null;
                    try {
                        // Load the file
                        FileInputStream stream = this.openFileInput(image);

                        // Set the lower quality of images for better performance
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.RGB_565;

                        // Load the image with options for lower quality
                        bitmap = BitmapFactory.decodeStream(stream, null, options);

                        // Close the stream
                        stream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    item_image.setImageBitmap(bitmap);

                    // load the title
                    TextView item_title_left = (TextView) calendar_item.findViewById(R.id.calendar_item_title_left);
                    if (title.contains(CalendarClass.SEPARATOR)) {
                        String titles[] = title.split(CalendarClass.SEPARATOR);
                        TextView item_title_right = (TextView) calendar_item.findViewById(R.id.calendar_item_title_right);
                        item_title_left.setTextSize(18);
                        item_title_right.setTextSize(18);
                        item_title_left.setText(titles[0]);
                        item_title_right.setText(titles[1]);
                    } else {
                        item_title_left.setTextSize(25);
                        item_title_left.setText(title);
                    }

                    // load the date
                    TextView item_date_left = (TextView) calendar_item.findViewById(R.id.calendar_item_date_left);
                    if (time.contains(CalendarClass.SEPARATOR)) {
                        String dates[] = time.split(CalendarClass.SEPARATOR);
                        TextView item_date_right = (TextView) calendar_item.findViewById(R.id.calendar_item_date_right);
                        item_date_left.setText(dates[0]);
                        item_date_right.setText(dates[1]);
                    } else {
                        item_date_left.setText(time);
                    }

                    // load the text
                    TextView item_text = (TextView) calendar_item.findViewById(R.id.calendar_item_text);
                    item_text.setTypeface(Typeface.createFromAsset(getAssets(), "roboto.ttf"));
                    text = text.replaceAll("<li>", "<br>&#149;&nbsp;");
                    item_text.setLinkTextColor(Color.BLUE);
                    item_text.setText(Html.fromHtml(text));

                    // attach completed view to container
                    if (calendar_item_container != null) {
                        calendar_item_container.addView(calendar_item);
                    }
                }
            }

            // Get and show the schedule
            LinearLayout calendar_schedule = (LinearLayout) findViewById(R.id.calendar_schedule);
            if (calendar_schedule != null) {
                calendar_schedule.setVisibility(View.VISIBLE);
            }
        }

        // release the resources
        cursor.close();
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
            loadCalendar(null);
        } else if(pick != id) {
            clearCalendar();
            loadCalendar(pick);
        }

        // Close the dialog
        dialog.dismiss();
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
