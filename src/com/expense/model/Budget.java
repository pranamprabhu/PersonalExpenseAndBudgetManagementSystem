package com.expense.model;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Model class representing a monthly budget allocation.
 * If category is null, this represents the total monthly spending budget limit.
 */
public class Budget {
    public static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private YearMonth yearMonth;
    private Category category; // null means overall budget for the month
    private double monthlyLimit;

    public Budget(YearMonth yearMonth, Category category, double monthlyLimit) {
        if (monthlyLimit < 0) {
            throw new IllegalArgumentException("Budget limit cannot be negative.");
        }
        this.yearMonth = Objects.requireNonNull(yearMonth, "YearMonth cannot be null.");
        this.category = category; // null allowed for overall monthly budget
        this.monthlyLimit = monthlyLimit;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.yearMonth = Objects.requireNonNull(yearMonth, "YearMonth cannot be null.");
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        if (monthlyLimit < 0) {
            throw new IllegalArgumentException("Budget limit cannot be negative.");
        }
        this.monthlyLimit = monthlyLimit;
    }

    public boolean isOverall() {
        return category == null;
    }

    public String getBudgetKey() {
        return yearMonth.format(MONTH_FORMATTER) + ":" + (category == null ? "OVERALL" : category.name());
    }

    public String toCsv() {
        return String.format("%s,%s,%.2f",
                yearMonth.format(MONTH_FORMATTER),
                category == null ? "OVERALL" : category.name(),
                monthlyLimit);
    }

    public static Budget fromCsv(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid Budget CSV line: " + csvLine);
        }
        YearMonth ym = YearMonth.parse(parts[0].trim(), MONTH_FORMATTER);
        String catStr = parts[1].trim();
        Category cat = "OVERALL".equalsIgnoreCase(catStr) ? null : Category.valueOf(catStr.toUpperCase());
        double limit = Double.parseDouble(parts[2].trim());
        return new Budget(ym, cat, limit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Budget budget = (Budget) o;
        return Objects.equals(yearMonth, budget.yearMonth) && category == budget.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(yearMonth, category);
    }

    @Override
    public String toString() {
        String catName = category == null ? "Overall Monthly Budget" : category.getDisplayName();
        return String.format("[%s] %-25s : Rs. %.2f", yearMonth.format(MONTH_FORMATTER), catName, monthlyLimit);
    }
}
