package com.example.timeviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TimeView) findViewById(R.id.time)).tick();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((TimeView) findViewById(R.id.time)).destroy();
    }
}
