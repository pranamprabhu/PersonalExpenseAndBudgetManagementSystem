# Personal Expense & Budget Management System

A robust, modular Java console application for tracking daily expenses, categorizing transactions, setting monthly budgets, and generating actionable financial reports with threshold alerts.

---

## 📌 Project Overview

Managing personal finances requires disciplined expense tracking, categorical categorization, and proactive budget monitoring. The **Personal Expense & Budget Management System** is engineered in Java following Object-Oriented Programming (OOP) principles, clean architectural layering, and the Java Collections Framework. It provides persistent file-based storage so users can manage their finances across sessions seamlessly.

---

## 🚀 Key Features

### 1. Expense Management (CRUD)
- **Add Daily Expense**: Record transaction date (`LocalDate`), category, amount, payment method (UPI, Card, Cash, Net Banking), and detailed description. Auto-assigns unique IDs.
- **View All Expenses**: Beautiful formatted ASCII tabular view with running total, transaction count, and alignment.
- **Edit Expense**: Modify any field of an existing expense record with interactive prompts (press Enter to retain current values).
- **Delete Expense**: Delete expense records with confirmation safeguard.

### 2. Multi-Criteria Search & Filtering
- Search transactions by **keyword / description / payment method** (case-insensitive).
- Filter transactions by **Category**.
- Filter transactions within a **Date Range** (`yyyy-MM-dd` start and end dates).
- Filter transactions within an **Amount Range** (min and max limits).

### 3. Category-Wise Expense Tracking
- Predefined categories: `Food & Dining`, `Transportation`, `Housing & Rent`, `Utilities & Bills`, `Entertainment & Leisure`, `Healthcare & Medical`, `Education & Learning`, `Shopping & Personal`, `Investment & Savings`, and `Miscellaneous / Other`.
- Category totals dynamically computed using Java Streams and Maps.
- Percentage contribution of each category relative to total monthly expenditure.

### 4. Monthly Budget Management
- **Overall Monthly Budget**: Set an aggregate spending ceiling for any month (`yyyy-MM`).
- **Category-Specific Budget**: Set granular monthly allowances per category.
- **Threshold Alerts**:
  - `[ OK - SAFE ]`: Spending < 80% of budget limit.
  - `[! WARNING >=80%]`: Spending reaches or exceeds 80% of allowance.
  - `[!! EXCEEDED !!]`: Spending has crossed 100% of the allocated budget.

### 5. Financial Summaries & Reports
- **Monthly Financial Summary**: Total spend, average daily expenditure, highest single expense, lowest single expense, and overall budget utilization.
- **Category-Wise Breakdown**: Detailed distribution with ASCII visual progress bars (`[###########---------] 55.0%`).
- **Budget vs Actual Report**: Side-by-side comparison table showing budget limit, actual spend, remaining balance, utilization percentage, and alert badges.
- **Historical Monthly Spending Trend**: Month-over-month relative spending graph.

### 6. File-Based Persistence
- Fast and reliable CSV storage:
  - `data/expenses.csv`: Transaction logs.
  - `data/budgets.csv`: Monthly budget targets.
- Auto-loads existing data on application launch.
- Auto-saves upon any modification (add, edit, delete, budget update) and upon application exit.
- Includes manual save/reload options under Menu Option 9.

### 7. Robust Error Handling & Input Validation
- Validates date inputs (`yyyy-MM-dd`) with friendly fallback to today's date on Enter.
- Validates monetary values (strictly non-negative).
- Gracefully handles missing files by automatically creating directories and template headers.

---

## 📂 Project Structure

```
PersonalExpenseBudgetManagementSystem/
├── src/
│   └── com/
│       └── expense/
│           ├── model/
│           │   ├── Category.java            # Category enum with display names & parsing
│           │   ├── Expense.java             # Expense transaction model (Comparable, CSV serializer)
│           │   ├── Budget.java              # Monthly budget model
│           │   └── BudgetStatus.java        # Budget evaluation status & alert levels
│           ├── service/
│           │   ├── ExpenseManager.java      # CRUD, filtering, sorting, aggregations
│           │   ├── BudgetManager.java       # Budget tracking and alert evaluation
│           │   └── ReportService.java       # Visual summaries, progress bars, tables
│           ├── storage/
│           │   └── FileStorageService.java  # Persistent CSV file I/O operations
│           ├── test/
│           │   └── SystemVerificationTest.java # Automated end-to-end test suite
│           └── ui/
│               └── Main.java                # Interactive console application
├── data/
│   ├── expenses.csv                         # CSV storage for transactions
│   └── budgets.csv                          # CSV storage for budgets
├── bin/                                     # Compiled bytecode (.class files)
├── README.md                                # Project documentation
└── REPORT.md                                # Comprehensive project report
```

---

## 🛠️ Technologies Used

