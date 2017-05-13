package com.techgig.wallet;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class OfflineSendFragment extends Fragment {

    private EditText amount;
    private EditText password;
    private Button continueButton;


    public OfflineSendFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_offline_send, container, false);

        amount = (EditText) v.findViewById(R.id.offlineAmount);
        password = (EditText) v.findViewById(R.id.offlineSendPassword);
        continueButton = (Button) v.findViewById(R.id.offlineSendButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

        return v;
    }

    private void validate() {

        String money = amount.getText().toString();
        String retrivedPassword = password.getText().toString();
        if(money.equals("") || money.equals("0")){
            Toast.makeText(getContext(), "Please enter a amount.", Toast.LENGTH_LONG).show();
            return;
        }
        if(retrivedPassword.equals("")){
            Toast.makeText(getContext(), "Please provide your password.", Toast.LENGTH_LONG).show();
            return;
        }

        String hashedPassword = Util.hashedPassword(retrivedPassword);
        SessionManager sessionManager = new SessionManager(getContext());
        HashMap<String, String> map = sessionManager.getUserDetails();
        if (hashedPassword.equals(map.get(SessionManager.KEY_WALLETPASSWORD))){
            QRGeneratorFragment fragment = new QRGeneratorFragment();
            Bundle args = new Bundle();
            args.putString(QRGeneratorFragment.KEY_AMOUNT, money);
            fragment.setArguments(args);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.content_body, fragment);
            fragmentTransaction.commit();
        } else {
            Toast.makeText(getContext(),"Invalid Password", Toast.LENGTH_LONG).show();
        }

    }

}
