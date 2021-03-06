package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String>arrayList=new ArrayList<String>();
    static ArrayList<LatLng> location=new ArrayList<LatLng>();
    static ArrayAdapter<String>arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);
        ArrayList<String> latitudes=new ArrayList<String>();
        ArrayList<String> longitudes=new ArrayList<String>();

        arrayList.clear();
        latitudes.clear();
        longitudes.clear();
        location.clear();
        try{
            arrayList= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lats",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes= (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("lons",ObjectSerializer.serialize(new ArrayList<String>())));

        }catch (Exception e){

            e.printStackTrace();
        }
        if(arrayList.size()>0&& latitudes.size()>0&&longitudes.size()>0){
            if(arrayList.size()==latitudes.size()&&arrayList.size()==longitudes.size()){
                for (int i=0;i<latitudes.size();i++){
                    location.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }
        }else {
            arrayList.add("Add a new place...");
            location.add(new LatLng(0,0));
        }

        ListView listView=findViewById(R.id.listView);
       

        arrayAdapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,arrayList);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("placeNumber",position);
                startActivity(intent);
            }
        });
    }
}