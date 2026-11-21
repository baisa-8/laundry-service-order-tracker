package com.laundry;


public class Staff extends User {

    public Staff() {
        super(0, "Counter Staff");
    }

    public Staff(int id, String name) {
        super(id, name);
    }

    @Override
    public void showProfile() {
        System.out.println("========================================");
        System.out.println("            STAFF PROFILE");
        System.out.println("========================================");
        System.out.println("Staff ID : " + getId());
        System.out.println("Name     : " + getName());
        System.out.println("Role     : Counter Staff (Built-in ID 0)");
        System.out.println("========================================");
    }
}
