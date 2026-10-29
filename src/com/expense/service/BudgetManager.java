package com.expense.service;

import com.expense.model.Budget;
import com.expense.model.BudgetStatus;
import com.expense.model.Category;
import com.expense.storage.FileStorageService;

import java.time.YearMonth;
import java.util.*;

/**
 * Service managing monthly budget definitions, threshold alerts, and comparisons.
 */
public class BudgetManager {
    private final FileStorageService storageService;
    private final ExpenseManager expenseManager;
    // Map key: YearMonth:CategoryName or YearMonth:OVERALL
    private final Map<String, Budget> budgetMap;

    public BudgetManager(FileStorageService storageService, ExpenseManager expenseManager) {
        this.storageService = Objects.requireNonNull(storageService);
        this.expenseManager = Objects.requireNonNull(expenseManager);
        this.budgetMap = new HashMap<>();
        loadData();
    }

    public void loadData() {
        budgetMap.clear();
        List<Budget> loaded = storageService.loadBudgets();
        for (Budget b : loaded) {
            budgetMap.put(b.getBudgetKey(), b);
        }
    }

    public boolean saveData() {
        return storageService.saveBudgets(new ArrayList<>(budgetMap.values()));
    }

    /**
     * Sets or updates a budget for a category (or overall if category is null).
     */
    public Budget setBudget(YearMonth yearMonth, Category category, double limit) {
        Budget b = new Budget(yearMonth, category, limit);
        budgetMap.put(b.getBudgetKey(), b);
        saveData();
        return b;
    }

    /**
     * Removes a budget entry.
     */
    public boolean deleteBudget(YearMonth yearMonth, Category category) {
        String key = yearMonth.format(Budget.MONTH_FORMATTER) + ":" + (category == null ? "OVERALL" : category.name());
        if (budgetMap.remove(key) != null) {
            saveData();
            return true;
        }
        return false;
    }

    public Optional<Budget> getBudget(YearMonth yearMonth, Category category) {
        String key = yearMonth.format(Budget.MONTH_FORMATTER) + ":" + (category == null ? "OVERALL" : category.name());
        return Optional.ofNullable(budgetMap.get(key));
    }

    /**
     * Returns all budgets configured for a given month.
     */
    public List<Budget> getBudgetsForMonth(YearMonth yearMonth) {
        List<Budget> list = new ArrayList<>();
        for (Budget b : budgetMap.values()) {
            if (b.getYearMonth().equals(yearMonth)) {
                list.add(b);
            }
        }
        list.sort(Comparator.comparing((Budget b) -> b.isOverall() ? "" : b.getCategory().name()));
        return list;
    }

    /**
     * Returns all configured budgets across all months.
     */
    public List<Budget> getAllBudgets() {
        List<Budget> list = new ArrayList<>(budgetMap.values());
        list.sort(Comparator.comparing(Budget::getYearMonth).thenComparing(b -> b.isOverall() ? "" : b.getCategory().name()));
        return list;
    }

    /**
     * Evaluates budget status for a given category in a month.
     */
    public Optional<BudgetStatus> getCategoryBudgetStatus(YearMonth yearMonth, Category category) {
        Optional<Budget> budgetOpt = getBudget(yearMonth, category);
        if (!budgetOpt.isPresent()) {
            return Optional.empty();
        }
        double limit = budgetOpt.get().getMonthlyLimit();
        double spent = expenseManager.getSpentForCategoryInMonth(yearMonth, category);
        return Optional.of(new BudgetStatus(category, limit, spent));
    }

    /**
     * Evaluates overall monthly budget status.
     */
    public Optional<BudgetStatus> getOverallBudgetStatus(YearMonth yearMonth) {
        Optional<Budget> budgetOpt = getBudget(yearMonth, null);
        if (!budgetOpt.isPresent()) {
            return Optional.empty();
        }
        double limit = budgetOpt.get().getMonthlyLimit();
        double spent = expenseManager.getTotalSpentInMonth(yearMonth);
        return Optional.of(new BudgetStatus(null, limit, spent));
    }

    /**
     * Evaluates all budgets for a month and returns statuses.
     */
    public List<BudgetStatus> getAllBudgetStatusesForMonth(YearMonth yearMonth) {
        List<BudgetStatus> statuses = new ArrayList<>();
        // Overall
        getOverallBudgetStatus(yearMonth).ifPresent(statuses::add);

        // Categories
        for (Category c : Category.values()) {
            getCategoryBudgetStatus(yearMonth, c).ifPresent(statuses::add);
        }
        return statuses;
    }

    /**
     * Checks if any budget is exceeded or in warning state for a month.
     */
    public List<BudgetStatus> getAlertsForMonth(YearMonth yearMonth) {
        List<BudgetStatus> alerts = new ArrayList<>();
        for (BudgetStatus status : getAllBudgetStatusesForMonth(yearMonth)) {
            if (status.isWarning() || status.isExceeded()) {
                alerts.add(status);
            }
        }
        return alerts;
    }
}
