package com.example.boki.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.boki.models.Budget;
import com.example.boki.models.Expense;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Repository class for managing Budget data operations.
 * Implements the Repository pattern to provide a clean API for CRUD operations.
 * Handles budget cycles (monthly/weekly) and expense calculations.
 */
public class BudgetRepository {
    
    private ExpenseDbHelper dbHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    /**
     * Constructor - initializes the database helper
     * @param context Application context
     */
    public BudgetRepository(Context context) {
        this.dbHelper = new ExpenseDbHelper(context);
    }
    
    /**
     * US18: Insert a new budget into the database
     * Automatically deactivates other budgets when inserting an active one
     * 
     * @param budget The budget to insert (ID will be ignored)
     * @return The row ID of the newly inserted budget, or -1 if error
     */
    public long insertBudget(Budget budget) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            db.beginTransaction();
            
            // If this budget is active, deactivate all other budgets
            if (budget.isActive()) {
                deactivateAllBudgets(db);
            }
            
            // ContentValues for safe insertion (prevents SQL injection)
            ContentValues values = new ContentValues();
            values.put(ExpenseDbHelper.COLUMN_BUDGET_NAME, budget.getName());
            values.put(ExpenseDbHelper.COLUMN_BUDGET_AMOUNT, budget.getAmount());
            values.put(ExpenseDbHelper.COLUMN_BUDGET_START_DATE, budget.getStartDate());
            values.put(ExpenseDbHelper.COLUMN_BUDGET_CYCLE_TYPE, budget.getCycleType());
            values.put(ExpenseDbHelper.COLUMN_BUDGET_CYCLE_VALUE, budget.getCycleValue());
            values.put(ExpenseDbHelper.COLUMN_BUDGET_ACTIVE, budget.isActive() ? 1 : 0);
            
            // Insert and get the new row ID
            long newRowId = db.insert(ExpenseDbHelper.TABLE_BUDGET, null, values);
            
            // Update the budget object with the new ID
            if (newRowId != -1) {
                budget.setId(newRowId);
                db.setTransactionSuccessful();
            }
            
