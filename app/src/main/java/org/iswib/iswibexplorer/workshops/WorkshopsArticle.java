package org.iswib.iswibexplorer.workshops;

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
import org.iswib.iswibexplorer.database.WorkshopsClass;


/**
 * The WorkshopsArticle represents the single item of workshops which displays all
 * the relevant info for that workshop
 *
 * @author ISWiB IT&D
 * @version 1.1
 */
public class WorkshopsArticle extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_workshops_article);

        Intent intent = getIntent();

        String title = intent.getStringExtra(WorkshopsClass.TITLE);
        String text = intent.getStringExtra(WorkshopsClass.TEXT);
        byte[] bytes = intent.getByteArrayExtra("Bytes");



        // pass values to views
        // load the image
        ImageView article_image = (ImageView)findViewById(R.id.workshops_article_image);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        if (article_image != null) {
            article_image.setImageBitmap(bitmapImage);
        }

        // load the title
        TextView article_title = (TextView)findViewById(R.id.workshops_article_title);
        if (article_title != null) {
            article_title.setText(title);
        }

        // load the text
        TextView article_text = (TextView)findViewById(R.id.workshops_article_text);
        if (article_text != null) {
            article_text.setTypeface(Typeface.createFromAsset(getAssets(), "roboto.ttf"));
            text = text.replaceAll("<li>", "<br>&#149;&nbsp;");
            article_text.setLinkTextColor(Color.BLUE);
            article_text.setText(Html.fromHtml(text));
        }

    }

    @Override
    public boolean onSupportNavigateUp () {
        onBackPressed();
        return true;
    }
}
