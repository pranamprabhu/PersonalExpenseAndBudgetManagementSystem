package com.expense.storage;

import com.expense.model.Budget;
import com.expense.model.Expense;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistent file storage for Expenses and Budgets using CSV format.
 */
public class FileStorageService {
    private static final String DEFAULT_DATA_DIR = "data";
    private static final String EXPENSES_FILE = "expenses.csv";
    private static final String BUDGETS_FILE = "budgets.csv";

    private static final String EXPENSES_HEADER = "id,date,category,amount,description,paymentMethod";
    private static final String BUDGETS_HEADER = "yearMonth,category,monthlyLimit";

    private final Path expensesPath;
    private final Path budgetsPath;

    public FileStorageService() {
        this(DEFAULT_DATA_DIR);
    }

    public FileStorageService(String dataDir) {
        Path dir = Paths.get(dataDir);
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            System.err.println("Warning: Unable to create data directory: " + e.getMessage());
        }
        this.expensesPath = dir.resolve(EXPENSES_FILE);
        this.budgetsPath = dir.resolve(BUDGETS_FILE);
    }

    /**
     * Loads all expenses from the persistent file.
     */
    public List<Expense> loadExpenses() {
        List<Expense> list = new ArrayList<>();
        if (!Files.exists(expensesPath)) {
            return list;
        }

        try (BufferedReader reader = Files.newBufferedReader(expensesPath, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // Header
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    Expense exp = Expense.fromCsv(line);
                    if (exp != null) {
                        list.add(exp);
                    }
                } catch (Exception ex) {
                    System.err.printf("Error parsing expense on line %d: %s (Content: %s)%n", lineNum, ex.getMessage(), line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading expenses file: " + e.getMessage());
        }
        return list;
    }

    /**
     * Saves all expenses to the persistent file.
     */
    public boolean saveExpenses(List<Expense> expenses) {
        try (BufferedWriter writer = Files.newBufferedWriter(expensesPath, StandardCharsets.UTF_8)) {
            writer.write(EXPENSES_HEADER);
            writer.newLine();
            for (Expense exp : expenses) {
                writer.write(exp.toCsv());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving expenses file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads all budgets from the persistent file.
     */
    public List<Budget> loadBudgets() {
        List<Budget> list = new ArrayList<>();
        if (!Files.exists(budgetsPath)) {
            return list;
        }

        try (BufferedReader reader = Files.newBufferedReader(budgetsPath, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // Header
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    Budget b = Budget.fromCsv(line);
                    if (b != null) {
                        list.add(b);
                    }
                } catch (Exception ex) {
                    System.err.printf("Error parsing budget on line %d: %s (Content: %s)%n", lineNum, ex.getMessage(), line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading budgets file: " + e.getMessage());
        }
        return list;
    }

    /**
     * Saves all budgets to the persistent file.
     */
    public boolean saveBudgets(List<Budget> budgets) {
        try (BufferedWriter writer = Files.newBufferedWriter(budgetsPath, StandardCharsets.UTF_8)) {
            writer.write(BUDGETS_HEADER);
            writer.newLine();
            for (Budget b : budgets) {
                writer.write(b.toCsv());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving budgets file: " + e.getMessage());
            return false;
        }
    }

    public Path getExpensesPath() {
        return expensesPath;
    }

    public Path getBudgetsPath() {
        return budgetsPath;
    }
}
