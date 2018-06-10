package com.ogbizi.android_library;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.ogbizi.android_swappable_imageview.SwappableImageView;

public class MainActivity extends AppCompatActivity {

    SwappableImageView swappableImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swappableImageView = findViewById(R.id.img_swappable);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

    public void onNextClick(View v) {
        swappableImageView.showNext(false);
    }

    public void onPrevClick(View v) {
        swappableImageView.showPrevious(false);
    }

    public void onToggleSwitch(View v) {
        SwitchCompat s = (SwitchCompat) v;
        swappableImageView.setLooping(s.isChecked());
    }
}
