package com.techgig.wallet;


import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.util.HashMap;

import static android.content.Context.WINDOW_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class QRGeneratorFragment extends Fragment {

    private String LOG_TAG = "GenerateQRCode";
    private ImageView imageView;
    private String amount;
    private String qrInputText;

    public static final String KEY_AMOUNT = "amount";


    public QRGeneratorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        amount = bundle.getString(KEY_AMOUNT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_qrgenerator, container, false);
        imageView = (ImageView) v.findViewById(R.id.QRGeneratorImage);
        generateQR(v);
        return v;
    }

    private void generateQR(View v) {

        Log.v("Amount : ", amount);
        SessionManager sessionManager = new SessionManager(getContext());
        HashMap<String, String> map = sessionManager.getUserDetails();
        String senderId = map.get(SessionManager.KEY_USERID);
        String senderWalletPassword = map.get(SessionManager.KEY_WALLETPASSWORD);
        qrInputText = getActivity().getPackageName()+":"+senderId+":"+senderWalletPassword+":"+amount;
        Log.v(LOG_TAG, qrInputText);
        //Find screen size
        WindowManager manager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3/4;

        //Encode with a QR Code image
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrInputText,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                smallerDimension);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            imageView.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }



    }

}
