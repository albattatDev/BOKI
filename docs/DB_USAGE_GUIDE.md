# BOKI Expense Tracker - Database Usage Guide

## 📚 Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Getting Started](#getting-started)
4. [CRUD Operations](#crud-operations)
5. [Testing Guidelines](#testing-guidelines)
6. [Test Results](#test-results)
7. [Schema Migration](#schema-migration)
8. [Best Practices](#best-practices)

---

## 📖 Overview

This guide explains how to use the SQLite persistence layer for the BOKI Expense Tracker app.

### Features
- ✅ **Persistent Local Storage** (US9): All expenses saved to SQLite database
- ✅ **Fast Data Loading** (US10): Indexed queries with ORDER BY optimization
- ✅ **Clean Architecture**: Separation of data layer from UI
- ✅ **Type Safety**: Domain models with proper encapsulation
- ✅ **SQL Injection Prevention**: Parameterized queries and ContentValues

---

## 🏗️ Architecture

```
UI Layer (Activities/Fragments)
    ↓
Repository Layer (ExpenseRepository)
    ↓
Database Helper (ExpenseDbHelper)
    ↓
SQLite Database (expenses.db)
```

### File Structure
```
app/src/main/java/com/example/boki/
├── models/
│   └── Expense.java                 // Domain model
└── data/local/
    ├── ExpenseDbHelper.java         // SQLiteOpenHelper
    └── ExpenseRepository.java       // CRUD operations
```

### Database Schema

**Table**: `expense`

| Column   | Type    | Constraints       | Description                    |
|----------|---------|-------------------|--------------------------------|
| id       | INTEGER | PK, AUTO_INCREMENT| Unique expense identifier      |
| title    | TEXT    | NOT NULL          | Expense description            |
| amount   | REAL    | NOT NULL          | Expense amount                 |
| category | TEXT    | NOT NULL          | Category (Food, Transport, etc)|
| note     | TEXT    | NULLABLE          | Optional notes                 |
| date     | TEXT    | NOT NULL          | ISO date: YYYY-MM-DD           |
| time     | TEXT    | NOT NULL          | ISO time: HH:MM:SS             |

**Index**: `idx_expense_date` on `date DESC` for fast sorting

---

## 🚀 Getting Started

### 1. Initialize the Repository

In your Activity or Fragment:

```java
import com.example.boki.data.local.ExpenseRepository;
import com.example.boki.models.Expense;

public class MainActivity extends AppCompatActivity {
    
    private ExpenseRepository expenseRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize repository (database will be created automatically)
        expenseRepository = new ExpenseRepository(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up database resources
        if (expenseRepository != null) {
            expenseRepository.close();
        }
    }
}
```

### 2. Understanding the Expense Model

```java
// Creating a new expense (no ID yet)
Expense newExpense = new Expense(
    "Grocery Shopping",      // title
    45.50,                   // amount
    "Food",                  // category
    "Weekly groceries",      // note (can be null)
    "2025-11-27",           // date (ISO format)
    "20:42:00"              // time (ISO format)
);

// Expense from database (has ID)
Expense savedExpense = new Expense(
    1,                       // id from database
    "Grocery Shopping",
    45.50,
    "Food",
    "Weekly groceries",
    "2025-11-27",
    "20:42:00"
);
```

---

## 📝 CRUD Operations

### Create (Insert)

```java
// Create a new expense
Expense expense = new Expense(
    "Taxi Ride",
    15.00,
    "Transport",
    "To airport",
    "2025-11-27",
    "14:30:00"
);

// Insert into database
long newId = expenseRepository.insertExpense(expense);

if (newId != -1) {
    // Success! The expense object now has the database ID
    Log.d("Database", "Inserted expense with ID: " + expense.getId());
} else {
    // Error occurred
    Log.e("Database", "Failed to insert expense");
}
```

### Read (Query)

#### Get All Expenses

```java
// Retrieve all expenses (sorted by date DESC, time DESC)
List<Expense> allExpenses = expenseRepository.getAllExpenses();

// Iterate through expenses
for (Expense expense : allExpenses) {
    Log.d("Database", "Expense: " + expense.getTitle() + " - $" + expense.getAmount());
}

// Check if empty
if (allExpenses.isEmpty()) {
    Log.d("Database", "No expenses found");
}
```

#### Get Single Expense by ID

```java
// Retrieve specific expense
long expenseId = 1;
Expense expense = expenseRepository.getExpenseById(expenseId);

if (expense != null) {
    // Expense found
    Log.d("Database", "Found: " + expense.getTitle());
} else {
    // Not found
    Log.d("Database", "Expense not found");
}
```

#### Get Expense Count

```java
int totalExpenses = expenseRepository.getExpenseCount();
Log.d("Database", "Total expenses: " + totalExpenses);
```

### Update

```java
// Get existing expense
Expense expense = expenseRepository.getExpenseById(1);

if (expense != null) {
    // Modify the expense
    expense.setTitle("Updated Title");
    expense.setAmount(100.00);
    expense.setNote("Updated note");
    
    // Save changes to database
    int rowsAffected = expenseRepository.updateExpense(expense);
    
    if (rowsAffected > 0) {
        Log.d("Database", "Successfully updated expense");
    } else {
        Log.e("Database", "Failed to update expense");
    }
}
```

### Delete

#### Delete Single Expense

```java
long expenseId = 1;
boolean success = expenseRepository.deleteExpense(expenseId);

if (success) {
    Log.d("Database", "Successfully deleted expense");
} else {
    Log.e("Database", "Failed to delete expense");
}
```

#### Delete All Expenses

```java
// WARNING: This deletes ALL expenses
int deletedCount = expenseRepository.deleteAllExpenses();
Log.d("Database", "Deleted " + deletedCount + " expenses");
```

---

## 🧪 Testing Guidelines

### Test 1: Basic CRUD Operations

**Objective**: Verify all Create, Read, Update, Delete operations work correctly

**Test Method**:
```java
private void testBasicCRUD() {
    Log.d(TAG, "=== Testing Basic CRUD Operations ===");
    
    // CREATE
    Expense expense = new Expense(
        "Grocery Shopping",
        45.50,
        "Food",
        "Weekly groceries at supermarket",
        getCurrentDate(),
        getCurrentTime()
    );
    
    long newId = expenseRepository.insertExpense(expense);
    Log.d(TAG, "Inserted expense with ID: " + newId);
    
    // READ - Get by ID
    Expense retrieved = expenseRepository.getExpenseById(newId);
    if (retrieved != null) {
        Log.d(TAG, "Retrieved: " + retrieved.toString());
    }
    
    // UPDATE
    retrieved.setTitle("Updated: Grocery Shopping");
    retrieved.setAmount(50.00);
    int rowsUpdated = expenseRepository.updateExpense(retrieved);
    Log.d(TAG, "Updated " + rowsUpdated + " rows");
    
    // READ ALL
    List<Expense> allExpenses = expenseRepository.getAllExpenses();
    Log.d(TAG, "Total expenses: " + allExpenses.size());
    
    // DELETE
    boolean deleted = expenseRepository.deleteExpense(newId);
    Log.d(TAG, "Deleted: " + deleted);
    
    Toast.makeText(this, "CRUD test complete - check logs", Toast.LENGTH_SHORT).show();
}
```

**Expected Results**:
- Expense inserted successfully with valid ID
- Retrieved expense matches inserted data
- Update modifies 1 row
- Query returns all expenses
- Delete removes the expense successfully

---

### Test 2: Data Persistence (US9)

**Objective**: Verify data persists after app restart

**Test Method**:
```java
private void testPersistence() {
    Log.d(TAG, "=== Testing US9: Data Persistence ===");
    
    int existingCount = expenseRepository.getExpenseCount();
    Log.d(TAG, "Existing expenses: " + existingCount);
    
    if (existingCount == 0) {
        // Add test expenses
        Log.d(TAG, "Adding test expenses...");
        
        expenseRepository.insertExpense(new Expense(
            "Morning Coffee", 5.50, "Food", "Starbucks latte",
            "2025-11-27", "08:30:00"
        ));
        
        expenseRepository.insertExpense(new Expense(
            "Taxi to Work", 12.00, "Transport", "Uber ride",
            "2025-11-27", "09:00:00"
        ));
        
        expenseRepository.insertExpense(new Expense(
            "Lunch", 15.75, "Food", "Restaurant lunch",
            "2025-11-27", "12:30:00"
        ));
        
        expenseRepository.insertExpense(new Expense(
            "Movie Ticket", 18.00, "Entertainment", "Cinema",
            "2025-11-27", "19:00:00"
        ));
        
        Log.d(TAG, "Added 4 test expenses");
        Toast.makeText(this, "Added test data. Close and reopen app to verify persistence.", 
                     Toast.LENGTH_LONG).show();
    } else {
        // Display existing expenses
        List<Expense> expenses = expenseRepository.getAllExpenses();
        Log.d(TAG, "Persistence verified! Found " + expenses.size() + " expenses:");
        
        for (Expense expense : expenses) {
            Log.d(TAG, String.format("  - %s: $%.2f [%s %s]", 
                expense.getTitle(), 
                expense.getAmount(),
                expense.getDate(),
                expense.getTime()
            ));
        }
        
        Toast.makeText(this, "Persistence verified! " + expenses.size() + " expenses found", 
                     Toast.LENGTH_SHORT).show();
    }
}
```

**Testing Steps**:
1. Run this test method
2. Close the app completely (swipe from recent apps)
3. Reopen the app
4. Run the test again
5. Check logs - expenses should still be present

**Expected Results**:
- All expenses survive app restart
- Data integrity maintained across sessions
- No data loss or corruption

---

### Test 3: Fast Data Loading (US10)

**Objective**: Verify query performance with 100+ expenses meets < 50ms requirement

**Test Method**:
```java
private void testPerformance() {
    Log.d(TAG, "=== Testing US10: Fast Data Loading ===");
    
    // Clear existing data for clean test
    int deleted = expenseRepository.deleteAllExpenses();
    Log.d(TAG, "Cleared " + deleted + " existing expenses");
    
    // Insert 300 test expenses
    Log.d(TAG, "Inserting 300 test expenses...");
    long insertStart = System.currentTimeMillis();
    
    String[] categories = {"Food", "Transport", "Shopping", "Entertainment", "Bills", "Other"};
    
    for (int i = 0; i < 300; i++) {
        // Vary dates to test ordering
        int day = 1 + (i % 27); // Days 1-27
        String date = String.format(Locale.US, "2025-11-%02d", day);
        
        // Vary times
        int hour = i % 24;
        int minute = (i * 13) % 60; // Pseudo-random minutes
        String time = String.format(Locale.US, "%02d:%02d:00", hour, minute);
        
        Expense expense = new Expense(
            "Test Expense #" + i,
            (i + 1) * 1.5,
            categories[i % categories.length],
            "Performance test expense " + i,
            date,
            time
        );
        
        expenseRepository.insertExpense(expense);
    }
    
    long insertEnd = System.currentTimeMillis();
    Log.d(TAG, "Inserted 300 expenses in " + (insertEnd - insertStart) + "ms");
    
    // Test query performance
    Log.d(TAG, "Querying all expenses...");
    long queryStart = System.currentTimeMillis();
    List<Expense> expenses = expenseRepository.getAllExpenses();
    long queryEnd = System.currentTimeMillis();
    
    long queryTime = queryEnd - queryStart;
    Log.d(TAG, "✓ Query performance: " + queryTime + "ms for " + expenses.size() + " expenses");
    
    // Verify ORDER BY (should be sorted by date DESC, time DESC)
    if (expenses.size() >= 2) {
        Expense first = expenses.get(0);
        Expense last = expenses.get(expenses.size() - 1);
        
        Log.d(TAG, "✓ First expense: " + first.getDate() + " " + first.getTime() + " - " + first.getTitle());
        Log.d(TAG, "✓ Last expense: " + last.getDate() + " " + last.getTime() + " - " + last.getTitle());
        
        // Check if properly sorted
        String firstDateTime = first.getDate() + " " + first.getTime();
        String lastDateTime = last.getDate() + " " + last.getTime();
        
        if (firstDateTime.compareTo(lastDateTime) >= 0) {
            Log.d(TAG, "✓ ORDER BY verified: Most recent expenses first");
        } else {
            Log.e(TAG, "✗ ORDER BY failed: Expenses not properly sorted");
        }
    }
    
    // Performance evaluation
    String performance;
    if (queryTime < 20) {
        performance = "EXCELLENT";
    } else if (queryTime < 50) {
        performance = "GOOD";
    } else if (queryTime < 100) {
        performance = "ACCEPTABLE";
    } else {
        performance = "NEEDS OPTIMIZATION";
    }
    
    Log.d(TAG, "=== Performance Test Complete ===");
    Log.d(TAG, "Result: " + performance + " (" + queryTime + "ms)");
    Log.d(TAG, "Target: < 50ms (US10 requirement)");
    
    Toast.makeText(this, 
                  String.format("Performance: %s (%dms for %d expenses)", 
                              performance, queryTime, expenses.size()), 
                  Toast.LENGTH_LONG).show();
}
```

**Expected Results**:
- Insert 300 expenses successfully
- Query time < 50ms (US10 requirement)
- Results properly sorted by date DESC, time DESC
- Most recent expenses appear first

---

## 📊 Test Results

### Test 1: Basic CRUD Operations ✅

**Test Date**: November 27, 2025 at 21:20:11

```
=== Testing Basic CRUD Operations ===
Inserted expense with ID: 1
Retrieved: Expense{id=1, title='Grocery Shopping', amount=45.5, 
           category='Food', note='Weekly groceries at supermarket', 
           date='2025-11-27', time='21:20:11'}
Updated 1 rows
Total expenses: 1
Deleted: true
```

**Status**: ✅ PASSED
- All CRUD operations executed successfully
- Data integrity maintained throughout operations
- Deletion confirmed successful

---

### Test 2: Data Persistence (US9) ✅

**Test Date**: November 27, 2025 at 21:25:49

**Initial Run**:
```
=== Testing US9: Data Persistence ===
Existing expenses: 0
Adding test expenses...
Added 4 test expenses
```

**After App Restart** (verified manually):
- All 4 expenses persisted across app restart
- No data loss detected
- Timestamps preserved correctly

**Status**: ✅ PASSED (US9 Requirement Met)
- Data successfully persists after app closure
- SQLite database maintains data integrity
- No corruption or data loss observed

---

### Test 3: Fast Data Loading (US10) ✅

**Test Date**: November 27, 2025 at 21:27:00

```
=== Testing US10: Fast Data Loading ===
Cleared 4 existing expenses
Inserting 300 test expenses...
Inserted 300 expenses in 7564ms

Querying all expenses...
✓ Query performance: 33ms for 300 expenses
✓ First expense: 2025-11-27 23:35:00 - Test Expense #215
✓ Last expense: 2025-11-01 00:00:00 - Test Expense #0
✓ ORDER BY verified: Most recent expenses first

=== Performance Test Complete ===
Result: GOOD (33ms)
Target: < 50ms (US10 requirement)
```

**Performance Metrics**:
- **Query Time**: 33ms (300 expenses)
- **Target**: < 50ms
- **Status**: ✅ PASSED - **34% faster than requirement**
- **Sorting**: ✅ Verified - Most recent expenses first
- **Insert Time**: 7564ms (300 expenses) ≈ 25ms per expense

**Performance Grade**: **GOOD**
- Excellent: < 20ms
- Good: 20-50ms ✅ **[Achieved]**
- Acceptable: 50-100ms
- Needs Optimization: > 100ms

**Status**: ✅ PASSED (US10 Requirement Met)
- Query performance exceeds US10 requirement
- Database index working effectively
- ORDER BY clause optimized correctly

---

### Summary

| Test | Requirement | Status | Result |
|------|-------------|--------|--------|
| Basic CRUD | All operations functional | ✅ PASSED | 100% success rate |
| Persistence (US9) | Data survives restart | ✅ PASSED | 0% data loss |
| Performance (US10) | Query < 50ms | ✅ PASSED | 33ms (34% faster) |

**Overall Database Health**: ✅ **EXCELLENT**

All user stories (US9, US10) requirements met and exceeded.

---

## 🔄 Schema Migration

### Current Version: 1

When you need to add new fields or tables:

1. **Increment version** in `ExpenseDbHelper.java`:
```java
private static final int DATABASE_VERSION = 2; // Was 1
```

2. **Update `onUpgrade()` method**:
```java
@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // Preserve existing data with ALTER TABLE
    if (oldVersion < 2) {
        // Add new column (example)
        db.execSQL("ALTER TABLE expense ADD COLUMN attachment TEXT;");
    }
    
    if (oldVersion < 3) {
        // Create new table (example)
        db.execSQL("CREATE TABLE category (id INTEGER PRIMARY KEY, name TEXT);");
    }
}
```

3. **Update model** if adding fields:
```java
public class Expense {
    // ... existing fields ...
    private String attachment; // New field for version 2
    
    // Add getter/setter
}
```

4. **Update Repository** to handle new fields in CRUD operations

### Migration Best Practices
- ✅ Always use `ALTER TABLE` to preserve data
- ✅ Test migrations on a backup database first
- ✅ Increment version number for any schema change
- ✅ Handle upgrades incrementally (version by version)
- ❌ Don't use `DROP TABLE` in production migrations

---

## 💡 Best Practices

### 1. Always Close Resources
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    expenseRepository.close(); // Prevents memory leaks
}
```

### 2. Use Try-Catch for Database Operations
```java
try {
    long id = expenseRepository.insertExpense(expense);
    // Handle success
} catch (Exception e) {
    Log.e("Database", "Error inserting expense", e);
    // Handle error
}
```

### 3. Validate Data Before Insertion
```java
public boolean isValidExpense(Expense expense) {
    return expense.getTitle() != null && !expense.getTitle().isEmpty()
        && expense.getAmount() > 0
        && expense.getCategory() != null
        && expense.getDate() != null
        && expense.getTime() != null;
}
```

### 4. Use Background Threads for Large Operations
For inserting many expenses or complex queries:
```java
// Using AsyncTask or Coroutines
new Thread(() -> {
    // Insert 100 expenses
    for (int i = 0; i < 100; i++) {
        expenseRepository.insertExpense(createTestExpense(i));
    }
    
    // Update UI on main thread
    runOnUiThread(() -> {
        Toast.makeText(this, "Import complete", Toast.LENGTH_SHORT).show();
    });
}).start();
```

### 5. Use Constants for Categories
```java
public class ExpenseCategory {
    public static final String FOOD = "Food";
    public static final String TRANSPORT = "Transport";
    public static final String SHOPPING = "Shopping";
    public static final String ENTERTAINMENT = "Entertainment";
    public static final String BILLS = "Bills";
    public static final String OTHER = "Other";
}
```

### 6. Date/Time Formatting Helper
```java
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateHelper {
    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat TIME_FORMAT = 
        new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    
    public static String getCurrentDate() {
        return DATE_FORMAT.format(new Date());
    }
    
    public static String getCurrentTime() {
        return TIME_FORMAT.format(new Date());
    }
}
```

---

**Last Updated**: November 27, 2025  
**Version**: 1.0  
**Database Schema Version**: 1  
**Test Status**: All tests passed ✅