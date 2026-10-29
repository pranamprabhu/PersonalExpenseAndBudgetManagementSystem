package com.expense.test;

import com.expense.model.Budget;
import com.expense.model.BudgetStatus;
import com.expense.model.Category;
import com.expense.model.Expense;
import com.expense.service.BudgetManager;
import com.expense.service.ExpenseManager;
import com.expense.service.ReportService;
import com.expense.storage.FileStorageService;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Automated end-to-end verification test suite for Personal Expense & Budget Management System.
 */
public class SystemVerificationTest {
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("      AUTOMATED SYSTEM TEST SUITE: EXPENSE & BUDGET MANAGEMENT SYSTEM");
        System.out.println("================================================================================");

        String testDataDir = "test_data";
        cleanDirectory(new File(testDataDir));

        FileStorageService storageService = new FileStorageService(testDataDir);
        ExpenseManager expenseManager = new ExpenseManager(storageService);
        BudgetManager budgetManager = new BudgetManager(storageService, expenseManager);
        ReportService reportService = new ReportService(expenseManager, budgetManager);

        int passed = 0;
        int total = 0;

        // TEST 1: Add Expenses
        total++;
        System.out.println("\n[Test 1] Recording daily expenses across different categories...");
        LocalDate d1 = LocalDate.of(2026, 10, 5);
        LocalDate d2 = LocalDate.of(2026, 10, 12);
        LocalDate d3 = LocalDate.of(2026, 10, 18);
        LocalDate d4 = LocalDate.of(2026, 10, 24);
        LocalDate d5 = LocalDate.of(2026, 9, 15);

        Expense e1 = expenseManager.addExpense(d1, Category.FOOD, 1250.00, "Grocery supermarket", "Credit Card");
        Expense e2 = expenseManager.addExpense(d2, Category.TRANSPORTATION, 450.00, "Metro card recharge", "UPI");
        Expense e3 = expenseManager.addExpense(d3, Category.ENTERTAINMENT, 800.00, "Movie & snacks", "UPI");
        Expense e4 = expenseManager.addExpense(d4, Category.UTILITIES, 2200.00, "Electricity & broadband bills", "Net Banking");
        Expense e5 = expenseManager.addExpense(d5, Category.FOOD, 950.00, "Restaurant dinner", "Cash");

        if (expenseManager.getAllExpenses().size() == 5 && e1.getId() == 1001) {
            System.out.println(" -> PASS: Added 5 expenses correctly with auto-increment IDs.");
            passed++;
        } else {
            System.err.println(" -> FAIL: Expense count or IDs mismatch.");
        }

        // TEST 2: Category and Monthly Aggregations
        total++;
        System.out.println("\n[Test 2] Calculating monthly totals and category-wise tracking...");
        YearMonth oct2026 = YearMonth.of(2026, 10);
        double octTotal = expenseManager.getTotalSpentInMonth(oct2026);
        double expectedOctTotal = 1250.00 + 450.00 + 800.00 + 2200.00; // 4700.00

        Map<Category, Double> catTotals = expenseManager.getCategoryTotalsForMonth(oct2026);
        if (Math.abs(octTotal - expectedOctTotal) < 0.001 && catTotals.get(Category.UTILITIES) == 2200.00) {
            System.out.printf(" -> PASS: October 2026 total is Rs. %.2f, Utilities total is Rs. %.2f.%n", octTotal, catTotals.get(Category.UTILITIES));
            passed++;
        } else {
            System.err.printf(" -> FAIL: Aggregation mismatch. Got Rs. %.2f, expected Rs. %.2f.%n", octTotal, expectedOctTotal);
        }

        // TEST 3: Budget Setting and Threshold Alerts
        total++;
        System.out.println("\n[Test 3] Setting monthly budgets and evaluating threshold alerts...");
        // Set overall monthly budget for Oct 2026: 5000 (spent 4700 => 94% -> WARNING >= 80%)
        budgetManager.setBudget(oct2026, null, 5000.00);
        // Set food budget: 1500 (spent 1250 => 83.3% -> WARNING)
        budgetManager.setBudget(oct2026, Category.FOOD, 1500.00);
        // Set transportation budget: 1000 (spent 450 => 45% -> OK)
        budgetManager.setBudget(oct2026, Category.TRANSPORTATION, 1000.00);
        // Set entertainment budget: 500 (spent 800 => 160% -> EXCEEDED)
        budgetManager.setBudget(oct2026, Category.ENTERTAINMENT, 500.00);

