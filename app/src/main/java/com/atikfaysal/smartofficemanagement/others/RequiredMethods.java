package com.atikfaysal.smartofficemanagement.others;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.main.SignIn;
import com.gdacciaro.iOSDialog.iOSDialog;
import com.gdacciaro.iOSDialog.iOSDialogBuilder;
import com.gdacciaro.iOSDialog.iOSDialogClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class RequiredMethods extends AlertDialog
{

    private Context context;
    private Activity activity;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;


    //constructor
    public RequiredMethods(Context context)
    {
        super(context);
        this.context = context;
        activity = (Activity) context;
    }


    //get current time and date
    @SuppressLint("SimpleDateFormat")
    public String getDateWithTime()
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");//date and time format
        return dateFormat.format(calendar.getTime());
    }

    //close top all activity and go to specific activity
    public void closeActivity(Activity context, Class<?> clazz) {
        Intent intent = new Intent(context, clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        context.finish();
    }

    public void congratulationDialog()
    {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.congrates_layout,null);
        Button bGo = view.findViewById(R.id.bGo);
        builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(false);
        alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        bGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeActivity((Activity) context, SignIn.class);
                alertDialog.dismiss();
            }
        });
    }


    @SuppressLint("HardwareIds")
    public String gettingDeviceId()
    {
        return Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);//getting device id
    }


    //execution failed
    public void errorMessage(String message)
    {
        Objects.requireNonNull(getWindow()).setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        iOSDialogBuilder builder = new iOSDialogBuilder(context);

        builder.setTitle("Attention")
                .setSubtitle(message)
                .setBoldPositiveLabel(true)
                .setCancelable(false)
                .setPositiveListener("ok",new iOSDialogClickListener() {
                    @Override
                    public void onClick(iOSDialog dialog) {
                        dialog.dismiss();
                    }
                }).build().show();
    }
}
