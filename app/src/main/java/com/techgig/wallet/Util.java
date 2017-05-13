package com.techgig.wallet;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by GokulSiva on 11-05-2017.
 */

public class Util {

    public static final String BASE_URL = "http://"+"192.168.1.109"+":5000";

    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_SENDER_PASSWORD = "senderPassword";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_AMOUNT = "amount";


    public static final String hashedPassword(String password){

        String hash = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(password.getBytes());
            hash = new BigInteger(1, messageDigest.digest()).toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("Unable to set this password.");
        }
        return hash;
    }

    public static final boolean isInternetOn(Activity activity) {

        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(activity.getBaseContext().CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
                connectivityManager.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING) {
            return true;
        } else if (
                connectivityManager.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||
                        connectivityManager.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED ){
            return false;
        }

        return false;
    }

}
