package org.iswib.iswibexplorer.map;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.iswib.iswibexplorer.R;
import org.iswib.iswibexplorer.calendar.CalendarActivity;
import org.iswib.iswibexplorer.dialogs.DialogLocations;
import org.iswib.iswibexplorer.news.NewsActivity;
import org.iswib.iswibexplorer.settings.SettingsActivity;
import org.iswib.iswibexplorer.workshops.WorkshopsActivity;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Dialog for location picking
    private DialogLocations dialog = new DialogLocations();

    // Zoom level
    int zoom = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // check if menu is present
        if (getSupportActionBar() != null) {
            // set the back and home buttons
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // hide the title
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng position;
        Marker marker;

        // Add a marker for Dom Petar Drapšin and move the camera
        position = new LatLng(44.8225824, 20.4588079);
        marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(getResources().getString(R.string.maps_location_dom)));
        marker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));

        // Handle zoom events
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            private float currentZoom = -1;

            @Override
            public void onCameraChange(CameraPosition pos) {
                if (pos.zoom != currentZoom){
                    currentZoom = pos.zoom;
                    zoom = (int) currentZoom;
                }
            }
        });
    }

    public void openLocations(View view) {

        dialog.show(getSupportFragmentManager(), "");
        getSupportFragmentManager().executePendingTransactions();
        // Set the background transparent
        dialog.getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Get display size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // Set the height of the dialog little smaller then the total display size
        dialog.getDialog().getWindow().setLayout(width - 100, height - 400);
    }

    public void focusTo(View view) {

        Marker marker;
        LatLng position;
        String title;

        String location = view.getTag().toString();
        switch (location) {
            case "kafana":
                position = new LatLng(44.824629, 20.457917);
                title = getString(R.string.maps_location_kafana);
                break;
            case "ada":
                position = new LatLng(44.786549, 20.395199);
                title = getString(R.string.maps_location_ada);
                break;
            case "workshops":
                position = new LatLng(44.8042004, 20.4810465);
                title = getString(R.string.maps_location_workshops);
                break;
            case "openingceremony":
                position = new LatLng(44.810799, 20.462531);
                title = getString(R.string.maps_location_openingceremony);
                break;
            case "closingceremony":
                position = new LatLng(44.811378, 20.469856);
                title = getString(R.string.maps_location_closingceremony);
                break;
            case "welcome":
                position = new LatLng(44.817796, 20.4657486);
                title = getString(R.string.maps_location_welcome);
                break;
            case "monparty":
                position = new LatLng(44.807985, 20.444820);
                title = getString(R.string.maps_location_monparty);
                break;
            case "countryfair":
                position = new LatLng(44.82329, 20.4676);
                title = getString(R.string.maps_location_countryfair);
                break;
            case "artnight":
                position = new LatLng(44.8178802, 20.4652923);
                title = getString(R.string.maps_location_artnight);
                break;
            case "flagparade":
                position = new LatLng(44.8119976, 20.4630151);
                title = getString(R.string.maps_location_flagparade);
                break;
            case "speakup":
                position = new LatLng(44.8121435, 20.4625028);
                title = getString(R.string.maps_location_speakup);
                break;
            case "farewell":
                position = new LatLng(44.8057579, 20.475727);
                title = getString(R.string.maps_location_farewell);
                break;
            default:
                // Dom Petar Drapšin
                position = new LatLng(44.8225824, 20.4588079);
                title = getString(R.string.maps_location_dom);
        }

        // Remove previos markers
        mMap.clear();

        // Add a marker for Kafana party
        marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title));
        marker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));

        dialog.dismiss();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.getItem(3);
        item.setIcon(R.drawable.maps_active);
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
            Intent intent = new Intent(this, WorkshopsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_maps) {
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
