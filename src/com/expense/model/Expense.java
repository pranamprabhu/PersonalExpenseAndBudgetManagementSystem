package com.expense.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Model class representing an individual expense transaction.
 */
public class Expense implements Comparable<Expense> {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private int id;
    private LocalDate date;
    private Category category;
    private double amount;
    private String description;
    private String paymentMethod;

    public Expense(int id, LocalDate date, Category category, double amount, String description, String paymentMethod) {
        if (amount < 0) {
            throw new IllegalArgumentException("Expense amount cannot be negative.");
        }
        this.id = id;
        this.date = Objects.requireNonNull(date, "Date cannot be null.");
        this.category = Objects.requireNonNull(category, "Category cannot be null.");
        this.amount = amount;
        this.description = description == null ? "" : description.trim();
        this.paymentMethod = paymentMethod == null ? "Cash" : paymentMethod.trim();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = Objects.requireNonNull(date, "Date cannot be null.");
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = Objects.requireNonNull(category, "Category cannot be null.");
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Expense amount cannot be negative.");
        }
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod == null ? "Cash" : paymentMethod.trim();
    }

    /**
     * Converts the expense record into a CSV string.
     * Format: id,date,category,amount,description,paymentMethod
     */
    public String toCsv() {
        // Escape commas and quotes in description if needed
        String safeDescription = description.replace("\"", "\"\"");
        if (safeDescription.contains(",") || safeDescription.contains("\"")) {
            safeDescription = "\"" + safeDescription + "\"";
        }
        return String.format("%d,%s,%s,%.2f,%s,%s",
                id,
                date.format(DATE_FORMATTER),
                category.name(),
                amount,
                safeDescription,
                paymentMethod);
    }

    /**
     * Parses a CSV line into an Expense instance.
     */
    public static Expense fromCsv(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }

        // Parse CSV handling optional quotes
        String[] parts = parseCsvLine(csvLine);
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid CSV line format: " + csvLine);
        }

        int id = Integer.parseInt(parts[0].trim());
        LocalDate date = LocalDate.parse(parts[1].trim(), DATE_FORMATTER);
        Category category = Category.valueOf(parts[2].trim().toUpperCase());
        double amount = Double.parseDouble(parts[3].trim());
        String description = parts[4].trim();
        String paymentMethod = parts.length > 5 ? parts[5].trim() : "Cash";

        return new Expense(id, date, category, amount, description, paymentMethod);
    }

    private static String[] parseCsvLine(String line) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    @Override
    public int compareTo(Expense other) {
        int dateCmp = this.date.compareTo(other.date);
        if (dateCmp != 0) {
            return dateCmp;
        }
        return Integer.compare(this.id, other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return id == expense.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[ID: %04d] %s | %-22s | Rs. %-10.2f | %-12s | %s",
                id,
                date.format(DATE_FORMATTER),
                category.getDisplayName(),
                amount,
                paymentMethod,
                description);
    }
}
