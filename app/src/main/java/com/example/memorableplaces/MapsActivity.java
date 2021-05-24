package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.memorableplaces.databinding.ActivityMapsBinding;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    LocationManager locationManager;
    LocationListener locationListener;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    public void centerMapOnLocation(Location location, String tittle) {
        if (location != null) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLocation).title(tittle));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation, "Your Location");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        if (intent.getIntExtra("placeNumber", 0) == 0) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    centerMapOnLocation(location, "Your Location");
                }
            };
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation, "Your Location");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }else{
            Location placeLocation =new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.location.get(intent.getIntExtra("placeNumber",0)).latitude);
            placeLocation.setLongitude(MainActivity.location.get(intent.getIntExtra("placeNumber",0)).longitude);
            centerMapOnLocation(placeLocation,MainActivity.arrayList.get(intent.getIntExtra("placeNumber",0)));

        }

    }

    @Override
    public void onMapLongClick(@NonNull @NotNull LatLng latLng) {
        Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
        String address="";
        try{
            List<Address> addressList=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addressList!=null&&addressList.size()>0) {
                if (addressList.get(0).getThoroughfare() != null) {
                    if (addressList.get(0).getSubThoroughfare() != null) {
                        address += addressList.get(0).getSubThoroughfare() + " ";
                    }
                    address += addressList.get(0).getThoroughfare();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(address.equals("")){
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm yyyy-MM-dd");
            address+=simpleDateFormat.format(new Date());
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        MainActivity.location.add(latLng);
        MainActivity.arrayList.add(address);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.memorableplaces",Context.MODE_PRIVATE);
        try{
            ArrayList<String> latitudes=new ArrayList<String>();
            ArrayList<String> longitudes=new ArrayList<String>();

            for (LatLng coord:MainActivity.location){
                latitudes.add(Double.toString(coord.latitude));
                longitudes.add(Double.toString(coord.longitude));
            }
            sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.arrayList)).apply();
            sharedPreferences.edit().putString("lats",ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("lons",ObjectSerializer.serialize(longitudes)).apply();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        Toast.makeText(this, "Location Saved!", Toast.LENGTH_SHORT).show();

    }
}