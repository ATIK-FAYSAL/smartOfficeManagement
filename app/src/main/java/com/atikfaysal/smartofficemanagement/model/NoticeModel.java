package com.atikfaysal.smartofficemanagement.model;

public class NoticeModel
{
    private String userName,userId,title,notice,priority,date;

    public NoticeModel(String userName, String userId, String title, String notice, String priority, String date) {
        this.userName = userName;
        this.userId = userId;
        this.title = title;
        this.notice = notice;
        this.priority = priority;
        this.date = date;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getNotice() {
        return notice;
    }

    public String getPriority() {
        return priority;
    }

    public String getDate() {
        return date;
    }
}
