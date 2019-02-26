package com.atikfaysal.smartofficemanagement.others;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.background.CheckInternetConnection;
import com.atikfaysal.smartofficemanagement.background.ServerConnection;
import com.atikfaysal.smartofficemanagement.background.StoreDataInSharedPref;
import com.atikfaysal.smartofficemanagement.interfaces.OnResponseTask;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQrCode extends AppCompatActivity implements ZXingScannerView.ResultHandler
{
    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private ProgressDialog progressDialog;
    private RequiredMethods methods;
    private StoreDataInSharedPref sharedPref;
    private CheckInternetConnection internetConnection;
    private String addType;
    private DataValidation validation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Please wait");

        addType = Objects.requireNonNull(getIntent().getExtras()).getString("type");//getting add type from profile and company profile

        int currentApiVersion = Build.VERSION.SDK_INT;

        if(currentApiVersion >=  Build.VERSION_CODES.M)
        {
            if(!checkPermission())
            {
                requestPermission();
            }
        }

        methods = new RequiredMethods(this);
        internetConnection = new CheckInternetConnection(this);
        sharedPref = new StoreDataInSharedPref(this);
        validation = new DataValidation();
    }

    private boolean checkPermission()
    {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if(scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (!cameraAccepted){
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(ScanQrCode.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        final String jsonString = result.getText();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Map<String,String> map = new HashMap<>();

            String id,name,email,phone;

            id = jsonObject.getString("id");//getting id
            name = jsonObject.getString("name");//getting name
            email = jsonObject.getString("email");//getting email
            phone = jsonObject.getString("phone");//getting phone


            if(TextUtils.isEmpty(id) || !validation.nameValidation(name) && !validation.emailValidation(email) && !validation.phoneNumberValidation(phone))
            {
                methods.errorMessage("Information are not valid.Please contact to your admin");
                return;
            }

            switch (addType) {
                case "company"://if user try to add a company

                    map.put("option", "addEmployee");//passing employee
                    map.put("userId", sharedPref.getUserId());//passing user id
                    map.put("comId", id);//passing company id

                    break;

                case "employee"://if company try to add new company

                    map.put("option", "addEmployee");//passing employee
                    map.put("userId", id);//passing user id
                    map.put("comId", sharedPref.getCompanyId());//passing company id

                    break;

                default:
                    methods.errorMessage(getResources().getString(R.string.unknown));//if any error occur
                    break;
            }

            if(internetConnection.isOnline())//if internet is enable
            {
                ServerConnection serverConnection = new ServerConnection(this,onResponseTask);//create connection object
                serverConnection.serverResponse(getResources().getString(R.string.insert),map);//passing a request
                progressDialog.show();
            }else methods.errorMessage(getResources().getString(R.string.offline));//if internet is disable

        } catch (JSONException e) {
            methods.errorMessage(getResources().getString(R.string.unknown));//if any error occur
        }

    }


    //getting add new employee server response
    private OnResponseTask onResponseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(String value) {

            switch (value) {
                case "success"://if successfully add a new employee
                    methods.congratulationDialog();
                    sharedPref.setUserType("employee");//change user type as admin
                    break;
                case "exist"://if user already an employee
                    methods.errorMessage("This user is already an employee");
                    break;
                default://failed to add new employee
                    methods.errorMessage(getResources().getString(R.string.unknown));
                    break;
            }
            progressDialog.dismiss();//dismiss progress dialog

        }
    };


}
