package com.example.boki.models;

public class ExpenseCategorySummary {
    private final String category;
    private final double totalAmount;
    private final double percentage; // e.g. 33.92 (we will display it as %33.92)

    public ExpenseCategorySummary(String category, double totalAmount, double percentage) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
    }

    public String getCategory() {
        return category;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getPercentage() {
        return percentage;
    }
}