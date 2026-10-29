# Project Report: Personal Expense & Budget Management System

**Course / Internship Track**: Day 4 of 4 · Final Project  
**Project Title**: Personal Expense & Budget Management System  
**Language & Platform**: Java 21, Standard Edition (SE)  
**Date of Submission**: October 29, 2026  

---

## 1. Executive Summary & Problem Statement

Financial discipline begins with accurate transaction recording, categorization, and continuous budget monitoring. In modern software applications, handling financial transactions requires strict data integrity, robust validation, efficient aggregations, and persistent storage.

The objective of this project is to build an end-to-end **Personal Expense & Budget Management System** in Java that fulfills all functional requirements:
- Recording and managing daily expenses (add, edit, delete, view).
- Multi-dimensional categorizing and searching of transactions.
- Defining monthly budgets (overall and category-specific).
- Automatic threshold evaluation and alerting (`SAFE`, `WARNING >=80%`, `EXCEEDED >100%`).
- Comprehensive financial summaries with ASCII distribution graphs.
- File-based persistent storage utilizing CSV format.

---

## 2. Requirements & Deliverables Verification Matrix

| Requirement / Deliverable | Status | Implementation Details |
|---|---|---|
| **Expense Management Application** | Completed | Full CRUD operations implemented in `ExpenseManager` and interactive CLI in `Main`. |
| **Category-Wise Expense Tracking** | Completed | `Category` enum with 10 structured categories; dynamic grouping, sum calculation, and share percentages. |
| **Monthly Budget Feature** | Completed | `BudgetManager` allows configuring both overall monthly ceilings and per-category limits. |
| **Expense Summary Reports** | Completed | `ReportService` computes monthly totals, daily averages, min/max transactions, and ASCII progress bars. |
| **File-Based Data Storage** | Completed | `FileStorageService` reads/writes flat CSV files (`data/expenses.csv` and `data/budgets.csv`) with automatic creation and backup safety. |
| **README & Report Documentation** | Completed | Comprehensive `README.md` and detailed `REPORT.md` provided. |

---

## 3. Architecture & Class Design

The system adheres to a multi-tiered separation of concerns:

```
+-------------------------------------------------------------+
|                      User Interface                         |
|                   com.expense.ui.Main                       |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                      Service Layer                          |
|  - ExpenseManager (CRUD, Search, Aggregations)             |
|  - BudgetManager  (Budgeting, Threshold Alerting)           |
|  - ReportService  (Formatted Summaries, ASCII Graphs)       |
+-------------------------------------------------------------+
               |                               |
               v                               v
+-----------------------------+ +-----------------------------+
|         Model Layer         | |        Storage Layer        |
|  - Expense (Comparable)     | |  - FileStorageService       |
|  - Category (Enum)          | |    (CSV Parser, Reader,     |
|  - Budget                   | |     Writer with NIO)        |
|  - BudgetStatus             | +-----------------------------+
+-----------------------------+
```

### Key Classes

1. **`Category.java`**:
   - Enumeration of 10 primary spending categories: `FOOD`, `TRANSPORTATION`, `HOUSING`, `UTILITIES`, `ENTERTAINMENT`, `HEALTHCARE`, `EDUCATION`, `SHOPPING`, `INVESTMENT`, `OTHER`.
   - Includes custom display names and resilient parsing methods from strings or numeric menu selections.

2. **`Expense.java`**:
   - Encapsulates transaction state: `id`, `date` (`LocalDate`), `category` (`Category`), `amount` (`double`), `description` (`String`), `paymentMethod` (`String`).
   - Implements `Comparable<Expense>` to sort naturally chronologically by date and ID.
   - Provides `toCsv()` and `fromCsv()` for flat-file serialization.

3. **`Budget.java`**:
   - Represents a monthly spending limit for a specific `YearMonth`.
   - Allows nullable category to designate an "Overall Monthly Ceiling".

