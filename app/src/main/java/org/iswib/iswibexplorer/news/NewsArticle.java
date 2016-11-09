package org.iswib.iswibexplorer.news;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import org.iswib.iswibexplorer.R;
import org.iswib.iswibexplorer.database.NewsClass;
import org.iswib.iswibexplorer.database.DatabaseHelper;
import org.iswib.iswibexplorer.workshops.WorkshopsActivity;

import java.io.FileInputStream;

/**
 * The NewsArticle represents the single news item which displays all
 * the relevant info for that news
 *
 * @author Jovan
 * @version 1.1
 */
public class NewsArticle extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_article);

        // get the database instance
        DatabaseHelper daHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = daHelper.getReadableDatabase();

        // get the passed id from intent
        Intent intent = getIntent(); // gets the previously created intent
        int id = intent.getIntExtra(WorkshopsActivity.WORKSHOPS_ID, 1);

        // Select what columns to return
        String[] tableColumns = {
                NewsClass.ID,
                NewsClass.TITLE,
                NewsClass.TEXT,
                NewsClass.IMAGE,
                NewsClass.DATE
        };

        Cursor cursor = db.query(
                NewsClass.TABLE_NAME,     // table
                tableColumns,             // columns
                NewsClass.ID + "=" + id,  // selection
                null,                     // selection arguments
                null,                     // group by
                null,                     // having
                null                      // order by
        );

        // move to first and only row
        cursor.moveToFirst();

        // get values
        String title = cursor.getString(cursor.getColumnIndex(NewsClass.TITLE));
        String text = cursor.getString(cursor.getColumnIndex(NewsClass.TEXT));
        String image = cursor.getString(cursor.getColumnIndex(NewsClass.IMAGE));
        String date = cursor.getString(cursor.getColumnIndex(NewsClass.DATE));

        // pass values to views
        // load the image
        ImageView article_image = (ImageView)findViewById(R.id.news_article_image);
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
        if (article_image != null) {
            article_image.setImageBitmap(bitmap);
        }

        // load the date
        TextView article_date = (TextView)findViewById(R.id.news_article_date);
        if (article_date != null) {
            article_date.setText(date);
        }

        // load the title
        TextView article_title = (TextView)findViewById(R.id.news_article_title);
        if (article_title != null) {
            article_title.setText(title);
        }

        // load the text
        TextView article_text = (TextView)findViewById(R.id.news_article_text);
        text = text.replaceAll("<li>", "<br>&#149;&nbsp;");
        if (article_text != null) {
            article_text.setTypeface(Typeface.createFromAsset(getAssets(), "roboto.ttf"));
            article_text.setLinkTextColor(Color.BLUE);
            article_text.setText(Html.fromHtml(text));
        }

        cursor.close();
    }

    @Override
    public boolean onSupportNavigateUp () {
        onBackPressed();
        return true;
    }
}
