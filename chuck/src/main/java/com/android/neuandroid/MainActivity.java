package com.android.neuandroid;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView tvChuck;
    private static final String CHUCK_QUERY = "http://api.icndb.com/jokes/random?limitTo=[nerdy]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvChuck = (TextView) findViewById(R.id.tv_chuck);
        tvChuck.setText(getText(R.string.click_to_load));
        tvChuck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadChuckQuotes();
            }
        });
    }

    private void loadChuckQuotes() {
        new ChuckQuoteTask().execute(CHUCK_QUERY);

    }

    /**
     * This class extends AsyncTask to execute the query out of the main thread.
     * We should always not run a time consuming task on main thread.
     */
    private class ChuckQuoteTask extends AsyncTask<String, Object, String> {

        @Override
        protected void onPreExecute() {
            // Here we prepare what we need to be done on the UI
            // For example, show a progress bar for a download task.
            // This method is executed on main thread.
            tvChuck.setText("Loading famous Chuck quotes");
        }

        @Override
        protected String doInBackground(String... params) {
            String result = null;

            try {
                URL url = new URL(params[0]);
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
            try {
                String joke = extractJokeFromJson(result);
                tvChuck.setText(joke);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String extractJokeFromJson(String json) throws JSONException {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject value = jsonObject.optJSONObject("value");
            String joke = value.optString("joke");
            return joke;
        }
    }

}
