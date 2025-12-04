package com.example.boki.models;

/**
 * Domain model representing an expense entry.
 * This POJO (Plain Old Java Object) is used across the application
 * to represent expense data from various sources (database, UI, etc.).
 */
public class Expense {
    
    // Database primary key (-1 indicates unsaved expense)
    private long id;
    
    // Core expense fields
    private String title;      // Description of the expense
    private double amount;     // Expense amount
    private String category;   // Category (Food, Transport, Shopping, etc.)
    private String note;       // Optional notes/description
    private String date;       // ISO 8601 date format: YYYY-MM-DD
    private String time;       // ISO 8601 time format: HH:MM:SS
    
    /**
     * Constructor for new expenses (no ID yet)
     * Use this when creating expenses before inserting to database
     */
    public Expense(String title, double amount, String category, String note, 
                   String date, String time) {
        this.id = -1; // -1 indicates new expense not yet in database
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
        this.time = time;
    }
    
    /**
     * Constructor for existing expenses (with ID from database)
     * Use this when retrieving expenses from database
     */
    public Expense(long id, String title, double amount, String category, 
                   String note, String date, String time) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
        this.time = time;
    }
    
    // Getters and Setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    /**
     * Utility method for debugging and logging
     * @return String representation of the expense
     */
    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", note='" + note + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
    
    /**
     * Check if this expense has been saved to database
     * @return true if expense exists in database (has valid ID)
     */
    public boolean isSaved() {
        return id > 0;
    }
}
