package com.champions.wayst;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Button navigateButton = (Button) findViewById(R.id.navigate_button);
        final EditText destinationEditText = (EditText) findViewById(R.id.destination);

        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destination = destinationEditText.getText().toString();

                // Query api using start and end locations, store the json somewhere?
                // if the json is empty (or however else indicates invalid query), then show toast to user

            }
        });

    }

    // TODO if using Google Maps API, need to attribute: https://developers.google.com/maps/documentation/android-api/intro

}