4. **`BudgetStatus.java`**:
   - Evaluates spending against limits and calculates remaining balance, utilization percentage, and alert level (`OK`, `WARNING`, `EXCEEDED`).

5. **`ExpenseManager.java`**:
   - Manages in-memory collections of expenses.
   - Generates auto-increment IDs (`1001`, `1002`, ...).
   - Multi-criteria filtering: keyword search, category filtering, date ranges, amount ranges.
   - Aggregate statistics: total spent, monthly totals, category totals, min/max single expenses.

6. **`BudgetManager.java`**:
   - Manages category-specific and overall budgets using composite keys (`YYYY-MM:CATEGORY`).
   - Provides alerts when expenditures reach or exceed 80% and 100% thresholds.

7. **`ReportService.java`**:
   - Formats clean ASCII tables and graphs for console display.
   - Generates custom visual progress bars (`[######--------------] 31.3%`).

8. **`FileStorageService.java`**:
   - Handles persistent read/write operations via `java.nio.file.Files`, `BufferedReader`, and `BufferedWriter`.
   - Safely creates missing directories and handles file corruption gracefully.

---

## 4. Java Collections & Algorithms Applied

1. **`List<Expense>` (`ArrayList`)**:
   - Preserves ordered sequence of transactions and supports stream processing.
2. **`Map<Integer, Expense>` (`HashMap`)**:
   - Provides O(1) average-time complexity lookups for editing and deleting expenses by ID.
3. **`EnumMap<Category, Double>`**:
   - Highly optimized array-backed map for calculating category totals efficiently.
4. **`TreeMap<YearMonth, Double>`**:
   - Maintains historical monthly totals in natural chronological order for trend reporting.
5. **`LinkedHashMap<Category, Double>`**:
   - Stores sorted category spending in descending order to present the largest expense categories first.
6. **Java Stream API**:
   - Stream filters, collectors, and reduction pipelines (`mapToDouble`, `sum`, `max`, `min`) ensure functional, declarative, and bug-free statistical aggregations.

---

## 5. Exception Handling & Input Validation

