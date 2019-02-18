package com.atikfaysal.smartofficemanagement.others;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.atikfaysal.smartofficemanagement.R;

public class MyQrCode extends AppCompatActivity implements View.OnClickListener
{
    private Button bMyQr,bScanQr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_qr_code);

        bMyQr = findViewById(R.id.bMyQr);
        bScanQr = findViewById(R.id.bScanQr);
        bMyQr.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_left_button));//set selecting button background

        bMyQr.setOnClickListener(this);
        bScanQr.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.bMyQr:
                bMyQr.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_left_button));//set selecting button background
                bScanQr.setBackgroundResource(0);//set default background
                break;
            case R.id.bScanQr:
                bScanQr.setBackgroundDrawable(getResources().getDrawable(R.drawable.style_right_button));//set selecting button background
                bMyQr.setBackgroundResource(0);//set default background
                break;
        }

    }
}
