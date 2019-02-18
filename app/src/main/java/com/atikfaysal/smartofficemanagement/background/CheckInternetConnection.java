package com.atikfaysal.smartofficemanagement.background;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckInternetConnection
{
     private Context context;

     public CheckInternetConnection(Context context)
     {
          this.context = context;
     }

     public boolean isOnline()
     {
          ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Service.CONNECTIVITY_SERVICE);
          if(connectivityManager!=null)
          {
               NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
               if(networkInfo!=null)
                    return networkInfo.getState() == NetworkInfo.State.CONNECTED;

          }
          return false;
     }
}
