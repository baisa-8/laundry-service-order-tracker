package com.laundry;


public class LaundryOrder {
    public static final String[] SERVICE_NAMES = {"Wash", "Dry", "Iron"};
    public static final int[] RATES = {50, 30, 20}; // Rupees per item

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_COLLECTED = "COLLECTED";

    private final int id;
    private final int customerId;
    private final int[] quantities; // Index 0: Wash, Index 1: Dry, Index 2: Iron
    private String status;

    public LaundryOrder(int id, int customerId, int[] quantities, String status) {
        if (quantities == null || quantities.length != 3) {
            throw new IllegalArgumentException("Quantities must have exactly 3 service values.");
        }
        this.id = id;
        this.customerId = customerId;
        // Defensive copying with clone() to ensure encapsulation
        this.quantities = quantities.clone();
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getCustomerId() {
        return customerId;
    }

    /**
     * Returns a copy of the quantities array to prevent outside modification.
     */
    public int[] getQuantities() {
        return quantities.clone();
    }

    public String getStatus() {
        return status;
    }

    /**
     * Calculates the total charge using service rates.
     */
    public int calculateCharge() {
        int charge = 0;
        for (int i = 0; i < quantities.length; i++) {
            charge += quantities[i] * RATES[i];
        }
        return charge;
    }

   
    public boolean changeStatus(String newStatus) {
        if (newStatus == null) {
            return false;
        }
        newStatus = newStatus.trim().toUpperCase();

        if (STATUS_PENDING.equalsIgnoreCase(this.status) && STATUS_READY.equalsIgnoreCase(newStatus)) {
            this.status = STATUS_READY;
            return true;
        } else if (STATUS_READY.equalsIgnoreCase(this.status) && STATUS_COLLECTED.equalsIgnoreCase(newStatus)) {
            this.status = STATUS_COLLECTED;
            return true;
        }
        return false;
    }

    public void displayOrderDetails() {
        System.out.printf("Order #%d | Customer #%d | Wash: %d, Dry: %d, Iron: %d | Charge: %d rupees | Status: %s%n",
                id, customerId, quantities[0], quantities[1], quantities[2], calculateCharge(), status);
    }

    @Override
    public String toString() {
        return String.format("Order #%d [Customer ID: %d, Wash: %d, Dry: %d, Iron: %d, Total: %d rupees, Status: %s]",
                id, customerId, quantities[0], quantities[1], quantities[2], calculateCharge(), status);
    }
}
