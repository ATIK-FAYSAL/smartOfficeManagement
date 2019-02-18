package com.atikfaysal.smartofficemanagement.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.adapters.EmployeeAdapter;
import com.atikfaysal.smartofficemanagement.adapters.NoticeAdapter;
import com.atikfaysal.smartofficemanagement.background.CheckInternetConnection;
import com.atikfaysal.smartofficemanagement.background.ServerConnection;
import com.atikfaysal.smartofficemanagement.background.StoreDataInSharedPref;
import com.atikfaysal.smartofficemanagement.interfaces.ImplementedMethods;
import com.atikfaysal.smartofficemanagement.interfaces.OnResponseTask;
import com.atikfaysal.smartofficemanagement.model.EmployeeModel;
import com.atikfaysal.smartofficemanagement.model.NoticeModel;
import com.atikfaysal.smartofficemanagement.others.RequiredMethods;
import com.google.gson.JsonObject;

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

public class Employees extends AppCompatActivity implements ImplementedMethods, View.OnClickListener
{

    private RelativeLayout relativeLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private TextView txtNoResult;
    private StoreDataInSharedPref storeDataInSharedPref;
    private CheckInternetConnection internetConnection;
    private RequiredMethods requiredMethods;
    private ProgressBar progressBar;
    private Button bAll,bHR,bDeptHead;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_employes);
        initComponent();
        setToolbar();
        gettingEmployeeList();
    }

    @Override
    public void onClick(View view) {

       switch (view.getId())
       {
           case R.id.bAllEmployee:
               bAll.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_left_button));//set selecting button background
               bHR.setBackgroundResource(0);//set default background
               bDeptHead.setBackgroundResource(0);//set default background
               break;

           case R.id.bHrAdmin:
               bHR.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_middle_button));//set selecting button background
               bAll.setBackgroundResource(0);//set default background
               bDeptHead.setBackgroundResource(0);//set default background
               break;

           case R.id.bDptHead:
               bDeptHead.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_right_button));//set selecting button background
               bHR.setBackgroundResource(0);//set default background
               bAll.setBackgroundResource(0);//set default background
               break;
       }

    }

    @Override
    public void initComponent() {

        recyclerView = findViewById(R.id.list);
        relativeLayout = findViewById(R.id.emptyView);
        txtNoResult = findViewById(R.id.txtNoResult);
        txtNoResult.setVisibility(View.INVISIBLE);
        progressBar = findViewById(R.id.progressBar);

        bAll = findViewById(R.id.bAllEmployee);
        bDeptHead = findViewById(R.id.bDptHead);
        bHR = findViewById(R.id.bHrAdmin);

        bAll.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_left_button));//set selecting button background

        bHR.setOnClickListener(this);
        bAll.setOnClickListener(this);
        bDeptHead.setOnClickListener(this);

        internetConnection = new CheckInternetConnection(this);
        storeDataInSharedPref = new StoreDataInSharedPref(this);
        requiredMethods = new RequiredMethods(this);
        layoutManager = new LinearLayoutManager(this);
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

    private void gettingEmployeeList()
    {
        if (internetConnection.isOnline())//if internet is enable
        {
            Map<String,String> map = new HashMap<>();
            map.put("option","empList");//passing case
            map.put("comId",storeDataInSharedPref.getCompanyId());//company id

            ServerConnection serverConnection = new ServerConnection(this,onResponseTask);
            serverConnection.serverResponse(getResources().getString(R.string.readInfo),map);

        }else requiredMethods.errorMessage(getResources().getString(R.string.offline));//if internet is not enable
    }

    @Override
    public void processJsonData(String jsonData) {

        List<EmployeeModel> models = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonObject.optJSONArray("empList");

            int count = 0;

            String name,empId,email,phone,image,userId;

            while (count<jsonArray.length())
            {
                JSONObject object = jsonArray.getJSONObject(count);

                name = object.getString("name");
                userId = object.getString("userId");
                empId = object.getString("empId");
                email = object.getString("email");
                phone = object.getString("phone");
                image = object.getString("image");

                models.add(new EmployeeModel(name,empId,image,userId,phone,email));

                count++;

            }

            viewNoticeInfo(models);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void viewNoticeInfo(final List<EmployeeModel> employeeModels)
    {
        //add progress bar ...
        final Timer timer = new Timer();
        final Handler handler = new Handler();
        final  Runnable runnable = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                try {

                    if(!employeeModels.isEmpty())//if array is not empty
                    {
                        relativeLayout.setVisibility(View.INVISIBLE);//empty view invisible
                        recyclerView.setVisibility(View.VISIBLE);//no result text invisible
                        EmployeeAdapter adapter = new EmployeeAdapter(Employees.this, employeeModels);//create adapter
                        recyclerView.setAdapter(adapter);//set adapter in recyler view
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                    }else//if list is empty
                    {
                        relativeLayout.setVisibility(View.VISIBLE);//empty view visible
                        txtNoResult.setVisibility(View.VISIBLE);//no result text visible
                        recyclerView.setVisibility(View.INVISIBLE);//list invisible
                    }

                    progressBar.setVisibility(View.GONE);
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

    private OnResponseTask onResponseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(String value) {

            processJsonData(value);
        }
    };
}
