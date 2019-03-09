package com.starthack2019.ReCupVendor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.starthack2019.ReCupVendor.barcode.BarcodeCaptureActivity;

import java.lang.InterruptedException;

import cz.msebera.android.httpclient.Header;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String ButtonClicked = "";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int BARCODE_READER_REQUEST_CODE = 1;
    private TextView mResultTextView;
    private String UserBarcode = "";
    private String CupBarcode = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //StrictMode.ThreadPolicy policy = StrictMode.ThreadPolicy.LAX;
        //StrictMode.setThreadPolicy(policy);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Button Remove_btn = (Button) findViewById(R.id.scan_cup2remove_barcode_button);
        Remove_btn.setOnClickListener(this);
        Button ScanUserBarcode_btn = (Button) findViewById(R.id.scan_user_barcode_button);
        ScanUserBarcode_btn.setOnClickListener(this);
        Button ScanCupBarcode_btn = (Button) findViewById(R.id.scan_cup_barcode_button);
        ScanCupBarcode_btn.setOnClickListener(this);
        Button Execute_btn = (Button) findViewById(R.id.execute_button);
        Execute_btn.setOnClickListener(this);
        mResultTextView = (TextView) findViewById(R.id.result_textview);

    }

    @Override
    public void onClick(View v) {
        int delay = 100;
        switch (v.getId()) {

            case R.id.scan_cup2remove_barcode_button:
                ButtonClicked = "scan_cup2remove_barcode_button";
                Intent intent_cup_remove = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent_cup_remove, BARCODE_READER_REQUEST_CODE);
                break;

            case R.id.scan_cup_barcode_button:
                ButtonClicked = "scan_cup_barcode_button";
                Intent intent_cup = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent_cup, BARCODE_READER_REQUEST_CODE);
                break;

            case R.id.scan_user_barcode_button:
                ButtonClicked = "scan_user_barcode_button";
                Intent intent_user = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent_user, BARCODE_READER_REQUEST_CODE);
                break;

            case R.id.execute_button:
                // Do not execute if we have no values for our codes
                if(UserBarcode.isEmpty() && CupBarcode.isEmpty()) {
                    mResultTextView.setText("Scan both codes first");
                    break;
                }

                mResultTextView.setText(CupBarcode+" "+UserBarcode);

                // Start Client
                AsyncHttpClient client = new AsyncHttpClient();

                // Do the POST request: Send data to backend
                RequestParams params = new RequestParams();
                params.put("customerqrcode", UserBarcode);
                params.put("cupqrcode", CupBarcode);

                // Prepare POST request
                client.post("http://130.82.239.118:3000/transactions/create", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (statusCode == 201) {
                            mResultTextView.setText("Sent data successfully");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        mResultTextView.setText("Error while sending data.");
                    }
                });

                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Point[] p = barcode.cornerPoints;
                    mResultTextView.setText(barcode.displayValue);
                    if (ButtonClicked == "scan_cup_barcode_button") {
                        CupBarcode = mResultTextView.getText().toString();
                    } else if (ButtonClicked == "scan_user_barcode_button"){
                        UserBarcode = mResultTextView.getText().toString();
                    }
                } else mResultTextView.setText(R.string.no_barcode_captured);
            } else Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                    CommonStatusCodes.getStatusCodeString(resultCode)));
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop(){
        super.onStop();

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
}
