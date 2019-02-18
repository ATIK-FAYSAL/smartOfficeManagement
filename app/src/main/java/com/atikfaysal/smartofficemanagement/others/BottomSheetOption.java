package com.atikfaysal.smartofficemanagement.others;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.atikfaysal.smartofficemanagement.R;

public class BottomSheetOption
{
    private BottomSheetDialog dialog;
    private Context context;
    private Activity activity;

    private String value;

    public BottomSheetOption(Context context) {
        this.context = context;
        activity = (Activity)context;
    }

    @SuppressLint("SetTextI18n")
    public void initBottomOptions(final String type, final TextView txtOutput)
    {
        @SuppressLint("InflateParams")
        View view = activity.getLayoutInflater().inflate(R.layout.bottom_sheet_layout,null);

        RadioGroup radioGender,radioBloodGroup;

        radioGender = view.findViewById(R.id.radioGroupA);
        radioBloodGroup = view.findViewById(R.id.radioGroupB);
        TextView bDone = view.findViewById(R.id.txtDone);
        TextView bCancel = view.findViewById(R.id.txtCancel);
        TextView txtText = view.findViewById(R.id.txtText);
        txtText.setText("Select "+type);

        dialog = new BottomSheetDialog(context);
        dialog.setContentView(view);//set view
        dialog.setCanceledOnTouchOutside(true);//dismiss dialog when user click on outside
        dialog.setCancelable(true);

        switch (type) {

            case "gender"://select gender

                radioBloodGroup.setVisibility(View.GONE);//invisible blood group layout
                radioGender.setVisibility(View.VISIBLE);//visible gender layout

                radioGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int id) {
                        selectGender(id);//select gender layout
                    }
                });

                break;

            case "bloodGroup"://select blood group
                radioBloodGroup.setVisibility(View.VISIBLE);//visible blood group layout
                radioGender.setVisibility(View.GONE);//invisible gender layout

                radioBloodGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int id) {
                        selectBloodGroup(id);//select blood group
                    }
                });
                break;
        }


        bDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtOutput.setText(value);//set selected value to output box
                dialog.dismiss();//dismiss dialog
            }
        });

        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //getting selected price range from radio group
    private void selectBloodGroup(int id)
    {
        switch (id) {

            case R.id.radioGroup1:
                value = "A+";
                break;
            case R.id.radioGroup2:
                value = "B+";
                break;
            case R.id.radioGroup3:
                value = "O+";
                break;
            case R.id.radioGroup4:
                value = "A-";
                break;

            case R.id.radioGroup5:
                value = "B-";
                break;

            case R.id.radioGroup6:
                value = "O-";
                break;

            case R.id.radioGroup7:
                value = "AB+";
                break;
            case R.id.radioGroup8:
                value = "AB-";
                break;
        }
    }

    //getting selected category from radio group
    private void selectGender(int id)
    {
        switch (id) {

            case R.id.radioMale:
                value = "male";
                break;
            case R.id.radioFemale:
                value = "female";
                break;
            case R.id.radioOthers:
                value = "others";
                break;
        }
    }

}