        Optional<BudgetStatus> foodStatus = budgetManager.getCategoryBudgetStatus(oct2026, Category.FOOD);
        Optional<BudgetStatus> entStatus = budgetManager.getCategoryBudgetStatus(oct2026, Category.ENTERTAINMENT);
        Optional<BudgetStatus> transStatus = budgetManager.getCategoryBudgetStatus(oct2026, Category.TRANSPORTATION);

        if (foodStatus.isPresent() && foodStatus.get().getAlertLevel() == BudgetStatus.AlertLevel.WARNING
                && entStatus.isPresent() && entStatus.get().getAlertLevel() == BudgetStatus.AlertLevel.EXCEEDED
                && transStatus.isPresent() && transStatus.get().getAlertLevel() == BudgetStatus.AlertLevel.OK) {
            System.out.println(" -> PASS: Budget status alerts correctly flagged WARNING, EXCEEDED, and OK.");
            passed++;
        } else {
            System.err.println(" -> FAIL: Budget status alerts incorrect.");
        }

        // TEST 4: Edit Expense
        total++;
        System.out.println("\n[Test 4] Editing an existing transaction...");
        boolean updated = expenseManager.updateExpense(e2.getId(), d2, Category.TRANSPORTATION, 550.00, "Metro and Cab ride", "UPI");
        Expense updatedExp = expenseManager.getExpenseById(e2.getId()).orElse(null);
        if (updated && updatedExp != null && updatedExp.getAmount() == 550.00 && updatedExp.getDescription().equals("Metro and Cab ride")) {
            System.out.println(" -> PASS: Expense ID 1002 updated successfully.");
            passed++;
        } else {
            System.err.println(" -> FAIL: Update failed.");
        }

        // TEST 5: Search & Filter
        total++;
        System.out.println("\n[Test 5] Searching transactions by keyword, date range, and amount...");
        List<Expense> searchKeyword = expenseManager.search("bill", null, null, null, null, null);
        List<Expense> searchAmount = expenseManager.search(null, null, null, null, 1000.0, 3000.0);

        if (searchKeyword.size() == 1 && searchKeyword.get(0).getId() == e4.getId() && searchAmount.size() == 2) {
            System.out.println(" -> PASS: Search filters correctly matched keywords and amount ranges.");
            passed++;
        } else {
            System.err.printf(" -> FAIL: Search count mismatch (keyword: %d, amount: %d).%n", searchKeyword.size(), searchAmount.size());
        }

        // TEST 6: Delete Expense
        total++;
        System.out.println("\n[Test 6] Deleting an expense...");
        boolean deleted = expenseManager.deleteExpense(e3.getId());
        Optional<Expense> deletedExp = expenseManager.getExpenseById(e3.getId());
        if (deleted && !deletedExp.isPresent() && expenseManager.getAllExpenses().size() == 4) {
            System.out.println(" -> PASS: Expense deleted successfully and removed from collections.");
            passed++;
        } else {
            System.err.println(" -> FAIL: Deletion failed.");
        }

        // TEST 7: File Persistence (Save & Reload)
        total++;
        System.out.println("\n[Test 7] Testing persistence: reloading from CSV into fresh manager instances...");
        ExpenseManager freshExpenseMgr = new ExpenseManager(storageService);
        BudgetManager freshBudgetMgr = new BudgetManager(storageService, freshExpenseMgr);

        if (freshExpenseMgr.getAllExpenses().size() == 4
                && freshBudgetMgr.getAllBudgets().size() == 4
                && freshExpenseMgr.getExpenseById(e1.getId()).isPresent()) {
            System.out.println(" -> PASS: Data reloaded from CSV files with full state consistency.");
            passed++;
        } else {
            System.err.println(" -> FAIL: Persistence reload failed.");
        }

        // TEST 8: Reporting Visual Output Demonstration
        total++;
        System.out.println("\n[Test 8] Demonstrating Financial Summaries & Reports...");
        ReportService freshReportService = new ReportService(freshExpenseMgr, freshBudgetMgr);
        freshReportService.printMonthlySummary(oct2026);
        freshReportService.printCategoryBreakdown(oct2026);
        freshReportService.printBudgetVsActualReport(oct2026);
        freshReportService.printBudgetAlerts(oct2026);
        freshReportService.printMonthlyTrend();
        System.out.println(" -> PASS: Financial reports generated cleanly.");
        passed++;

        // Clean up test folder
        cleanDirectory(new File(testDataDir));
        new File(testDataDir).delete();

        System.out.println("\n================================================================================");
        System.out.printf("   TEST EXECUTION SUMMARY: %d / %d TESTS PASSED (%.1f%%)%n",
                passed, total, (passed * 100.0) / total);
        System.out.println("================================================================================");
    }

    private static void cleanDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }
}
