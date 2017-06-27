package com.example.administrator.bitmapfactory;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ImageView image_first;
    private ImageView image_second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image_first = (ImageView) findViewById(R.id.image_first);
        image_second = (ImageView) findViewById(R.id.image_second);
        image_first.setImageBitmap(BitmapUtil.decodeBitmap(this,R.mipmap.test01, 2000, 2000));

        try {
            image_second.setImageBitmap( BitmapUtil.decodeBitmap(this,"girl.jpg", 2000, 2000));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
