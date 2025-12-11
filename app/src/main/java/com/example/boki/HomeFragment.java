package com.example.boki;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.boki.data.local.BudgetRepository;
import com.example.boki.models.Budget;

import java.util.Locale;

/**
 * Home Fragment - Dashboard showing budget remaining balance
 * Implements US21: Display Remaining Balance
 */
public class HomeFragment extends Fragment {

    private BudgetRepository budgetRepository;
    private TextView remainingBalanceTextView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize repository
        budgetRepository = new BudgetRepository(requireContext());
        
        // Find the TextView that shows the remaining balance
        remainingBalanceTextView = view.findViewById(R.id.textView);
        
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Load and display remaining balance
        updateRemainingBalance();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh balance when fragment becomes visible
        updateRemainingBalance();
    }
    
    /**
     * US21: Update the remaining balance display
     * Shows the remaining balance from the active budget
     */
    private void updateRemainingBalance() {
        try {
            Budget activeBudget = budgetRepository.getActiveBudget();
            
            if (activeBudget != null) {
                double remainingBalance = budgetRepository.getRemainingBalance(activeBudget);
                
                // Format the balance to 2 decimal places - always show absolute value
                String formattedBalance = String.format(Locale.getDefault(), "%.2f", Math.abs(remainingBalance));
                remainingBalanceTextView.setText(formattedBalance);
                
                // Optional: Change text color based on remaining balance
                if (remainingBalance < 0) {
                    // Over budget - show in red
                    remainingBalanceTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else if (remainingBalance < activeBudget.getAmount() * 0.2) {
                    // Less than 20% remaining - show warning color
                    remainingBalanceTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    // Sufficient balance - normal color
                    remainingBalanceTextView.setTextColor(getResources().getColor(R.color.BOKI_TextPrimary));
                }
            } else {
                // No active budget
                remainingBalanceTextView.setText("0.00");
                remainingBalanceTextView.setTextColor(getResources().getColor(R.color.BOKI_TextPrimary));
            }
        } catch (Exception e) {
            // Handle any errors gracefully
            remainingBalanceTextView.setText("0.00");
            remainingBalanceTextView.setTextColor(getResources().getColor(R.color.BOKI_TextPrimary));
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Close repository connection
        if (budgetRepository != null) {
            budgetRepository.close();
        }
    }
}