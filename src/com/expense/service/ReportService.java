package com.expense.service;

import com.expense.model.BudgetStatus;
import com.expense.model.Category;
import com.expense.model.Expense;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generates formatted financial summaries, category breakdown analytics, and budget reports.
 */
public class ReportService {
    private final ExpenseManager expenseManager;
    private final BudgetManager budgetManager;

    public ReportService(ExpenseManager expenseManager, BudgetManager budgetManager) {
        this.expenseManager = expenseManager;
        this.budgetManager = budgetManager;
    }

    /**
     * Prints a comprehensive monthly summary report.
     */
    public void printMonthlySummary(YearMonth yearMonth) {
        List<Expense> monthExpenses = expenseManager.getExpensesByMonth(yearMonth);
        double totalSpent = expenseManager.getTotalSpentInMonth(yearMonth);
        int count = monthExpenses.size();
        int daysInMonth = yearMonth.lengthOfMonth();
        double dailyAverage = daysInMonth > 0 ? totalSpent / daysInMonth : 0.0;
        Optional<Expense> highest = expenseManager.getHighestExpense(yearMonth);
        Optional<Expense> lowest = expenseManager.getLowestExpense(yearMonth);

        System.out.println();
        System.out.println("================================================================================");
        System.out.printf("                   FINANCIAL SUMMARY REPORT - %s%n", yearMonth);
        System.out.println("================================================================================");
        System.out.printf("  Total Transactions Recorded  : %d%n", count);
        System.out.printf("  Total Amount Spent           : Rs. %,.2f%n", totalSpent);
        System.out.printf("  Average Daily Expenditure    : Rs. %,.2f (over %d days)%n", dailyAverage, daysInMonth);

        if (highest.isPresent()) {
            Expense h = highest.get();
            System.out.printf("  Highest Single Expense       : Rs. %,.2f [%s - %s on %s]%n",
                    h.getAmount(), h.getCategory().getDisplayName(), h.getDescription(), h.getDate());
        } else {
            System.out.println("  Highest Single Expense       : N/A");
        }

        if (lowest.isPresent()) {
            Expense l = lowest.get();
            System.out.printf("  Lowest Single Expense        : Rs. %,.2f [%s - %s on %s]%n",
                    l.getAmount(), l.getCategory().getDisplayName(), l.getDescription(), l.getDate());
        } else {
            System.out.println("  Lowest Single Expense        : N/A");
        }

        // Overall Budget evaluation
        Optional<BudgetStatus> overallBudget = budgetManager.getOverallBudgetStatus(yearMonth);
        if (overallBudget.isPresent()) {
            BudgetStatus bs = overallBudget.get();
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("  Overall Monthly Budget       : Rs. %,.2f%n", bs.getBudgetLimit());
            System.out.printf("  Budget Utilization           : %.1f%%%n", bs.getPercentUsed());
            System.out.printf("  Remaining Monthly Balance    : Rs. %,.2f%n", bs.getRemaining());
            System.out.printf("  Budget Status                : [%s]%n", bs.getAlertLevel().getLabel().toUpperCase());
        }
        System.out.println("================================================================================");
    }

    /**
     * Prints a detailed category-wise expense breakdown with visual progress bars.
     */
    public void printCategoryBreakdown(YearMonth yearMonth) {
        double totalSpent = expenseManager.getTotalSpentInMonth(yearMonth);
        Map<Category, Double> categoryTotals = expenseManager.getCategoryTotalsForMonth(yearMonth);

        System.out.println();
        System.out.println("================================================================================");
        System.out.printf("              CATEGORY-WISE EXPENSE BREAKDOWN - %s%n", yearMonth);
        System.out.println("================================================================================");
        if (categoryTotals.isEmpty()) {
            System.out.println("  No expenses recorded for this month.");
            System.out.println("================================================================================");
            return;
        }

        System.out.printf("%-4s | %-24s | %-14s | %-8s | %s%n", "#", "Category", "Spent (Rs.)", "Share", "Distribution Bar");
        System.out.println("-----+--------------------------+----------------+----------+-----------------------------");

        int index = 1;
        for (Map.Entry<Category, Double> entry : categoryTotals.entrySet()) {
            Category cat = entry.getKey();
            double spent = entry.getValue();
            double pct = totalSpent > 0 ? (spent / totalSpent) * 100.0 : 0.0;
            String bar = generateProgressBar(pct, 20);

            System.out.printf("%-4d | %-24s | Rs. %10.2f | %6.1f%%  | %s%n",
                    index++, cat.getDisplayName(), spent, pct, bar);
        }
        System.out.println("-----+--------------------------+----------------+----------+-----------------------------");
        System.out.printf("     | %-24s | Rs. %10.2f | 100.0%%  |%n", "MONTH TOTAL", totalSpent);
        System.out.println("================================================================================");
    }

