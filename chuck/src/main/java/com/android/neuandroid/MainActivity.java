package com.android.neuandroid;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

    // This is a fake loading progress to simulate how text field is updated.
    // We don't actually query the quote from web for now.
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
            tvChuck.setText(result);
        }

        // We can ignore this method, it's just a faked query progress.
        private String fakingQueryProgress() {
            int i = 6;
            while (i > 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvChuck.append(".");
                    }
                });
                i--;
            }
            return "Chuck Norris can access private methods.";
        }
    }

}
