package com.champions.wayst;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Switch objectAvoidanceSwitch = (Switch) findViewById(R.id.object_avoidance_switch);
        final Switch navigationSwitch = (Switch) findViewById(R.id.navigation_switch);
        final Button continueButton = (Button) findViewById(R.id.continue_button);

        // Object Avoidance Mode will be on by default
        // Object Avoidance Mode will be switched off if Navigation Mode is switched on
        // Navigation Mode will be switched off if Object Avoidance Mode is switched on

        objectAvoidanceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "object avoidance on");
                    navigationSwitch.setChecked(false);
                } else {
                    Log.d(TAG, "object avoidance off");
                }
            }
        });

        navigationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "navigation on");
                    objectAvoidanceSwitch.setChecked(false);
                    continueButton.setVisibility(View.VISIBLE);
                } else {
                    Log.d(TAG, "navigation off");
                    continueButton.setVisibility(View.GONE);
                }
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "continue button clicked, going to navigation activity");
                Intent intent = new Intent(v.getContext(), NavigationActivity.class);
                startActivity(intent);
            }
        });
    }


}
