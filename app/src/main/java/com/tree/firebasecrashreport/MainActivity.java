package com.tree.firebasecrashreport;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FirebaseCrash.report(new Exception("My first Android non-fatal error"));
        //FirebaseCrash.log("Activity created");

        //ArrayList<String> s  = null;
        //s.add("r");

        TextView tv = null;
        tv.setText("3");
    }
}
