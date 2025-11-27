package com.example.boki.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteOpenHelper subclass for managing the Expense database.
 * Handles database creation, versioning, and schema upgrades.
 */
public class ExpenseDbHelper extends SQLiteOpenHelper {
    
    // Database Configuration
    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table Name
    public static final String TABLE_EXPENSE = "expense";
    
    // Column Names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    
    // Index name for performance optimization
    private static final String INDEX_DATE = "idx_expense_date";
    
    // SQL Statement: Create expense table
    private static final String SQL_CREATE_TABLE = 
        "CREATE TABLE " + TABLE_EXPENSE + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT NOT NULL, " +
            COLUMN_AMOUNT + " REAL NOT NULL, " +
            COLUMN_CATEGORY + " TEXT NOT NULL, " +
            COLUMN_NOTE + " TEXT, " +
            COLUMN_DATE + " TEXT NOT NULL, " +
            COLUMN_TIME + " TEXT NOT NULL" +
        ");";
    
    // SQL Statement: Create index on date column for fast ORDER BY queries (US10)
    private static final String SQL_CREATE_INDEX = 
        "CREATE INDEX " + INDEX_DATE + " ON " + TABLE_EXPENSE + 
        " (" + COLUMN_DATE + " DESC);";
    
    // SQL Statement: Drop table (used in upgrades)
    private static final String SQL_DROP_TABLE = 
        "DROP TABLE IF EXISTS " + TABLE_EXPENSE + ";";
    
    /**
     * Constructor - creates or opens the database
     * @param context Application context
     */
    public ExpenseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    /**
     * Called when the database is created for the first time.
     * This is where the table creation happens.
     * 
     * @param db The database instance
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the expense table
        db.execSQL(SQL_CREATE_TABLE);
        
        // Create index on date column for performance (US10 - Fast Data Loading)
        // This dramatically improves ORDER BY date DESC performance
        db.execSQL(SQL_CREATE_INDEX);
    }
    
    /**
     * Called when the database needs to be upgraded.
     * This happens when DATABASE_VERSION is increased.
     *
     * Current Strategy (v1): Simple drop and recreate
     * Future Strategy: Use ALTER TABLE to preserve data during schema changes
     * 
     * @param db The database instance
     * @param oldVersion The old database version
     * @param newVersion The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1: No upgrades yet, just drop and recreate
        // TODO: When adding new columns/tables, use ALTER TABLE instead
        // to preserve existing user data
        
        // For now, simple drop and recreate (acceptable for v1)
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);
        
        // Future upgrade examples:
        // if (oldVersion < 2) {
        //     db.execSQL("ALTER TABLE expense ADD COLUMN attachment TEXT;");
        // }
        // if (oldVersion < 3) {
        //     db.execSQL("CREATE TABLE category (...);");
        // }
    }
    
    /**
     * Called when the database needs to be downgraded.
     * This happens when DATABASE_VERSION is decreased.
     * 
     * @param db The database instance
     * @param oldVersion The old database version
     * @param newVersion The new database version
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now, treat downgrade same as upgrade (drop and recreate)
        onUpgrade(db, oldVersion, newVersion);
    }
}
