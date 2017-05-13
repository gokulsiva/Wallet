package com.techgig.wallet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.HashMap;


public class RechargeFragment extends Fragment {


    //The views
    private Button buttonPay;
    private EditText editTextAmount;
    private View rechargeProgress;
    private View rechargeForm;

    //Payment Amount
    private String paymentAmount;

    //Paypal intent request code to track onActivityResult method
    public static final int PAYPAL_REQUEST_CODE = 123;

    private static String URL = Util.BASE_URL+"/wallet/UpdateAmount";


    //Paypal Configuration Object
    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready, switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getContext(), PayPalService.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        getActivity().startService(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_recharge, container, false);

        buttonPay = (Button) v.findViewById(R.id.buttonPay);
        editTextAmount = (EditText) v.findViewById(R.id.editTextAmount);
        rechargeForm = (View) v.findViewById(R.id.recharge_form);
        rechargeProgress = (View) v.findViewById(R.id.recharge_progress);

        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPayment();
            }
        });

        return v;
    }

    @Override
    public void onDestroy() {
        getActivity().stopService(new Intent(getContext(), PayPalService.class));
        super.onDestroy();
    }

    private void getPayment() {

        //Getting the amount from editText
        paymentAmount = editTextAmount.getText().toString();

        //Creating a paypalpayment
        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(paymentAmount)), "USD", "Transfer to wallet",
                PayPalPayment.PAYMENT_INTENT_SALE);

        //Creating Paypal Payment activity intent
        Intent intent = new Intent(getContext(), PaymentActivity.class);

        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        //Puting paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        //Starting the intent activity for result
        //the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If the result is from paypal
        if (requestCode == PAYPAL_REQUEST_CODE) {

            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                //Getting the payment confirmation
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                //if confirmation is not null
                if (confirm != null) {
                    try {
                        //Getting the payment details
                        showProgress(true);
                        String paymentDetails = confirm.toJSONObject().toString(4);
                        Log.i("paymentExample", paymentDetails);
                        Log.i("PaymentAmount", paymentAmount);



                        final HashMap<String, String> map = new SessionManager(getContext()).getUserDetails();
                        HashMap<String, String> params = new HashMap<String, String>(1);
                        params.put("userId", map.get(SessionManager.KEY_USERID));
                        params.put("amount", paymentAmount);

                        //Volley Request
                        VolleyRequest volleyRequest = new VolleyRequest(getContext(), URL, params);
                        volleyRequest.jsonStringRequest(new VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                showProgress(false);
                                if(result.equalsIgnoreCase("success")){
                                    //update shared preference balance

                                    long newBalance = Long.parseLong(paymentAmount)+Long.parseLong(map.get(SessionManager.KEY_BALANCE));
                                    Log.v("Non updated preference:", map.get(SessionManager.KEY_BALANCE));
                                    SessionManager sessionManager = new SessionManager(getContext());
                                    sessionManager.updatePreference(SessionManager.KEY_BALANCE, new Long(newBalance).toString());
                                    Log.v("Updated preference : ", new SessionManager(getContext()).getUserDetails().get(SessionManager.KEY_BALANCE));
                                    editTextAmount.setText("");
                                    Toast.makeText(getContext(),"Successfully updated wallet", Toast.LENGTH_LONG).show();

                                } else {
                                    Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                        Toast.makeText(getContext(), "Error occured", Toast.LENGTH_LONG).show();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
                Toast.makeText(getContext(), "Payment cancelled.", Toast.LENGTH_LONG).show();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
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

            rechargeForm.setVisibility(show ? View.GONE : View.VISIBLE);
            rechargeForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rechargeForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            rechargeProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            rechargeProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rechargeProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            rechargeProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            rechargeForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


}
