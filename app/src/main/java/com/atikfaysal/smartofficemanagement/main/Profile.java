package com.atikfaysal.smartofficemanagement.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.background.CheckInternetConnection;
import com.atikfaysal.smartofficemanagement.background.ServerConnection;
import com.atikfaysal.smartofficemanagement.background.StoreDataInSharedPref;
import com.atikfaysal.smartofficemanagement.interfaces.ImplementedMethods;
import com.atikfaysal.smartofficemanagement.interfaces.OnResponseTask;
import com.atikfaysal.smartofficemanagement.model.UserModel;
import com.atikfaysal.smartofficemanagement.others.BottomSheetOption;
import com.atikfaysal.smartofficemanagement.others.DataValidation;
import com.atikfaysal.smartofficemanagement.others.MyQrCode;
import com.atikfaysal.smartofficemanagement.others.RequiredMethods;
import com.bumptech.glide.Glide;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.lang.Thread.sleep;


/*
        * Show user details
        * Change user details
        * User complete work
        * Change user profile picture
     */

public class Profile extends AppCompatActivity implements ImplementedMethods, View.OnClickListener,DatePickerDialog.OnDateSetListener
{

    private CircleImageView imgUserImage;
    private Button bSave;
    private EditText inputName,inputEmail,inputPhone,inputAddress,inputEmpId;
    private TextView txtChooseImage,txtDob,txtGender,txtCompany,txtSalary,txtOfficeTime,txtDepartment,txtDesignation,txtWeekend,txtUserType,txtBloodGroup;

    private TextView txtEdit;
    private StoreDataInSharedPref storeDataInSharedPref;
    private CheckInternetConnection internetConnection;
    private RequiredMethods methods;
    private DataValidation validation;
    private BottomSheetOption bottomSheetOption;
    private ProgressDialog progressDialog;
    private int day,month,year;

