package com.techgig.wallet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OfflineTransactionFragment extends Fragment {

    private OfflineTransactionDAO dao;
    private TextView claimableBalance;
    private TextView offlineFragmentResult;
    private Button claimButton;
    private View progress;
    private View form;

    private List<OfflineTransaction> values;
    private List<OfflineTransaction> failed = new ArrayList<>(1);

    private String URL = Util.BASE_URL+"/wallet/UpdateWallet";
    private JSONObject jsonObject;
    private String transactionStatus;

    public OfflineTransactionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dao = new OfflineTransactionDAO(getContext());
        dao.open();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_offline_transaction, container, false);
        form = (View) v.findViewById(R.id.offlineTransaction_form);
        progress = (View) v.findViewById(R.id.offlineTransaction_progress);
        claimableBalance = (TextView) v.findViewById(R.id.offlineTransactionBalance);
        claimButton = (Button) v.findViewById(R.id.offlineFragmentClaimButton);

        values = dao.getAllOfflineTransactions();

        long claimBalance = 0;
        for(OfflineTransaction o : values){
            claimBalance+=o.getAmount();
        }

        claimableBalance.setText(Long.toString(claimBalance));


        claimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Util.isInternetOn(getActivity())){
                    claim(v);
                } else {
                    Toast.makeText(getContext(), "Please connect to internet and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        dao.open();
        super.onResume();
    }

    @Override
    public void onPause() {
        dao.close();
        super.onPause();
    }


    private void claim(View v) {


        if(!Util.isInternetOn(getActivity())){
            Toast.makeText(getContext(), "Please connect to internet and try again.", Toast.LENGTH_LONG).show();
            return;
        }
        if(values.size() == 0){
            Toast.makeText(getContext(), "Nothing to claim", Toast.LENGTH_LONG).show();
            return;
        }

        showProgress(true);
        for(final OfflineTransaction transaction : values){

            final long money = transaction.getAmount();

            Log.v("Transaction", Long.toString(transaction.getId()));

            final HashMap<String, String> paramsMap = new HashMap<String, String>(1);
            paramsMap.put(Util.KEY_SENDER_ID, transaction.getSenderId());
            paramsMap.put(Util.KEY_SENDER_PASSWORD, transaction.getSenderPassword());
            paramsMap.put(Util.KEY_RECEIVER_ID, transaction.getReceiverId());
            paramsMap.put(Util.KEY_AMOUNT, new Long(money).toString());

            VolleyRequest volleyRequest = new VolleyRequest(getContext(), URL, paramsMap);
            volleyRequest.jsonStringRequest(new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    try {
                        jsonObject = new JSONObject(result);
                        transactionStatus = jsonObject.getString("Transaction");
                        if (transactionStatus.equalsIgnoreCase("success")) {

                            //Toast.makeText(getApplicationContext(), "Transaction success.", Toast.LENGTH_LONG).show();
                            long actualBalance = Long.parseLong(new SessionManager(getContext()).getUserDetails().get(SessionManager.KEY_BALANCE));
                            long newBalance = actualBalance + money;
                            SessionManager sessionManager = new SessionManager(getContext());
                            sessionManager.updatePreference(SessionManager.KEY_BALANCE, Long.toString(newBalance));
                            Log.v("newBalance", Long.toString(newBalance));

                        } else {
                            Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                            failed.add(transaction);
                        }
                    } catch (Exception e){
                        Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                        failed.add(transaction);
                    }
                }
            });


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