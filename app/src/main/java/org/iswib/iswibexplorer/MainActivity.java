package org.iswib.iswibexplorer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;

import org.iswib.iswibexplorer.calendar.CalendarActivity;
import org.iswib.iswibexplorer.map.MapsActivity;
import org.iswib.iswibexplorer.news.NewsActivity;
import org.iswib.iswibexplorer.news.NewsArticle;
import org.iswib.iswibexplorer.notifications.NotificationValues;
import org.iswib.iswibexplorer.workshops.WorkshopsActivity;
import org.iswib.iswibexplorer.workshops.WorkshopsArticle;

/**
 * The MainActivity will display the homepage to user with
 * some basic info from where he can navigate to other pages
 *
 * @author ISWiB IT&D
 * @version 1.1
 */
public class MainActivity extends AppCompatActivity {

    // make a static instance of activity and updaters that can be passed to async tasks
    public static MainActivity activity;

    // constructor
    public MainActivity() {
        activity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        // Set the default values of settings, this will be only called once
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        //PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);
        //PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);


        // Handle notifications
        final Intent intent = getIntent();
        String notification_type = intent.getStringExtra(NotificationValues.NOTIFICATION_TYPE);

        if (notification_type != null) {
            // Determine which notification is received
            if (notification_type.equals(NotificationValues.GENERAL_NOTIFICATION)) {
                Log.i("MainActivity", "Started from general notification");
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle(intent.getStringExtra("title"));
                alertDialog.setMessage(intent.getStringExtra("message"));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            } else if(notification_type.equals(NotificationValues.NEWS_NOTIFICATION)) {
                // Handle news notification
                Log.i("MainActivity", "Started from news notification");

                // create a new intent
                Intent new_intent = new Intent(activity, NewsArticle.class);
                // Get id from intent
                String news_id = intent.getStringExtra(NewsActivity.NEWS_ID);
                // pass the id to the intent
                new_intent.putExtra(NewsActivity.NEWS_ID, Integer.parseInt(news_id));
                // open the article
                startActivity(new_intent);

            } else if(notification_type.equals(NotificationValues.CALENDAR_NOTIFICATION)) {
                // Handle calendar notification
                Log.i("MainActivity", "Started from calendar notification");

                // create a new intent
                Intent new_intent = new Intent(activity, CalendarActivity.class);
                // Get id from intent
                String calendar_day = intent.getStringExtra(CalendarActivity.CALENDAR_DAY);
                // pass the id to the intent
                new_intent.putExtra(CalendarActivity.CALENDAR_DAY, calendar_day);
                // open the article
                startActivity(new_intent);

            } else if(notification_type.equals(NotificationValues.WORKSHOPS_NOTIFICATION)) {
                // Handle workshops notification
                Log.i("MainActivity", "Started from workshops notification");

                // create a new intent
                Intent new_intent = new Intent(activity, WorkshopsArticle.class);
                // Get id from intent
                String workshops_id = intent.getStringExtra(WorkshopsActivity.WORKSHOPS_ID);
                // pass the id to the intent
                new_intent.putExtra(WorkshopsActivity.WORKSHOPS_ID, Integer.parseInt(workshops_id));
                // open the article
                startActivity(new_intent);
            }
        }
    }

    public void goTo(View view) {

        Intent intent;
        int tag = Integer.valueOf(view.getTag().toString());

        switch (tag) {
            case 1:
                intent = new Intent(this, NewsActivity.class);
                startActivity(intent);
                break;
            case 2:
                intent = new Intent(this, CalendarActivity.class);
                startActivity(intent);
                break;
            case 3:
                intent = new Intent(this, WorkshopsActivity.class);
                startActivity(intent);
                break;
            case 4:
                intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                break;
        }
    }
}
