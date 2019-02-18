package com.atikfaysal.smartofficemanagement.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.background.CheckInternetConnection;
import com.atikfaysal.smartofficemanagement.background.ServerConnection;
import com.atikfaysal.smartofficemanagement.background.StoreDataInSharedPref;
import com.atikfaysal.smartofficemanagement.interfaces.ImplementedMethods;
import com.atikfaysal.smartofficemanagement.interfaces.OnResponseTask;
import com.atikfaysal.smartofficemanagement.others.DataValidation;
import com.atikfaysal.smartofficemanagement.others.RequiredMethods;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.lang.Thread.sleep;

public class CompanyProfile extends AppCompatActivity implements
        ImplementedMethods, View.OnClickListener
{


    private EditText inputName,inputEmail,inputPhone,inputAddress,inputWebsite,inputDescription;
    private Button bSavceChanges,bSave;
    private CircleImageView imgProPic;
    private TextView txtChoose,txtEdit,txtAdmin;
    private ImageView imgQrCode;
    private RelativeLayout relativeLayout;



    private Button bCreateCompany;

    private StoreDataInSharedPref sharedPref;
    private CheckInternetConnection internetConnection;
    private RequiredMethods requiredMethods;
    private DataValidation validation;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_company);
        initComponent();
        setToolbar();
        gettingCompanyInformation();
    }


    @Override
    protected void onStart() {
        super.onStart();

        if(sharedPref.getUserType().equals("admin") || sharedPref.getUserType().equals("employee"))
            bCreateCompany.setVisibility(View.GONE);//if  company available then create button disable,invisible create new company button
        else relativeLayout.setVisibility(View.GONE);//if no company available then create button enable
    }

    @Override
    public void initComponent() {

        bCreateCompany = findViewById(R.id.bCreateCompany);
        bSave = findViewById(R.id.bSave);
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputAddress = findViewById(R.id.inputAddress);
        inputPhone = findViewById(R.id.inputPhone);
        inputDescription = findViewById(R.id.inputDescription);
        inputWebsite = findViewById(R.id.inputWebsite);
        txtAdmin = findViewById(R.id.txtAdmin);
        txtEdit = findViewById(R.id.txtEdit);
        imgQrCode = findViewById(R.id.imgQrCode);
        txtChoose = findViewById(R.id.txtChoose);
        relativeLayout = findViewById(R.id.relativeLayoutB);

        enableDisableComponent(false);//disable editable component
        bSave.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_disable_button));//button disable for the first time

        //set click listener
        bCreateCompany.setOnClickListener(this);
        txtEdit.setOnClickListener(this);
        txtChoose.setOnClickListener(this);
        bSave.setOnClickListener(this);
        imgQrCode.setOnClickListener(this);

        sharedPref = new StoreDataInSharedPref(this);
        internetConnection = new CheckInternetConnection(this);
        requiredMethods = new RequiredMethods(this);
        validation = new DataValidation();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("Please wait");
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.bCreateCompany://create new company
                startActivity(new Intent(CompanyProfile.this,CompanyRegistration.class));//create new company
                break;

            case R.id.txtEdit:
                enableDisableComponent(true);//enable all component
                bSave.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_button));//button disable for the first time
                txtChoose.setVisibility(View.VISIBLE);//visible choose image button
                break;

            case R.id.txtChoose:
                break;

            case R.id.imgQrCode:
                break;

            case R.id.bSave:

                if(internetConnection.isOnline())//internet is enable
                    updateCompanyInformation();//update compnay info
                else requiredMethods.errorMessage(getResources().getString(R.string.offline));//if internet is disable

                break;
        }
    }

    //getting company information from server
    private void gettingCompanyInformation()//getting company information from server
    {
        if(internetConnection.isOnline())//if internet connection is enable
        {
            Map<String,String> map = new HashMap<>();

            if(!sharedPref.getCompanyId().equals("none"))
            {
                map.put("option","comInfo");//passing case
                map.put("comId",sharedPref.getCompanyId());//passing company id

                ServerConnection serverConnection = new ServerConnection(this,responseTask);//create connection object
                serverConnection.serverResponse(getResources().getString(R.string.readInfo),map);//passing a request to server
                progressDialog.show();
            }else requiredMethods.errorMessage(getResources().getString(R.string.unknown));

        }else requiredMethods.errorMessage(getResources().getString(R.string.offline));//if internet is disable
    }

    //enable & disable component
    private void enableDisableComponent(boolean flag)
    {
        bSave.setEnabled(flag);
        inputName.setEnabled(flag);
        inputEmail.setEnabled(flag);
        inputPhone.setEnabled(flag);
        inputAddress.setEnabled(flag);
        inputWebsite.setEnabled(flag);
        inputDescription.setEnabled(flag);
    }


    //update company information
    private void updateCompanyInformation()
    {
        Map<String,String> map = new HashMap<>();

        if(!TextUtils.isEmpty(inputName.getText().toString()) && validation.emailValidation(inputEmail.getText().toString())
        && validation.phoneNumberValidation(inputPhone.getText().toString()) && validation.linkValidation(inputWebsite.getText().toString()) &&
        !TextUtils.isEmpty(inputAddress.getText().toString()) && !TextUtils.isEmpty(inputDescription.getText().toString()))
        {

            map.put("option","updateCompany");//passing case
            map.put("comName",inputName.getText().toString());//passing company name
            map.put("comEmail",inputEmail.getText().toString());//passing company email
            map.put("comPhone",inputPhone.getText().toString());//passing company phone
            map.put("comAddress",inputAddress.getText().toString());//passing company address
            map.put("website",inputWebsite.getText().toString());//passing company website
            map.put("description",inputDescription.getText().toString());//passing company description
            map.put("longitude","10.10.30.10");//passing company address
            map.put("latitude","10.10.30.10");//passing company website
            map.put("category","Information technology");//passing company description

            ServerConnection serverConnection = new ServerConnection(this,onResponseTask);
            serverConnection.serverResponse(getResources().getString(R.string.updateInfo),map);
            progressDialog.show();


        }else requiredMethods.errorMessage(getResources().getString(R.string.invalid));

    }

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

    @Override
    public void processJsonData(String jsonData)
    {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonObject.optJSONArray("comInfo");

            JSONObject object = jsonArray.getJSONObject(0);
            String name,email,phone,description,address,website,admin,category;

            name = object.getString("comName");//getting company name
            email = object.getString("comEmail");//getting company name
            phone = object.getString("comPhone");//getting company name
            description = object.getString("description");//getting company name
            address = object.getString("comAddress");//getting company name
            website = object.getString("website");//getting company name
            admin = object.getString("admin");//getting company name
            category = object.getString("category");//getting company category

            inputName.setText(name);
            inputEmail.setText(email);
            inputPhone.setText(phone);
            inputDescription.setText(description);
            inputAddress.setText(address);
            inputWebsite.setText(website);
            txtAdmin.setText(admin);

        } catch (JSONException e) {
            e.printStackTrace();
        }finally {
            progressDialog.dismiss();
        }

    }

    //getting update information response
    private OnResponseTask onResponseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(final String value) {

            try {
                sleep(getResources().getInteger(R.integer.progTime));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progressDialog.dismiss();//dismiss progress dialog
                        if(value.equals("success"))//if update successful
                        {
                            Toast.makeText(CompanyProfile.this,"Company information update successfully",Toast.LENGTH_LONG).show();
                        }else
                        {
                            requiredMethods.errorMessage(getResources().getString(R.string.unknown));
                        }

                    }
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    };

    //getting information response
    private OnResponseTask responseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(String value) {
            processJsonData(value);
        }
    };
}
