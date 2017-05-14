package com.techgig.wallet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ReceiverReceipt extends AppCompatActivity {

    public static final String RequestArg = "arg";

    private View progress;
    private View form;
    private TextView finalStatus;
    private Button okButton;
    private String result;

    private OfflineTransactionDAO dao;

    private String URL = Util.BASE_URL+"/wallet/UpdateWallet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_receipt);
        progress = (View) findViewById(R.id.receiverReceipt_progress);
        form = (View) findViewById(R.id.receiverReceipt_form);
        finalStatus = (TextView) findViewById(R.id.receiverReceipt_textView);
        okButton = (Button) findViewById(R.id.receiverReceipt_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReceiverReceipt.this, MainActivity.class);
                finish();
                startActivity(intent);
            }
        });
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            result = bundle.getString(RequestArg);
            updateWallet();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ReceiverReceipt.this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    private void updateWallet() {

        String[] array = result.split(":");
        if (array.length > 4 || array.length < 4) {
            Toast.makeText(getApplicationContext(), "Invalid QRCode", Toast.LENGTH_LONG).show();
            return;
        }

        HashMap<String, String> map = new SessionManager(getApplicationContext()).getUserDetails();

        String packageName = array[0];
        String sender = array[1];
        String hashedPassword = array[2];
        String money = array[3];
        String receiver = map.get(SessionManager.KEY_USERID);

        final long actualBalance = Long.parseLong(map.get(SessionManager.KEY_BALANCE));
        final long receivedAmount = Long.parseLong(money);

        if (!this.getPackageName().equals(packageName)) {
            Toast.makeText(getApplicationContext(), "Invalid QRCode", Toast.LENGTH_LONG).show();
            return;
        }

        if (Util.isInternetOn(this)) {

            Toast.makeText(this, "Connection Available", Toast.LENGTH_SHORT).show();

            final HashMap<String, String> paramsMap = new HashMap<String, String>(1);
            paramsMap.put(Util.KEY_SENDER_ID, sender);
            paramsMap.put(Util.KEY_SENDER_PASSWORD, hashedPassword);
            paramsMap.put(Util.KEY_RECEIVER_ID, receiver);
            paramsMap.put(Util.KEY_AMOUNT, new Long(money).toString());

            showProgress(true);

            VolleyRequest volleyRequest = new VolleyRequest(getApplicationContext(), URL, paramsMap);
            volleyRequest.jsonStringRequest(new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    showProgress(false);
                    JSONObject jsonObject = null;
                    String transactionStatus = "";
                    try {
                        jsonObject = new JSONObject(result);
                        transactionStatus = jsonObject.getString("Transaction");
                        if (transactionStatus.equalsIgnoreCase("success")) {

                            //Toast.makeText(getApplicationContext(), "Transaction success.", Toast.LENGTH_LONG).show();
                            finalStatus.setText("Transaction success.");
                            long newBalance = actualBalance + receivedAmount;
                            SessionManager sessionManager = new SessionManager(getApplicationContext());
                            sessionManager.updatePreference(SessionManager.KEY_BALANCE, Long.toString(newBalance));
                        } else {
                            //Toast.makeText(getApplicationContext(), "Transaction failed please retry.", Toast.LENGTH_LONG).show();
                            finalStatus.setText("Transaction failed please retry.");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        finalStatus.setText("Transaction failed please retry.");
                    }
                }
            });


        } else {

            //Offline actions
            //Toast.makeText(this, "Added to offline", Toast.LENGTH_LONG).show();

            dao = new OfflineTransactionDAO(getApplicationContext());
            dao.open();
            OfflineTransaction transaction = dao.createOfflineTransaction(map.get(SessionManager.KEY_USERID), sender, hashedPassword, receiver, money);
            if(transaction.getId() != 0){
                finalStatus.setText("Transaction successfully added to offline.");
            } else {
                finalStatus.setText("Transaction to offline failed.\nTransaction id : "+transaction.getId());
            }

        }


    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            form.setVisibility(show ? View.GONE : View.VISIBLE);
            form.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    form.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            form.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }



}