    /**
     * Prints the Budget vs Actual Expense status table for a month.
     */
    public void printBudgetVsActualReport(YearMonth yearMonth) {
        List<BudgetStatus> statuses = budgetManager.getAllBudgetStatusesForMonth(yearMonth);

        System.out.println();
        System.out.println("==========================================================================================");
        System.out.printf("                     BUDGET VS ACTUAL REPORT - %s%n", yearMonth);
        System.out.println("==========================================================================================");

        if (statuses.isEmpty()) {
            System.out.println("  No budgets configured for this month. Set budgets using Menu Option 7.");
            System.out.println("==========================================================================================");
            return;
        }

        System.out.printf("%-26s | %-14s | %-14s | %-14s | %-8s | %s%n",
                "Category / Scope", "Budget (Rs.)", "Spent (Rs.)", "Remaining", "Used %", "Alert Status");
        System.out.println("---------------------------+----------------+----------------+----------------+----------+------------------");

        for (BudgetStatus bs : statuses) {
            String name = bs.getCategory() == null ? "** OVERALL MONTH **" : bs.getCategory().getDisplayName();
            String alertBadge;
            switch (bs.getAlertLevel()) {
                case EXCEEDED:
                    alertBadge = "[!! EXCEEDED !!]";
                    break;
                case WARNING:
                    alertBadge = "[! WARNING >=80%]";
                    break;
                default:
                    alertBadge = "[ OK - SAFE ]";
                    break;
            }

            System.out.printf("%-26s | Rs. %10.2f | Rs. %10.2f | Rs. %10.2f | %6.1f%%  | %s%n",
                    name,
                    bs.getBudgetLimit(),
                    bs.getSpentAmount(),
                    bs.getRemaining(),
                    bs.getPercentUsed(),
                    alertBadge);
        }
        System.out.println("==========================================================================================");
    }

    /**
     * Prints spending alert warnings if any budget is near or over limit.
     */
    public void printBudgetAlerts(YearMonth yearMonth) {
        List<BudgetStatus> alerts = budgetManager.getAlertsForMonth(yearMonth);
        if (alerts.isEmpty()) {
            System.out.printf("%n[i] Good news! All spending for %s is within the safe budget threshold (<80%%).%n", yearMonth);
            return;
        }

        System.out.println();
        System.out.println("********************************************************************************");
        System.out.printf("                   !!! ATTENTION: SPENDING ALERTS FOR %s !!!%n", yearMonth);
        System.out.println("********************************************************************************");
        for (BudgetStatus alert : alerts) {
            String name = alert.getCategory() == null ? "Overall Monthly Budget" : alert.getCategory().getDisplayName();
            if (alert.isExceeded()) {
                System.out.printf("  [EXCEEDED] %s: Spent Rs. %.2f of Rs. %.2f limit (Over by Rs. %.2f, %.1f%% used)%n",
                        name, alert.getSpentAmount(), alert.getBudgetLimit(), -alert.getRemaining(), alert.getPercentUsed());
            } else if (alert.isWarning()) {
                System.out.printf("  [WARNING]  %s: Approaching limit! Spent Rs. %.2f of Rs. %.2f limit (Rs. %.2f left, %.1f%% used)%n",
                        name, alert.getSpentAmount(), alert.getBudgetLimit(), alert.getRemaining(), alert.getPercentUsed());
            }
        }
        System.out.println("********************************************************************************");
    }

    /**
     * Prints historical monthly spending trend.
     */
    public void printMonthlyTrend() {
        Map<YearMonth, Double> monthlyTotals = expenseManager.getMonthlyTotals();
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("                       HISTORICAL MONTHLY SPENDING TREND");
        System.out.println("================================================================================");
        if (monthlyTotals.isEmpty()) {
            System.out.println("  No expense history recorded.");
            System.out.println("================================================================================");
            return;
        }

        double maxSpent = monthlyTotals.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        System.out.printf("%-10s | %-16s | %s%n", "Month", "Total Spent", "Trend Relative Graph");
        System.out.println("-----------+------------------+-------------------------------------------------");
        for (Map.Entry<YearMonth, Double> entry : monthlyTotals.entrySet()) {
            double spent = entry.getValue();
            double pctOfMax = maxSpent > 0 ? (spent / maxSpent) * 100.0 : 0.0;
            String bar = generateProgressBar(pctOfMax, 25);
            System.out.printf("%-10s | Rs. %12.2f | %s%n", entry.getKey(), spent, bar);
        }
        System.out.println("================================================================================");
    }

    private String generateProgressBar(double percentage, int barLength) {
        int filled = (int) Math.round((Math.min(percentage, 100.0) / 100.0) * barLength);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                sb.append("#");
            } else {
                sb.append("-");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
