package com.atikfaysal.smartofficemanagement.model;

public class EmployeeModel
{

    private String empName,empId,empImage,userId,phone,email;


    public EmployeeModel(String empName, String empId, String empImage, String userId,String phone,String email) {
        this.empName = empName;
        this.empId = empId;
        this.empImage = empImage;
        this.userId = userId;
        this.phone = phone;
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getEmpName() {
        return empName;
    }

    public String getEmpId() {
        return empId;
    }

    public String getEmpImage() {
        return empImage;
    }

    public String getUserId() {
        return userId;
    }
}
