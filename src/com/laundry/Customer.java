package com.laundry;


public class Customer extends User {
    private final String phone;

    public Customer(int id, String name, String phone) {
        super(id, name);
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public void showProfile() {
        System.out.println("========================================");
        System.out.println("           CUSTOMER PROFILE");
        System.out.println("========================================");
        System.out.println("Customer ID : " + getId());
        System.out.println("Name        : " + getName());
        System.out.println("Phone       : " + phone);
        System.out.println("========================================");
    }
}
