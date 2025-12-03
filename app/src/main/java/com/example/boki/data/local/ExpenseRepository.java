package com.example.boki.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.boki.models.Expense;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing Expense data operations.
 * Implements the Repository pattern to provide a clean API for CRUD operations.
 */
public class ExpenseRepository {
    
    private ExpenseDbHelper dbHelper;
    
    /**
     * Constructor - initializes the database helper
     * @param context Application context
     */
    public ExpenseRepository(Context context) {
        this.dbHelper = new ExpenseDbHelper(context);
    }
    
    /**
     * Insert a new expense into the database
     * 
     * @param expense The expense to insert (ID will be ignored)
     * @return The row ID of the newly inserted expense, or -1 if error
     */
    public long insertExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // ContentValues for safe insertion (prevents SQL injection)
        ContentValues values = new ContentValues();
        values.put(ExpenseDbHelper.COLUMN_TITLE, expense.getTitle());
        values.put(ExpenseDbHelper.COLUMN_AMOUNT, expense.getAmount());
        values.put(ExpenseDbHelper.COLUMN_CATEGORY, expense.getCategory());
        values.put(ExpenseDbHelper.COLUMN_NOTE, expense.getNote());
        values.put(ExpenseDbHelper.COLUMN_DATE, expense.getDate());
        values.put(ExpenseDbHelper.COLUMN_TIME, expense.getTime());
        
        // Insert and get the new row ID
        long newRowId = db.insert(ExpenseDbHelper.TABLE_EXPENSE, null, values);
        
        // Update the expense object with the new ID
        if (newRowId != -1) {
            expense.setId(newRowId);
        }
        
        return newRowId;
    }
    
    /**
     * Retrieve all expenses from the database
     * Optimized for fast loading (US10) with ORDER BY date DESC, time DESC
     * 
     * @return List of all expenses, ordered by most recent first
     */
    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            // Define columns to retrieve (best practice)
            String[] projection = {
                ExpenseDbHelper.COLUMN_ID,
                ExpenseDbHelper.COLUMN_TITLE,
                ExpenseDbHelper.COLUMN_AMOUNT,
                ExpenseDbHelper.COLUMN_CATEGORY,
                ExpenseDbHelper.COLUMN_NOTE,
                ExpenseDbHelper.COLUMN_DATE,
                ExpenseDbHelper.COLUMN_TIME
            };
            
            // ORDER BY date DESC, time DESC for most recent first (US10)
            // The index on date column makes this very fast
            String sortOrder = ExpenseDbHelper.COLUMN_DATE + " DESC, " + 
                              ExpenseDbHelper.COLUMN_TIME + " DESC";
            
            // Execute query
            cursor = db.query(
                ExpenseDbHelper.TABLE_EXPENSE,  // Table name
                projection,                      // Columns to return
                null,                           // WHERE clause (null = all rows)
                null,                           // WHERE clause arguments
                null,                           // GROUP BY
                null,                           // HAVING
                sortOrder                       // ORDER BY
            );
            
            // Iterate through cursor and build expense list
            while (cursor.moveToNext()) {
                Expense expense = cursorToExpense(cursor);
                expenses.add(expense);
            }
            
        } finally {
            // CRITICAL: Always close cursor to prevent memory leaks
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return expenses;
    }
    
    /**
     * Retrieve a single expense by ID
     * 
     * @param id The expense ID
     * @return The expense object, or null if not found
     */
    public Expense getExpenseById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        Expense expense = null;
        
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
            
            // Parameterized query to prevent SQL injection
            String selection = ExpenseDbHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = { String.valueOf(id) };
            
            cursor = db.query(
                ExpenseDbHelper.TABLE_EXPENSE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
            );
            
            // If found, convert to Expense object
            if (cursor.moveToFirst()) {
                expense = cursorToExpense(cursor);
            }
            
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return expense;
    }
    
    /**
     * Update an existing expense in the database
     * 
     * @param expense The expense to update (must have valid ID)
     * @return Number of rows affected (should be 1 if successful)
     */
    public int updateExpense(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // ContentValues with new data
        ContentValues values = new ContentValues();
        values.put(ExpenseDbHelper.COLUMN_TITLE, expense.getTitle());
        values.put(ExpenseDbHelper.COLUMN_AMOUNT, expense.getAmount());
        values.put(ExpenseDbHelper.COLUMN_CATEGORY, expense.getCategory());
        values.put(ExpenseDbHelper.COLUMN_NOTE, expense.getNote());
        values.put(ExpenseDbHelper.COLUMN_DATE, expense.getDate());
        values.put(ExpenseDbHelper.COLUMN_TIME, expense.getTime());
        
        // WHERE clause with parameterized query (prevents SQL injection)
        String whereClause = ExpenseDbHelper.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(expense.getId()) };
        
        // Perform update
        int rowsAffected = db.update(
            ExpenseDbHelper.TABLE_EXPENSE,
            values,
            whereClause,
            whereArgs
        );
        
        return rowsAffected;
    }
    
    /**
     * Delete an expense from the database
     * 
     * @param id The expense ID to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteExpense(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        // WHERE clause with parameterized query
        String whereClause = ExpenseDbHelper.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(id) };
        
        // Perform delete
        int rowsDeleted = db.delete(
            ExpenseDbHelper.TABLE_EXPENSE,
            whereClause,
            whereArgs
        );
        
        return rowsDeleted > 0;
    }
    
    /**
     * Delete all expenses from the database
     * Useful for testing and clearing data
     * 
     * @return Number of rows deleted
     */
    public int deleteAllExpenses() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(ExpenseDbHelper.TABLE_EXPENSE, null, null);
    }
    
    /**
     * Get the total count of expenses in the database
     * 
     * @return Total number of expenses
     */
    public int getExpenseCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + ExpenseDbHelper.TABLE_EXPENSE, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return count;
    }
    
    /**
     * Helper method to convert Cursor to Expense object
     * 
     * @param cursor The cursor positioned at a valid row
     * @return Expense object created from cursor data
     */
    private Expense cursorToExpense(Cursor cursor) {
        // Get column indices
        int idIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_ID);
        int titleIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_TITLE);
        int amountIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_AMOUNT);
        int categoryIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_CATEGORY);
        int noteIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_NOTE);
        int dateIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_DATE);
        int timeIndex = cursor.getColumnIndex(ExpenseDbHelper.COLUMN_TIME);
        
        // Extract data from cursor
        long id = cursor.getLong(idIndex);
        String title = cursor.getString(titleIndex);
        double amount = cursor.getDouble(amountIndex);
        String category = cursor.getString(categoryIndex);
        String note = cursor.getString(noteIndex);
        String date = cursor.getString(dateIndex);
        String time = cursor.getString(timeIndex);
        
        // Create and return Expense object
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
