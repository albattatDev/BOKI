package com.example.boki.models;

/**
 * Domain model representing a budget entry.
 * Supports monthly and weekly budget cycles with automatic period tracking.
 */
public class Budget {
    
    // Database primary key (-1 indicates unsaved budget)
    private long id;
    
    // Core budget fields
    private String name;           // Budget name/description
    private double amount;         // Total budget amount for the period
    private String startDate;      // ISO 8601 date format: YYYY-MM-DD (when budget period starts)
    private String cycleType;      // "MONTHLY" or "WEEKLY"
    private int cycleValue;        // For MONTHLY: day of month (1-31), For WEEKLY: day of week (1=Sunday, 7=Saturday)
    private boolean active;        // Only one budget can be active at a time
    
    /**
     * Constructor for new budgets (no ID yet)
     * Use this when creating budgets before inserting to database
     */
    public Budget(String name, double amount, String startDate, String cycleType, int cycleValue, boolean active) {
        this.id = -1; // -1 indicates new budget not yet in database
        this.name = name;
        this.amount = amount;
        this.startDate = startDate;
        this.cycleType = cycleType;
        this.cycleValue = cycleValue;
        this.active = active;
    }
    
    /**
     * Constructor for existing budgets (with ID from database)
     * Use this when retrieving budgets from database
     */
    public Budget(long id, String name, double amount, String startDate, String cycleType, int cycleValue, boolean active) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.startDate = startDate;
        this.cycleType = cycleType;
        this.cycleValue = cycleValue;
        this.active = active;
    }
    
    // Getters and Setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getCycleType() {
        return cycleType;
    }
    
    public void setCycleType(String cycleType) {
        this.cycleType = cycleType;
    }
    
    public int getCycleValue() {
        return cycleValue;
    }
    
    public void setCycleValue(int cycleValue) {
        this.cycleValue = cycleValue;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Utility method for debugging and logging
     * @return String representation of the budget
     */
    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", startDate='" + startDate + '\'' +
                ", cycleType='" + cycleType + '\'' +
                ", cycleValue=" + cycleValue +
                ", active=" + active +
                '}';
    }
    
    /**
     * Check if this budget has been saved to database
     * @return true if budget exists in database (has valid ID)
     */
    public boolean isSaved() {
        return id > 0;
    }
}
