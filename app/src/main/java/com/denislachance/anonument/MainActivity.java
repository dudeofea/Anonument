package com.denislachance.anonument;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //load the layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //Creating a new monument
    public void create_anonument(View view) {
        Intent intent = new Intent(this, CreateAnonumentActivity.class);
        startActivity(intent);
    }

    //Finding nearby monuments
    public void find_nearby(View view) {
        Intent intent = new Intent(this, FindActivity.class);
        startActivity(intent);
    }
}
