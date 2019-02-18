package com.atikfaysal.smartofficemanagement.main;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.adapters.NoticeAdapter;
import com.atikfaysal.smartofficemanagement.background.CheckInternetConnection;
import com.atikfaysal.smartofficemanagement.background.ServerConnection;
import com.atikfaysal.smartofficemanagement.background.StoreDataInSharedPref;
import com.atikfaysal.smartofficemanagement.interfaces.ImplementedMethods;
import com.atikfaysal.smartofficemanagement.interfaces.OnResponseTask;
import com.atikfaysal.smartofficemanagement.model.NoticeModel;
import com.atikfaysal.smartofficemanagement.others.RequiredMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

public class Notices extends AppCompatActivity implements
        ImplementedMethods, View.OnClickListener
{
    private RelativeLayout relativeLayoutA,relativeLayoutB,emptyView;
    private RecyclerView recyclerView;
    private Button bNotice,bCreateNotice;
    private TextView txtNoResult;
    private EditText inputTitle,inputNotice;
    private CheckInternetConnection internetConnection;
    private StoreDataInSharedPref storeDataInSharedPref;
    private RequiredMethods methods;
    private ProgressDialog progressDialog;
    private String priority ;
    private LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_notice);
        initComponent();
        setToolbar();
        gettingAllNotice();//fetch notice of this company
    }

    //initialize all component
    @Override
    public void initComponent() {

        recyclerView = findViewById(R.id.list);
        relativeLayoutA = findViewById(R.id.relativeLayoutA);
        relativeLayoutB = findViewById(R.id.relativeLayoutB);
        bNotice = findViewById(R.id.bNotice);
        bCreateNotice = findViewById(R.id.bCreateNotice);
        Button bPublish = findViewById(R.id.bPublish);
        inputNotice = findViewById(R.id.inputNotice);
        inputTitle = findViewById(R.id.inputTitle);
        emptyView = findViewById(R.id.emptyView);
        txtNoResult = findViewById(R.id.txtNoResult);
        txtNoResult.setVisibility(View.INVISIBLE);
        bNotice.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_left_button));//set selecting button background

        RadioGroup radioPriority = findViewById(R.id.radioGroupA);

        radioPriority.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                selectPriority(id);//select priority layout
            }
        });

        bNotice.setOnClickListener(this);
        bPublish.setOnClickListener(this);
        bCreateNotice.setOnClickListener(this);

        internetConnection = new CheckInternetConnection(this);
        storeDataInSharedPref = new StoreDataInSharedPref(this);
        layoutManager = new LinearLayoutManager(this);
        methods = new RequiredMethods(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait....");//set dialog title
        progressDialog.setMessage("Publishing new notice");//set message
    }

    //getting all notice from server
    private void gettingAllNotice()
    {
        if(internetConnection.isOnline())//if internet connection is enable
        {
            Map<String,String> map = new HashMap<>();

            map.put("option","notice");//passing case
            map.put("userId",storeDataInSharedPref.getUserId());//passing user id
            map.put("companyId",storeDataInSharedPref.getCompanyId());//

            ServerConnection serverConnection = new ServerConnection(this,onResponseTask);//create connection object
            serverConnection.serverResponse(getResources().getString(R.string.readInfo),map);//passing a request

        }else methods.errorMessage(getResources().getString(R.string.offline));//if internet connection is disable
    }

    //button click
    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.bNotice://display all notice
                bNotice.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_left_button));//set selecting button background
                bCreateNotice.setBackgroundResource(0);//set default background
                relativeLayoutA.setVisibility(View.GONE);//invisible new notice publish
                relativeLayoutB.setVisibility(View.VISIBLE);//visible list view
                break;

            case R.id.bCreateNotice://create new notice
                bCreateNotice.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_right_button));//set selecting button background
                bNotice.setBackgroundResource(0);//set default background
                relativeLayoutA.setVisibility(View.VISIBLE);//visible new notice publish
                relativeLayoutB.setVisibility(View.GONE);//invisible list view
                break;

            case R.id.bPublish://publish new notice

                if(internetConnection.isOnline())//check internet connection is enable
                    publishNewNotice();//publish new notice
                else methods.errorMessage(getResources().getString(R.string.offline));//if internet connection is disable

                break;
        }
    }

    //publish new notice
    private void publishNewNotice()
    {

        Map<String,String> map = new HashMap<>();

        String noticeTitle,notice;

        notice = inputNotice.getText().toString();//getting notice
        noticeTitle = inputTitle.getText().toString();//getting notice title

        if(!TextUtils.isEmpty(notice) && !TextUtils.isEmpty(noticeTitle) && !TextUtils.isEmpty(priority))
        {

            map.put("option","notice");//passing case
            map.put("userId",storeDataInSharedPref.getUserId());//passing user id
            map.put("title",noticeTitle);//passing notice title
            map.put("notice",notice);//passing new notice
            map.put("priority",priority);//set priority
            map.put("date",methods.getDateWithTime());//passing current date & time

            ServerConnection serverConnection = new ServerConnection(this,responseTask);//create connection object
            serverConnection.serverResponse(getResources().getString(R.string.insert),map);//passing new request
            progressDialog.show();//display progress dialog
        }else methods.errorMessage(getResources().getString(R.string.invalid));//if info is not valid
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
    public void processJsonData(String jsonData) {

        List<NoticeModel> noticeModel = new ArrayList<>();

        try {

            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonObject.optJSONArray("notice");

            String notice,userName,userId,title,priority,date;

            int count = 0;

            while (count<jsonArray.length())
            {
                JSONObject object = jsonArray.getJSONObject(count);

                userId = object.getString("userId");//getting publisher user id
                userName = object.getString("userName");//getting publisher user name
                title = object.getString("title");//getting notice title
                notice = object.getString("notice");//getting notice
                priority = object.getString("priority");//getting notice priority
                date = object.getString("date");//getting publishing date and time

                noticeModel.add(new NoticeModel(userName,userId,title,notice,priority,date));

                count++;
            }

            viewNoticeInfo(noticeModel);//view notice

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void viewNoticeInfo(final List<NoticeModel> noticeModel)
    {
        //add progress bar ...
        final Timer timer = new Timer();
        final Handler handler = new Handler();
        final  Runnable runnable = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                try {

                    if(!noticeModel.isEmpty())//if array is not empty
                    {
                        emptyView.setVisibility(View.INVISIBLE);//empty view invisible
                        recyclerView.setVisibility(View.VISIBLE);//no result text invisible
                        NoticeAdapter adapter = new NoticeAdapter(Notices.this, noticeModel);//create adapter
                        recyclerView.setAdapter(adapter);//set adapter in recyler view
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                    }else//if list is empty
                    {
                        emptyView.setVisibility(View.VISIBLE);//empty view visible
                        txtNoResult.setVisibility(View.VISIBLE);//no result text visible
                        recyclerView.setVisibility(View.INVISIBLE);//list invisible
                    }

                    progressDialog.dismiss();
                    timer.cancel();
                }catch (NullPointerException e)
                {
                    txtNoResult.setVisibility(View.VISIBLE);
                    txtNoResult.setText(getResources().getString(R.string.noResult));

                }
            }
        };
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        },getResources().getInteger(R.integer.progTime));
    }

    //select priority
    private void selectPriority(int id)
    {
        switch (id) {

            case R.id.radioHigh:
                priority = "5";
                break;

            case R.id.radioMedium:
                priority = "3";
                break;

            case R.id.radioLow:
                priority = "1";
                break;
        }
    }

    private OnResponseTask onResponseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(String value) {
            Log.d("notice",value);
            processJsonData(value);
        }
    };

    //getting new publish response
    private OnResponseTask responseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(final String value) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        sleep(getResources().getInteger(R.integer.progTime));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                progressDialog.dismiss();//dismiss progress dialog
                                if(value.equals("success"))//if update successful
                                {
                                    Toast.makeText(Notices.this,"Notice successfully published",Toast.LENGTH_LONG).show();
                                    bNotice.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_left_button));//set selecting button background
                                    bCreateNotice.setBackgroundResource(0);//set default background
                                    relativeLayoutA.setVisibility(View.GONE);//invisible new notice publish
                                    relativeLayoutB.setVisibility(View.VISIBLE);//visible list view
                                    gettingAllNotice();//fetch again all notice of this company

                                }else//if any update failed
                                {
                                    methods.errorMessage(getResources().getString(R.string.unknown));//show message
                                }

                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });

            thread.start();
        }
    };

}
