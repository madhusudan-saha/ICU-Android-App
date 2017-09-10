package com.example.madhusudansaha.icu;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class AnotherActivity extends AppCompatActivity {
    TextView timeTextView;
    TextView messageTextView;
    ImageView imageView;
    TextView categoryText;
    TextView severityText;
    TextView locationText;
    TextView remarksText;
    Button updateButton;
    String message;
    String path;
    String category;
    String severity;
    String location;
    String remarks;
    String uri;
    String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);

        Intent values = getIntent();

        message = values.getExtras().getString("message");
        path = values.getExtras().getString("image");
        category = values.getExtras().getString("category");
        severity = values.getExtras().getString("severity");
        location = values.getExtras().getString("location");
        remarks = values.getExtras().getString("remarks");
        uri = values.getExtras().getString("uri");
        time = values.getExtras().getString("time");

        Bitmap image = ImageRetrieve.loadImageFromStorage(path);

        messageTextView = (TextView) findViewById(R.id.messageTextView);
        messageTextView.setText(message);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(image);

        categoryText = (TextView) findViewById(R.id.category);
        categoryText.setText(category);

        severityText = (TextView) findViewById(R.id.severity);
        severityText.setText(severity);

        locationText = (TextView) findViewById(R.id.location);
        locationText.setText(location);

        remarksText = (TextView) findViewById(R.id.remarks);
        //remarksText.setText(remarks);
        remarksText.setText(message);

        timeTextView = (TextView) findViewById(R.id.timeTextView);
        timeTextView.setText(time);

        updateButton = (Button) findViewById(R.id.updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    callEditable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Toast.makeText(this, "Alert!", Toast.LENGTH_SHORT).show();

        setResult(RESULT_OK, values);
    }

    private void callEditable() {
        if (message != null) {
            Intent intent = new Intent(this, EditableActivity.class);

            intent.putExtra("message", message);
            intent.putExtra("image", path);
            intent.putExtra("category", category);
            intent.putExtra("severity", severity);
            intent.putExtra("location", location);
            intent.putExtra("remarks", remarks);
            intent.putExtra("uri", uri);
            intent.putExtra("time", time);

            final int result = 1;
            startActivityForResult(intent, result);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        severity = data.getExtras().getString("severity");
        category = data.getExtras().getString("category");
        remarks = data.getExtras().getString("remarks");
        severityText.setText(severity);
        categoryText.setText(category);
        remarksText.setText(remarks);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void backButton(View view) {
        setResult(RESULT_OK);
        this.finish();
    }

    public void okButton(View view) {
        FileOutputStream stream = null;
        if(severity.equals("Major")) {
            try {
                ContextWrapper cw = new ContextWrapper(getApplicationContext());

                File directory = cw.getDir("dir", Context.MODE_PRIVATE);

                File file = new File(directory, "buzz_time");

                stream = new FileOutputStream(file);
                stream.write(new Date().toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        this.finish();
    }
}