package com.atikfaysal.smartofficemanagement.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.atikfaysal.smartofficemanagement.R;

import com.atikfaysal.smartofficemanagement.main.Profile;
import com.atikfaysal.smartofficemanagement.model.EmployeeModel;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder>
{

    private List<EmployeeModel> employeeModels;
    private Context context;
    private LayoutInflater inflater;
    private Activity activity;

    public EmployeeAdapter(Context context, List<EmployeeModel>models)
    {
        this.context = context;
        this.employeeModels = models;
        inflater = LayoutInflater.from(context);
        activity = (Activity)context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_employee_model,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmployeeModel model = employeeModels.get(position);
        holder.setData(model,position);
        holder.setListener();
    }

    @Override
    public int getItemCount() {
        return employeeModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private CircleImageView imageView;
        private TextView txtName,txtEmpId;
        private TextView txtMessage,txtAttendance;
        private TextView txtPhone,txtEmail;
        private CardView cardView;

        private EmployeeModel employeeModel;

        private ViewHolder(View view) {
            super(view);

            imageView = view.findViewById(R.id.imgProPic);
            txtName = view.findViewById(R.id.txtEmpName);
            txtEmpId = view.findViewById(R.id.txtEmpId);
            txtPhone = view.findViewById(R.id.txtPhone);
            txtEmail = view.findViewById(R.id.txtEmail);
            txtMessage = view.findViewById(R.id.txtMessage);
            txtAttendance = view.findViewById(R.id.txtAttendance);
            cardView = view.findViewById(R.id.cardViewA);
        }

        @SuppressLint("SetTextI18n")
        private void setData(EmployeeModel model, int pos)
        {

            employeeModel = model;
            txtName.setText(model.getEmpName());
            txtEmpId.setText(model.getEmpId());
            txtEmail.setText(model.getEmail());
            txtPhone.setText(model.getPhone());

            if(!model.getEmpImage().equals("null"))
            {
                Glide.with(context).
                        load(model.getEmpImage()).
                        into(imageView);
            }
        }

        private void setListener()
        {
            txtMessage.setOnClickListener(EmployeeAdapter.ViewHolder.this);
            txtAttendance.setOnClickListener(EmployeeAdapter.ViewHolder.this);
            cardView.setOnClickListener(EmployeeAdapter.ViewHolder.this);
        }

        @Override
        public void onClick(View view) {

            Intent intent;
            switch (view.getId())
            {
                case R.id.cardViewA:
                    intent = new Intent(context, Profile.class);
                    intent.putExtra("userId",employeeModel.getUserId());//passing user id
                    activity.startActivity(intent);//start new activity
                    break;
            }
        }
    }


}

