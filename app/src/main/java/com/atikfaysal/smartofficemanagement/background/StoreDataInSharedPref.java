package com.atikfaysal.smartofficemanagement.background;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/*
 * store current user all information
 * get current user information from another class where needed
 * clear all information when current user sign out from device
 */

public class StoreDataInSharedPref
{
    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    private static final String UserInfo = "user_info";//file name for current user info
    private static final String IS_LOGIN = "login";//check is user log in or not

    public StoreDataInSharedPref(Context context)//constructor with context and user information map
    {
        this.context = context;
    }

    //store current user info
    public void storeCurrentUserInfo(Map<String,String> userInfoMap)
    {
        sharedPreferences = context.getSharedPreferences(UserInfo, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editor.putString("userId",userInfoMap.get("userId"));//store user id
        editor.putString("employeeId",userInfoMap.get("employeeId"));//store employee id
        editor.putString("name",userInfoMap.get("name"));//store user name
        editor.putString("email",userInfoMap.get("email"));//store user email
        editor.putString("phone",userInfoMap.get("phone"));//store user phone
        //editor.putString("address",userInfoMap.get("address"));//store user address
        //editor.putString("gender",userInfoMap.get("gender"));//store user gender
        //editor.putString("dob",userInfoMap.get("dob"));//store user date of birth
        editor.putString("userType",userInfoMap.get("userType"));//store user type
        //editor.putString("bloodGroup",userInfoMap.get("bloodGroup"));//store blood group
        editor.putString("password",userInfoMap.get("password"));//store user password
        //editor.putString("empStatus",userInfoMap.get("employeeStatus"));//store user employee status
        //editor.putString("date",userInfoMap.get("date"));//store user date
        editor.putString("image",userInfoMap.get("image"));//store user image
        //editor.putString("companyName",userInfoMap.get("companyName"));//store user company name
        //editor.putString("department",userInfoMap.get("department"));//store user department
        //editor.putString("designation",userInfoMap.get("designation"));//store user designation
        //editor.putString("salary",userInfoMap.get("salary"));//store user salary
        editor.putString("companyId",userInfoMap.get("companyId"));//store user companyId

        editor.apply();//store user info
    }


    //store user log in status
    public void isUserLogin(boolean flag)
    {
        sharedPreferences = context.getSharedPreferences(IS_LOGIN,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putBoolean("login",flag);
        editor.apply();
    }





    //get user information, call this function at another class

    //get user log in status as true or false
    public boolean getLoginStatus()
    {
        sharedPreferences = context.getSharedPreferences(IS_LOGIN,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("login",false);//return user log in status
    }

    //getting current user id
    public String getUserId()
    {
        sharedPreferences = context.getSharedPreferences(UserInfo,Context.MODE_PRIVATE);
        return sharedPreferences.getString("userId","none");//return user id
    }

    //getting current phone number
    public String getUserName()
    {
        sharedPreferences = context.getSharedPreferences(UserInfo,Context.MODE_PRIVATE);
        return sharedPreferences.getString("name","none");//return current user name
    }

    //getting current phone number
    public String getPhoneNumber()
    {
        sharedPreferences = context.getSharedPreferences(UserInfo,Context.MODE_PRIVATE);
        return sharedPreferences.getString("phone","none");//return current user phone number
    }

    //getting current email
    public String getEmail()
    {
        sharedPreferences = context.getSharedPreferences(UserInfo,Context.MODE_PRIVATE);
        return sharedPreferences.getString("email","none");//return current user email
    }

    //getting current email
    public String getCompanyId()
    {
        sharedPreferences = context.getSharedPreferences(UserInfo,Context.MODE_PRIVATE);
        return sharedPreferences.getString("companyId","none");//return current user email
    }


    //getting current user type
    public String getUserType()
    {
        sharedPreferences = context.getSharedPreferences(UserInfo,Context.MODE_PRIVATE);
        return sharedPreferences.getString("userType","none");//return current user email
    }

    //clear all data when user log out
    //clear all stored data when user log out
    public void clearData()
    {
        String[] prefNames = new String[]{UserInfo};
        for(int i=0;i<prefNames.length-1;i++)
        {
            sharedPreferences = context.getSharedPreferences(prefNames[i],Context.MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
        }
    }


}
