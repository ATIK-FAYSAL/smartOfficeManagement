package com.atikfaysal.smartofficemanagement.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.atikfaysal.smartofficemanagement.R;
import com.atikfaysal.smartofficemanagement.main.Profile;
import com.atikfaysal.smartofficemanagement.model.NoticeModel;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder>
{
    private List<NoticeModel> noticeModels;
    private Context context;
    private LayoutInflater inflater;
    private Activity activity;

    public NoticeAdapter(Context context, List<NoticeModel>models)
    {
        this.context = context;
        this.noticeModels = models;
        inflater = LayoutInflater.from(context);
        activity = (Activity)context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.notice_model,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoticeModel model = noticeModels.get(position);
        holder.setData(model,position);
        holder.setListener();
    }

    @Override
    public int getItemCount() {
        return noticeModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        private TextView txtUserName,txtPriority,txtDate,txtNotice,txtTitle;
        private ImageButton imgExpand,imgColups;
        private NoticeModel noticeModel;

        private ViewHolder(View view) {
            super(view);

            txtUserName = view.findViewById(R.id.txtUserName);
            txtPriority = view.findViewById(R.id.txtPriority);
            txtDate = view.findViewById(R.id.txtDate);
            imgExpand = view.findViewById(R.id.imgExpand);
            imgColups = view.findViewById(R.id.imgColaps);
            txtNotice = view.findViewById(R.id.txtNotice);
            txtTitle = view.findViewById(R.id.txtTitle);

            if(txtNotice.getLineCount()>3)
                imgExpand.setVisibility(View.VISIBLE);

        }

        @SuppressLint("SetTextI18n")
        private void setData(NoticeModel model, int pos)
        {
            noticeModel = model;
            txtUserName.setText(model.getUserName());
            txtDate.setText(model.getDate());
            txtNotice.setText(model.getNotice());
            txtTitle.setText(model.getTitle());

            switch (model.getPriority())
            {
                case "5":txtPriority.setText("High");//set priority as high
                    break;

                case "3":txtPriority.setText("Medium");//set priority as medium
                    break;

                case "1":txtPriority.setText("Low");//set priority as low
                    break;
            }

        }

        private void setListener()
        {
            txtUserName.setOnClickListener(this);
            imgExpand.setOnClickListener(this);
            imgColups.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.txtUserName:

                    Intent intent = new Intent(context, Profile.class);
                    intent.putExtra("userId",noticeModel.getUserId());//passing
                    context.startActivity(intent);//start new activity

                    break;

                case R.id.imgColaps:

                    imgColups.setVisibility(View.GONE);//visibility gone
                    imgExpand.setVisibility(View.VISIBLE);//visibility visible
                    txtNotice.setMaxLines(3);//set max line 3

                    break;

                case R.id.imgExpand:
                    imgColups.setVisibility(View.VISIBLE);//visibility visible
                    imgExpand.setVisibility(View.GONE);//visibility gone
                    txtNotice.setMaxLines(Integer.MAX_VALUE);
                    break;

            }
        }
    }

}
