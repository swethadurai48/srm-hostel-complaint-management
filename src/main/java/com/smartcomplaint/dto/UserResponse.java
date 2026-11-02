package com.smartcomplaint.dto;

import com.smartcomplaint.model.Role;
import com.smartcomplaint.model.User;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private String studentId;
    private String department;
    private String roomOrHostel;
    private String yearOfStudy;
    private String phoneNumber;
    private String hostel;
    private String block;
    private String roomNumber;

    public UserResponse() {
    }

    public static UserResponse fromEntity(User user) {
        if (user == null) return null;
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStudentId(user.getStudentId());
        dto.setDepartment(user.getDepartment());
        dto.setRoomOrHostel(user.getRoomOrHostel());
        dto.setYearOfStudy(user.getYearOfStudy());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setHostel(user.getHostel());
        dto.setBlock(user.getBlock());
        dto.setRoomNumber(user.getRoomNumber());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRoomOrHostel() {
        return roomOrHostel;
    }

    public void setRoomOrHostel(String roomOrHostel) {
        this.roomOrHostel = roomOrHostel;
    }

    public String getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(String yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getHostel() {
        return hostel;
    }

    public void setHostel(String hostel) {
        this.hostel = hostel;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}
