package com.atikfaysal.smartofficemanagement.main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.atikfaysal.smartofficemanagement.R;

public class SplashScreen extends AppCompatActivity {


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        //TextView txtCurVersion = findViewById(R.id.txtVersion);
        //if(getCurrentVersion()!=null)
            //txtCurVersion.setText("Version : "+getCurrentVersion());//set current version in textView
        threadStart();
    }

    //get current version from system
    private String getCurrentVersion()
    {
        String version = null;

        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(getPackageName(),0);
            version = packageInfo.versionName;//get app version
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }


    protected void threadStart()
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Thread.sleep(2000);
                    startActivity(new Intent(SplashScreen.this, SignIn.class));
                    finish();
                }catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

}
