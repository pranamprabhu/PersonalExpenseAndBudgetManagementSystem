package com.expense.model;

/**
 * Enumeration representing expense categories.
 */
public enum Category {
    FOOD("Food & Dining"),
    TRANSPORTATION("Transportation"),
    HOUSING("Housing & Rent"),
    UTILITIES("Utilities & Bills"),
    ENTERTAINMENT("Entertainment & Leisure"),
    HEALTHCARE("Healthcare & Medical"),
    EDUCATION("Education & Learning"),
    SHOPPING("Shopping & Personal"),
    INVESTMENT("Investment & Savings"),
    OTHER("Miscellaneous / Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Parses a string to a Category. Matches by enum name or display name (case-insensitive).
     * Defaults to OTHER if no match is found.
     */
    public static Category fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHER;
        }
        String clean = text.trim();
        for (Category c : values()) {
            if (c.name().equalsIgnoreCase(clean) || c.displayName.equalsIgnoreCase(clean)) {
                return c;
            }
        }
        // Try substring or index match if numeric
        try {
            int index = Integer.parseInt(clean);
            if (index >= 1 && index <= values().length) {
                return values()[index - 1];
            }
        } catch (NumberFormatException ignored) {}

        return OTHER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
