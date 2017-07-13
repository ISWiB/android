package org.iswib.iswibexplorer.news;

import android.content.Intent;
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

/**
 * The NewsArticle represents the single news item which displays all
 * the relevant info for that news
 *
 * @author ISWiB IT&D
 * @version 1.1
 */
public class NewsArticle extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_news_article);

        // get the passed id from intent
        Intent intent = getIntent(); // gets the previously created intent

        String title = intent.getStringExtra(NewsClass.TITLE);
        String text = intent.getStringExtra(NewsClass.TEXT);
        String date = intent.getStringExtra(NewsClass.DATE);
        byte[] bytes = intent.getByteArrayExtra("Bytes");

        // pass values to views
        // load the image
        ImageView article_image = (ImageView)findViewById(R.id.news_article_image);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        if (article_image != null) {
            article_image.setImageBitmap(bitmapImage);
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
        if (article_text != null) {
            article_text.setTypeface(Typeface.createFromAsset(getAssets(), "roboto.ttf"));
            text = text.replaceAll("<li>", "<br>&#149;&nbsp;");
            article_text.setLinkTextColor(Color.BLUE);
            article_text.setText(Html.escapeHtml(text));
        }


    }

    @Override
    public boolean onSupportNavigateUp () {
        onBackPressed();
        return true;
    }
}
