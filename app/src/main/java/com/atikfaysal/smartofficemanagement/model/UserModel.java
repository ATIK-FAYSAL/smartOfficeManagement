package com.atikfaysal.smartofficemanagement.model;

public class UserModel
{

    private String userId,employeeId,name,email,phone,address,dob,gender,userType,bloodGroup,password,empStatus,date,image;
    private String companyName,department,designation,salary,inTime,outTime,offDay,companyId;

    public UserModel(String userId, String employeeId, String name,
                     String email, String phone, String address,
                     String dob, String gender, String userType,
                     String bloodGroup, String password, String empStatus,
                     String date, String image, String companyName, String department,
                     String designation, String salary, String inTime, String outTime, String offDay, String companyId) {

        this.userId = userId;
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dob = dob;
        this.gender = gender;
        this.userType = userType;
        this.bloodGroup = bloodGroup;
        this.password = password;
        this.empStatus = empStatus;
        this.date = date;
        this.image = image;
        this.companyName = companyName;
        this.department = department;
        this.designation = designation;
        this.salary = salary;
        this.inTime = inTime;
        this.outTime = outTime;
        this.offDay = offDay;
        this.companyId = companyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmpStatus() {
        return empStatus;
    }

    public void setEmpStatus(String empStatus) {
        this.empStatus = empStatus;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getInTime() {
        return inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public String getOffDay() {
        return offDay;
    }

    public String getCompanyId() {
        return companyId;
    }

}
