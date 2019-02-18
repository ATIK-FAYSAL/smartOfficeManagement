package com.atikfaysal.smartofficemanagement.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.adapters.ViewPagerAdapter;
import com.atikfaysal.smartofficemanagement.background.CheckInternetConnection;
import com.atikfaysal.smartofficemanagement.background.ServerConnection;
import com.atikfaysal.smartofficemanagement.background.StoreDataInSharedPref;
import com.atikfaysal.smartofficemanagement.interfaces.ImplementedMethods;
import com.atikfaysal.smartofficemanagement.interfaces.OnResponseTask;
import com.atikfaysal.smartofficemanagement.model.UserModel;
import com.atikfaysal.smartofficemanagement.others.RequiredMethods;
import com.gdacciaro.iOSDialog.iOSDialog;
import com.gdacciaro.iOSDialog.iOSDialogBuilder;
import com.gdacciaro.iOSDialog.iOSDialogClickListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Dash;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;



/*
        * display running,pending,complete work details
        * Give attendance
        * Capture image
        * get current location
        * Implement navigation drawer menu
     */

public class Dashboard extends AppCompatActivity implements
        ImplementedMethods,View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
{
    private Button bRunning,bPending,bComplete;

    private String longitude,latitude;

    private CheckInternetConnection internetConnection;
    private StoreDataInSharedPref storeDataInSharedPref;
    private RequiredMethods methods;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mToggle;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private SignIn signIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dashboard);
        initComponent();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        closeApp();//terminate app
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!isLocationEnabled())//check gps is disable
            enableGpsLocation();//enable gps location


        if (mGoogleApiClient != null) //if google api client not null
            mGoogleApiClient.connect();//connect google api client

        gettingUserInformation();
    }

    /*
     * All component initialization
     * Object creation
     * set click listener
     */
    @Override
    public void initComponent()
    {
        drawerLayout = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.setDrawerListener(mToggle);
        mToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        CircleImageView imgAttendance = findViewById(R.id.imgAttendance);
        imgAttendance.setOnClickListener(this);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.inflateHeaderView(R.layout.nav_header);

        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtPhone = view.findViewById(R.id.txtPhone);
        CircleImageView userImage = view.findViewById(R.id.imgUserImage);
        bRunning = findViewById(R.id.bRunningWork);//for running work button
        bPending = findViewById(R.id.bPendingWork);//for pending work button
        bComplete = findViewById(R.id.bComplete);//for complete
        bRunning.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_left_button));//set selecting button background

        bRunning.setOnClickListener(this);//set listener for running work button
        bComplete.setOnClickListener(this);//set listener for complete work button
        bPending.setOnClickListener(this);//set listener for pending work button

        //making objects
        internetConnection = new CheckInternetConnection(this);
        methods = new RequiredMethods(this);
        storeDataInSharedPref = new StoreDataInSharedPref(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait....");//set progress dialog title
        progressDialog.setMessage("Taking today's attendance");//set progress dialog message
        signIn = new SignIn();


        if(!storeDataInSharedPref.getUserName().equals("none") && !storeDataInSharedPref.getPhoneNumber().equals("none"))
        {
            txtName.setText(storeDataInSharedPref.getUserName());//set user name
            txtPhone.setText(storeDataInSharedPref.getPhoneNumber());//set phone number
        }
    }

    //if gps location is disable,then enable gps location
    private void enableGpsLocation() {

        final AlertDialog alertDialog;
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(this).inflate(R.layout.layout_unavailable_location,null);
        Button bSetting = view.findViewById(R.id.bSetting);
        builder.setView(view);//set view in builder
        builder.setCancelable(false);//set cancelable false

        alertDialog = builder.create();//create alertDialog
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();//display alertDialog

        bSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));//open device setting
                alertDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 1 && resultCode == RESULT_OK && data!=null)
        {
            Bundle bundle = data.getExtras();
            assert bundle != null;
            Bitmap image = (Bitmap) bundle.get("data");
            takeAttendance(image);
        }else Toast.makeText(this,"empty",Toast.LENGTH_LONG).show();
    }

    //image upload to server
    private void takeAttendance(final Bitmap imageBit)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.image_viewer,null);
        builder.setView(view);//set view in builder
        builder.setCancelable(false);//set not cancelable false
        ImageView imageView = view.findViewById(R.id.uImage);
        Button bCancel,bUpload;

        bCancel = view.findViewById(R.id.bDismiss);
        bUpload = view.findViewById(R.id.bAttendance);

        if(imageBit!=null)
            imageView.setImageBitmap(imageBit);

        alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        bUpload.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {

                if(internetConnection.isOnline())
                {
                    Map<String,String> map = new HashMap<>();

                    assert imageBit != null;
                    if(!TextUtils.isEmpty(storeDataInSharedPref.getUserId()) && //check user id
                            !TextUtils.isEmpty(longitude) && //check longitude
                            !TextUtils.isEmpty(latitude) && //check latitude
                            !TextUtils.isEmpty(convertImageToString(imageBit)))//check image
                    {
                        map.put("option","attendance");//passing option
                        map.put("userId",storeDataInSharedPref.getUserId());//passing user id
                        map.put("latitude",latitude);//passing latitude
                        map.put("longitude",longitude);//passing longitude
                        map.put("image",convertImageToString(imageBit));//passing image as string

                        progressDialog.show();//display progress dialog

                        ServerConnection serverConnection = new ServerConnection(Dashboard.this,responseTask);//create connection object
                        serverConnection.serverResponse(getResources().getString(R.string.attendance),map);//connect to server and passing a request
                    }else {

                        alertDialog.dismiss();//dismiss alert dialog
                        methods.errorMessage(getResources().getString(R.string.invalidInfo));//if information are not valid
                    }

                }else //if device is not connected with internet
                {
                    alertDialog.dismiss();//dismiss alertDialog
                    methods.errorMessage(getResources().getString(R.string.offline));//show offline message
                }

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
                map.put("userId",storeDataInSharedPref.getUserId());//passing user id

                ServerConnection serverConnection = new ServerConnection(this,onResponseTask);//create connection object
                serverConnection.serverResponse(getResources().getString(R.string.readInfo),map);//passing a request
            }else methods.errorMessage(getResources().getString(R.string.unknown));//unknown error

        }else methods.errorMessage(getResources().getString(R.string.offline));//if internet connection is disable
    }

    //convert image bitmap to string
    private String convertImageToString(Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    //exit from app
    private void closeApp()
    {
        if(getIntent().getBooleanExtra("flag",false))finish();
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        startLocationUpdates();//getting current location

        Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLocation == null){
            startLocationUpdates();//getting current location
        }
        if (mLocation != null) {

            longitude = String.valueOf(mLocation.getLongitude());//get current longitude
            latitude = String.valueOf(mLocation.getLatitude());//get current latitude

        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("debug", "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {//if google api connected
            mGoogleApiClient.disconnect();//disconnect google api
        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        /* 10 secs */
        long UPDATE_INTERVAL = 2 * 1000;
        /* 2 sec */
        long FASTEST_INTERVAL = 2000;
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    //location changed
    @Override
    public void onLocationChanged(Location location) {
        longitude = String.valueOf(location.getLongitude());//get update longitude
        latitude = String.valueOf(location.getLatitude());//get update latitude
    }

    //check is location enable or disable
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void setToolbar() {}

    @Override
    public void processJsonData(String jsonData) {

        try {
            List<UserModel> userModels = signIn.processUserInfo(jsonData);//getting current user data from server
            signIn.storeUserInfo(userModels,this);//store user info
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //when user click on back button,terminate app
    @Override
    public void onBackPressed() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        iOSDialogBuilder builder = new iOSDialogBuilder(Dashboard.this);

        builder.setTitle("App termination")
                .setSubtitle("Do you want to close app?")
                .setBoldPositiveLabel(true)
                .setCancelable(false)
                .setPositiveListener("Close App",new iOSDialogClickListener() {
                    @Override
                    public void onClick(iOSDialog dialog) {
                        Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("flag",true);
                        startActivity(intent);
                        dialog.dismiss();

                    }
                })
                .setNegativeListener("cancel", new iOSDialogClickListener() {
                    @Override
                    public void onClick(iOSDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .build().show();
    }

    //create toolbar option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_item,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.imgAttendance://take attendance
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//open camera intent
                if(intent.resolveActivity(getPackageManager()) !=null)
                    startActivityForResult(intent,1);
                break;

            case R.id.bRunningWork:
                bRunning.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_left_button));//set selecting button background
                bPending.setBackgroundResource(0);//set default background
                bComplete.setBackgroundResource(0);//set default background
                break;

            case R.id.bPendingWork:
                bPending.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_middle_button));//set selecting button background
                bRunning.setBackgroundResource(0);//set default background
                bComplete.setBackgroundResource(0);//set default background
                break;

            case R.id.bComplete:
                bComplete.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_right_button));//set selecting button background
                bPending.setBackgroundResource(0);//set default background
                bRunning.setBackgroundResource(0);//set default background
                break;
        }
    }

    //navigation selected item
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId())
        {
            case R.id.itemProfile://user profile
                Intent intent = new Intent(Dashboard.this, Profile.class);
                intent.putExtra("userId",storeDataInSharedPref.getUserId());//passing user id
                startActivity(intent);//start new activity
                break;

            case R.id.itemNotice:
                startActivity(new Intent(Dashboard.this,Notices.class));//display and publish new notice
                break;

            case R.id.itemCompany:
                startActivity(new Intent(Dashboard.this,CompanyProfile.class));//display and publish new notice
                break;

            case R.id.itemEmployee:
                startActivity(new Intent(Dashboard.this,Employees.class));//display and publish new notice
                break;

            case R.id.itemSignOut://sign out
                userSignOut();//sign out from this device
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //sing out
    private void userSignOut()
    {
        final ProgressDialog progressDialog = ProgressDialog.show(Dashboard.this, "Please wait", "User Sign out...", true);
        progressDialog.setCancelable(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                } catch (Exception e) {
                }
                progressDialog.dismiss();
            }
        }).start();
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                storeDataInSharedPref.isUserLogin(false);
                storeDataInSharedPref.clearData();//clear all data when user sign out
                methods.closeActivity(Dashboard.this,SignIn.class);
            }
        });
    }

    private OnResponseTask responseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(String value) {

            if(value.equals("success"))
            {
                Toast.makeText(Dashboard.this,"Your attendance is added successfully",Toast.LENGTH_LONG).show();
                alertDialog.dismiss();
            }else //if failed to give attendance
            {
                methods.errorMessage(getResources().getString(R.string.unknown));
            }

            progressDialog.dismiss();//dismiss progress dialog
        }
    };

    private OnResponseTask onResponseTask = new OnResponseTask() {
        @Override
        public void onResultSuccess(String value) {
            processJsonData(value);
        }
    };
}
