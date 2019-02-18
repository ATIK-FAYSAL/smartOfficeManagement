package com.atikfaysal.smartofficemanagement.main;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.background.CheckInternetConnection;
import com.atikfaysal.smartofficemanagement.background.ServerConnection;
import com.atikfaysal.smartofficemanagement.interfaces.ImplementedMethods;
import com.atikfaysal.smartofficemanagement.interfaces.OnResponseTask;
import com.atikfaysal.smartofficemanagement.others.DataValidation;
import com.atikfaysal.smartofficemanagement.others.RequiredMethods;

import java.util.HashMap;
import java.util.Map;

public class UserRegistration extends AppCompatActivity implements ImplementedMethods, View.OnClickListener
{

    private EditText inputName,inputEmail,inputPassword,inputConPass;
    private Button bCreate;

    private DataValidation validation;
    private CheckInternetConnection internetConnection;
    private RequiredMethods requiredMethods;
    private String phone;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_registration);
        initComponent();
        addTextChangeListener();
    }

    /*
     * All component initialization
     * Object creation
     * set click listener
     *
     */
    @Override
    public void initComponent() {

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConPass = findViewById(R.id.inputConPass);
        bCreate = findViewById(R.id.bCreateAccount);
        progressBar = findViewById(R.id.progress);
        TextView txtSignIn = findViewById(R.id.txtSignIn);

        bCreate.setOnClickListener(this);
        txtSignIn.setOnClickListener(this);


        //create object
        internetConnection = new CheckInternetConnection(this);
        requiredMethods = new RequiredMethods(this);
        validation = new DataValidation();
    }

    //button click listener
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.bCreateAccount://create new account
                if(internetConnection.isOnline())
                {
                    createNewUser();//create new user method
                }else requiredMethods.errorMessage(getResources().getString(R.string.offline));//if no internet connection
                break;

            case R.id.txtSignIn://if already have account,go to sign in page
                startActivity(new Intent(UserRegistration.this,SignIn.class));//go to sign in
                break;
        }
    }

    //user information validation & set valid or invalid icon
    private void addTextChangeListener()
    {
        final Drawable iconValid = getResources().getDrawable(R.drawable.icon_valid);//valid icon
        iconValid.setBounds(0,0,iconValid.getIntrinsicWidth(),iconValid.getIntrinsicHeight());
        final Drawable iconInvalid = getResources().getDrawable(R.drawable.icon_error);//invalid icon
        iconInvalid.setBounds(0,0,iconInvalid.getIntrinsicWidth(),iconInvalid.getIntrinsicHeight());


        //user name validation
        inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String name = inputName.getText().toString();//getting name
                if(validation.nameValidation(name))//check user name
                    inputName.setError("Valid",iconValid);//if name is ok
                else inputName.setError("Invalid name",iconInvalid);//if name is not ok
            }
        });

        //user email validation
        inputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String email = inputEmail.getText().toString();//getting user email
                if(validation.emailValidation(email))//check user email is ok or not
                    inputEmail.setError("Valid",iconValid);//if user email is ok
                else inputEmail.setError("Invalid email",iconInvalid);//if user email is not ok
            }
        });

        //user password validation
        inputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String password = inputPassword.getText().toString();//getting user email
                if(validation.passwordValidation(password))//check user email is ok or not
                    inputPassword.setError("Valid",iconValid);//if user email is ok
                else inputPassword.setError("Invalid password,Password should contains [a-z,A-Z,0-9,@#$%^&+=]",iconInvalid);//if user email is not ok
            }
        });

        //user confirm password validation
        inputConPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String password = inputConPass.getText().toString();//getting user email
                if(validation.passwordValidation(password) && inputPassword.getText().toString().equals(password))//check user confirm password is ok or not
                    inputConPass.setError("Valid",iconValid);//if user email is ok
                else inputConPass.setError("Invalid password,Password does not match",iconInvalid);//if user email is not ok
            }
        });

    }

    //create new user
    private void createNewUser()
    {
        if(internetConnection.isOnline())//check device is connect to internet
        {
            String name = inputName.getText().toString();
            String email = inputEmail.getText().toString();
            String password = inputPassword.getText().toString();
            String conPass = inputConPass.getText().toString();

            //user information validation
            if(validation.nameValidation(name) && validation.emailValidation(email) && validation.passwordValidation(password) && password.equals(conPass))
            {
                Map<String,String> map = new HashMap<>();
                map.put("option","registration");//passing case
                map.put("regName", name);//passing user name
                map.put("regEmail", email);//passing user email
                map.put("regPhone","01647544954");//passing user phone
                map.put("regPassword", password);//passing user password
                map.put("deviceId",requiredMethods.gettingDeviceId());//getting device id
                map.put("regDate",requiredMethods.getDateWithTime());//passing current date and time

                ServerConnection serverConnection = new ServerConnection(this, responseTask);
                serverConnection.serverResponse(getResources().getString(R.string.authentication),map);//passing a request to server

                bCreate.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_disable_button));//button disable after clicking
                bCreate.setEnabled(false);//disable button

                progressBar.setVisibility(View.VISIBLE);//display progress bar

            }else requiredMethods.errorMessage(getResources().getString(R.string.invalidInfo));//if information is not valid

        }else requiredMethods.errorMessage(getResources().getString(R.string.offline));//if no internet connection
    }

    @Override
    public void setToolbar() {}

    @Override
    public void processJsonData(String jsonData) {}

    //if new user registration success
    private void ifRegistrationSuccess()
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);//sleep for 2s
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);//gone progress bar
                            requiredMethods.congratulationDialog();//display congratulation layout
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    //get server response for new user registration
    private OnResponseTask responseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(String value) {

            if(value.equals("success"))//if registration success
                ifRegistrationSuccess();
            else
            {
                bCreate.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_button));//button enable after failed registration
                bCreate.setEnabled(true);//enable button
                progressBar.setVisibility(View.GONE);//display progress bar

                if(value.equals("contactExist"))
                    requiredMethods.errorMessage(getResources().getString(R.string.alreadyExist));//if phone number or email are exist
                else requiredMethods.errorMessage(getResources().getString(R.string.registrationFailed));//another error is occur
            }

        }
    };

}