            return newRowId;
        } finally {
            db.endTransaction();
        }
    }
    
    /**
     * US19: Update an existing budget in the database
     * 
     * @param budget The budget to update (must have valid ID)
     * @return Number of rows affected (should be 1 if successful)
     */
    public int updateBudget(Budget budget) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            db.beginTransaction();
            
            // If setting this budget to active, deactivate others first
            if (budget.isActive()) {
                deactivateAllBudgets(db);
            }
            
            // ContentValues with new data
            ContentValues values = new ContentValues();
            values.put(ExpenseDbHelper.COLUMN_BUDGET_NAME, budget.getName());
            values.put(ExpenseDbHelper.COLUMN_BUDGET_AMOUNT, budget.getAmount());
            values.put(ExpenseDbHelper.COLUMN_BUDGET_START_DATE, budget.getStartDate());
            values.put(ExpenseDbHelper.COLUMN_BUDGET_CYCLE_TYPE, budget.getCycleType());
            values.put(ExpenseDbHelper.COLUMN_BUDGET_CYCLE_VALUE, budget.getCycleValue());
            values.put(ExpenseDbHelper.COLUMN_BUDGET_ACTIVE, budget.isActive() ? 1 : 0);
            
            // WHERE clause with parameterized query (prevents SQL injection)
            String whereClause = ExpenseDbHelper.COLUMN_BUDGET_ID + " = ?";
            String[] whereArgs = { String.valueOf(budget.getId()) };
            
            // Perform update
            int rowsAffected = db.update(
                ExpenseDbHelper.TABLE_BUDGET,
                values,
                whereClause,
                whereArgs
            );
            
            if (rowsAffected > 0) {
                db.setTransactionSuccessful();
            }
            
            return rowsAffected;
        } finally {
            db.endTransaction();
        }
    }
    
    /**
     * US19: Delete a budget from the database
     * 
     * @param id The budget ID to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteBudget(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        String whereClause = ExpenseDbHelper.COLUMN_BUDGET_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };
        
        int rowsDeleted = db.delete(ExpenseDbHelper.TABLE_BUDGET, whereClause, whereArgs);
        return rowsDeleted > 0;
    }
    
    /**
     * Get the currently active budget
     * 
     * @return The active budget, or null if none exists
     */
    public Budget getActiveBudget() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        Budget budget = null;
        
        try {
            String[] projection = {
                ExpenseDbHelper.COLUMN_BUDGET_ID,
                ExpenseDbHelper.COLUMN_BUDGET_NAME,
                ExpenseDbHelper.COLUMN_BUDGET_AMOUNT,
                ExpenseDbHelper.COLUMN_BUDGET_START_DATE,
                ExpenseDbHelper.COLUMN_BUDGET_CYCLE_TYPE,
                ExpenseDbHelper.COLUMN_BUDGET_CYCLE_VALUE,
                ExpenseDbHelper.COLUMN_BUDGET_ACTIVE
            };
            
            String selection = ExpenseDbHelper.COLUMN_BUDGET_ACTIVE + " = ?";
            String[] selectionArgs = { "1" };
            
            cursor = db.query(
                ExpenseDbHelper.TABLE_BUDGET,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
            );
            
            if (cursor.moveToFirst()) {
                budget = cursorToBudget(cursor);
            }
            
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return budget;
    }
    
    /**
     * Get all budgets from the database
     * 
     * @return List of all budgets
     */
    public List<Budget> getAllBudgets() {
        List<Budget> budgets = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String[] projection = {
                ExpenseDbHelper.COLUMN_BUDGET_ID,
                ExpenseDbHelper.COLUMN_BUDGET_NAME,
                ExpenseDbHelper.COLUMN_BUDGET_AMOUNT,
                ExpenseDbHelper.COLUMN_BUDGET_START_DATE,
                ExpenseDbHelper.COLUMN_BUDGET_CYCLE_TYPE,
                ExpenseDbHelper.COLUMN_BUDGET_CYCLE_VALUE,
                ExpenseDbHelper.COLUMN_BUDGET_ACTIVE
            };
            
            cursor = db.query(
                ExpenseDbHelper.TABLE_BUDGET,
                projection,
                null,
                null,
                null,
                null,
                ExpenseDbHelper.COLUMN_BUDGET_ID + " DESC"
            );
            
            while (cursor.moveToNext()) {
                Budget budget = cursorToBudget(cursor);
                budgets.add(budget);
            }
            
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return budgets;
    }
    
    /**
     * US20: Get expenses within the current budget cycle
     * Calculates the cycle period based on budget settings
     * 
     * @param budget The budget to get expenses for
     * @return List of expenses within the current cycle
     */
    public List<Expense> getExpensesInCurrentCycle(Budget budget) {
        if (budget == null) {
            return new ArrayList<>();
        }
        
        String[] cycleDates = getCurrentCycleDates(budget);
        String cycleStart = cycleDates[0];
        String cycleEnd = cycleDates[1];
        
        return getExpensesBetweenDates(cycleStart, cycleEnd);
    }
    
    /**
     * Get expenses between two dates (inclusive)
     * 
     * @param startDate Start date in YYYY-MM-DD format
     * @param endDate End date in YYYY-MM-DD format
     * @return List of expenses in the date range
     */
    public List<Expense> getExpensesBetweenDates(String startDate, String endDate) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String[] projection = {
                ExpenseDbHelper.COLUMN_ID,
                ExpenseDbHelper.COLUMN_TITLE,
                ExpenseDbHelper.COLUMN_AMOUNT,
                ExpenseDbHelper.COLUMN_CATEGORY,
                ExpenseDbHelper.COLUMN_NOTE,
                ExpenseDbHelper.COLUMN_DATE,
                ExpenseDbHelper.COLUMN_TIME
            };
            
            String selection = ExpenseDbHelper.COLUMN_DATE + " >= ? AND " + 
                              ExpenseDbHelper.COLUMN_DATE + " <= ?";
            String[] selectionArgs = { startDate, endDate };
            
            cursor = db.query(
                ExpenseDbHelper.TABLE_EXPENSE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                ExpenseDbHelper.COLUMN_DATE + " DESC"
            );
            
            while (cursor.moveToNext()) {
                Expense expense = cursorToExpense(cursor);
                expenses.add(expense);
            }
            
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return expenses;
    }
    
    /**
     * US20, US21: Calculate total expenses in current budget cycle
     * 
     * @param budget The budget to calculate for
     * @return Total amount of expenses in current cycle
     */
    public double getTotalExpensesInCycle(Budget budget) {
        if (budget == null) {
            return 0.0;
        }
        
        List<Expense> expenses = getExpensesInCurrentCycle(budget);
        double total = 0.0;
        
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        
        return total;
    }
    
    /**
     * US21: Calculate remaining balance for active budget
     * Remaining = Budget Amount - Total Expenses in Current Cycle
     * 
     * @param budget The budget to calculate for
     * @return Remaining balance
     */
    public double getRemainingBalance(Budget budget) {
        if (budget == null) {
            return 0.0;
        }
        
        double totalExpenses = getTotalExpensesInCycle(budget);
        return budget.getAmount() - totalExpenses;
    }
    
    /**
     * US22: Start a new budget cycle with the same budget amount
     * Updates the start date to today
     * 
     * @param budget The budget to reset
     * @return true if successful
     */
    public boolean startNewCycle(Budget budget) {
        if (budget == null) {
            return false;
        }
        
        // Set start date to today
        String today = dateFormat.format(new Date());
        budget.setStartDate(today);
        
        // Update in database
        int rowsAffected = updateBudget(budget);
        return rowsAffected > 0;
    }
    
    /**
     * Calculate the current cycle dates based on budget settings
     * 
     * @param budget The budget to calculate cycle for
     * @return String array [startDate, endDate] in YYYY-MM-DD format
     */
    public String[] getCurrentCycleDates(Budget budget) {
        try {
            Date startDate = dateFormat.parse(budget.getStartDate());
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            
            Calendar today = Calendar.getInstance();
            
            if (budget.getCycleType().equals("MONTHLY")) {
                // Monthly cycle: find the most recent occurrence of the cycle day
                int targetDay = budget.getCycleValue();
                
                // Set calendar to target day of current month
                cal.setTime(today.getTime());
                cal.set(Calendar.DAY_OF_MONTH, Math.min(targetDay, cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
                
                // If target date is in the future, go back one month
                if (cal.after(today)) {
                    cal.add(Calendar.MONTH, -1);
                    cal.set(Calendar.DAY_OF_MONTH, Math.min(targetDay, cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
                }
                
                String cycleStart = dateFormat.format(cal.getTime());
                
                // End date is one day before next cycle starts
                cal.add(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, Math.min(targetDay, cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
                cal.add(Calendar.DAY_OF_MONTH, -1);
                
                String cycleEnd = dateFormat.format(cal.getTime());
                
                return new String[] { cycleStart, cycleEnd };
                
            } else if (budget.getCycleType().equals("WEEKLY")) {
                // Weekly cycle: find the most recent occurrence of the cycle day
                int targetDayOfWeek = budget.getCycleValue(); // 1=Sunday, 7=Saturday
                
                cal.setTime(today.getTime());
                int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 7=Saturday
                
                int daysToSubtract = (currentDayOfWeek - targetDayOfWeek + 7) % 7;
                cal.add(Calendar.DAY_OF_MONTH, -daysToSubtract);
                
                String cycleStart = dateFormat.format(cal.getTime());
                
                // End date is 6 days later
                cal.add(Calendar.DAY_OF_MONTH, 6);
                String cycleEnd = dateFormat.format(cal.getTime());
                
                return new String[] { cycleStart, cycleEnd };
            }
            
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        // Fallback: return today as both start and end
        String today = dateFormat.format(new Date());
        return new String[] { today, today };
    }
    
    /**
     * Deactivate all budgets in the database
     * Used internally when activating a new budget
     */
    private void deactivateAllBudgets(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(ExpenseDbHelper.COLUMN_BUDGET_ACTIVE, 0);
        db.update(ExpenseDbHelper.TABLE_BUDGET, values, null, null);
    }
    
    /**
     * Helper method to convert Cursor to Budget object
     * 
     * @param cursor The cursor positioned at a valid row
     * @return Budget object created from cursor data
     */
    private Budget cursorToBudget(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_BUDGET_ID);
        int nameIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_BUDGET_NAME);
        int amountIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_BUDGET_AMOUNT);
        int startDateIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_BUDGET_START_DATE);
        int cycleTypeIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_BUDGET_CYCLE_TYPE);
        int cycleValueIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_BUDGET_CYCLE_VALUE);
        int activeIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_BUDGET_ACTIVE);
        
        long id = cursor.getLong(idIndex);
        String name = cursor.getString(nameIndex);
        double amount = cursor.getDouble(amountIndex);
        String startDate = cursor.getString(startDateIndex);
        String cycleType = cursor.getString(cycleTypeIndex);
        int cycleValue = cursor.getInt(cycleValueIndex);
        boolean active = cursor.getInt(activeIndex) == 1;
        
        return new Budget(id, name, amount, startDate, cycleType, cycleValue, active);
    }
    
    /**
     * Helper method to convert Cursor to Expense object
     * 
     * @param cursor The cursor positioned at a valid row
     * @return Expense object created from cursor data
     */
    private Expense cursorToExpense(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_ID);
        int titleIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_TITLE);
        int amountIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_AMOUNT);
        int categoryIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_CATEGORY);
        int noteIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_NOTE);
        int dateIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_DATE);
        int timeIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_TIME);
        
        long id = cursor.getLong(idIndex);
        String title = cursor.getString(titleIndex);
        double amount = cursor.getDouble(amountIndex);
        String category = cursor.getString(categoryIndex);
        String note = cursor.getString(noteIndex);
        String date = cursor.getString(dateIndex);
        String time = cursor.getString(timeIndex);
        
        return new Expense(id, title, amount, category, note, date, time);
    }
    
    /**
     * Close the database helper
     * Call this when the repository is no longer needed
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
