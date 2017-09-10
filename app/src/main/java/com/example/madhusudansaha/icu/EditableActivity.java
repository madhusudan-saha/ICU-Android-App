package com.example.madhusudansaha.icu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class EditableActivity extends AppCompatActivity {

    TextView timeTextViewEdit;
    TextView messageTextViewEdit;
    ImageView imageViewEdit;
    TextView locationEdit;
    Spinner categoryEdit;
    Spinner severityEdit;
    EditText remarksEdit;
    Button submitFeedback;

    String message;
    String path;
    String category;
    String severity;
    String location;
    String remarks;
    String uri;
    String time;
    int categoryId;
    int severityId;

    List<String> severities;
    List<String> categories;

    Intent values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editable);

        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setVisibility(View.INVISIBLE);

        severities = new ArrayList<String>();
        categories = new ArrayList<String>();

        severities.add("Major");
        severities.add("Minor");

        categories.add("Medical");
        categories.add("Violence");
        categories.add("Weapon");
        categories.add("General");

        values = getIntent();

        message = values.getExtras().getString("message");
        path = values.getExtras().getString("image");
        category = values.getExtras().getString("category");
        severity = values.getExtras().getString("severity");
        location = values.getExtras().getString("location");
        remarks = values.getExtras().getString("remarks");
        uri = values.getExtras().getString("uri");
        time = values.getExtras().getString("time");

        categoryId = categories.indexOf(category);
        severityId = severities.indexOf(severity);

        Bitmap image = ImageRetrieve.loadImageFromStorage(path);

        messageTextViewEdit = (TextView) findViewById(R.id.messageTextViewEdit);
        messageTextViewEdit.setText(message);

        imageViewEdit = (ImageView) findViewById(R.id.imageViewEdit);
        imageViewEdit.setImageBitmap(image);

        locationEdit = (TextView) findViewById(R.id.locationEdit);
        locationEdit.setText(location);

        categoryEdit = (Spinner) findViewById(R.id.categoryEdit);
        categoryEdit.setId(categoryId);

        severityEdit = (Spinner) findViewById(R.id.severityEdit);
        severityEdit.setId(severityId);

        remarksEdit = (EditText) findViewById(R.id.remarksEdit);
        //remarksEdit.setText(remarks);
        remarksEdit.setText(message);

        remarksEdit.setScroller(new Scroller(this));
        remarksEdit.setMaxLines(1);
        remarksEdit.setVerticalScrollBarEnabled(true);
        remarksEdit.setMovementMethod(new ScrollingMovementMethod());

        categoryEdit.setEnabled(false);
        severityEdit.setEnabled(false);
        remarksEdit.setEnabled(false);

        timeTextViewEdit = (TextView) findViewById(R.id.timeTextViewEdit);
        timeTextViewEdit.setText(time);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryEdit.setAdapter(categoryAdapter);

        ArrayAdapter<String> severityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, severities);
        severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        severityEdit.setAdapter(severityAdapter);


        submitFeedback = (Button) findViewById(R.id.submitFeedback);
        submitFeedback.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    sendFeedback();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        values.putExtra("severity", severity);
        values.putExtra("category", category);
        values.putExtra("remarks", remarks);
    }

    private void sendFeedback() throws UnsupportedEncodingException {
        JSONObject dataObj = new JSONObject();

        severityId = severityEdit.getSelectedItemPosition();
        severity = severities.get(severityId);
        categoryId = categoryEdit.getSelectedItemPosition();
        category = categories.get(categoryId);
        remarks = remarksEdit.getText().toString();

        try {
            dataObj.put("imgUrl", uri);
            dataObj.put("severity", severity);
            dataObj.put("category", category);
            dataObj.put("location", location);
            dataObj.put("text", remarks);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = "http://hjd23547:8181/Dashboard/rest/cnt/updateFeedback";
        String data = dataObj.toString();
        SubmitFeedback s = new SubmitFeedback();
        s.execute(url, data);

        Toast.makeText(getApplicationContext(), "Feedback sent!", Toast.LENGTH_SHORT).show();

        values.putExtra("severity", severity);
        values.putExtra("category", category);
        values.putExtra("remarks", remarks);
        setResult(RESULT_OK, values);
        this.finish();
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

    public void remarksEditButton(View view) {
        remarksEdit.setEnabled(true);
    }

    public void categoryEditButton(View view) {
        categoryEdit.setEnabled(true);
    }

    public void severityEditButton(View view) {
        severityEdit.setEnabled(true);
    }

}