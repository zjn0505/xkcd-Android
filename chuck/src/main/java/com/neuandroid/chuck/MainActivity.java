package com.neuandroid.chuck;

import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.seismic.ShakeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements ShakeDetector.Listener {

    private TextView tvChuck;
    private ProgressBar pbLoading;
    private EditText edtFirstName;
    private EditText edtLastName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);



        tvChuck = (TextView) findViewById(R.id.tv_chuck);
        tvChuck.setText(getText(R.string.click_to_load));
        tvChuck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadChuckQuotes();
            }
        });
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        edtFirstName = (EditText) findViewById(R.id.edt_first_name);
        edtLastName = (EditText) findViewById(R.id.edt_last_name);
    }

    private void loadChuckQuotes() {
        String firstName = edtFirstName.getText().toString();
        String lastName = edtLastName.getText().toString();
        URL url = NetworkUtils.buildUrl(firstName, lastName);

        new ChuckQuoteTask().execute(url);

    }

    @Override
    public void hearShake() {
        loadChuckQuotes();
    }

    /**
     * This class extends AsyncTask to execute the query out of the main thread.
     * We should always not run a time consuming task on main thread.
     */
    private class ChuckQuoteTask extends AsyncTask<URL, Object, String> {

        @Override
        protected void onPreExecute() {
            // Here we prepare what we need to be done on the UI
            // For example, show a progress bar for a download task.
            // This method is executed on main thread.
            pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... params) {
            String result = null;

            try {
                URL url = params[0];
                result = NetworkUtils.getResponseFromHttpUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // This is method is executed after the task is over.
            // Since we use this task to get a String.
            // We should have a function to let us render what we got after the task
            // And here the function is onPostExecute()


            // This function is executed on main thread as well.
            pbLoading.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(result)) {

                try {
                    String joke = extractJokeFromJson(result);
                    tvChuck.setText(joke);
                } catch (JSONException e) {
                    e.printStackTrace();
                    tvChuck.setText(getString(R.string.result_empty));
                }
            }
            tvChuck.setText(getString(R.string.result_empty));
        }

        private String extractJokeFromJson(String json) throws JSONException {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject value = jsonObject.optJSONObject("value");
            String joke = value.optString("joke");
            return joke;
        }
    }

}
