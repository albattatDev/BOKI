package com.example.boki;

import android.content.res.ColorStateList;
import android.os.Bundle;import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;

import com.example.boki.databinding.FragmentExpensesBinding;

import java.util.ArrayList;
import java.util.List;


public class ExpensesFragment extends Fragment {

    // NOTE 1: Declare binding variable. It will be initialized in onCreateView.
    private FragmentExpensesBinding binding;

    // (Optional but recommended) Your adapters for the RecyclerView solution
//    private ExpenseCategoryAdapter expenseAdapter;
//    private HeaderAdapter headerAdapter;
//    private List<ExpenseCategory> expenseCategoryList = new ArrayList<>();


    public ExpensesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // NOTE 2: CORRECT WAY to initialize binding in a Fragment.
        // This inflates the layout and creates the view.
        binding = FragmentExpensesBinding.inflate(inflater, container, false);
        // Return the root view of the binding.
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NOTE 3: ALL view-related logic goes here, AFTER the view has been created.

        // This is the simplest solution using your old XML structure, assuming you are NOT using RecyclerView yet.
        // If you are using the RecyclerView solution, you should put this logic inside the HeaderAdapter.
        setupButtonClickListeners();

        // --- If you are using the ConcatAdapter solution, you would call this ---
        // setupRecyclerView();
    }

    private void setupButtonClickListeners() {
        // Define colors correctly within a lifecycle method where context is available.
        int selectedBgColor = ContextCompat.getColor(requireContext(), R.color.BOKI_MidPurple);
        int unselectedBgColor = ContextCompat.getColor(requireContext(), android.R.color.transparent);

        int selectedTextColor = ContextCompat.getColor(requireContext(), R.color.BOKI_MainPurple);
        int unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.BOKI_TextSecondary);

        // Group buttons for easier management
        Button monthButton = binding.monthDaysBtn;
        Button weekButton = binding.weekDaysBtn;
        Button dayButton = binding.daysBtn;

        monthButton.setOnClickListener(v -> {
            // Update UI for Monthly view
            updateButtonState(monthButton, selectedBgColor, selectedTextColor, weekButton, dayButton);
        });

        weekButton.setOnClickListener(v -> {
            // Update UI for Weekly view
            updateButtonState(weekButton, selectedBgColor, selectedTextColor, monthButton, dayButton);
        });

        dayButton.setOnClickListener(v -> {
            // Update UI for Daily view
            updateButtonState(dayButton, selectedBgColor, selectedTextColor, monthButton, weekButton);
        });

        // Set an initial state, e.g., "Weekly" is selected by default
        weekButton.performClick();
    }

    /**
     * A helper method to manage the visual state of the cycle buttons.
     * @param selectedButton The button that was just clicked.
     * @param otherButton1 The first of the other two buttons.
     * @param otherButton2 The second of the other two buttons.
     */
    private void updateButtonState(Button selectedButton, int selectedBgColor, int selectedTextColor, Button otherButton1, Button otherButton2) {
        // Style for the selected button
        selectedButton.setBackgroundTintList(ColorStateList.valueOf(selectedBgColor));
        selectedButton.setTextColor(selectedTextColor);

        // Reset styles for the other buttons
        int unselectedBgColor = ContextCompat.getColor(requireContext(), android.R.color.transparent);
        int unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.BOKI_TextSecondary);

        otherButton1.setBackgroundTintList(ColorStateList.valueOf(unselectedBgColor));
        otherButton1.setTextColor(unselectedTextColor);

        otherButton2.setBackgroundTintList(ColorStateList.valueOf(unselectedBgColor));
        otherButton2.setTextColor(unselectedTextColor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // NOTE 4: CRITICAL step to avoid memory leaks in Fragments.
        binding = null;
    }
}
