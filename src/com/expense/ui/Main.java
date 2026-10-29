package com.expense.ui;

import com.expense.model.Budget;
import com.expense.model.Category;
import com.expense.model.Expense;
import com.expense.service.BudgetManager;
import com.expense.service.ExpenseManager;
import com.expense.service.ReportService;
import com.expense.storage.FileStorageService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Main interactive command-line interface for Personal Expense & Budget Management System.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static FileStorageService storageService;
    private static ExpenseManager expenseManager;
    private static BudgetManager budgetManager;
    private static ReportService reportService;

    public static void main(String[] args) {
        initServices();

        System.out.println("================================================================================");
        System.out.println("             PERSONAL EXPENSE & BUDGET MANAGEMENT SYSTEM");
        System.out.println("================================================================================");
        System.out.println("  Data loaded successfully from persistent storage.");

        // Check if there are any immediate alerts for the current month
        YearMonth currentMonth = YearMonth.now();
        reportService.printBudgetAlerts(currentMonth);

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = promptString("Select an option (0-9): ");
            switch (choice) {
                case "1":
                    handleAddExpense();
                    break;
                case "2":
                    handleViewAllExpenses();
                    break;
                case "3":
                    handleEditExpense();
                    break;
                case "4":
                    handleDeleteExpense();
                    break;
                case "5":
                    handleSearchAndFilter();
                    break;
                case "6":
                    handleCategoryTracking();
                    break;
                case "7":
                    handleBudgetManagement();
                    break;
                case "8":
                    handleReportsAndSummaries();
                    break;
                case "9":
                    handleDataPersistence();
                    break;
                case "0":
                    running = false;
                    System.out.println("\nSaving data before exit...");
                    expenseManager.saveData();
                    budgetManager.saveData();
                    System.out.println("Thank you for using Personal Expense & Budget Management System. Goodbye!");
                    break;
                default:
                    System.out.println("[-] Invalid selection. Please enter a number between 0 and 9.");
            }
        }
    }

    private static void initServices() {
        storageService = new FileStorageService("data");
        expenseManager = new ExpenseManager(storageService);
        budgetManager = new BudgetManager(storageService, expenseManager);
        reportService = new ReportService(expenseManager, budgetManager);
    }

    private static void printMainMenu() {
        System.out.println();
        System.out.println("┌──────────────────────────── MAIN MENU ───────────────────────────┐");
        System.out.println("│  [1] Add Daily Expense                                           │");
        System.out.println("│  [2] View All Expenses (Formatted Table)                         │");
        System.out.println("│  [3] Edit Existing Expense                                       │");
        System.out.println("│  [4] Delete Expense                                              │");
        System.out.println("│  [5] Search & Filter Transactions                                │");
        System.out.println("│  [6] Category-wise Expense Tracking                              │");
        System.out.println("│  [7] Monthly Budget Management (Set / View Budgets)              │");
        System.out.println("│  [8] Financial Summaries & Reports                               │");
        System.out.println("│  [9] Reload / Force Save Persistent Data                         │");
        System.out.println("│  [0] Exit Application                                            │");
        System.out.println("└──────────────────────────────────────────────────────────────────┘");
    }

    // =========================================================================
    // 1. ADD EXPENSE
    // =========================================================================
    private static void handleAddExpense() {
        System.out.println("\n--- [1] ADD NEW EXPENSE ---");
        LocalDate date = promptDate("Enter date (yyyy-MM-dd, press Enter for TODAY): ", LocalDate.now());
        Category category = promptCategory("Select Category:");
        double amount = promptPositiveDouble("Enter amount (Rs.): ");
        String description = promptString("Enter description / note: ");
        String payment = promptPaymentMethod("Select payment method:");

        Expense created = expenseManager.addExpense(date, category, amount, description, payment);
        System.out.printf("[+] Expense #%04d added successfully! (Amount: Rs. %.2f, Category: %s)%n",
                created.getId(), created.getAmount(), created.getCategory().getDisplayName());

        // Check budget warning for this month and category
        YearMonth ym = YearMonth.from(date);
        budgetManager.getCategoryBudgetStatus(ym, category).ifPresent(status -> {
            if (status.isExceeded()) {
                System.out.printf("  [!] CAUTION: Category '%s' is now OVER BUDGET by Rs. %.2f (%.1f%% spent)!%n",
                        category.getDisplayName(), -status.getRemaining(), status.getPercentUsed());
            } else if (status.isWarning()) {
                System.out.printf("  [*] WARNING: Category '%s' has reached %.1f%% of its monthly budget (Rs. %.2f remaining).%n",
                        category.getDisplayName(), status.getPercentUsed(), status.getRemaining());
            }
        });
    }

    // =========================================================================
    // 2. VIEW ALL EXPENSES
    // =========================================================================
    private static void handleViewAllExpenses() {
        System.out.println("\n--- [2] ALL RECORDED EXPENSES ---");
        List<Expense> all = expenseManager.getAllExpenses();
        renderExpenseTable(all);
    }

    // =========================================================================
    // 3. EDIT EXPENSE
    // =========================================================================
    private static void handleEditExpense() {
        System.out.println("\n--- [3] EDIT EXPENSE ---");
        int id = promptInt("Enter Expense ID to edit: ");
        Optional<Expense> expOpt = expenseManager.getExpenseById(id);
        if (!expOpt.isPresent()) {
            System.out.printf("[-] Expense with ID %d not found.%n", id);
            return;
        }

        Expense current = expOpt.get();
        System.out.println("Current details:");
        System.out.println(current);

        System.out.println("\nEnter new details (or press Enter to keep current value):");

        // Date
        LocalDate newDate = current.getDate();
        String dateStr = promptString(String.format("Date [%s]: ", current.getDate()));
        if (!dateStr.trim().isEmpty()) {
            try {
                newDate = LocalDate.parse(dateStr.trim(), Expense.DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("[-] Invalid date format. Keeping original date.");
            }
        }

        // Category
        System.out.printf("Current Category: %s. Change? (y/N): ", current.getCategory().getDisplayName());
        String changeCat = scanner.nextLine().trim();
        Category newCat = current.getCategory();
        if (changeCat.equalsIgnoreCase("y")) {
            newCat = promptCategory("Select New Category:");
        }

        // Amount
        double newAmount = current.getAmount();
        String amtStr = promptString(String.format("Amount [Rs. %.2f]: ", current.getAmount()));
        if (!amtStr.trim().isEmpty()) {
            try {
                double parsed = Double.parseDouble(amtStr.trim());
                if (parsed > 0) newAmount = parsed;
                else System.out.println("[-] Amount must be > 0. Keeping original.");
            } catch (NumberFormatException e) {
                System.out.println("[-] Invalid amount. Keeping original.");
            }
        }

        // Description
        String descStr = promptString(String.format("Description [%s]: ", current.getDescription()));
        String newDesc = descStr.trim().isEmpty() ? current.getDescription() : descStr.trim();

        // Payment Method
        String payStr = promptString(String.format("Payment Method [%s]: ", current.getPaymentMethod()));
        String newPay = payStr.trim().isEmpty() ? current.getPaymentMethod() : payStr.trim();

        boolean updated = expenseManager.updateExpense(id, newDate, newCat, newAmount, newDesc, newPay);
        if (updated) {
            System.out.printf("[+] Expense #%04d updated successfully!%n", id);
        } else {
            System.out.println("[-] Failed to update expense.");
        }
    }

    // =========================================================================
    // 4. DELETE EXPENSE
    // =========================================================================
    private static void handleDeleteExpense() {
        System.out.println("\n--- [4] DELETE EXPENSE ---");
        int id = promptInt("Enter Expense ID to delete: ");
        Optional<Expense> expOpt = expenseManager.getExpenseById(id);
        if (!expOpt.isPresent()) {
            System.out.printf("[-] Expense with ID %d not found.%n", id);
            return;
        }

        System.out.println("Record to be deleted:");
        System.out.println(expOpt.get());
        String confirm = promptString("Are you sure you want to delete this expense? (y/N): ");
        if (confirm.equalsIgnoreCase("y")) {
            if (expenseManager.deleteExpense(id)) {
                System.out.printf("[+] Expense #%04d deleted successfully.%n", id);
            } else {
                System.out.println("[-] Failed to delete expense.");
            }
        } else {
            System.out.println("[i] Deletion cancelled.");
        }
    }

    // =========================================================================
    // 5. SEARCH & FILTER
    // =========================================================================
    private static void handleSearchAndFilter() {
        System.out.println("\n--- [5] SEARCH & FILTER TRANSACTIONS ---");
        System.out.println("Select search criteria (press Enter on any prompt to skip that filter):");

        String kw = promptString("Keyword / Description search: ");
        if (kw.trim().isEmpty()) kw = null;

        System.out.print("Filter by Category? (y/N): ");
        String catOpt = scanner.nextLine().trim();
        Category cat = null;
        if (catOpt.equalsIgnoreCase("y")) {
            cat = promptCategory("Select Category to filter by:");
        }

        LocalDate startDate = null;
        String startStr = promptString("Start Date (yyyy-MM-dd): ");
        if (!startStr.trim().isEmpty()) {
            try {
                startDate = LocalDate.parse(startStr.trim(), Expense.DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("[-] Invalid date format. Skipping start date filter.");
            }
        }

        LocalDate endDate = null;
        String endStr = promptString("End Date (yyyy-MM-dd): ");
        if (!endStr.trim().isEmpty()) {
            try {
                endDate = LocalDate.parse(endStr.trim(), Expense.DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("[-] Invalid date format. Skipping end date filter.");
            }
        }

        Double minAmt = null;
        String minStr = promptString("Minimum Amount (Rs.): ");
        if (!minStr.trim().isEmpty()) {
            try {
                minAmt = Double.parseDouble(minStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        Double maxAmt = null;
        String maxStr = promptString("Maximum Amount (Rs.): ");
        if (!maxStr.trim().isEmpty()) {
            try {
                maxAmt = Double.parseDouble(maxStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        List<Expense> results = expenseManager.search(kw, cat, startDate, endDate, minAmt, maxAmt);
        System.out.println("\n--- SEARCH RESULTS ---");
        renderExpenseTable(results);
    }

    // =========================================================================
    // 6. CATEGORY-WISE EXPENSE TRACKING
    // =========================================================================
    private static void handleCategoryTracking() {
        System.out.println("\n--- [6] CATEGORY-WISE EXPENSE TRACKING ---");
        YearMonth ym = promptYearMonth("Enter Month (yyyy-MM, press Enter for CURRENT MONTH): ", YearMonth.now());
        reportService.printCategoryBreakdown(ym);
    }

    // =========================================================================
    // 7. MONTHLY BUDGET MANAGEMENT
    // =========================================================================
    private static void handleBudgetManagement() {
        System.out.println("\n--- [7] MONTHLY BUDGET MANAGEMENT ---");
        System.out.println("  [1] Set Overall Monthly Budget");
        System.out.println("  [2] Set Category-Specific Budget");
        System.out.println("  [3] View All Configured Budgets");
        System.out.println("  [4] View Budget vs Actual Comparison Report");
        System.out.println("  [5] Check Spending Alerts");
        System.out.println("  [6] Remove a Budget");
        System.out.println("  [0] Back to Main Menu");

        String choice = promptString("Select an option (0-6): ");
        switch (choice) {
            case "1": {
                YearMonth ym = promptYearMonth("Enter Month (yyyy-MM, press Enter for CURRENT MONTH): ", YearMonth.now());
                double limit = promptPositiveDouble("Enter overall monthly budget limit (Rs.): ");
                budgetManager.setBudget(ym, null, limit);
                System.out.printf("[+] Overall budget for %s set to Rs. %,.2f%n", ym, limit);
                break;
            }
            case "2": {
                YearMonth ym = promptYearMonth("Enter Month (yyyy-MM, press Enter for CURRENT MONTH): ", YearMonth.now());
                Category cat = promptCategory("Select Category:");
                double limit = promptPositiveDouble("Enter budget limit for " + cat.getDisplayName() + " (Rs.): ");
                budgetManager.setBudget(ym, cat, limit);
                System.out.printf("[+] Budget for %s under %s set to Rs. %,.2f%n", ym, cat.getDisplayName(), limit);
                break;
            }
            case "3": {
                List<Budget> budgets = budgetManager.getAllBudgets();
                System.out.println("\n==================================================================");
                System.out.println("                    CONFIGURED BUDGETS");
                System.out.println("==================================================================");
                if (budgets.isEmpty()) {
                    System.out.println("  No budgets have been set yet.");
                } else {
                    System.out.printf("%-10s | %-26s | %s%n", "Month", "Category", "Budget Limit (Rs.)");
                    System.out.println("-----------+----------------------------+-------------------------");
                    for (Budget b : budgets) {
                        String catName = b.isOverall() ? "** OVERALL MONTH **" : b.getCategory().getDisplayName();
                        System.out.printf("%-10s | %-26s | Rs. %,12.2f%n", b.getYearMonth(), catName, b.getMonthlyLimit());
                    }
                }
                System.out.println("==================================================================");
                break;
            }
            case "4": {
                YearMonth ym = promptYearMonth("Enter Month (yyyy-MM, press Enter for CURRENT MONTH): ", YearMonth.now());
                reportService.printBudgetVsActualReport(ym);
                break;
            }
            case "5": {
                YearMonth ym = promptYearMonth("Enter Month (yyyy-MM, press Enter for CURRENT MONTH): ", YearMonth.now());
                reportService.printBudgetAlerts(ym);
                break;
            }
            case "6": {
                YearMonth ym = promptYearMonth("Enter Month (yyyy-MM): ", YearMonth.now());
                System.out.print("Remove overall budget (1) or category budget (2)? ");
                String type = scanner.nextLine().trim();
                Category cat = null;
                if ("2".equals(type)) {
                    cat = promptCategory("Select Category to remove budget for:");
                }
                if (budgetManager.deleteBudget(ym, cat)) {
                    System.out.println("[+] Budget removed successfully.");
                } else {
                    System.out.println("[-] No budget found matching criteria.");
                }
                break;
            }
            case "0":
                break;
            default:
                System.out.println("[-] Invalid option.");
        }
    }

    // =========================================================================
    // 8. REPORTS & SUMMARIES
    // =========================================================================
    private static void handleReportsAndSummaries() {
        System.out.println("\n--- [8] FINANCIAL SUMMARIES & REPORTS ---");
        System.out.println("  [1] Complete Monthly Financial Summary");
        System.out.println("  [2] Category-wise Expense Breakdown");
        System.out.println("  [3] Budget vs Actual Spending Comparison");
        System.out.println("  [4] Historical Monthly Spending Trend");
        System.out.println("  [0] Back to Main Menu");

        String choice = promptString("Select an option (0-4): ");
        switch (choice) {
            case "1": {
                YearMonth ym = promptYearMonth("Enter Month (yyyy-MM, press Enter for CURRENT MONTH): ", YearMonth.now());
                reportService.printMonthlySummary(ym);
                break;
            }
            case "2": {
                YearMonth ym = promptYearMonth("Enter Month (yyyy-MM, press Enter for CURRENT MONTH): ", YearMonth.now());
                reportService.printCategoryBreakdown(ym);
                break;
            }
            case "3": {
                YearMonth ym = promptYearMonth("Enter Month (yyyy-MM, press Enter for CURRENT MONTH): ", YearMonth.now());
                reportService.printBudgetVsActualReport(ym);
                break;
            }
            case "4": {
                reportService.printMonthlyTrend();
                break;
            }
            case "0":
                break;
            default:
                System.out.println("[-] Invalid option.");
        }
    }

    // =========================================================================
    // 9. PERSISTENCE
    // =========================================================================
    private static void handleDataPersistence() {
        System.out.println("\n--- [9] DATA PERSISTENCE ---");
        System.out.println("  [1] Force Save Data to Disk");
        System.out.println("  [2] Reload Data from Disk");
        System.out.println("  [0] Back");

        String choice = promptString("Select an option: ");
        if ("1".equals(choice)) {
            boolean expOk = expenseManager.saveData();
            boolean bgtOk = budgetManager.saveData();
            if (expOk && bgtOk) {
                System.out.println("[+] All expense and budget data successfully written to CSV files.");
            } else {
                System.out.println("[-] Warning: Failed to save some data. Check file permissions.");
            }
        } else if ("2".equals(choice)) {
            expenseManager.loadData();
            budgetManager.loadData();
            System.out.println("[+] Reloaded data from disk. Current total expenses: " + expenseManager.getCount());
        }
    }

    // =========================================================================
    // HELPER UI RENDERING & INPUT METHODS
    // =========================================================================
    public static void renderExpenseTable(List<Expense> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("  No transactions found.");
            return;
        }

        System.out.println("=============================================================================================================");
        System.out.printf("%-6s | %-10s | %-24s | %-14s | %-12s | %s%n",
                "ID", "Date", "Category", "Amount (Rs.)", "Payment", "Description");
        System.out.println("-------+------------+--------------------------+----------------+--------------+-----------------------------");

        double total = 0.0;
        for (Expense e : list) {
            total += e.getAmount();
            System.out.printf("#%04d  | %s | %-24s | Rs. %10.2f | %-12s | %s%n",
                    e.getId(),
                    e.getDate(),
                    e.getCategory().getDisplayName(),
                    e.getAmount(),
                    e.getPaymentMethod(),
                    e.getDescription());
        }
        System.out.println("-------+------------+--------------------------+----------------+--------------+-----------------------------");
        System.out.printf("TOTAL: %d transaction(s) | Grand Total: Rs. %,.2f%n", list.size(), total);
        System.out.println("=============================================================================================================");
    }

    private static String promptString(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private static int promptInt(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[-] Please enter a valid whole number.");
            }
        }
    }

    private static double promptPositiveDouble(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val >= 0) {
                    return val;
                }
                System.out.println("[-] Amount must be non-negative.");
            } catch (NumberFormatException e) {
                System.out.println("[-] Please enter a valid decimal number (e.g., 250.50).");
            }
        }
    }

    private static LocalDate promptDate(String message, LocalDate defaultDate) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            if (input.isEmpty() && defaultDate != null) {
                return defaultDate;
            }
            try {
                return LocalDate.parse(input, Expense.DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("[-] Invalid date format. Please use yyyy-MM-dd (e.g. 2026-10-29).");
            }
        }
    }

    private static YearMonth promptYearMonth(String message, YearMonth defaultYm) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            if (input.isEmpty() && defaultYm != null) {
                return defaultYm;
            }
            try {
                return YearMonth.parse(input, Budget.MONTH_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("[-] Invalid month format. Please use yyyy-MM (e.g. 2026-10).");
            }
        }
    }

    private static Category promptCategory(String header) {
        System.out.println(header);
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            System.out.printf("  [%2d] %s%n", (i + 1), cats[i].getDisplayName());
        }
        while (true) {
            System.out.print("Select Category (1-" + cats.length + "): ");
            String input = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(input);
                if (idx >= 1 && idx <= cats.length) {
                    return cats[idx - 1];
                }
            } catch (NumberFormatException ignored) {}
            // Also try string matching
            Category c = Category.fromString(input);
            if (c != null) return c;
            System.out.println("[-] Invalid option. Please choose between 1 and " + cats.length);
        }
    }

    private static String promptPaymentMethod(String header) {
        String[] methods = {"UPI", "Credit Card", "Debit Card", "Cash", "Net Banking"};
        System.out.println(header);
        for (int i = 0; i < methods.length; i++) {
            System.out.printf("  [%d] %s%n", (i + 1), methods[i]);
        }
        System.out.print("Select payment method (1-" + methods.length + ", press Enter for UPI): ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return "UPI";
        }
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= methods.length) {
                return methods[idx - 1];
            }
        } catch (NumberFormatException ignored) {}
        return input;
    }
}
