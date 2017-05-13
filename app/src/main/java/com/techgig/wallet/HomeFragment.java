package com.techgig.wallet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;


public class HomeFragment extends Fragment implements View.OnClickListener {


    private TextView greeting;
    private TextView userId;
    private TextView balance;
    private Button sendButton;
    private Button receiveButton;
    private Button rechargeButton;

    private View progress;
    private View form;
    private Button refreshButton;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        HashMap<String, String> map = new SessionManager(getContext()).getUserDetails();

        greeting = (TextView) v.findViewById(R.id.home_greeting);
        greeting.setText("Welcome "+map.get(SessionManager.KEY_NAME));
        userId = (TextView) v.findViewById(R.id.home_userId);
        userId.setText("Your ID : "+map.get(SessionManager.KEY_USERID));
        balance = (TextView) v.findViewById(R.id.home_balance);
        balance.setText("Balance : "+map.get(SessionManager.KEY_BALANCE));
        sendButton = (Button) v.findViewById(R.id.home_send_button);
        sendButton.setOnClickListener(this);
        receiveButton = (Button) v.findViewById(R.id.home_receive_button);
        receiveButton.setOnClickListener(this);
        rechargeButton = (Button) v.findViewById(R.id.home_recharge_button);

        form = (View) v.findViewById(R.id.home_form);
        progress = (View) v.findViewById(R.id.home_progress);

        refreshButton = (Button) v.findViewById(R.id.home_refresh);
        refreshButton.setOnClickListener(this);

        rechargeButton.setOnClickListener(this);

        return v;
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();
        Fragment fragment = null;

        switch (id){
            case R.id.home_send_button:
                if(Util.isInternetOn(getActivity())){
                    fragment = new SendFragment();
                } else {
                    fragment = new OfflineSendFragment();
                }
                break;
            case R.id.home_receive_button:
                fragment = new ReceiveFragment();
                break;
            case R.id.home_recharge_button:
                fragment = new RechargeFragment();
                break;
            case R.id.home_refresh:
                refreshBalance();
                break;
        }

        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.content_body, fragment);
            fragmentTransaction.commit();
        }

    }

    private void refreshBalance() {
        HashMap<String, String> map = new SessionManager(getContext()).getUserDetails();
        String userId = map.get(SessionManager.KEY_USERID);

        HashMap<String, String> param = new HashMap<>(1);
        param.put("userId", userId);

        String URL = Util.BASE_URL+"/wallet/GetWallet";

        showProgress(true);

        VolleyRequest volleyRequest = new VolleyRequest(getContext(), URL, param);
        volleyRequest.jsonStringRequest(new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                showProgress(false);
                try {
                    long amount = Long.parseLong(result);
                    SessionManager sessionManager = new SessionManager(getContext());
                    sessionManager.updatePreference(SessionManager.KEY_BALANCE, Long.toString(amount));
                    balance.setText(Long.toString(amount));
                    Toast.makeText(getContext(), "Successfully updated", Toast.LENGTH_LONG).show();
                } catch (Exception e){
                    Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        HashMap<String, String> map = new SessionManager(getContext()).getUserDetails();
        balance.setText(map.get(SessionManager.KEY_BALANCE));
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