- **Language**: Java (JDK 21)
- **OOP Architecture**: Encapsulation, Separation of Concerns (Model-Service-Storage-UI), Polymorphism.
- **Collections Framework**: `List`, `ArrayList`, `Map`, `HashMap`, `TreeMap`, `LinkedHashMap`, `EnumMap`.
- **Date Handling**: `java.time.LocalDate`, `java.time.YearMonth`, `java.time.format.DateTimeFormatter`.
- **File I/O**: `java.nio.file.Files`, `BufferedReader`, `BufferedWriter`, `StandardCharsets.UTF_8`.
- **Stream API**: Filtering, mapping, reducing, statistical aggregations (`DoubleSummaryStatistics`, `mapToDouble`, `max`, `min`).

---

## ⚡ How to Compile and Run

### Prerequisites
- JDK 17 or higher (tested with OpenJDK 21).

### Step 1: Open Terminal and Navigate to Directory
```powershell
cd "c:\Users\Pranam Prabhu\OneDrive\Desktop\ONLINE_INTERN\PersonalExpenseBudgetManagementSystem"
```

### Step 2: Compile the Java Source Files
```powershell
javac -d bin src/com/expense/model/*.java src/com/expense/storage/*.java src/com/expense/service/*.java src/com/expense/ui/*.java src/com/expense/test/*.java
```

### Step 3: Run the Interactive Console Application
```powershell
java -cp bin com.expense.ui.Main
```

### Step 4: Run the Automated System Verification Test Suite
```powershell
java -cp bin com.expense.test.SystemVerificationTest
```

---

## 💻 Sample Interactive Menu

```
================================================================================
             PERSONAL EXPENSE & BUDGET MANAGEMENT SYSTEM
================================================================================
  Data loaded successfully from persistent storage.

┌──────────────────────────── MAIN MENU ───────────────────────────┐
│  [1] Add Daily Expense                                           │
│  [2] View All Expenses (Formatted Table)                         │
│  [3] Edit Existing Expense                                       │
│  [4] Delete Expense                                              │
│  [5] Search & Filter Transactions                                │
│  [6] Category-wise Expense Tracking                              │
│  [7] Monthly Budget Management (Set / View Budgets)              │
│  [8] Financial Summaries & Reports                               │
│  [9] Reload / Force Save Persistent Data                         │
│  [0] Exit Application                                            │
└──────────────────────────────────────────────────────────────────┘
Select an option (0-9):
```

---

## 📊 Sample Reports Output

### Category-Wise Expense Breakdown
```
================================================================================
              CATEGORY-WISE EXPENSE BREAKDOWN - 2026-10
================================================================================
#    | Category                 | Spent (Rs.)    | Share    | Distribution Bar
-----+--------------------------+----------------+----------+-----------------------------
1    | Housing & Rent           | Rs.   12000.00 |   47.2%  | [#########-----------]
2    | Investment & Savings     | Rs.    5000.00 |   19.7%  | [####----------------]
3    | Shopping & Personal      | Rs.    2499.00 |    9.8%  | [##------------------]
4    | Food & Dining            | Rs.    1870.00 |    7.4%  | [#-------------------]
5    | Utilities & Bills        | Rs.    1850.00 |    7.3%  | [#-------------------]
6    | Education & Learning     | Rs.    1200.00 |    4.7%  | [#-------------------]
7    | Healthcare & Medical     | Rs.     850.00 |    3.3%  | [#-------------------]
8    | Entertainment & Leisure  | Rs.     650.00 |    2.6%  | [#-------------------]
9    | Transportation           | Rs.     180.00 |    0.7%  | [--------------------]
-----+--------------------------+----------------+----------+-----------------------------
     | MONTH TOTAL              | Rs.   25449.00 | 100.0%  |
================================================================================
```

### Budget vs Actual Comparison
```
==========================================================================================
                     BUDGET VS ACTUAL REPORT - 2026-10
==========================================================================================
Category / Scope           | Budget (Rs.)   | Spent (Rs.)    | Remaining      | Used %   | Alert Status
---------------------------+----------------+----------------+----------------+----------+------------------
** OVERALL MONTH **        | Rs.   30000.00 | Rs.   25449.00 | Rs.    4551.00 |   84.8%  | [! WARNING >=80%]
Food & Dining              | Rs.    3500.00 | Rs.    1870.00 | Rs.    1630.00 |   53.4%  | [ OK - SAFE ]
Transportation             | Rs.    1500.00 | Rs.     180.00 | Rs.    1320.00 |   12.0%  | [ OK - SAFE ]
Housing & Rent             | Rs.   12500.00 | Rs.   12000.00 | Rs.     500.00 |   96.0%  | [! WARNING >=80%]
Utilities & Bills          | Rs.    2500.00 | Rs.    1850.00 | Rs.     650.00 |   74.0%  | [ OK - SAFE ]
Entertainment & Leisure    | Rs.    1000.00 | Rs.     650.00 | Rs.     350.00 |   65.0%  | [ OK - SAFE ]
Shopping & Personal        | Rs.    3000.00 | Rs.    2499.00 | Rs.     501.00 |   83.3%  | [! WARNING >=80%]
Healthcare & Medical       | Rs.    2000.00 | Rs.     850.00 | Rs.    1150.00 |   42.5%  | [ OK - SAFE ]
Investment & Savings       | Rs.    5000.00 | Rs.    5000.00 | Rs.       0.00 |  100.0%  | [! WARNING >=80%]
==========================================================================================
```
