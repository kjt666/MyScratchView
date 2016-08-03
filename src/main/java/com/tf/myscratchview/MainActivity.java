package com.tf.myscratchview;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private MyScratchView myScratchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myScratchView = (MyScratchView) findViewById(R.id.myScratchView);
        myScratchView.setViewType(MyScratchView.ViewType.TEXT_MODE);
        myScratchView.setGiftText("一等奖！");
        myScratchView.setTextColor(Color.RED);
        myScratchView.setTextSize(20);
    }
}