    private static String USER_ID;
    private final String NULL_VALUE = "Not set yet";//if user value is not set
    private String name,email,phone,address,gender,bloodGroup,dateOfBirth,empId;
    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);
        initComponent();//initialize all component
        setToolbar();//toolbar on top
        gettingUserInformation();
        userRestriction();
    }

    @SuppressLint("IntentReset")
    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.bSave://update user profile
                updateUserInformation();
                break;

            case R.id.imgQrCode://scan qr code
                startActivity(new Intent(Profile.this, MyQrCode.class));//go to my qr code activity
                break;

            case R.id.txtEdit://information update enable
                enableDisableComponent(true);
                bSave.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_button));//button enable
                inputName.requestFocus();
                txtChooseImage.setVisibility(View.VISIBLE);
                break;

            case R.id.txtBloodGroup://change blood group
                bottomSheetOption.initBottomOptions("bloodGroup",txtBloodGroup);
                break;

            case R.id.txtGender://change gender
                bottomSheetOption.initBottomOptions("gender",txtGender);
                break;

            case R.id.txtDob://change dob
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, Profile.this, year, month, day);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
                break;

            case R.id.txtChoose://choose image

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)//check permission is granted or not
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);//request permission
                else
                {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent,1);
                }

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                uploadImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //image upload to server
    private void uploadImage(final Bitmap imageBit)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_pro_image_viewer,null);
        builder.setView(view);
        builder.setCancelable(false);
        ImageView imageView = view.findViewById(R.id.uImage);
        Button bCancel,bUpload;

        bCancel = view.findViewById(R.id.cancel);
        bUpload = view.findViewById(R.id.bUpload);


        if(bitmap!=null)
            imageView.setImageBitmap(imageBit);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        bUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServerConnection backgroundTask = new ServerConnection(Profile.this,respTask);
                Map<String,String>maps = new HashMap<>();
                maps.put("option","uploadImage");//passing case
                maps.put("userId",storeDataInSharedPref.getUserId());//passing user id
                maps.put("image",convertImageToString(imageBit));//passing image file
                if(internetConnection.isOnline())
                {
                    backgroundTask.serverResponse(getResources().getString(R.string.updateInfo),maps);
                    progressDialog.show();//show progress dialog
                }
                else methods.errorMessage(getResources().getString(R.string.unknown));
                alertDialog.dismiss();
            }
        });

    }

    //convert bitmap to string
    private String convertImageToString(Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    public void initComponent() {

        imgUserImage = findViewById(R.id.imgUserImage);
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        inputAddress = findViewById(R.id.inputAddress);
        inputEmpId = findViewById(R.id.inputEmpId);
        txtDob = findViewById(R.id.txtDob);
        txtGender = findViewById(R.id.txtGender);
        txtCompany = findViewById(R.id.txtCompany);
        txtSalary = findViewById(R.id.txtSalary);
        txtOfficeTime = findViewById(R.id.txtOfficeTime);
        txtDepartment = findViewById(R.id.txtDepartment);
        txtDesignation = findViewById(R.id.txtDesignation);
        txtWeekend = findViewById(R.id.txtWeekEnd);
        txtBloodGroup = findViewById(R.id.txtBloodGroup);
        txtUserType = findViewById(R.id.txtUserType);
        txtChooseImage = findViewById(R.id.txtChoose);
        txtEdit = findViewById(R.id.txtEdit);
        ImageView imgQrCode = findViewById(R.id.imgQrCode);
        bSave = findViewById(R.id.bSave);
        bSave.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_disable_button));//button disable for the first time
        Calendar calendar = Calendar.getInstance();

        day = calendar.get(Calendar.DAY_OF_MONTH);//get current date
        month = calendar.get(Calendar.MONTH);//get current month
        year = calendar.get(Calendar.YEAR);//get current year


        USER_ID = Objects.requireNonNull(getIntent().getExtras()).getString("userId");//getting user id from another activity

        //set click listener
        imgQrCode.setOnClickListener(this);
        txtEdit.setOnClickListener(this);
        bSave.setOnClickListener(this);
        imgUserImage.setOnClickListener(this);
        txtGender.setOnClickListener(this);
        txtDob.setOnClickListener(this);
        txtBloodGroup.setOnClickListener(this);
        txtChooseImage.setOnClickListener(this);

        internetConnection = new CheckInternetConnection(this);
        storeDataInSharedPref = new StoreDataInSharedPref(this);
        validation = new DataValidation();
        bottomSheetOption = new BottomSheetOption(this);
        methods = new RequiredMethods(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait....");//set dialog title
        progressDialog.setMessage("Updating your information");//set dialog message
        enableDisableComponent(false);//disable component
    }

    //setting toolbar
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

    //make a request for getting user information
    private void gettingUserInformation()
    {
        if (internetConnection.isOnline())//check internet connection is enable
        {
            Map<String,String> map = new HashMap<>();

            if(!storeDataInSharedPref.getUserId().equals("none"))//check user id is not null
            {
                map.put("option","userInfo");//passing case
                map.put("userId",USER_ID);//passing user id

                ServerConnection serverConnection = new ServerConnection(this,responseTask);//create connection object
                serverConnection.serverResponse(getResources().getString(R.string.readInfo),map);//passing a request
            }else methods.errorMessage(getResources().getString(R.string.unknown));//unknown error

        }else methods.errorMessage(getResources().getString(R.string.offline));//if internet connection is disable
    }

    //enable & disable component
    private void enableDisableComponent(boolean flag)
    {
        bSave.setEnabled(flag);
        inputName.setEnabled(flag);
        inputEmail.setEnabled(flag);
        inputPhone.setEnabled(flag);
        inputAddress.setEnabled(flag);
        imgUserImage.setEnabled(flag);
        txtDob.setEnabled(flag);
        txtGender.setEnabled(flag);
        txtBloodGroup.setEnabled(flag);
        inputEmpId.setEnabled(flag);
    }

    //update user information
    private void updateUserInformation()
    {
        Map<String,String> map = new HashMap<>();

        map.put("option","updateInfo");//passing case
        map.put("userId",storeDataInSharedPref.getUserId());//passing user id

        name = inputName.getText().toString();//getting name
        empId = inputEmpId.getText().toString();//getting employee id
        email = inputEmail.getText().toString();//get user email
        phone = inputPhone.getText().toString();//get user phone number
        dateOfBirth = txtDob.getText().toString();//get user date of birth
        gender = txtGender.getText().toString();//get user gender
        bloodGroup = txtBloodGroup.getText().toString();//get blood group
        address = inputAddress.getText().toString();//get address

        if(validation.nameValidation(name))//check name validation
            map.put("name",name);//passing user name
        else
        {
            methods.errorMessage("Name is not valid,Please try again");
            return;
        }

        if(validation.phoneNumberValidation(phone))//check phone number validation
            map.put("phone",phone);//passing user phone
        else
        {
            methods.errorMessage("Phone is not valid,Please try again");
            return;
        }

        if(validation.emailValidation(email))//check email validation
            map.put("email",email);//passing user email
        else
        {
            methods.errorMessage("Email is not valid,Please try again");
            return;
        }

        if(!empId.equalsIgnoreCase(NULL_VALUE) && !TextUtils.isEmpty(empId))
            map.put("empId",empId);//passing employee id
        else map.put("empId","null");

        if(!dateOfBirth.equalsIgnoreCase(NULL_VALUE) && !TextUtils.isEmpty(dateOfBirth))
            map.put("dateOfBirth",dateOfBirth);//passing user date of birth
        else map.put("dateOfBirth","null");

        if(!gender.equalsIgnoreCase(NULL_VALUE) && !TextUtils.isEmpty(gender))
            map.put("gender",gender);//passing user gender
        else map.put("gender","null");

        if(!bloodGroup.equalsIgnoreCase(NULL_VALUE) && !TextUtils.isEmpty(bloodGroup))
            map.put("bloodGroup",bloodGroup);//passing user blood group
        else map.put("bloodGroup","null");

        if(!address.equalsIgnoreCase(NULL_VALUE) && !TextUtils.isEmpty(address))
            map.put("address",address);//passing user address
        else map.put("address","null");

        map.put("userType",txtUserType.getText().toString());//passing user type

        if(internetConnection.isOnline())//internet connection is enable
        {
            ServerConnection serverConnection = new ServerConnection(this,onResponseTask);//create connection object
            serverConnection.serverResponse(getResources().getString(R.string.updateInfo),map);//passing a request
            progressDialog.show();//display progress dialog

        }else methods.errorMessage(getResources().getString(R.string.offline));//if internet connection is disable

    }

    private void userRestriction()
    {
        if(!storeDataInSharedPref.getUserId().equals(USER_ID))
        {
            bSave.setVisibility(View.GONE);//save button disable
            txtEdit.setVisibility(View.GONE);
        }
    }

    //json parsing user info
    @SuppressLint("SetTextI18n")
    @Override
    public void processJsonData(String jsonData) {

        try {

            List<UserModel> modelList = new SignIn().processUserInfo(jsonData);//getting user json parsing array

            inputName.setText(modelList.get(0).getName());//setting user name
            inputEmail.setText(modelList.get(0).getEmail());//setting user name
            inputPhone.setText(modelList.get(0).getPhone());//setting user name

            if(!modelList.get(0).getAddress().equals("null") && !TextUtils.isEmpty(modelList.get(0).getAddress()))//if value is null
                inputAddress.setText(modelList.get(0).getAddress());//setting user name

            if(!modelList.get(0).getEmployeeId().equals("null") && !TextUtils.isEmpty(modelList.get(0).getEmployeeId()))//if value is null
                inputEmpId.setText(modelList.get(0).getEmployeeId());//setting employee id

            if(!modelList.get(0).getGender().equals("null") && !TextUtils.isEmpty(modelList.get(0).getGender()))//if value is null
                txtGender.setText(modelList.get(0).getGender());//setting gender
            else txtGender.setText(NULL_VALUE);

            if(!modelList.get(0).getBloodGroup().equals("null") && !TextUtils.isEmpty(modelList.get(0).getBloodGroup()))//if value is null
                txtBloodGroup.setText(modelList.get(0).getBloodGroup());//setting blood group
            else txtBloodGroup.setText(NULL_VALUE);

            if(!modelList.get(0).getUserType().equals("null") && !TextUtils.isEmpty(modelList.get(0).getUserType()))//if value is null
                txtUserType.setText(modelList.get(0).getUserType());//setting user type
            else txtUserType.setText(NULL_VALUE);

            if(!modelList.get(0).getDepartment().equals("null") && !TextUtils.isEmpty(modelList.get(0).getDepartment()))//if value is null
                txtDepartment.setText(modelList.get(0).getDepartment());//setting department name
            else txtDepartment.setText(NULL_VALUE);

            if(!modelList.get(0).getCompanyName().equals("null") && !TextUtils.isEmpty(modelList.get(0).getCompanyName()))//if value is null
                txtCompany.setText(modelList.get(0).getCompanyName());//setting company name
            else txtCompany.setText(NULL_VALUE);

            if(!modelList.get(0).getSalary().equals("null") && !TextUtils.isEmpty(modelList.get(0).getSalary()))//if value is null
                txtSalary.setText(modelList.get(0).getSalary());//setting user salary
            else txtSalary.setText(NULL_VALUE);

            if(!modelList.get(0).getSalary().equals("null") && !TextUtils.isEmpty(modelList.get(0).getSalary()))//if value is null
                txtDesignation.setText(modelList.get(0).getDesignation());//setting user designation
            else txtDesignation.setText(NULL_VALUE);

            if(!modelList.get(0).getDob().equals("null") && !TextUtils.isEmpty(modelList.get(0).getDob()))//if value is null
                txtDob.setText(modelList.get(0).getDob());//setting user date of birth
            else txtDob.setText(NULL_VALUE);

            if(!modelList.get(0).getOffDay().equals("null") && !TextUtils.isEmpty(modelList.get(0).getOffDay()))//if value is null
                txtWeekend.setText(modelList.get(0).getOffDay());//setting user off day
            else txtWeekend.setText(NULL_VALUE);

            if(!modelList.get(0).getInTime().equals("null") && !modelList.get(0).getOutTime().equals("null"))//if value is null
                txtOfficeTime.setText(modelList.get(0).getInTime()+" - "+modelList.get(0).getInTime());//setting user office time
            else txtOfficeTime.setText(NULL_VALUE);

            if(!modelList.get(0).getImage().equals("null"))
            {
                    Glide.with(this).
                            load(getResources().getString(R.string.readInfo)).
                            into(imgUserImage);
            }

        } catch (JSONException e) {//if any exception occur
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onDateSet(DatePicker datePicker, int yy, int mm, int dd) {
        day = dd;month = mm+1;year=yy;
        String d,m;
        d = day+"";m = month+"";
        if(day<10)
            d="0"+day;
        if(month<10)
            m = "0"+month;

        txtDob.setText(year+"-"+m+"-"+d);

    }

    //getting server response
    private OnResponseTask responseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(String value) {
            processJsonData(value);
        }
    };

    private OnResponseTask respTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(final String value) {

            try {
                sleep(2000);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progressDialog.dismiss();//dismiss progress dialog
                        if(value.equals("success"))//if update successful
                        {
                            imgUserImage.setImageBitmap(bitmap);//set image on image view
                        }else
                        {
                            methods.errorMessage(getResources().getString(R.string.unknown));
                        }

                    }
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    };

    private OnResponseTask onResponseTask = new OnResponseTask() {
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
                                    txtChooseImage.setVisibility(View.GONE);//invisible image choose option
                                    enableDisableComponent(false);//disable component
                                    bSave.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_disable_button));//button disable for the first time

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
