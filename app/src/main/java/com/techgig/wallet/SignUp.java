package com.techgig.wallet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    private View progressView;
    private View signUpFormView;
    private EditText userName;
    private EditText userEmail;
    private EditText userAccountPassword;
    private EditText reUserAccountPassword;
    private EditText userWalletPassword;
    private EditText reUserWalletPassword;
    private Button signUp;
    private Button signIn;

    private String URL = Util.BASE_URL+"/wallet/CreateUser";
    private HashMap<String, String > params = new HashMap<String, String>(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        progressView = (View) findViewById(R.id.signUp_progress);
        signUpFormView = (View) findViewById(R.id.signUp_form);

        userName = (EditText) findViewById(R.id.userName);
        userEmail = (EditText) findViewById(R.id.userEmail);
        userAccountPassword = (EditText) findViewById(R.id.userPassword);
        reUserAccountPassword = (EditText) findViewById(R.id.reUserPassword);
        userWalletPassword = (EditText) findViewById(R.id.userWalletPassword);
        reUserWalletPassword = (EditText) findViewById(R.id.reWalletPassword);

        signUp = (Button) findViewById(R.id.userSignUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser(SignUp.this);
            }
        });

        signIn = (Button) findViewById(R.id.userLogIn);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    private void createUser(final Context context){

        showProgress(true);
        String username = userName.getText().toString();
        String useremail = userEmail.getText().toString();
        String password = userAccountPassword.getText().toString();
        String repassword = reUserAccountPassword.getText().toString();
        String walletpassword = userWalletPassword.getText().toString();
        String rewalletpassword = reUserWalletPassword.getText().toString();

        Log.v("userName", username);
        Log.v("userEmail", useremail);
        Log.v("password", password);
        Log.v("repassword", repassword);
        Log.v("walletpassword", walletpassword);
        Log.v("rewalletpassword", rewalletpassword);

        if((username.equals("") || useremail.equals("") || password.equals("") || repassword.equals("") || walletpassword.equals("") || rewalletpassword.equals(""))){
            showProgress(false);
            Toast.makeText(context, "Invalid Credentials please fill all the fields.", Toast.LENGTH_LONG).show();
        } else if(!(password.equals(repassword) || walletpassword.equals(rewalletpassword))){
            showProgress(false);
            Toast.makeText(context, "Passwords doesn't match.", Toast.LENGTH_LONG).show();
        } else {
            params.put("name", username);
            params.put("email", useremail);
            params.put("password", password);
            params.put("walletPassword", walletpassword);
            VolleyRequest volleyRequest = new VolleyRequest(context, URL, params);
            volleyRequest.jsonStringRequest(new VolleyCallback() {
                @Override
                public void onSuccess(String result) {
                    showProgress(false);
                    if(result.equalsIgnoreCase("success")){
                        userName.setText("");
                        userEmail.setText("");
                        userAccountPassword.setText("");
                        reUserAccountPassword.setText("");
                        userWalletPassword.setText("");
                        reUserWalletPassword.setText("");
                        Toast.makeText(context, "Successfully created user account.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show();
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

            signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            signUpFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


}
