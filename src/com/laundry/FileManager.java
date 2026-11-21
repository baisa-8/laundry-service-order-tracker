package com.laundry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileManager {
    private final Path dataDir;
    private final Path customersFile;
    private final Path ordersFile;
    private final Path reportFile;

    @SuppressWarnings("serial")
	public static class FileValidationException extends Exception {
        private final String fileName;
        private final int lineNumber;

        public FileValidationException(String fileName, int lineNumber, String message) {
            super(String.format("Data validation error in [%s] at line %d: %s", fileName, lineNumber, message));
            this.fileName = fileName;
            this.lineNumber = lineNumber;
        }

        public String getFileName() {
            return fileName;
        }

        public int getLineNumber() {
            return lineNumber;
        }
    }

    public FileManager() {
        this(Paths.get("data"));
    }

    public FileManager(Path dataDir) {
        this.dataDir = dataDir;
        this.customersFile = dataDir.resolve("customers.txt");
        this.ordersFile = dataDir.resolve("orders.txt");
        this.reportFile = dataDir.resolve("report.txt");
    }

    public Path getDataDir() {
        return dataDir;
    }

    /* Ensures data directory exists.*/
    public void ensureDataDir() throws IOException {
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }
    }

    
    public List<Customer> loadCustomers() throws IOException, FileValidationException {
        ensureDataDir();
        List<Customer> customerList = new ArrayList<>();
        Set<Integer> customerIds = new HashSet<>();
        Set<String> phoneSet = new HashSet<>();

        if (!Files.exists(customersFile)) {
            return customerList; // First run: empty list
        }

        try (BufferedReader reader = Files.newBufferedReader(customersFile, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue; // Skip accidental blank lines
                }

                String[] fields = line.split("\\|", -1);
                if (fields.length != 3) {
                    throw new FileValidationException("customers.txt", lineNumber,
                            "Expected 3 fields (id|name|phone), found " + fields.length);
                }

                int id;
                try {
                    id = Integer.parseInt(fields[0].trim());
                    if (id <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    throw new FileValidationException("customers.txt", lineNumber,
                            "Customer ID must be a positive integer. Found: '" + fields[0] + "'");
                }

                if (customerIds.contains(id)) {
                    throw new FileValidationException("customers.txt", lineNumber,
                            "Duplicate customer ID: " + id);
                }

                String name = fields[1].trim();
                if (name.isEmpty()) {
                    throw new FileValidationException("customers.txt", lineNumber,
                            "Customer name cannot be blank.");
                }

                String phone = fields[2].trim();
                if (!phone.matches("^[0-9]{10}$")) {
                    throw new FileValidationException("customers.txt", lineNumber,
                            "Customer phone must be exactly 10 digits. Found: '" + phone + "'");
                }

                if (phoneSet.contains(phone)) {
                    throw new FileValidationException("customers.txt", lineNumber,
                            "Duplicate phone number detected: " + phone);
                }

                customerIds.add(id);
                phoneSet.add(phone);
                customerList.add(new Customer(id, name, phone));
            }
        }
        return customerList;
    }

    
    public List<LaundryOrder> loadOrders(Set<Integer> existingCustomerIds) throws IOException, FileValidationException {
        ensureDataDir();
        List<LaundryOrder> orderList = new ArrayList<>();
        Set<Integer> orderIds = new HashSet<>();

        if (!Files.exists(ordersFile)) {
            return orderList; // First run: empty list
        }

        try (BufferedReader reader = Files.newBufferedReader(ordersFile, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] fields = line.split("\\|", -1);
                if (fields.length != 4) {
                    throw new FileValidationException("orders.txt", lineNumber,
                            "Expected 4 fields (id|customerId|quantities|status), found " + fields.length);
                }

                int id;
                try {
                    id = Integer.parseInt(fields[0].trim());
                    if (id <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    throw new FileValidationException("orders.txt", lineNumber,
                            "Order ID must be a positive integer. Found: '" + fields[0] + "'");
                }

                if (orderIds.contains(id)) {
                    throw new FileValidationException("orders.txt", lineNumber,
                            "Duplicate order ID: " + id);
                }

                int customerId;
                try {
                    customerId = Integer.parseInt(fields[1].trim());
                } catch (NumberFormatException e) {
                    throw new FileValidationException("orders.txt", lineNumber,
                            "Customer ID must be an integer. Found: '" + fields[1] + "'");
                }

                if (!existingCustomerIds.contains(customerId)) {
                    throw new FileValidationException("orders.txt", lineNumber,
                            "Order references nonexistent customer ID: " + customerId);
                }

                String[] qParts = fields[2].split(",", -1);
                if (qParts.length != 3) {
                    throw new FileValidationException("orders.txt", lineNumber,
                            "Quantities field must contain exactly 3 comma-separated numbers (Wash,Dry,Iron). Found: '" + fields[2] + "'");
                }

                int[] quantities = new int[3];
                int totalQuantity = 0;
                for (int i = 0; i < 3; i++) {
                    try {
                        quantities[i] = Integer.parseInt(qParts[i].trim());
                    } catch (NumberFormatException e) {
                        throw new FileValidationException("orders.txt", lineNumber,
                                "Quantity at index " + i + " is not a valid integer: '" + qParts[i] + "'");
                    }
                    if (quantities[i] < 0 || quantities[i] > 50) {
                        throw new FileValidationException("orders.txt", lineNumber,
                                "Quantity for " + LaundryOrder.SERVICE_NAMES[i] + " must be between 0 and 50. Found: " + quantities[i]);
                    }
                    totalQuantity += quantities[i];
                }

                if (totalQuantity <= 0) {
                    throw new FileValidationException("orders.txt", lineNumber,
                            "Order must have at least one positive quantity.");
                }

                String status = fields[3].trim().toUpperCase();
                if (!LaundryOrder.STATUS_PENDING.equals(status) &&
                    !LaundryOrder.STATUS_READY.equals(status) &&
                    !LaundryOrder.STATUS_COLLECTED.equals(status)) {
                    throw new FileValidationException("orders.txt", lineNumber,
                            "Invalid order status: '" + fields[3] + "'. Must be PENDING, READY, or COLLECTED.");
                }

                orderIds.add(id);
                orderList.add(new LaundryOrder(id, customerId, quantities, status));
            }
        }
        return orderList;
    }

    
    public void saveCustomers(List<Customer> customerList) throws IOException  {
        ensureDataDir();
        Path tempFile = Files.createTempFile(dataDir, "customers_", ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                for (Customer c : customerList) {
                    writer.write(c.getId() + "|" + c.getName() + "|" + c.getPhone());
                    writer.newLine();
                }
            }
            Files.move(tempFile, customersFile, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    
    public void saveOrders(List<LaundryOrder> orderList) throws IOException {
        ensureDataDir();
        Path tempFile = Files.createTempFile(dataDir, "orders_", ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                for (LaundryOrder o : orderList) {
                    int[] q = o.getQuantities();
                    writer.write(o.getId() + "|" + o.getCustomerId() + "|" + q[0] + "," + q[1] + "," + q[2] + "|" + o.getStatus());
                    writer.newLine();
                }
            }
            Files.move(tempFile, ordersFile, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    
    public void saveReport(String reportContent) throws IOException {
        ensureDataDir();
        Path tempFile = Files.createTempFile(dataDir, "report_", ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                writer.write(reportContent);
            }
            Files.move(tempFile, reportFile, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
