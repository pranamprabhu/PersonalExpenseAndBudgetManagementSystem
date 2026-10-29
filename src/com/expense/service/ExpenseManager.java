package com.expense.service;

import com.expense.model.Category;
import com.expense.model.Expense;
import com.expense.storage.FileStorageService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing expense records, search filters, aggregations, and persistence.
 */
public class ExpenseManager {
    private final List<Expense> expenses;
    private final Map<Integer, Expense> expenseMap;
    private final FileStorageService storageService;
    private int nextId;

    public ExpenseManager(FileStorageService storageService) {
        this.storageService = Objects.requireNonNull(storageService);
        this.expenses = new ArrayList<>();
        this.expenseMap = new HashMap<>();
        this.nextId = 1001;
        loadData();
    }

    /**
     * Loads existing expenses from persistent storage into memory collections.
     */
    public void loadData() {
        expenses.clear();
        expenseMap.clear();

        List<Expense> loaded = storageService.loadExpenses();
        for (Expense exp : loaded) {
            expenses.add(exp);
            expenseMap.put(exp.getId(), exp);
            if (exp.getId() >= nextId) {
                nextId = exp.getId() + 1;
            }
        }
        Collections.sort(expenses);
    }

    /**
     * Saves in-memory expenses to persistent storage.
     */
    public boolean saveData() {
        return storageService.saveExpenses(expenses);
    }

    /**
     * Adds a new expense record with auto-assigned unique ID.
     */
    public Expense addExpense(LocalDate date, Category category, double amount, String description, String paymentMethod) {
        Expense expense = new Expense(nextId++, date, category, amount, description, paymentMethod);
        expenses.add(expense);
        expenseMap.put(expense.getId(), expense);
        Collections.sort(expenses);
        saveData();
        return expense;
    }

    /**
     * Updates an existing expense. Returns true if updated, false if ID not found.
     */
    public boolean updateExpense(int id, LocalDate date, Category category, double amount, String description, String paymentMethod) {
        Expense existing = expenseMap.get(id);
        if (existing == null) {
            return false;
        }

        existing.setDate(date);
        existing.setCategory(category);
        existing.setAmount(amount);
        existing.setDescription(description);
        existing.setPaymentMethod(paymentMethod);

        Collections.sort(expenses);
        saveData();
        return true;
    }

    /**
     * Deletes an expense by its ID. Returns true if removed, false if not found.
     */
    public boolean deleteExpense(int id) {
        Expense existing = expenseMap.remove(id);
        if (existing != null) {
            expenses.remove(existing);
            saveData();
            return true;
        }
        return false;
    }

    /**
     * Retrieves an expense by its ID.
     */
    public Optional<Expense> getExpenseById(int id) {
        return Optional.ofNullable(expenseMap.get(id));
    }

    /**
     * Returns an unmodifiable view of all expenses.
     */
    public List<Expense> getAllExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    /**
     * Filters expenses by YearMonth.
     */
    public List<Expense> getExpensesByMonth(YearMonth yearMonth) {
        return expenses.stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(yearMonth))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Comprehensive multi-criteria search and filter.
     */
    public List<Expense> search(String keyword, Category category, LocalDate startDate, LocalDate endDate,
                                Double minAmount, Double maxAmount) {
        return expenses.stream()
                .filter(e -> {
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String kw = keyword.trim().toLowerCase();
                        boolean matchDesc = e.getDescription().toLowerCase().contains(kw);
                        boolean matchCat = e.getCategory().getDisplayName().toLowerCase().contains(kw);
                        boolean matchPay = e.getPaymentMethod().toLowerCase().contains(kw);
                        if (!matchDesc && !matchCat && !matchPay) {
                            return false;
                        }
                    }
                    if (category != null && e.getCategory() != category) {
                        return false;
                    }
                    if (startDate != null && e.getDate().isBefore(startDate)) {
                        return false;
                    }
                    if (endDate != null && e.getDate().isAfter(endDate)) {
                        return false;
                    }
                    if (minAmount != null && e.getAmount() < minAmount) {
                        return false;
                    }
                    if (maxAmount != null && e.getAmount() > maxAmount) {
                        return false;
                    }
                    return true;
                })
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Total amount spent across all recorded expenses.
     */
    public double getTotalSpentAllTime() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    /**
     * Total amount spent in a specific month.
     */
    public double getTotalSpentInMonth(YearMonth yearMonth) {
        return expenses.stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(yearMonth))
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    /**
     * Total amount spent for a specific category in a specific month.
     */
    public double getSpentForCategoryInMonth(YearMonth yearMonth, Category category) {
        return expenses.stream()
                .filter(e -> YearMonth.from(e.getDate()).equals(yearMonth) && e.getCategory() == category)
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    /**
     * Returns category totals for a given month. Sorted descending by amount spent.
     */
    public Map<Category, Double> getCategoryTotalsForMonth(YearMonth yearMonth) {
        Map<Category, Double> totals = new EnumMap<>(Category.class);
        for (Expense e : getExpensesByMonth(yearMonth)) {
            totals.put(e.getCategory(), totals.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
        }

        // Return sorted linked map by value descending
        return totals.entrySet().stream()
                .sorted(Map.Entry.<Category, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Returns total spent grouped by YearMonth across all history.
     */
    public Map<YearMonth, Double> getMonthlyTotals() {
        Map<YearMonth, Double> result = new TreeMap<>();
        for (Expense e : expenses) {
            YearMonth ym = YearMonth.from(e.getDate());
            result.put(ym, result.getOrDefault(ym, 0.0) + e.getAmount());
        }
        return result;
    }

    /**
     * Returns highest expense for a month.
     */
    public Optional<Expense> getHighestExpense(YearMonth yearMonth) {
        return getExpensesByMonth(yearMonth).stream().max(Comparator.comparingDouble(Expense::getAmount));
    }

    /**
     * Returns lowest expense for a month.
     */
    public Optional<Expense> getLowestExpense(YearMonth yearMonth) {
        return getExpensesByMonth(yearMonth).stream().min(Comparator.comparingDouble(Expense::getAmount));
    }

    public int getCount() {
        return expenses.size();
    }
}
