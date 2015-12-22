package com.wrx.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.esri.android.action.IdentifyResultSpinner;
import com.esri.android.action.IdentifyResultSpinnerAdapter;
import com.esri.android.map.Callout;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.tasks.identify.IdentifyParameters;
import com.esri.core.tasks.identify.IdentifyResult;
import com.esri.core.tasks.identify.IdentifyTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    MapView mMapView;
    static ProgressDialog dialog;
    IdentifyParameters params = null;
    LocationManager locationManager;
    Location currenLocation;
    int MY_PERMISSIONS_REQUEST = 0;
    String TAG="Error";
    public Map<String,List<MapLayerField>>layerFieldsList;
    LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        layerFieldsList = new HashMap<String, List<MapLayerField>>();
        layerFieldsList.put("Railway", new ArrayList<MapLayerField>(
                Arrays.asList(
                        new MapLayerField("Railway","STATEAB","State Abb" ),
                        new MapLayerField("Railway","STATEFIPS","State Fips"),
                        new MapLayerField("Railway","OBJECTID","ObjectId"),
                        new MapLayerField("Railway","RROWNER1","Owner")
                )));
        layerFieldsList.put("Airport", new ArrayList<MapLayerField>(
                Arrays.asList(
                        new MapLayerField("Airport","OBJECTID","ObjectID" ),
                        new MapLayerField("Airport","SiteNumber","SiteNumber"),
                        new MapLayerField("Airport","LocationID","LocationId"),
                        new MapLayerField("Airport","Region","Region")
                )));

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Snackbar.make(findViewById(android.R.id.content),"x:" + String.valueOf(location.getLongitude()),Snackbar.LENGTH_LONG).show();
                    currenLocation = location;
                    mMapView.centerAndZoom(location.getLatitude(),location.getLongitude(),1);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Boolean IsGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0, locationListener);
        currenLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(currenLocation==null){
            currenLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (IsGPSEnabled) {
            if (currenLocation == null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0, locationListener);
                Log.d("Network", "Network");

                if (locationManager != null) {
                    currenLocation = locationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    //updateGPSCoordinates();
                }
            }

        }
        mMapView = (MapView) findViewById(R.id.map);
        MapOptions options;
        if (currenLocation != null) {
            options = new MapOptions(MapOptions.MapType.GRAY, currenLocation.getLatitude(), currenLocation.getLongitude(), 12);
        } else {
            options = new MapOptions(MapOptions.MapType.GRAY, 44.0, -85.2,12);
        }

        mMapView.setMapOptions(options);

        mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://server.arcgisonline.com/arcgis/rest/services/Canvas/World_Dark_Gray_Base/MapServer"));
        mMapView.addLayer(new ArcGISDynamicMapServiceLayer("https://maps3.arcgisonline.com/ArcGIS/rest/services/A-16/FRA_US_Railway_Network/MapServer"));
        mMapView.addLayer(new ArcGISDynamicMapServiceLayer("https://maps6.arcgisonline.com/ArcGIS/rest/services/A-16/FAA_5010_Airports/MapServer"));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        params = new IdentifyParameters();
        params.setTolerance(20);
        params.setDPI(98);
        params.setLayers(new int[]{1});
        params.setLayerMode(IdentifyParameters.ALL_LAYERS);

        mMapView.setOnSingleTapListener(new OnSingleTapListener() {

            private static final long serialVersionUID = 1L;

            @Override
            public void onSingleTap(final float x, final float y) {

                if (!mMapView.isLoaded()) {
                    return;
                }

                // Add to Identify Parameters based on tapped location
                Point identifyPoint = mMapView.toMapPoint(x, y);

                params.setGeometry(identifyPoint);
                params.setSpatialReference(mMapView.getSpatialReference());
                params.setMapHeight(mMapView.getHeight());
                params.setMapWidth(mMapView.getWidth());
                params.setReturnGeometry(false);

                // add the area of extent to identify parameters
                Envelope env = new Envelope();
                mMapView.getExtent().queryEnvelope(env);
                params.setMapExtent(env);

                // execute the identify task off UI thread
                MyIdentifyTask mTask = new MyIdentifyTask(identifyPoint);
                mTask.execute(params);
            }

        });

        /*DrawCanvasCircle c= new DrawCanvasCircle(getApplicationContext());
        Bitmap result = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(result);
        c.setLeft(50);
        c.setTop(200);
        c.draw(canvas);
        c.setLayoutParams(new LinearLayout.LayoutParams( 250, 250));

        LinearLayout myLayout = (LinearLayout)findViewById(R.id.myLayout);
        myLayout.addView(c);*/

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mMapView == null || !mMapView.isLoaded()) {
                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    currenLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }

    private ViewGroup createIdentifyContent(final List<IdentifyResult> results) {

        // create a new LinearLayout in application context
        LinearLayout layout = new LinearLayout(this);

        // view height and widthwrap content
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // default orientation
        layout.setOrientation(LinearLayout.HORIZONTAL);

        // Spinner to hold the results of an identify operation
        IdentifyResultSpinner spinner = new IdentifyResultSpinner(this, results);

        // make view clickable
        spinner.setClickable(false);
        spinner.canScrollHorizontally(BIND_ADJUST_WITH_ACTIVITY);

        // MyIdentifyAdapter creates a bridge between spinner and it's data
        MyIdentifyAdapter adapter = new MyIdentifyAdapter(this, results);
        spinner.setAdapter(adapter);
        spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(spinner);

        return layout;
    }

    public class MyIdentifyAdapter extends IdentifyResultSpinnerAdapter {
        String m_show = null;
        List<IdentifyResult> resultList;
        int currentDataViewed = -1;
        Context m_context;

        public MyIdentifyAdapter(Context context, List<IdentifyResult> results) {
            super(context, results);
            this.resultList = results;
            this.m_context = context;
        }

        // Get a TextView that displays identify results in the callout.
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String LSP = System.getProperty("line.separator");
            StringBuilder outputVal = new StringBuilder();

            // Resource Object to access the Resource fields
            Resources res = getResources();

            // Get Name attribute from identify results
            IdentifyResult curResult = this.resultList.get(position);
            String layerName = curResult.getLayerName();

            List<MapLayerField> fields = layerFieldsList.get(layerName);
            for(MapLayerField field:fields){
                outputVal.append(field.get_outputFieldName() + curResult.getAttributes().get(field.get_fieldName()).toString());
            }
            /*outputVal.append("State Abb: " + curResult.getAttributes().get("STATEAB").toString());
            outputVal.append("State fips: " + curResult.getAttributes().get("STATEFIPS").toString());
            outputVal.append("ObjectId: " + curResult.getAttributes().get("OBJECTID").toString());
            outputVal.append("Owner: " + curResult.getAttributes().get("RROWNER1").toString());

            if (curResult.getAttributes().containsKey(
                    res.getString(R.string.NAME))) {
                outputVal.append("Place: "
                        + curResult.getAttributes()
                        .get(res.getString(R.string.NAME)).toString());
                outputVal.append(LSP);
            }

            if (curResult.getAttributes().containsKey(
                    res.getString(R.string.ID))) {
                outputVal.append("State ID: "
                        + curResult.getAttributes()
                        .get(res.getString(R.string.ID)).toString());
                outputVal.append(LSP);
            }

            if (curResult.getAttributes().containsKey(
                    res.getString(R.string.ST_ABBREV))) {
                outputVal.append("Abbreviation: "
                        + curResult.getAttributes()
                        .get(res.getString(R.string.ST_ABBREV))
                        .toString());
                outputVal.append(LSP);
            }

            if (curResult.getAttributes().containsKey(
                    res.getString(R.string.TOTPOP_CY))) {
                outputVal.append("Population: "
                        + curResult.getAttributes()
                        .get(res.getString(R.string.TOTPOP_CY))
                        .toString());
                outputVal.append(LSP);

            }

            if (curResult.getAttributes().containsKey(
                    res.getString(R.string.LANDAREA))) {
                outputVal.append("Area: "
                        + curResult.getAttributes()
                        .get(res.getString(R.string.LANDAREA))
                        .toString());
                outputVal.append(LSP);

            }
            */
            // Create a TextView to write identify results
            TextView txtView;
            txtView = new TextView(this.m_context);
            txtView.setText(outputVal);
            txtView.setTextColor(Color.BLACK);
            txtView.setLayoutParams(new ListView.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            txtView.setGravity(Gravity.CENTER_VERTICAL);

            return txtView;
        }
    }

    private class MyIdentifyTask extends
            AsyncTask<IdentifyParameters, Void, IdentifyResult[]> {

        IdentifyTask task = new IdentifyTask("https://maps3.arcgisonline.com/ArcGIS/rest/services/A-16/FRA_US_Railway_Network/MapServer/identify");

        IdentifyResult[] M_Result;

        Point mAnchor;

        MyIdentifyTask(Point anchorPoint) {
            mAnchor = anchorPoint;
        }

        @Override
        protected void onPreExecute() {
            // create dialog while working off UI thread
            try {
                ProgressDialog.show(getApplicationContext(), "Identify Task",
                        "Identify query ...");
            }catch (Exception ex){

            }

        }

        protected IdentifyResult[] doInBackground(IdentifyParameters... params) {

            // check that you have the identify parameters
            if (params != null && params.length > 0) {
                IdentifyParameters mParams = params[0];

                try {
                    // Run IdentifyTask with Identify Parameters

                    M_Result = task.execute(mParams);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return M_Result;
        }

        @Override
        protected void onPostExecute(IdentifyResult[] results) {

            // dismiss dialog
           /* if (dialog.isShowing()) {
                dialog.dismiss();
            }*/

            ArrayList<IdentifyResult> resultList = new ArrayList<IdentifyResult>();

            IdentifyResult result_1;

            for (int index = 0; index < results.length; index++) {

                result_1 = results[index];
                String displayFieldName = result_1.getDisplayFieldName();
                Map<String, Object> attr = result_1.getAttributes();
                for (String key : attr.keySet()) {
                    if (key.equalsIgnoreCase(displayFieldName)) {
                        resultList.add(result_1);
                    }
                }
            }

            Callout callout = mMapView.getCallout();
            callout.setContent(createIdentifyContent(resultList));
            callout.show(mAnchor);
        }
    }
}