The system implements defensive programming throughout:
- **`DateTimeParseException`**: Handled during date and month input. If the user presses Enter, reasonable defaults (e.g., today's date or current month) are applied.
- **`NumberFormatException`**: Trapped during menu selection, ID lookup, and monetary input, with clear prompts prompting the user to re-enter valid numeric values.
- **`IllegalArgumentException`**: Thrown in model constructors if negative amounts or corrupt CSV tokens are passed.
- **`IOException`**: Handled in `FileStorageService` with error logging without terminating the running application.

---

## 6. Empirical Verification & Test Results

An automated test suite `SystemVerificationTest.java` was executed to validate the entire workflow without manual input.

### Test Execution Log

```
================================================================================
      AUTOMATED SYSTEM TEST SUITE: EXPENSE & BUDGET MANAGEMENT SYSTEM
================================================================================

[Test 1] Recording daily expenses across different categories...
 -> PASS: Added 5 expenses correctly with auto-increment IDs.

[Test 2] Calculating monthly totals and category-wise tracking...
 -> PASS: October 2026 total is Rs. 4700.00, Utilities total is Rs. 2200.00.

[Test 3] Setting monthly budgets and evaluating threshold alerts...
 -> PASS: Budget status alerts correctly flagged WARNING, EXCEEDED, and OK.

[Test 4] Editing an existing transaction...
 -> PASS: Expense ID 1002 updated successfully.

[Test 5] Searching transactions by keyword, date range, and amount...
 -> PASS: Search filters correctly matched keywords and amount ranges.

[Test 6] Deleting an expense...
 -> PASS: Expense deleted successfully and removed from collections.

[Test 7] Testing persistence: reloading from CSV into fresh manager instances...
 -> PASS: Data reloaded from CSV files with full state consistency.

[Test 8] Demonstrating Financial Summaries & Reports...

================================================================================
                   FINANCIAL SUMMARY REPORT - 2026-10
================================================================================
  Total Transactions Recorded  : 3
  Total Amount Spent           : Rs. 4,000.00
  Average Daily Expenditure    : Rs. 129.03 (over 31 days)
  Highest Single Expense       : Rs. 2,200.00 [Utilities & Bills - Electricity & broadband bills on 2026-10-24]
  Lowest Single Expense        : Rs. 550.00 [Transportation - Metro and Cab ride on 2026-10-12]
--------------------------------------------------------------------------------
  Overall Monthly Budget       : Rs. 5,000.00
  Budget Utilization           : 80.0%
  Remaining Monthly Balance    : Rs. 1,000.00
  Budget Status                : [WARNING (>=80%)]
================================================================================

================================================================================
              CATEGORY-WISE EXPENSE BREAKDOWN - 2026-10
================================================================================
#    | Category                 | Spent (Rs.)    | Share    | Distribution Bar
-----+--------------------------+----------------+----------+-----------------------------
1    | Utilities & Bills        | Rs.    2200.00 |   55.0%  | [###########---------]
2    | Food & Dining            | Rs.    1250.00 |   31.3%  | [######--------------]
3    | Transportation           | Rs.     550.00 |   13.8%  | [###-----------------]
-----+--------------------------+----------------+----------+-----------------------------
     | MONTH TOTAL              | Rs.    4000.00 | 100.0%  |
================================================================================

==========================================================================================
                     BUDGET VS ACTUAL REPORT - 2026-10
==========================================================================================
Category / Scope           | Budget (Rs.)   | Spent (Rs.)    | Remaining      | Used %   | Alert Status
---------------------------+----------------+----------------+----------------+----------+------------------
** OVERALL MONTH **        | Rs.    5000.00 | Rs.    4000.00 | Rs.    1000.00 |   80.0%  | [! WARNING >=80%]
Food & Dining              | Rs.    1500.00 | Rs.    1250.00 | Rs.     250.00 |   83.3%  | [! WARNING >=80%]
Transportation             | Rs.    1000.00 | Rs.     550.00 | Rs.     450.00 |   55.0%  | [ OK - SAFE ]
Entertainment & Leisure    | Rs.     500.00 | Rs.       0.00 | Rs.     500.00 |    0.0%  | [ OK - SAFE ]
==========================================================================================

********************************************************************************
                   !!! ATTENTION: SPENDING ALERTS FOR 2026-10 !!!
********************************************************************************
  [WARNING]  Overall Monthly Budget: Approaching limit! Spent Rs. 4000.00 of Rs. 5000.00 limit (Rs. 1000.00 left, 80.0% used)
  [WARNING]  Food & Dining: Approaching limit! Spent Rs. 1250.00 of Rs. 1500.00 limit (Rs. 250.00 left, 83.3% used)
********************************************************************************

================================================================================
                       HISTORICAL MONTHLY SPENDING TREND
================================================================================
Month      | Total Spent      | Trend Relative Graph
-----------+------------------+-------------------------------------------------
2026-09    | Rs.       950.00 | [######-------------------]
2026-10    | Rs.      4000.00 | [#########################]
================================================================================
 -> PASS: Financial reports generated cleanly.

================================================================================
   TEST EXECUTION SUMMARY: 8 / 8 TESTS PASSED (100.0%)
================================================================================
```

---

## 7. Conclusion

The **Personal Expense & Budget Management System** successfully accomplishes all requirements specified in the project brief:
1. Implemented complete transaction management (add, edit, delete, search, multi-criteria filtering).
2. Integrated monthly budgeting and threshold alerts (80% warning and 100% exceeded).
3. Developed category-wise aggregation and formatted visual financial summaries.
4. Ensured persistent storage with CSV flat files for seamless cross-session persistence.
5. Achieved 100% pass rate across all automated unit and integration test cases.
