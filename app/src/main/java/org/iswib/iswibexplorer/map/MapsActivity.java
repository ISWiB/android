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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Dialog for location picking
    private DialogLocations dialog = new DialogLocations();
    private ArrayList<Marker> markers = new ArrayList<>();

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

        // Add a marker for Dom Petar Drapšin and move the camera
        position = new LatLng(44.8225824, 20.4588079);
        markers.add(mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(getResources().getString(R.string.maps_location_dom))));
        markers.get(0).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));

        // add other locations
        Map<String, LatLng> positions = new HashMap<>();
        positions.put(getResources().getString(R.string.maps_location_kafana), new LatLng(44.824629, 20.457917));
        positions.put(getResources().getString(R.string.maps_location_ada), new LatLng(44.786549, 20.395199));
        positions.put(getResources().getString(R.string.maps_location_workshops), new LatLng(44.8042004, 20.4810465));
        positions.put(getResources().getString(R.string.maps_location_openingceremony), new LatLng(44.810799, 20.462531));
        positions.put(getResources().getString(R.string.maps_location_closingceremony), new LatLng(44.811378, 20.469856));
        positions.put(getResources().getString(R.string.maps_location_welcome), new LatLng(44.817796, 20.4657486));
        positions.put(getResources().getString(R.string.maps_location_monparty), new LatLng(44.807985, 20.444820));
        positions.put(getResources().getString(R.string.maps_location_countryfair), new LatLng(44.82329, 20.4676));
        positions.put(getResources().getString(R.string.maps_location_artnight), new LatLng(44.8178802, 20.4652923));
        positions.put(getResources().getString(R.string.maps_location_flagparade), new LatLng(44.8119976, 20.4630151));
        positions.put(getResources().getString(R.string.maps_location_speakup), new LatLng(44.8121435, 20.4625028));
        positions.put(getResources().getString(R.string.maps_location_farewell), new LatLng(44.8057579, 20.475727));
        positions.put(getResources().getString(R.string.maps_location_dom), new LatLng(44.8225824, 20.4588079));

        // show them on map
        for (String name : positions.keySet()) {
            markers.add(mMap.addMarker(new MarkerOptions()
                .position(positions.get(name))
                .title(name)));
        }

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

        String focus_to = view.getTag().toString();
        Marker focus_marker = null;
        for (Marker marker : markers) {
            if (marker.getTitle().equals(focus_to)) {
                focus_marker = marker;
            }
        }

        // Find a marker
        if (focus_marker != null) {
            focus_marker.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(focus_marker.getPosition(), zoom));
        }

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
