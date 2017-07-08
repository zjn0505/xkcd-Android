package com.android.neuandroid;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tvChuck;

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
        new ChuckQuoteTask().execute();

    }

    // This is a fake loading progress to simulate how text field is updated.
    // We don't actually query the quote from web for now.
    private class ChuckQuoteTask extends AsyncTask<Void, Object, String> {

        @Override
        protected void onPreExecute() {
            // Here we prepare what we need to be done on the UI
            // For example, show a progress bar for a download task.
            // This method is executed on main thread.
            tvChuck.setText("Loading famous Chuck quotes");
        }

        @Override
        protected String doInBackground(Void... params) {
            // We can ignore what is done in this method for now
            // The task should retrieve a String from internet.
            // Since we use fake query, we pretend here that we've got a String result from internet.
            String result = fakingQueryProgress();

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
