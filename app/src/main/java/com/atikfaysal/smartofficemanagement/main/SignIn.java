package com.atikfaysal.smartofficemanagement.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.background.CheckInternetConnection;
import com.atikfaysal.smartofficemanagement.background.ServerConnection;
import com.atikfaysal.smartofficemanagement.background.StoreDataInSharedPref;
import com.atikfaysal.smartofficemanagement.interfaces.ImplementedMethods;
import com.atikfaysal.smartofficemanagement.interfaces.OnResponseTask;
import com.atikfaysal.smartofficemanagement.model.UserModel;
import com.atikfaysal.smartofficemanagement.others.DataValidation;
import com.atikfaysal.smartofficemanagement.others.RequiredMethods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignIn extends AppCompatActivity implements View.OnClickListener, ImplementedMethods
{

    private EditText inputEmail,inputPassword;

    private ProgressDialog progressDialog;
    private CheckInternetConnection internetConnection;
    private StoreDataInSharedPref inSharedPref;
    private RequiredMethods methods;
    private DataValidation validation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_sign_in);
        initComponent();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(inSharedPref.getLoginStatus())//if user already log in
            startActivity(new Intent(SignIn.this,Dashboard.class));//if user log in, Go to dashboard

    }

    /*
        * All component initialization
        * Object creation
        * set click listener
        *
     */
    @Override
    public void initComponent() {

        Button bSignIn = findViewById(R.id.bSignIn);
        TextView txtSignUp = findViewById(R.id.txtSignUp);
        TextView txtForgotPass = findViewById(R.id.txtForgetPassword);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);

        //set on click listener
        txtSignUp.setOnClickListener(this);
        txtForgotPass.setOnClickListener(this);
        bSignIn.setOnClickListener(this);

        //making objects
        internetConnection = new CheckInternetConnection(this);
        inSharedPref = new StoreDataInSharedPref(this);
        methods = new RequiredMethods(this);
        validation = new DataValidation();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait.....");//set dialog title
        progressDialog.setMessage("Authenticating");//set dialog message
        progressDialog.setCancelable(false);//can not cancel dialog
    }

    /*
        * on click listener
     */
    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.bSignIn://sign in

                //startActivity(new Intent(SignIn.this,Dashboard.class));

                if(internetConnection.isOnline())//check device is connected to internet
                {
                    String userEmail = inputEmail.getText().toString();
                    String userPassword = inputPassword.getText().toString();

                    if(validation.emailValidation(userEmail) && validation.passwordValidation(userPassword))//check email and password is match to our format
                    {
                        Map<String,String> map = new HashMap<>();

                        map.put("option","login");//passing case
                        map.put("email", userEmail);//passing user email
                        map.put("password", userPassword);//passing user password

                        ServerConnection serverConnection = new ServerConnection(this,responseTask);//create object
                        serverConnection.serverResponse(getResources().getString(R.string.authentication),map);//passing request to server

                        progressDialog.show();//display progress dialog

                    }else methods.errorMessage(getResources().getString(R.string.authenticationFailed));//if user information is not valid

                }else methods.errorMessage(getResources().getString(R.string.offline));//if device is not connected to internet

                break;

            case R.id.txtForgetPassword://forgot password
                break;

            case R.id.txtSignUp://create new account
                startActivity(new Intent(SignIn.this,UserRegistration.class));

                break;
        }

    }

    @Override
    public void setToolbar() {}

    @Override
    public void processJsonData(String jsonData) {

        try {
            List<UserModel> models = processUserInfo(jsonData);//get user information json data as normal form

            if(!models.isEmpty())
            {
                storeUserInfo(models,SignIn.this);//store user information

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
                        methods.closeActivity(SignIn.this,Dashboard.class);
                    }
                });


            }else
            {
                methods.errorMessage(getResources().getString(R.string.authenticationFailed));//authentication failed message
                progressDialog.dismiss();//dismiss
            }

        } catch (JSONException e) {
            methods.errorMessage(e.toString());//if any exception occur
            progressDialog.dismiss();//dismiss progress dialog
        }
    }

    public void storeUserInfo(List<UserModel> models, Context context)
    {
        Map<String,String> userInfoMap = new HashMap<>();
        StoreDataInSharedPref sharedPref = new StoreDataInSharedPref(context);

        userInfoMap.put("userId",models.get(0).getUserId());//store user id
        userInfoMap.put("employeeId",models.get(0).getEmployeeId());//store employee id
        userInfoMap.put("name",models.get(0).getName());//store user name
        userInfoMap.put("email",models.get(0).getEmail());//store user email
        userInfoMap.put("phone",models.get(0).getPhone());//store user phone
        //userInfoMap.put("address",models.get(0).getAddress());//store user address
        //userInfoMap.put("gender",models.get(0).getGender());//store user gender
        //userInfoMap.put("dob",models.get(0).getDob());//store user dob
        userInfoMap.put("userType",models.get(0).getUserType());//store user type
        //userInfoMap.put("bloodGroup",models.get(0).getBloodGroup());//store blood group
        userInfoMap.put("password",models.get(0).getPassword());//store user password
        //userInfoMap.put("employeeStatus",models.get(0).getEmpStatus());//store user employee status
        //userInfoMap.put("date",models.get(0).getDate());//store user joining date
        userInfoMap.put("image",models.get(0).getImage());//store user image
        //userInfoMap.put("companyName",models.get(0).getCompanyName());//store company name
        //userInfoMap.put("department",models.get(0).getDepartment());//store department name
        //userInfoMap.put("designation",models.get(0).getDesignation());//store used designation
        //userInfoMap.put("salary",models.get(0).getSalary());//store user salary
        userInfoMap.put("companyId",models.get(0).getCompanyId());//store company id

        sharedPref.storeCurrentUserInfo(userInfoMap);//store current user info
        sharedPref.isUserLogin(true);//store log in status as true
    }

    //process json data
    public List<UserModel> processUserInfo(String jsonData) throws JSONException
    {
        List<UserModel> userModels = new ArrayList<>();


        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray jsonArray = jsonObject.optJSONArray("info");

        String userId,employeeId,name,email,phone,address,gender,dob,userType,bloodGroup,password,employeeStatus,date;
        String image,companyName,department,designation,salary,inTime,outTime,offDay,companyId;

        int count = 0;

        while (count<jsonArray.length())
        {
            JSONObject object = jsonArray.getJSONObject(count);

            userId = object.getString("userId");//getting user id
            employeeId = object.getString("empId");//getting employee id
            name = object.getString("name");//getting user name
            email = object.getString("email");//getting user email
            phone = object.getString("phone");//getting user phone
            address = object.getString("address");//getting user address
            gender = object.getString("gender");//getting user gender
            dob = object.getString("dob");//getting user date of birth
            userType = object.getString("userType");//getting user user type
            bloodGroup = object.getString("bloodGroup");//getting user blood group
            password = object.getString("password");//getting user password
            employeeStatus = object.getString("employeeStatus");//getting user employee status
            date = object.getString("date");//getting user date
            image = object.getString("image");//getting user image
            companyName = object.getString("companyName");//getting company name
            department = object.getString("department");//getting department
            designation = object.getString("designation");//getting designation
            salary = object.getString("salary");//getting salary
            inTime = object.getString("inTime");//getting office enter time
            outTime = object.getString("outTime");//getting office out time
            offDay = object.getString("offDay");//getting offDay
            companyId = object.getString("companyId");//getting company id

            userModels.add(new UserModel(userId,employeeId,name,email,phone,address,dob,gender,userType,bloodGroup,
                    password,employeeStatus,date,image,companyName,department,designation,salary,inTime,outTime,offDay,companyId));
            count++;
        }

        return userModels;//return user info
    }

    //getting server response
    private OnResponseTask responseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(String value) {

            Log.d("value",value);

            if(!value.equals("authFailed"))//if authentication success
            {
                processJsonData(value);//processing user information json data
            }
            else{
                methods.errorMessage(getResources().getString(R.string.authenticationFailed));//if authentication failed
                progressDialog.dismiss();//dismiss progress dialog
            }

        }
    };

}
