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
    private static final int DATABASE_VERSION = 2; // Updated for budget table
    
    // Table Names
    public static final String TABLE_EXPENSE = "expense";
    public static final String TABLE_BUDGET = "budget";
    
    // Expense Column Names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    
    // Budget Column Names
    public static final String COLUMN_BUDGET_ID = "id";
    public static final String COLUMN_BUDGET_NAME = "name";
    public static final String COLUMN_BUDGET_AMOUNT = "amount";
    public static final String COLUMN_BUDGET_START_DATE = "start_date";
    public static final String COLUMN_BUDGET_CYCLE_TYPE = "cycle_type";
    public static final String COLUMN_BUDGET_CYCLE_VALUE = "cycle_value";
    public static final String COLUMN_BUDGET_ACTIVE = "active";
    
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
    
    // SQL Statement: Create budget table
    private static final String SQL_CREATE_BUDGET_TABLE = 
        "CREATE TABLE " + TABLE_BUDGET + " (" +
            COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_BUDGET_NAME + " TEXT NOT NULL, " +
            COLUMN_BUDGET_AMOUNT + " REAL NOT NULL, " +
            COLUMN_BUDGET_START_DATE + " TEXT NOT NULL, " +
            COLUMN_BUDGET_CYCLE_TYPE + " TEXT NOT NULL, " +
            COLUMN_BUDGET_CYCLE_VALUE + " INTEGER NOT NULL, " +
            COLUMN_BUDGET_ACTIVE + " INTEGER NOT NULL DEFAULT 0" +
        ");";
    
    // SQL Statement: Drop tables (used in upgrades)
    private static final String SQL_DROP_TABLE = 
        "DROP TABLE IF EXISTS " + TABLE_EXPENSE + ";";
    
    private static final String SQL_DROP_BUDGET_TABLE = 
        "DROP TABLE IF EXISTS " + TABLE_BUDGET + ";";
    
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
        
        // Create the budget table (US18-US22)
        db.execSQL(SQL_CREATE_BUDGET_TABLE);
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
        // Upgrade from version 1 to 2: Add budget table
        if (oldVersion < 2) {
            // Add budget table without dropping expense table (preserve user data)
            db.execSQL(SQL_CREATE_BUDGET_TABLE);
        }
        
        // For future upgrades, use similar pattern to preserve data
        // if (oldVersion < 3) {
        //     db.execSQL("ALTER TABLE ...");
        // }
        
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
