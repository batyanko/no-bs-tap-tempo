package com.batyanko.nobstaptempo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private int pos2;
    private int pos5;
    private int pos10;
    private int pos20;
    private long[] bpm2;
    private long[] bpm5;
    private long[] bpm10;
    private long[] bpm20;
    private long tsNow;
    private int beatCount;
    private Chronometer chronometer;
    private SharedPreferences pref;
    private TextView bpm2Tv;
    private TextView bpm5Tv;
    private TextView bpm10Tv;
    private TextView bpm20Tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref = this.getPreferences(Context.MODE_PRIVATE);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView prefTv = findViewById(R.id.pref_tv);
        PopupMenu popupMenu = new PopupMenu(this, prefTv);
        popupMenu.inflate(R.menu.menu_options);
        popupMenu.setOnMenuItemClickListener(this);
        prefTv.setOnClickListener(view -> popupMenu.show());

        chronometer = findViewById(R.id.elapsed);

        TextView beatCountTv = findViewById(R.id.beat_count_tv);

        bpm2Tv = findViewById(R.id.bpm2_tv);
        bpm5Tv = findViewById(R.id.bpm5_tv);
        bpm10Tv = findViewById(R.id.bpm10_tv);
        bpm20Tv = findViewById(R.id.bpm20_tv);

        Button resetButton = findViewById(R.id.reset_button);
        resetButton.setOnClickListener(v -> init());

        Button hitMeButton = findViewById(R.id.hit_me_button);
        hitMeButton.setOnClickListener(v -> {
            tsNow = System.nanoTime();

            if (beatCount++ == 0) {
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
            }
            beatCountTv.setText(String.valueOf(beatCount));

            bpm2[pos2 % 2] = tsNow;
            bpm5[pos5 % 5] = tsNow;
            bpm10[pos10 % 10] = tsNow;
            bpm20[pos20 % 20] = tsNow;

            pos2++;
            pos5++;
            pos10++;
            pos20++;

            refresh();
        });
        init();

    }

    private void refresh() {

        if (beatCount >= 2) {
            long bpm2l = 60000000000000L / ((bpm2[(pos2 - 1) % 2] - bpm2[(pos2) % 2]));
            bpm2Tv.setText(String.format("%s%s", round(bpm2l), fraction(bpm2l)));

        }

        if (beatCount >= 5) {
            long bpm5l = 240000000000000L / ((bpm5[(pos5 - 1) % 5] - bpm5[(pos5) % 5]));
            bpm5Tv.setText(String.format("%s%s", round(bpm5l), fraction(bpm5l)));
        }

        if (beatCount >= 10) {
            long bpm10l = 540000000000000L / ((bpm10[(pos10 - 1) % 10] - bpm10[(pos10) % 10]));
            bpm10Tv.setText(String.format("%s%s", round(bpm10l), fraction(bpm10l)));
        }

        if (beatCount >= 20) {
            long bpm20l = 1140000000000000L / ((bpm20[(pos20 - 1) % 20] - bpm20[(pos20) % 20]));
            bpm20Tv.setText(String.format("%s%s", round(bpm20l), fraction(bpm20l)));
        }

    }

    //    Round BPM as double, divide by 1000 and convert to string
    private String round(long bpm) {
        double bpmDouble = bpm;
        bpmDouble /= 1000;
        return String.valueOf(Math.round(bpmDouble));
    }

    private String fraction(long bpm) {
        if (!pref.getBoolean("pref_fractions", false)) {
            return "";
        }
//        double remainder = (double) bpm % 1000;
        String bpmString = String.valueOf(bpm);
        String fraction = bpmString.substring(bpmString.length() - 3);
        Log.d("BPM", String.valueOf(bpm));
        Log.d("REMAINDER", fraction.substring(0, 2));
        return "," + fraction.substring(0, 2);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        init();
//    }

    @Override
    protected void onRestart() {
        super.onRestart();
        init();
    }

    // Initialize/reset values
    private void init() {

        TextView beatCountTv = findViewById(R.id.beat_count_tv);
        beatCountTv.setText("0");

        TextView bpm2Tv = findViewById(R.id.bpm2_tv);
        bpm2Tv.setText("0");

        TextView bpm5Tv = findViewById(R.id.bpm5_tv);
        bpm5Tv.setText("0");

        TextView bpm10Tv = findViewById(R.id.bpm10_tv);
        bpm10Tv.setText("0");

        TextView bpm20Tv = findViewById(R.id.bpm20_tv);
        bpm20Tv.setText("0");

        tsNow = 0;
        beatCount = 0;
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());

        bpm2 = new long[2];
        bpm5 = new long[5];
        bpm10 = new long[10];
        bpm20 = new long[20];

        pos2 = 0;
        pos5 = 0;
        pos10 = 0;
        pos20 = 0;
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.show_fractions) {
            boolean frac = pref.getBoolean("pref_fractions", false);
            Log.d("FRAC", String.valueOf(frac));
            pref.edit().putBoolean("pref_fractions", !frac).apply();
            refresh();
            return true;
        } else if (itemId == R.id.menu_item_backup) {
            return true;
        } else if (itemId == R.id.menu_item_settings) {
            return true;
        } else if (itemId == R.id.menu_item_help) {
            return true;
        } else if (itemId == R.id.menu_item_about) {
            return true;
        }
        return false;
    }

}