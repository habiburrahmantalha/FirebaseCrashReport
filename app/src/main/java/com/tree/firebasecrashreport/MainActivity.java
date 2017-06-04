package com.tree.firebasecrashreport;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements DrawRoute.onDrawRoute {

    MapView mapView;
    GoogleMap map;
    String key = "AIzaSyCzve4VAODV-3x0udLpjCTU2R3nQxcaRXQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.map_view);
        getPermissions();
        createMapView(savedInstanceState);

        //FirebaseCrash.report(new Exception("My first Android non-fatal error"));
        //FirebaseCrash.log("Activity created");

        //ArrayList<String> s  = null;
        //s.add("r");

        //TextView tv = null;
        //tv.setText("3");
    }

    void getPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.INTERNET,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    private void createMapView(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;


                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                map.setMyLocationEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
                mapView.onResume();
                updateLocation();

            }
        });

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void updateLocation() {

        TrackGPS gps = new TrackGPS(this);
        if (!gps.canGetLocation())
            return;;

        double longitude = gps.getLongitude();
        double latitude = gps.getLatitude();

        LatLng latLng = new LatLng(latitude,longitude);

        int t = 1;
        while(t-->0){
            LatLng newLatlng = getRandomLocation(latLng);
            map.addMarker(new MarkerOptions()
                    .position(newLatlng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            );

            DrawRoute.getInstance(MainActivity.this, this).setFromLatLong(latitude,longitude)
                    .setToLatLong(newLatlng.latitude,newLatlng.longitude).setGmapAndKey(key, map).run();
        }


        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        map.animateCamera(cameraUpdate);

        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng).title("My Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );
        marker.showInfoWindow();

    }

    public static LatLng getRandomLocation(LatLng latLng) {
        double newLat = latLng.latitude +  randomNumber(0.009, -0.009);
        double newLon = latLng.longitude + randomNumber(0.009, -0.009);

        return new LatLng(newLat, newLon);
    }
    public static double randomNumber(double max, double min) {
        Random random = new Random();
        return random.nextDouble()* (max - min)  + min;
    }
    List<LatLng> directionPoint;
    @Override
    public void afterDraw(String result,List<LatLng> directionPoint) {
        this.directionPoint = directionPoint;
        Log.d("Directional Point", directionPoint.size()+"");
        setAnimation(map,directionPoint);
        //bearingBetweenLatLngs(directionPoint.get(0),directionPoint.get(1));

    }

Marker myMarkerLOC;
    int position = 1;

    public  void setAnimation(GoogleMap myMap, final List<LatLng> directionPoint) {


        myMarkerLOC = myMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .position(directionPoint.get(0))
                .flat(true));

        Location location = new Location("Test");
        location.setLatitude(directionPoint.get(position).latitude);
        location.setLongitude(directionPoint.get(position).longitude);
        animateMarker(location);

        //myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0), 10));

        //animateMarker(myMap, marker, directionPoint, false);
    }


    private static void animateMarker(GoogleMap myMap, final Marker marker, final List<LatLng> directionPoint,
                                      final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = myMap.getProjection();
        final long duration = 600000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                Location location=new Location(String.valueOf(directionPoint.get(i)));
                if(i==directionPoint.size()-1)
                    return;
                Location newlocation=new Location(String.valueOf(directionPoint.get(i+1)));
                marker.setAnchor(0.5f, 0.5f);
                //marker.setRotation(location.bearingTo(newlocation)  - 45);
                if (i < directionPoint.size()) {
                    marker.setPosition(directionPoint.get(i));
                }

                i++;


                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    private float bearingBetweenLatLngs(LatLng beginLatLng,LatLng endLatLng) {
        Location beginLocation = convertLatLngToLocation(beginLatLng);
        Location endLocation = convertLatLngToLocation(endLatLng);
        return beginLocation.bearingTo(endLocation);
    }
    private Location convertLatLngToLocation(LatLng latLng) {
        Location location = new Location("someLoc");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    public void animateMarker(final Location location)
    {
        if (myMarkerLOC!=null) {
            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = new ValueAnimator();
            final LatLng startPosition = myMarkerLOC.getPosition();
            final float startRotation = myMarkerLOC.getRotation();
            final float angle = 180 - Math.abs(Math.abs(startRotation - location.getBearing()) - 180);
            final float right = WhichWayToTurn(startRotation, location.getBearing());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate(ValueAnimator animation)
                {
                    try {
                        if (myMarkerLOC == null) // oops... destroying map during animation...
                        {
                            return;
                        }
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, toLatLng(location));
                        float rotation = startRotation + right * v * angle;
                        myMarkerLOC.setRotation((float) rotation);
                        myMarkerLOC.setPosition(newPosition);
                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    position++;
                    if(position<directionPoint.size()){
                        Location location = new Location("Test");
                        location.setLatitude(directionPoint.get(position).latitude);
                        location.setLongitude(directionPoint.get(position).longitude);
                        animateMarker(location);
                    }


                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            valueAnimator.setFloatValues(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.start();
        }
    }
    private float WhichWayToTurn(float currentDirection, float targetDirection)
    {
        float diff = targetDirection - currentDirection;
        if (Math.abs(diff) == 0)
        {
            return 0;
        }
        if(diff > 180)
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }
    public interface LatLngInterpolator
    {
        public LatLng interpolate(float fraction, LatLng a, LatLng b);

        public class Linear implements LatLngInterpolator
        {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b)
            {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lng = (b.longitude - a.longitude) * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
        public class LinearFixed implements LatLngInterpolator
        {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }

    public static LatLng toLatLng(final Location location)
    {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }


}
