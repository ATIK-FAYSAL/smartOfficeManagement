package com.atikfaysal.smartofficemanagement.others;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.background.StoreDataInSharedPref;
import com.atikfaysal.smartofficemanagement.interfaces.ImplementedMethods;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;


public class MyQrCode extends AppCompatActivity implements ImplementedMethods, View.OnClickListener
{
    private final static int QRcodeWidth = 500 ;
    private static final String IMAGE_DIRECTORY = "/MyQrCode";
    private Bitmap bitmap;

    private StoreDataInSharedPref sharedPref;
    private ImageView imgQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_qr_code);
        initComponent();
        setToolbar();
        userQrCode();
    }

    //initialize all components
    @Override
    public void initComponent() {

        imgQrCode = findViewById(R.id.imgMyQrCode);
        TextView txtScanView = findViewById(R.id.txtScan);
        Button saveCode = findViewById(R.id.bSave);
        txtScanView.setOnClickListener(this);
        saveCode.setOnClickListener(this);

        sharedPref = new StoreDataInSharedPref(this);
    }

    //display user qr code to imageView
    private void userQrCode()
    {
        try {
            bitmap = TextToImageEncode(createUserInfoJson());//convert user information to qr code
            if(bitmap!=null)
            {
                imgQrCode.setImageBitmap(bitmap);//set user qr code
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    //set toolbar
    @Override
    public void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.icon_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //pass user information into a json
    private String createUserInfoJson()
    {
        JSONObject object = new JSONObject();

        try {
            object.put("id",sharedPref.getUserId());//current user id
            object.put("name",sharedPref.getUserName());//current user name
            object.put("phone",sharedPref.getPhoneNumber());//current user phone number
            object.put("email",sharedPref.getEmail());//current user email
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString();//return json object as string

    }

    @Override
    public void processJsonData(String jsonData) {}

    //convert json array to string
    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }


    //save user qr code to gallery
    public void saveImage(Bitmap myBitmap) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        String root = Environment.getExternalStorageDirectory().toString();
        File wallpaperDirectory = new File(root);
        // have the object build the directory structure, if needed.

        if (!wallpaperDirectory.exists())
            wallpaperDirectory.mkdirs();

        try {
            File file = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            file.createNewFile();   //give read write permission
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{file.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();

            Toast.makeText(MyQrCode.this,"QR code saved to your storage",Toast.LENGTH_LONG).show();

        } catch (IOException e1) {
            Log.d("error", e1.toString());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.txtScan://scan qr code
                Intent intent = new Intent(MyQrCode.this,ScanQrCode.class);//scan qr code
                intent.putExtra("type","company");
                startActivity(intent);
                break;

            case R.id.bSave:

                if(ContextCompat.checkSelfPermission(MyQrCode.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(MyQrCode.this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                else saveImage(bitmap);//save image to gallery

                break;
        }
    }
}