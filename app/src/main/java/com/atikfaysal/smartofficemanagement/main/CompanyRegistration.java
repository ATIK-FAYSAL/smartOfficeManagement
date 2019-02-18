package com.atikfaysal.smartofficemanagement.main;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.background.CheckInternetConnection;
import com.atikfaysal.smartofficemanagement.background.ServerConnection;
import com.atikfaysal.smartofficemanagement.background.StoreDataInSharedPref;
import com.atikfaysal.smartofficemanagement.interfaces.ImplementedMethods;
import com.atikfaysal.smartofficemanagement.interfaces.OnResponseTask;
import com.atikfaysal.smartofficemanagement.others.DataValidation;
import com.atikfaysal.smartofficemanagement.others.RequiredMethods;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class CompanyRegistration extends AppCompatActivity implements ImplementedMethods, View.OnClickListener
{
    private EditText inputComName,inputComEmail,inputComPhone,inputComAddress,inputComWebsite,inputComDescription;
    private Spinner spinnerCategory;
    private Button bCreate;

    private CheckInternetConnection internetConnection;
    private StoreDataInSharedPref storeDataInSharedPref;
    private DataValidation dataValidation;
    private RequiredMethods methods;
    private ProgressDialog progressDialog;

    private String comName,comEmail,comPhone,comAddress,comWebsite,comDescription,comCategory,longitude = "10.10.10.10",latitude="10.10.10.10";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_company_registration);
        initComponent();
        setToolbar();
        spinnerLocation();
    }


    //initialize component
    @Override
    public void initComponent() {

        bCreate = findViewById(R.id.bCreate);
        inputComName = findViewById(R.id.inputComName);
        inputComEmail = findViewById(R.id.inputComEmail);
        inputComPhone = findViewById(R.id.inputComPhone);
        inputComAddress = findViewById(R.id.inputComAddress);
        inputComWebsite = findViewById(R.id.inputComWebsite);
        inputComDescription = findViewById(R.id.inputDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);

        bCreate.setOnClickListener(this);

        storeDataInSharedPref = new StoreDataInSharedPref(this);
        internetConnection = new CheckInternetConnection(this);
        methods = new RequiredMethods(this);
        dataValidation = new DataValidation();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");//set dialog title
        progressDialog.setMessage("Creating new company");//set dialog message
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
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.bCreate:

                if(internetConnection.isOnline())//if internet is enable
                {
                    createNewCompany();//create new company
                }else methods.errorMessage(getResources().getString(R.string.offline));//if internet is disable

                break;
        }
    }

    //create new company
    private void createNewCompany()
    {

        comName = inputComName.getText().toString();
        comEmail = inputComEmail.getText().toString();
        comPhone = inputComPhone.getText().toString();
        comAddress = inputComAddress.getText().toString();
        comWebsite = inputComWebsite.getText().toString();
        comDescription = inputComDescription.getText().toString();


        Map<String,String> map = new HashMap<>();

        if(!TextUtils.isEmpty(comName) && dataValidation.emailValidation(comEmail) &&
                dataValidation.phoneNumberValidation(comPhone) && !TextUtils.isEmpty(comAddress) &&
                dataValidation.linkValidation(comWebsite) && !TextUtils.isEmpty(comDescription) &&
                !TextUtils.isEmpty(longitude) && !TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(comCategory))
        {
            map.put("option","createCompany");//passing option
            map.put("createdBy",storeDataInSharedPref.getUserId());//create use id/current user id
            map.put("comName",comName);//passing company name
            map.put("comEmail",comEmail);//passing company email
            map.put("comPhone",comPhone);//passing company email
            map.put("comAddress",comAddress);//passing company email
            map.put("website",comWebsite);//passing company email
            map.put("category",comCategory);//passing company email
            map.put("longitude",longitude);//passing company email
            map.put("latitude",latitude);//passing company email
            map.put("description",comDescription);//passing company email
            map.put("date",methods.getDateWithTime());//passing current date and time

            ServerConnection serverConnection = new ServerConnection(this,onResponseTask);//create connection object
            serverConnection.serverResponse(getResources().getString(R.string.insert),map);//passing new request
            progressDialog.show();//display progress dialog

        }else methods.errorMessage(getResources().getString(R.string.invalid));//if information is not valid
    }

    //select company category
    protected void spinnerLocation()
    {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.comCategory,R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                comCategory = parent.getItemAtPosition(i).toString();//getting selected item
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }


    @Override
    public void processJsonData(String jsonData) { }

    //create company server response
    private OnResponseTask onResponseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(final String value) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(getResources().getInteger(R.integer.progTime));
                    } catch (Exception e) {
                        Log.d("error",e.toString());
                    }
                    progressDialog.dismiss();
                }
            }).start();
            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                    if(value.equals("success"))
                    {
                        Toast.makeText(CompanyRegistration.this,"Company created successfully",Toast.LENGTH_LONG).show();
                        methods.closeActivity(CompanyRegistration.this,Dashboard.class);//go to dashboard
                    }
                    else methods.errorMessage("");//if company creation failed
                }
            });
        }
    };
}
