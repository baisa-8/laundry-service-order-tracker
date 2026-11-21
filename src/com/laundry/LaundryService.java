package com.laundry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LaundryService {
    private final ArrayList<Customer> customers;
    private final ArrayList<LaundryOrder> orders;
    private final HashSet<String> phones;
    private final FileManager fileManager;

    private int nextCustomerId = 1;
    private int nextOrderId = 1;

    public LaundryService() throws IOException, FileManager.FileValidationException {
        this(new FileManager());
    }

    public LaundryService(FileManager fileManager) throws IOException, FileManager.FileValidationException {
        this.fileManager = fileManager;
        this.customers = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.phones = new HashSet<>();
        loadData();
    }

    
    private void loadData() throws IOException, FileManager.FileValidationException {
        // Step 1: Load customers into temporary list
        List<Customer> loadedCustomers = fileManager.loadCustomers();

        // Step 2: Validate customer IDs and build phone set
        Set<Integer> customerIds = new HashSet<>();
        HashSet<String> loadedPhones = new HashSet<>();
        int maxCustId = 0;

        for (Customer c : loadedCustomers) {
            customerIds.add(c.getId());
            loadedPhones.add(c.getPhone());
            if (c.getId() > maxCustId) {
                maxCustId = c.getId();
            }
        }

        // Step 3: Load orders referencing valid customer IDs
        List<LaundryOrder> loadedOrders = fileManager.loadOrders(customerIds);
        int maxOrderId = 0;
        for (LaundryOrder o : loadedOrders) {
            if (o.getId() > maxOrderId) {
                maxOrderId = o.getId();
            }
        }

        // Only accept data when all records are valid
        this.customers.addAll(loadedCustomers);
        this.orders.addAll(loadedOrders);
        this.phones.addAll(loadedPhones);

        this.nextCustomerId = (maxCustId > 0) ? maxCustId + 1 : 1;
        this.nextOrderId = (maxOrderId > 0) ? maxOrderId + 1 : 1;
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public ArrayList<LaundryOrder> getOrders() {
        return orders;
    }

    public Customer findCustomerById(int id) {
        for (Customer c : customers) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public LaundryOrder findOrderById(int id) {
        for (LaundryOrder o : orders) {
            if (o.getId() == id) {
                return o;
            }
        }
        return null;
    }

    public List<LaundryOrder> getOrdersByCustomerId(int customerId) {
        List<LaundryOrder> list = new ArrayList<>();
        for (LaundryOrder o : orders) {
            if (o.getCustomerId() == customerId) {
                list.add(o);
            }
        }
        return list;
    }

    
    public Customer registerCustomer(String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: Customer name cannot be blank.");
            return null;
        }
        name = name.trim();
        if (name.contains("|") || name.contains("\n") || name.contains("\r")) {
            System.out.println("Error: Name contains invalid characters ('|' or newline).");
            return null;
        }

        if (phone == null) {
            System.out.println("Error: Phone number is required.");
            return null;
        }
        phone = phone.trim();
        if (!phone.matches("^[0-9]{10}$")) {
            System.out.println("Error: Phone number must contain exactly 10 digits.");
            return null;
        }

        if (phones.contains(phone)) {
            System.out.println("Customer phone already exists");
            return null;
        }

        Customer newCustomer = new Customer(nextCustomerId, name, phone);

        // Prepare temporary list to save safely
        List<Customer> tempCustomers = new ArrayList<>(customers);
        tempCustomers.add(newCustomer);

        try {
            fileManager.saveCustomers(tempCustomers);
            // In-memory update only after successful file save
            customers.add(newCustomer);
            phones.add(phone);
            nextCustomerId++;
            System.out.println("Customer " + newCustomer.getId() + " saved.");
            return newCustomer;
        } catch (IOException e) {
            System.out.println("File save error: " + e.getMessage() + ". Registration cancelled.");
            return null;
        }
    }

    
    public LaundryOrder addOrder(int customerId, int[] quantities) {
        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Error: Customer with ID " + customerId + " does not exist.");
            return null;
        }

        if (quantities == null || quantities.length != 3) {
            System.out.println("Error: Quantities must contain exactly 3 service values (Wash, Dry, Iron).");
            return null;
        }

        int totalQuantity = 0;
        for (int i = 0; i < 3; i++) {
            if (quantities[i] < 0 || quantities[i] > 50) {
                System.out.println("Error: Quantity for " + LaundryOrder.SERVICE_NAMES[i] + " must be between 0 and 50.");
                return null;
            }
            totalQuantity += quantities[i];
        }

        if (totalQuantity <= 0) {
            System.out.println("Error: At least one quantity must be greater than zero.");
            return null;
        }

        LaundryOrder newOrder = new LaundryOrder(nextOrderId, customerId, quantities, LaundryOrder.STATUS_PENDING);

        List<LaundryOrder> tempOrders = new ArrayList<>(orders);
        tempOrders.add(newOrder);

        try {
            fileManager.saveOrders(tempOrders);
            // In-memory update only after successful file save
            orders.add(newOrder);
            nextOrderId++;
            System.out.printf("Order %d saved. Charge: %d rupees%nStatus: %s%n",
                    newOrder.getId(), newOrder.calculateCharge(), newOrder.getStatus());
            return newOrder;
        } catch (IOException e) {
            System.out.println("File save error: " + e.getMessage() + ". Order cancelled.");
            return null;
        }
    }

    
    public boolean updateStatus(int orderId, String newStatus) {
        LaundryOrder order = findOrderById(orderId);
        if (order == null) {
            System.out.println("Error: Order with ID " + orderId + " not found.");
            return false;
        }

        if (newStatus == null) {
            System.out.println("Error: Invalid status.");
            return false;
        }
        newStatus = newStatus.trim().toUpperCase();

        // Validate allowed transitions
        String currentStatus = order.getStatus();
        boolean validTransition = (LaundryOrder.STATUS_PENDING.equalsIgnoreCase(currentStatus) &&
                                    LaundryOrder.STATUS_READY.equalsIgnoreCase(newStatus)) ||
                                   (LaundryOrder.STATUS_READY.equalsIgnoreCase(currentStatus) &&
                                    LaundryOrder.STATUS_COLLECTED.equalsIgnoreCase(newStatus));

        if (!validTransition) {
            System.out.println("Error: Transition from " + currentStatus + " to " + newStatus + " is refused.");
            System.out.println("Allowed transitions: PENDING -> READY, READY -> COLLECTED.");
            return false;
        }

        // Create updated list for atomic save
        List<LaundryOrder> tempOrders = new ArrayList<>();
        for (LaundryOrder o : orders) {
            if (o.getId() == orderId) {
                tempOrders.add(new LaundryOrder(o.getId(), o.getCustomerId(), o.getQuantities(), newStatus));
            } else {
                tempOrders.add(o);
            }
        }

        try {
            fileManager.saveOrders(tempOrders);
            // In-memory update
            order.changeStatus(newStatus);
            System.out.println("Order " + orderId + " updated to " + newStatus + " successfully.");
            return true;
        } catch (IOException e) {
            System.out.println("File save error: " + e.getMessage() + ". Status change cancelled.");
            return false;
        }
    }

    public String generateReportSummary() {
        int pendingCount = 0;
        int readyCount = 0;
        int collectedCount = 0;
        int collectedSales = 0;

        for (LaundryOrder o : orders) {
            if (LaundryOrder.STATUS_PENDING.equalsIgnoreCase(o.getStatus())) {
                pendingCount++;
            } else if (LaundryOrder.STATUS_READY.equalsIgnoreCase(o.getStatus())) {
                readyCount++;
            } else if (LaundryOrder.STATUS_COLLECTED.equalsIgnoreCase(o.getStatus())) {
                collectedCount++;
                collectedSales += o.calculateCharge();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Total Customers: ").append(customers.size()).append(System.lineSeparator());
        sb.append("Total Orders: ").append(orders.size()).append(System.lineSeparator());
        sb.append("Pending Orders: ").append(pendingCount).append(System.lineSeparator());
        sb.append("Ready Orders: ").append(readyCount).append(System.lineSeparator());
        sb.append("Collected Orders: ").append(collectedCount).append(System.lineSeparator());
        sb.append("Collected Sales: ").append(collectedSales).append(" rupees").append(System.lineSeparator());
        return sb.toString();
    }

    public void showReport() {
        System.out.println("========================================");
        System.out.println("         LAUNDRY SERVICE REPORT");
        System.out.println("========================================");
        System.out.print(generateReportSummary());
        System.out.println("========================================");
    }

    public boolean saveReportToFile() {
        try {
            fileManager.saveReport(generateReportSummary());
            System.out.println("Report successfully saved to data/report.txt.");
            return true;
        } catch (IOException e) {
            System.out.println("Error saving report to file: " + e.getMessage());
            return false;
        }
    }
}
