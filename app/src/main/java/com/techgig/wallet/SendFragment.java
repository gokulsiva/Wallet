package com.techgig.wallet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class SendFragment extends Fragment {

    private View progress;
    private View form;
    private EditText receiverId;
    private EditText walletPassword;
    private EditText amount;
    private Button send;

    private String URL = Util.BASE_URL+"/wallet/UpdateWallet";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_send, container, false);

        final HashMap<String, String> map = new SessionManager(getContext()).getUserDetails();

        progress = (View) v.findViewById(R.id.send_password);
        form = (View) v.findViewById(R.id.send_form);

        receiverId = (EditText) v.findViewById(R.id.send_receiverId);
        walletPassword = (EditText) v.findViewById(R.id.send_password);
        amount = (EditText) v.findViewById(R.id.send_amount);
        send = (Button) v.findViewById(R.id.send_sendButton);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(map);
            }
        });


        return v;
    }


    private void sendData(HashMap<String, String> map){

        String sender = map.get(SessionManager.KEY_USERID);
        String receiver = receiverId.getText().toString().trim();
        String senderPassword = walletPassword.getText().toString().trim();
        final long money = Long.parseLong(amount.getText().toString());
        final long actualBalance = Long.parseLong(map.get(SessionManager.KEY_BALANCE));

        if(receiver.equals("") || senderPassword.equals("") || money == 0){
            Toast.makeText(getContext(), "Please fill all the fields", Toast.LENGTH_LONG).show();
            return;
        }

        if(money > actualBalance){
            Toast.makeText(getContext(), "Insufficient balance", Toast.LENGTH_LONG).show();
            return;
        }

        String hashedPassword = Util.hashedPassword(senderPassword);

        if(!(map.get(SessionManager.KEY_WALLETPASSWORD).equals(hashedPassword))){
            Toast.makeText(getContext(), "Invalid Password.", Toast.LENGTH_LONG).show();
            return;
        }

        HashMap<String, String> params = new HashMap<String, String>(1);
        params.put(Util.KEY_SENDER_ID, sender);
        params.put(Util.KEY_SENDER_PASSWORD, hashedPassword);
        params.put(Util.KEY_RECEIVER_ID, receiver);
        params.put(Util.KEY_AMOUNT, new Long(money).toString());


        showProgress(true);

        VolleyRequest volleyRequest = new VolleyRequest(getContext(), URL, params);
        volleyRequest.jsonStringRequest(new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                showProgress(false);
                JSONObject jsonObject = null;
                String transactionStatus = "";
                try {
                    jsonObject = new JSONObject(result);
                    transactionStatus = jsonObject.getString("Transaction");
                    if(transactionStatus.equalsIgnoreCase("success")){

                        Toast.makeText(getContext(), "Transaction success.", Toast.LENGTH_LONG).show();
                        long newBalance = actualBalance - money;
                        SessionManager sessionManager = new SessionManager(getContext());
                        sessionManager.updatePreference(SessionManager.KEY_BALANCE, Long.toString(newBalance));
                    } else {
                        Toast.makeText(getContext(), "Transaction failed please retry.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                }
            }
        });

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
