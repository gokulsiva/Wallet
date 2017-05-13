package com.techgig.wallet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ReceiveFragment extends Fragment implements ZXingScannerView.ResultHandler{

    private TextView scanResult;
    private Button scanButton;

    private View receiveProgress;
    private View receiveForm;
    private View resumeView;

    private ZXingScannerView mScannerView;



    private String json;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_receive, container, false);

        receiveForm = (View) v.findViewById(R.id.receive_form);
        receiveProgress = (View) v.findViewById(R.id.receive_progress);

        scanResult = (TextView) v.findViewById(R.id.scanResult);
        scanButton = (Button) v.findViewById(R.id.buttonScan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeView = v;
                QrScanner(v);
            }
        });

        return v;
    }


    public void QrScanner(View view){

        mScannerView = new ZXingScannerView(getContext()); // Programmatically initialize the scanner view
        getActivity().setContentView(mScannerView);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera(); // Start camera
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera(); // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
// Do something with the result here

        Log.e("handler", rawResult.getText()); // Prints scan results
        Log.e("handler", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode)
        String result = rawResult.getText();

// show the scanner result into dialog box.
        //Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(getContext(), ReceiverReceipt.class);
        intent.putExtra(ReceiverReceipt.RequestArg,result);
        mScannerView.stopCamera();
        getActivity().finish();
        startActivity(intent);
    }



    }
