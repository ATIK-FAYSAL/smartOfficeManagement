package com.atikfaysal.smartofficemanagement.main;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.background.CheckInternetConnection;
import com.atikfaysal.smartofficemanagement.background.ServerConnection;
import com.atikfaysal.smartofficemanagement.background.StoreDataInSharedPref;
import com.atikfaysal.smartofficemanagement.interfaces.ImplementedMethods;
import com.atikfaysal.smartofficemanagement.interfaces.OnResponseTask;
import com.atikfaysal.smartofficemanagement.others.RequiredMethods;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/*
        * create new project
        * display list of all project
        * remove project
        *
     */

public class Projects extends AppCompatActivity implements
        ImplementedMethods, View.OnClickListener,DatePickerDialog.OnDateSetListener
{

    private RecyclerView recyclerView;
    private RelativeLayout emptyView,relativeLayoutA,relativeLayoutB;
    private Button bCreateNewProject,bProject;
    private RadioButton radioPrivate,radioPublic,radioPrioHigh,radioPrioMed,radioPrioLow;
    private EditText inputProName,inputProGoal,inputProDes;
    private TextView txtProStartDate,txtProEndDate,txtNoResult;

    private LinearLayoutManager layoutManager;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    private CheckInternetConnection internetConnection;
    private StoreDataInSharedPref sharedPref;
    private RequiredMethods methods;

    private int day,month,year;
    private boolean flag;
    String proName,proDescription,proGoal,proStartDate,proEndDate,proType,proPriority;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_project);
        initComponent();
        gettingProInfo();
        setToolbar();
    }

    //initialize all component
    @Override
    public void initComponent() {

        recyclerView = findViewById(R.id.list);
        emptyView = findViewById(R.id.emptyView);
        txtNoResult = findViewById(R.id.txtNoResult);
        progressBar = findViewById(R.id.progressBar);
        inputProName = findViewById(R.id.inputProName);
        inputProDes = findViewById(R.id.inputProDes);
        inputProGoal = findViewById(R.id.inputProGoal);
        txtProStartDate = findViewById(R.id.txtStartDate);
        txtProEndDate = findViewById(R.id.txtEndDate);
        RadioGroup radioGroupA = findViewById(R.id.radioGroupA);
        RadioGroup radioGroupB = findViewById(R.id.radioGroupB);
        radioPrioHigh = findViewById(R.id.radioHigh);
        radioPrioMed = findViewById(R.id.radioMedium);
        radioPrioLow = findViewById(R.id.radioLow);
        radioPrivate = findViewById(R.id.radioPrivate);
        radioPublic = findViewById(R.id.radioPublic);
        relativeLayoutA = findViewById(R.id.relativeLayoutA);
        relativeLayoutB = findViewById(R.id.relativeLayoutB);

        bCreateNewProject = findViewById(R.id.bCreateProject);
        Button bCreate = findViewById(R.id.bCreate);
        bProject = findViewById(R.id.bProject);
        bProject.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_left_button));//set selecting button background
        txtNoResult.setVisibility(View.GONE);//invisible no result text

        relativeLayoutA.setVisibility(View.VISIBLE);//visible all project layout
        relativeLayoutB.setVisibility(View.GONE);//invisible create new project layout

        Calendar calendar = Calendar.getInstance();

        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);

        bProject.setOnClickListener(this);
        bCreate.setOnClickListener(this);
        bCreateNewProject.setOnClickListener(this);
        txtProStartDate.setOnClickListener(this);
        txtProEndDate.setOnClickListener(this);

        radioGroupA.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                selectProType(id);//select project type
            }
        });

        radioGroupB.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                selectProPriority(id);//select project priority
            }
        });

        internetConnection = new CheckInternetConnection(this);
        sharedPref = new StoreDataInSharedPref(this);
        methods = new RequiredMethods(this);
        layoutManager = new LinearLayoutManager(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Loading");
    }

    //getting all project information from server
    private void gettingProInfo()
    {
        if(internetConnection.isOnline())//if internet is enable
        {
            Map<String,String> map = new HashMap<>();
            map.put("option","projectInfo");
            map.put("comId",sharedPref.getCompanyId());

            ServerConnection connection = new ServerConnection(this,onResponseTask);//create connection object
            connection.serverResponse(getResources().getString(R.string.readInfo),map);//passing a request to server

        }else methods.errorMessage(getResources().getString(R.string.offline));//if internet is disable
    }

    //button click listener
    @Override
    public void onClick(View view) {

        switch (view.getId())
        {

            case R.id.bProject:
                bProject.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_left_button));//set selecting button background
                bCreateNewProject.setBackgroundResource(0);//set default background
                relativeLayoutA.setVisibility(View.VISIBLE);
                relativeLayoutB.setVisibility(View.GONE);
                break;

            case R.id.bCreateProject:
                bCreateNewProject.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_right_button));//set selecting button background
                bProject.setBackgroundResource(0);//set default background
                relativeLayoutA.setVisibility(View.GONE);
                relativeLayoutB.setVisibility(View.VISIBLE);

                break;

            case R.id.txtStartDate://project start date

                DatePickerDialog datePickerDialog = new DatePickerDialog(Projects.this, Projects.this, day, month, year);
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
                flag = true;//if choose this button

                break;

            case R.id.txtEndDate://project end date

                DatePickerDialog datePickerDialog1 = new DatePickerDialog(Projects.this, Projects.this, day, month, year);
                datePickerDialog1.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog1.show();
                flag = false;//if choose this button
                break;


            case R.id.bCreate://send a request to server button

                if(internetConnection.isOnline())//if internet connection is enable
                    createNewProject();//create new project
                else methods.errorMessage(getResources().getString(R.string.offline));//if internet connection is disable

                break;
        }

    }

    //create new project
    private void createNewProject()
    {
        Map<String,String> map = new HashMap<>();

        map.put("option","createProject");//passing option

        proName = inputProName.getText().toString();
        proGoal = inputProGoal.getText().toString();
        proDescription = inputProDes.getText().toString();
        proStartDate = txtProStartDate.getText().toString();
        proEndDate = txtProEndDate.getText().toString();

        if(!TextUtils.isEmpty(proName) && !TextUtils.isEmpty(proGoal) &&
                !TextUtils.isEmpty(proDescription) && !TextUtils.isEmpty(proStartDate) &&
                !TextUtils.isEmpty(proEndDate) && !TextUtils.isEmpty(proPriority) && !TextUtils.isEmpty(proType))//if project information are not empty
            map.put("jsonObj",createProjectJsonObj());//passing project info json object
        else {
            methods.errorMessage(getResources().getString(R.string.invalid));// if information are not valid
            return;
        }

        progressDialog.show();//display progress dialog
        ServerConnection serverConnection = new ServerConnection(this,responseTask);//create connection object
        serverConnection.serverResponse(getResources().getString(R.string.insert),map);//passing a request to server
    }

    //creating project information json object
    private String createProjectJsonObj()
    {
        JSONObject proInfo = new JSONObject();
        try {
            proInfo.put("createBy", sharedPref.getUserId());//passing current user id
            proInfo.put("comId",sharedPref.getCompanyId());//passing current user company id
            proInfo.put("proName", proName);//passing project name
            proInfo.put("proDes", proDescription);///passing project description
            proInfo.put("proGoal", proGoal);//passing project goal
            proInfo.put("proStartDate", proStartDate);//passing project start date
            proInfo.put("proEndDate", proEndDate);//passing project end date
            proInfo.put("proType", proType);//passing project type
            proInfo.put("proPriority", proPriority);//passing project priority
            proInfo.put("date",methods.getDateWithTime());//passing project created date,which is current date

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return proInfo.toString();
    }

    //select date from date picker
    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSet(DatePicker datePicker, int yy, int mm, int dd)
    {
        day = dd;month = mm+1;year=yy;
        String d,m;
        d = day+"";m = month+"";
        if(day<10)
            d="0"+day;
        if(month<10)
            m = "0"+month;

        if (flag)//if choose project start date button
            txtProStartDate.setText(yy+"-"+m+"-"+ dd);//set date on date field
        else //if choose project end date button
            txtProEndDate.setText(yy+"-"+m+"-"+ dd);//set date on date field

    }

    //getting selected project priority from radio group
    private void selectProPriority(int id)
    {
        switch (id) {

            case R.id.radioHigh:
                proPriority = "5";
                break;
            case R.id.radioMedium:
                proPriority = "3";
                break;
            case R.id.radioLow:
                proPriority = "1";
                break;
        }
    }

    //getting selected project type from radio group
    private void selectProType(int id)
    {
        switch (id) {

            case R.id.radioPrivate:
                proType = "public";
                break;
            case R.id.radioPublic:
                proType = "private";
                break;
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

    @Override
    public void processJsonData(String jsonData) { }

    //clear all text and selected item after successfully execution
    private void clearAllField()
    {
        inputProName.setText(null);
        inputProGoal.setText(null);
        inputProDes.setText(null);
        txtProStartDate.setText(null);
        txtProEndDate.setText(null);
        radioPublic.setChecked(false);
        radioPrivate.setChecked(false);
        radioPrioLow.setChecked(false);
        radioPrioMed.setChecked(false);
        radioPrioHigh.setChecked(false);
    }

    //getting create new project server response
    private OnResponseTask responseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(String value) {

            if(value.equals("success"))//if execution success
            {
                clearAllField();//clear all text
                progressDialog.dismiss();//dismiss progress dialog
            }else methods.errorMessage(getResources().getString(R.string.unknown));//if any error occur
        }
    };

    //getting project information array
    private OnResponseTask onResponseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(String value) {

            if(!value.equals("failed"))//if query successfully executed
                processJsonData(value);
            else//if query is not successfully executed
                methods.errorMessage(getResources().getString(R.string.unknown));//if any error occur
        }
    };

}
