package com.example.boki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boki.models.Budget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Adapter to display a list of Budget objects in a RecyclerView.
 */
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    
    private List<Budget> budgets = new ArrayList<>();
    private Context context;
    private OnBudgetClickListener listener;
    
    /**
     * Interface for handling budget item clicks
     */
    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
    }
    
    public BudgetAdapter(Context context, OnBudgetClickListener listener) {
        this.context = context;
        this.listener = listener;
    }
    
    /**
     * Update the list of budgets displayed
     */
    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets != null ? budgets : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
            .inflate(R.layout.budget_list_item, parent, false);
        return new BudgetViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        holder.bind(budget);
    }
    
    @Override
    public int getItemCount() {
        return budgets.size();
    }
    
    /**
     * ViewHolder class for budget items
     */
    public class BudgetViewHolder extends RecyclerView.ViewHolder {
        
        private TextView budgetTitle;
        private TextView budgetAmount;
        private TextView daysLeft;
        
        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            
            budgetTitle = itemView.findViewById(R.id.budget_title_id);
            budgetAmount = itemView.findViewById(R.id.amount_id);
            daysLeft = itemView.findViewById(R.id.date_id); // Reusing this TextView for days left
            
            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBudgetClick(budgets.get(position));
                }
            });
        }
        
        public void bind(Budget budget) {
            // Set budget name
            budgetTitle.setText(budget.getName());
            
            // Format and set budget amount
            String formattedAmount = String.format(Locale.getDefault(), "%.2f", budget.getAmount());
            budgetAmount.setText(formattedAmount);
            
            // Calculate and display cycle info
            String cycleInfo = getCycleInfo(budget);
            daysLeft.setText(cycleInfo);
            
            // Highlight active budget
            if (budget.isActive()) {
                itemView.setAlpha(1.0f);
            } else {
                itemView.setAlpha(0.6f);
            }
        }
        
        /**
         * Get cycle information string (e.g., "أسبوعي - الأحد" or "شهري - يوم 27")
         */
        private String getCycleInfo(Budget budget) {
            if (budget.getCycleType().equals("WEEKLY")) {
                String dayName = getDayName(budget.getCycleValue());
                return "أسبوعي - " + dayName;
            } else {
                return "شهري - يوم " + budget.getCycleValue();
            }
        }
        
        /**
         * Convert Calendar day constant to Arabic day name
         */
        private String getDayName(int dayOfWeek) {
            switch (dayOfWeek) {
                case Calendar.SUNDAY: return "الأحد";
                case Calendar.MONDAY: return "الاثنين";
                case Calendar.TUESDAY: return "الثلاثاء";
                case Calendar.WEDNESDAY: return "الأربعاء";
                case Calendar.THURSDAY: return "الخميس";
                case Calendar.FRIDAY: return "الجمعة";
                case Calendar.SATURDAY: return "السبت";
                default: return "غير محدد";
            }
        }
    }
}
