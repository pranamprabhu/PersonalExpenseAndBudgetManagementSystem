package com.expense.model;

/**
 * Encapsulates the evaluation status of a budget against actual spending.
 */
public class BudgetStatus {
    public enum AlertLevel {
        OK("Safe"),
        WARNING("Warning (>=80%)"),
        EXCEEDED("Over Budget (>100%)");

        private final String label;
        AlertLevel(String label) {
            this.label = label;
        }
        public String getLabel() {
            return label;
        }
    }

    private final Category category;
    private final double budgetLimit;
    private final double spentAmount;
    private final double remaining;
    private final double percentUsed;
    private final AlertLevel alertLevel;

    public BudgetStatus(Category category, double budgetLimit, double spentAmount) {
        this.category = category;
        this.budgetLimit = budgetLimit;
        this.spentAmount = spentAmount;
        this.remaining = budgetLimit - spentAmount;
        this.percentUsed = budgetLimit > 0 ? (spentAmount / budgetLimit) * 100.0 : 0.0;

        if (percentUsed > 100.0) {
            this.alertLevel = AlertLevel.EXCEEDED;
        } else if (percentUsed >= 80.0) {
            this.alertLevel = AlertLevel.WARNING;
        } else {
            this.alertLevel = AlertLevel.OK;
        }
    }

    public Category getCategory() {
        return category;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public double getRemaining() {
        return remaining;
    }

    public double getPercentUsed() {
        return percentUsed;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public boolean isExceeded() {
        return alertLevel == AlertLevel.EXCEEDED;
    }

    public boolean isWarning() {
        return alertLevel == AlertLevel.WARNING;
    }
}
