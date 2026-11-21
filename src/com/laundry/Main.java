package com.laundry;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;


public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static LaundryService laundryService;
   // private static final Staff staffMember = new Staff(); // Built-in staff with ID 0

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  WELCOME TO LAUNDRY SERVICE ORDER TRACKER");
        System.out.println("==================================================");

        try {
            laundryService = new LaundryService();
        } catch (FileManager.FileValidationException e) {
            System.err.println("\n[CRITICAL FILE ERROR]");
            System.err.println(e.getMessage());
            System.err.println("Startup halted to preserve data integrity. Please correct the file and restart.");
            return;
        } catch (IOException e) {
            System.err.println("\n[I/O ERROR]");
            System.err.println("Could not initialize storage: " + e.getMessage());
            return;
        }

        runMainMenu();
        System.out.println("\nThank you for using Laundry Service Order Tracker. Goodbye!");
    }

    private static void runMainMenu() {
        while (true) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Staff menu");
            System.out.println("2. Customer menu");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    runStaffMenu();
                    break;
                case "2":
                    runCustomerLogin();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 0.");
            }
        }
    }

    // ==========================================
    // STAFF MENU
    // ==========================================
    private static void runStaffMenu() {
        while (true) {
            System.out.println("\n--- STAFF MENU ---");
            System.out.println("1. Register customer");
            System.out.println("2. View customers");
            System.out.println("3. Add laundry order");
            System.out.println("4. Update order status");
            System.out.println("5. View orders and reports");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleRegisterCustomer();
                    break;
                case "2":
                    handleViewCustomers();
                    break;
                case "3":
                    handleAddOrder();
                    break;
                case "4":
                    handleUpdateOrderStatus();
                    break;
                case "5":
                    handleViewOrdersAndReports();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a number between 0 and 5.");
            }
        }
    }

    private static void handleRegisterCustomer() {
        System.out.println("\n--- Register New Customer ---");
        String name;
        while (true) {
            System.out.print("Enter customer name (or '0' to cancel): ");
            name = scanner.nextLine().trim();
            if ("0".equals(name)) return;
            if (name.isEmpty()) {
                System.out.println("Error: Name cannot be blank.");
                continue;
            }
            if (name.contains("|")) {
                System.out.println("Error: Name cannot contain the '|' character.");
                continue;
            }
            break;
        }

        String phone;
        while (true) {
            System.out.print("Enter 10-digit phone number (or '0' to cancel): ");
            phone = scanner.nextLine().trim();
            if ("0".equals(phone)) return;
            if (!phone.matches("^[0-9]{10}$")) {
                System.out.println("Error: Phone number must be exactly 10 digits.");
                continue;
            }
            break;
        }

        laundryService.registerCustomer(name, phone);
    }

    private static void handleViewCustomers() {
        System.out.println("\n--- Customer Records ---");
        List<Customer> list = laundryService.getCustomers();
        if (list.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        System.out.printf("%-6s | %-20s | %-12s%n", "ID", "Name", "Phone");
        System.out.println("----------------------------------------------");
        for (Customer c : list) {
            System.out.printf("%-6d | %-20s | %-12s%n", c.getId(), c.getName(), c.getPhone());
        }
    }

    private static void handleAddOrder() {
        System.out.println("\n--- Add Laundry Order ---");
        if (laundryService.getCustomers().isEmpty()) {
            System.out.println("No customers registered yet. Please register a customer first.");
            return;
        }

        int customerId;
        while (true) {
            System.out.print("Enter customer ID (or '0' to cancel): ");
            String input = scanner.nextLine().trim();
            if ("0".equals(input)) return;
            try {
                customerId = Integer.parseInt(input);
                if (laundryService.findCustomerById(customerId) == null) {
                    System.out.println("No customer found with ID " + customerId + ". Please try again.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid numeric ID.");
            }
        }

        int[] quantities = new int[3];
        System.out.println("Enter service quantities (0 - 50 each; at least one must be > 0):");
        for (int i = 0; i < 3; i++) {
            String serviceName = LaundryOrder.SERVICE_NAMES[i];
            int rate = LaundryOrder.RATES[i];
            while (true) {
                System.out.printf("  %s (%d rupees/item): ", serviceName, rate);
                String input = scanner.nextLine().trim();
                try {
                    int qty = Integer.parseInt(input);
                    if (qty < 0 || qty > 50) {
                        System.out.println("  Quantity must be between 0 and 50.");
                        continue;
                    }
                    quantities[i] = qty;
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("  Please enter a valid whole number.");
                }
            }
        }

        if (quantities[0] == 0 && quantities[1] == 0 && quantities[2] == 0) {
            System.out.println("Order rejected: At least one quantity must be greater than zero.");
            return;
        }

        laundryService.addOrder(customerId, quantities);
    }

    private static void handleUpdateOrderStatus() {
        System.out.println("\n--- Update Order Status ---");
        List<LaundryOrder> orders = laundryService.getOrders();
        if (orders.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        int orderId;
        LaundryOrder order;
        while (true) {
            System.out.print("Enter Order ID (or '0' to cancel): ");
            String input = scanner.nextLine().trim();
            if ("0".equals(input)) return;
            try {
                orderId = Integer.parseInt(input);
                order = laundryService.findOrderById(orderId);
                if (order == null) {
                    System.out.println("Order ID " + orderId + " not found. Please try again.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid numeric Order ID.");
            }
        }

        System.out.print("Current details: ");
        order.displayOrderDetails();

        String currentStatus = order.getStatus();
        if (LaundryOrder.STATUS_COLLECTED.equalsIgnoreCase(currentStatus)) {
            System.out.println("This order is already COLLECTED. No further transitions allowed.");
            return;
        }

        String allowedNext = LaundryOrder.STATUS_PENDING.equalsIgnoreCase(currentStatus)
                ? LaundryOrder.STATUS_READY
                : LaundryOrder.STATUS_COLLECTED;

        System.out.printf("Allowed next status: %s%n", allowedNext);
        System.out.printf("Update order status to %s? (y/n): ", allowedNext);
        String confirm = scanner.nextLine().trim().toLowerCase();
        if ("y".equals(confirm) || "yes".equals(confirm)) {
            laundryService.updateStatus(order.getId(), allowedNext);
        } else {
            System.out.println("Status update cancelled.");
        }
    }

    private static void handleViewOrdersAndReports() {
        System.out.println("\n--- Orders List ---");
        List<LaundryOrder> orders = laundryService.getOrders();
        if (orders.isEmpty()) {
            System.out.println("No records found.");
        } else {
            for (LaundryOrder o : orders) {
                o.displayOrderDetails();
            }
        }

        System.out.println();
        laundryService.showReport();

        System.out.print("Save report to data/report.txt? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if ("y".equals(confirm) || "yes".equals(confirm)) {
            laundryService.saveReportToFile();
        }
    }

    // ==========================================
    // CUSTOMER MENU
    // ==========================================
    private static void runCustomerLogin() {
        while (true) {
            System.out.print("\nEnter customer ID (or '0' to return to Main Menu): ");
            String input = scanner.nextLine().trim();
            if ("0".equals(input)) {
                return;
            }

            int customerId;
            try {
                customerId = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid numeric Customer ID.");
                continue;
            }

            Customer customer = laundryService.findCustomerById(customerId);
            if (customer == null) {
                System.out.println("Customer ID " + customerId + " not found. Please try again.");
                continue;
            }

            runCustomerMenu(customer);
            return;
        }
    }

    private static void runCustomerMenu(Customer customer) {
        while (true) {
            System.out.println("\n--- CUSTOMER MENU (Logged in as: " + customer.getName() + ") ---");
            System.out.println("1. View my profile");
            System.out.println("2. View my orders");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    // Demonstrates Polymorphism: calling showProfile on Customer object
                    System.out.println();
                    customer.showProfile();
                    break;
                case "2":
                    handleViewCustomerOrders(customer.getId());
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 0.");
            }
        }
    }

    private static void handleViewCustomerOrders(int customerId) {
        System.out.println("\n--- My Orders ---");
        List<LaundryOrder> list = laundryService.getOrdersByCustomerId(customerId);
        if (list.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        for (LaundryOrder o : list) {
            o.displayOrderDetails();
        }
    }
}
