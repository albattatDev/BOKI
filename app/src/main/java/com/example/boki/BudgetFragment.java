package com.example.boki;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boki.databinding.AddbudgetDialogBinding;
import com.example.boki.databinding.DeletebudgetDialogBinding;
import com.example.boki.databinding.FragmentBudgetBinding;
import com.example.boki.data.local.BudgetRepository;
import com.example.boki.models.Budget;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetFragment extends Fragment implements BudgetAdapter.OnBudgetClickListener {

    // NOTE: Declare a variable for the fragment's binding object.
    private FragmentBudgetBinding binding;

    // NOTE: These variables are for managing the state of the day selection buttons in the dialog.
    private List<Button> dayButtons = new ArrayList<>();
    private Button selectedDayButton;
    
    // Repository for budget operations
    private BudgetRepository budgetRepository;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    // RecyclerView and adapter for budget list
    private RecyclerView budgetRecyclerView;
    private BudgetAdapter budgetAdapter;

    public BudgetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // NOTE: Inflate the layout for this fragment using View Binding.
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        
        // Initialize budget repository
        budgetRepository = new BudgetRepository(requireContext());
        
        // NOTE: Return the root view of the binding.
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Setup RecyclerView
            budgetRecyclerView = binding.expensesRecyclerView; // RecyclerView ID: expenses_recycler_view
            if (budgetRecyclerView != null) {
                budgetRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                budgetAdapter = new BudgetAdapter(getContext(), this);
                budgetRecyclerView.setAdapter(budgetAdapter);
            }

            // NOTE: Access views directly through the binding object.
            binding.addBudgetBtn.setOnClickListener(v -> {
                showAddBudgetDialog();
            });
            
            // Load and display existing budgets
            loadBudgets();
        } catch (Exception e) {
            Toast.makeText(getContext(), "خطأ في تهيئة الصفحة", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Load all budgets from database and display them
     */
    private void loadBudgets() {
        try {
            List<Budget> budgets = budgetRepository.getAllBudgets();
            if (budgetAdapter != null) {
                budgetAdapter.setBudgets(budgets);
            }
            
            // Show/hide empty state based on budget count
            if (budgets == null || budgets.isEmpty()) {
                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                binding.expensesRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyStateLayout.setVisibility(View.GONE);
                binding.expensesRecyclerView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "خطأ في تحميل الميزانيات", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * US19: Handle budget item click - show update/delete dialog
     */
    @Override
    public void onBudgetClick(Budget budget) {
        showUpdateDeleteBudgetDialog(budget);
    }

    private void showAddBudgetDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // NOTE 1: Inflate the dialog layout using View Binding.
        AddbudgetDialogBinding dialogBinding = AddbudgetDialogBinding.inflate(getLayoutInflater());

        // NOTE 2: Set the dialog's content to the root of the binding.
        dialog.setContentView(dialogBinding.getRoot());
        dialog.setCancelable(false);

        // NOTE 3: Configure the NumberPicker using the binding object.
        dialogBinding.dayOfMonthPicker.setMinValue(1);   // Set the minimum value to 1
        dialogBinding.dayOfMonthPicker.setMaxValue(31);  // Set the maximum value to 31
        dialogBinding.dayOfMonthPicker.setValue(15);     // Set a default value

        // Define colors for easy access
        int selectedBgColor = getResources().getColor(R.color.BOKI_MidPurple); // The purple background for the selected button
        int unselectedBgColor = getResources().getColor(android.R.color.transparent); // Transparent for the unselected one

        int selectedTextColor = getResources().getColor(R.color.BOKI_MainPurple); // A bright color for selected text
        int unselectedTextColor = getResources().getColor(R.color.BOKI_TextSecondary); // The default purple text color

        // ---  CANCEL BUTTON LISTENER ---
        dialogBinding.cancelDialogBtn.setOnClickListener(v -> {
            dialog.dismiss(); // Closes the dialog
        });

        // NOTE 4: Set up click listeners for the "Monthly" and "Weekly" buttons.
        dialogBinding.monthDaysBtn.setOnClickListener(v -> {
            // When "Monthly" is clicked:
            dialogBinding.dayOfMonthPicker.setVisibility(View.VISIBLE);  // Show NumberPicker
            dialogBinding.dayOfWeekScrollview.setVisibility(View.GONE);    // Hide day buttons

            // NOTE 1: Update background tint correctly
            dialogBinding.monthDaysBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedBgColor));
            dialogBinding.weekDaysBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselectedBgColor));

            // NOTE 2: Update text color
            dialogBinding.monthDaysBtn.setTextColor(selectedTextColor);
            dialogBinding.weekDaysBtn.setTextColor(unselectedTextColor);
        });

        dialogBinding.weekDaysBtn.setOnClickListener(v -> {
            // When "Weekly" is clicked:
            dialogBinding.dayOfMonthPicker.setVisibility(View.GONE);     // Hide NumberPicker
            dialogBinding.dayOfWeekScrollview.setVisibility(View.VISIBLE); // Show day buttons

            // NOTE 1: Update background tint correctly
            dialogBinding.weekDaysBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedBgColor));
            dialogBinding.monthDaysBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselectedBgColor));

            // NOTE 2: Update text color
            dialogBinding.weekDaysBtn.setTextColor(selectedTextColor);
            dialogBinding.monthDaysBtn.setTextColor(unselectedTextColor);
        });

        // To set the initial state correctly when the dialog opens
        dialogBinding.weekDaysBtn.performClick();

        // --- Day Button Selection Logic ---
        dayButtons.clear(); // Clear list from any previous dialog session
        dayButtons.add(dialogBinding.buttonSunday);
        dayButtons.add(dialogBinding.buttonMonday);
        dayButtons.add(dialogBinding.buttonTuesday);
        dayButtons.add(dialogBinding.buttonWednesday);
        dayButtons.add(dialogBinding.buttonThursday);
        dayButtons.add(dialogBinding.buttonFriday);
        dayButtons.add(dialogBinding.buttonSaturday);

        View.OnClickListener dayClickListener = v -> {
            // --- TASK 3: CHANGE TEXT COLOR FOR SELECTED DAY ---
            for (Button btn : dayButtons) {
                btn.setTextColor(unselectedTextColor); // Reset all buttons to gray
            }
            Button selectedButton = (Button) v;
            selectedButton.setTextColor(selectedTextColor); // Set the clicked button to purple
            selectDayButton(selectedButton);
        };

        // Assign the listener to each button
        for (Button btn : dayButtons) {
            btn.setOnClickListener(dayClickListener);
        }

        // Set initial state
        dialogBinding.weekDaysBtn.performClick();
        selectDayButton(dialogBinding.buttonSunday); // Select Sunday by default
        dialogBinding.buttonSunday.setTextColor(selectedTextColor); // Make default selection purple
        // --- End of Day Button Logic ---

        // --- CREATE BUDGET BUTTON LISTENER ---
        dialogBinding.crateBudgetBtn.setOnClickListener(v -> {
            // Get budget name and amount
            String budgetName = dialogBinding.budgetName.getText().toString().trim();
            String budgetAmount = dialogBinding.budgetAmount.getText().toString().trim();

            // Validate inputs
            if (budgetName.isEmpty()) {
                Toast.makeText(getContext(), "الرجاء إدخال اسم الميزانية", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (budgetAmount.isEmpty()) {
                Toast.makeText(getContext(), "الرجاء إدخال المبلغ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double amount;
            try {
                amount = Double.parseDouble(budgetAmount);
                if (amount <= 0) {
                    Toast.makeText(getContext(), "المبلغ يجب أن يكون أكبر من صفر", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "المبلغ غير صحيح", Toast.LENGTH_SHORT).show();
                return;
            }

            String cycleType;
            int cycleValue;

            // Check if weekly or monthly is selected
            if (dialogBinding.dayOfWeekScrollview.getVisibility() == View.VISIBLE) {
                // Weekly cycle
                cycleType = "WEEKLY";
                
                // Map day button to Calendar day of week (1=Sunday, 7=Saturday)
                if (selectedDayButton == null) {
                    Toast.makeText(getContext(), "الرجاء اختيار يوم", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Get the day value based on button
                if (selectedDayButton.getId() == R.id.button_sunday) {
                    cycleValue = Calendar.SUNDAY; // 1
                } else if (selectedDayButton.getId() == R.id.button_monday) {
                    cycleValue = Calendar.MONDAY; // 2
                } else if (selectedDayButton.getId() == R.id.button_tuesday) {
                    cycleValue = Calendar.TUESDAY; // 3
                } else if (selectedDayButton.getId() == R.id.button_wednesday) {
                    cycleValue = Calendar.WEDNESDAY; // 4
                } else if (selectedDayButton.getId() == R.id.button_thursday) {
                    cycleValue = Calendar.THURSDAY; // 5
                } else if (selectedDayButton.getId() == R.id.button_friday) {
                    cycleValue = Calendar.FRIDAY; // 6
                } else if (selectedDayButton.getId() == R.id.button_saturday) {
                    cycleValue = Calendar.SATURDAY; // 7
                } else {
                    cycleValue = Calendar.SUNDAY; // Default to Sunday
                }
            } else {
                // Monthly cycle
                cycleType = "MONTHLY";
                cycleValue = dialogBinding.dayOfMonthPicker.getValue(); // Get value from NumberPicker (1-31)
            }

            try {
                // Get today's date as start date
                String startDate = dateFormat.format(new Date());
                
                // Create budget object (will be set as active by default)
                Budget budget = new Budget(budgetName, amount, startDate, cycleType, cycleValue, true);
                
                // Insert into database
                long budgetId = budgetRepository.insertBudget(budget);
                
                if (budgetId > 0) {
                    // Close dialog first
                    dialog.dismiss();
                    
                    // Show success message and reload on UI thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "تم إنشاء الميزانية بنجاح", Toast.LENGTH_SHORT).show();
                            loadBudgets();
                        });
                    }
                } else {
                    Toast.makeText(getContext(), "فشل في إنشاء الميزانية", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                dialog.dismiss();
                Toast.makeText(getContext(), "حدث خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /**
     * US19, US22: Show dialog to update, delete, or start new cycle for a budget
     */
    private void showUpdateDeleteBudgetDialog(Budget budget) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DeletebudgetDialogBinding dialogBinding = DeletebudgetDialogBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        dialog.setCancelable(false);

        // Pre-fill budget data
        dialogBinding.budgetName.setText(budget.getName());
        dialogBinding.budgetAmount.setText(String.valueOf(budget.getAmount()));

        // Setup NumberPicker
        dialogBinding.dayOfMonthPicker.setMinValue(1);
        dialogBinding.dayOfMonthPicker.setMaxValue(31);
        
        if (budget.getCycleType().equals("MONTHLY")) {
            dialogBinding.dayOfMonthPicker.setValue(budget.getCycleValue());
            dialogBinding.dayOfMonthPicker.setVisibility(View.VISIBLE);
            dialogBinding.dayOfWeekScrollview.setVisibility(View.GONE);
        } else {
            dialogBinding.dayOfMonthPicker.setVisibility(View.GONE);
            dialogBinding.dayOfWeekScrollview.setVisibility(View.VISIBLE);
        }

        // Define colors
        int selectedBgColor = getResources().getColor(R.color.BOKI_MidPurple);
        int unselectedBgColor = getResources().getColor(android.R.color.transparent);
        int selectedTextColor = getResources().getColor(R.color.BOKI_MainPurple);
        int unselectedTextColor = getResources().getColor(R.color.BOKI_TextSecondary);

        // Cancel button
        dialogBinding.cancelDialogBtn.setOnClickListener(v -> dialog.dismiss());

        // Delete button (US19)
        dialogBinding.deleteBtn.setOnClickListener(v -> {
            try {
                if (budgetRepository.deleteBudget(budget.getId())) {
                    dialog.dismiss();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "تم حذف الميزانية", Toast.LENGTH_SHORT).show();
                            loadBudgets();
                        });
                    }
                } else {
                    Toast.makeText(getContext(), "فشل في حذف الميزانية", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                dialog.dismiss();
                Toast.makeText(getContext(), "حدث خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Monthly/Weekly toggle buttons
        dialogBinding.monthDaysBtn.setOnClickListener(v -> {
            dialogBinding.dayOfMonthPicker.setVisibility(View.VISIBLE);
            dialogBinding.dayOfWeekScrollview.setVisibility(View.GONE);
            dialogBinding.monthDaysBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedBgColor));
            dialogBinding.weekDaysBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselectedBgColor));
            dialogBinding.monthDaysBtn.setTextColor(selectedTextColor);
            dialogBinding.weekDaysBtn.setTextColor(unselectedTextColor);
        });

        dialogBinding.weekDaysBtn.setOnClickListener(v -> {
            dialogBinding.dayOfMonthPicker.setVisibility(View.GONE);
            dialogBinding.dayOfWeekScrollview.setVisibility(View.VISIBLE);
            dialogBinding.weekDaysBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedBgColor));
            dialogBinding.monthDaysBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselectedBgColor));
            dialogBinding.weekDaysBtn.setTextColor(selectedTextColor);
            dialogBinding.monthDaysBtn.setTextColor(unselectedTextColor);
        });

        // Day button selection logic
        dayButtons.clear();
        dayButtons.add(dialogBinding.buttonSunday);
        dayButtons.add(dialogBinding.buttonMonday);
        dayButtons.add(dialogBinding.buttonTuesday);
        dayButtons.add(dialogBinding.buttonWednesday);
        dayButtons.add(dialogBinding.buttonThursday);
        dayButtons.add(dialogBinding.buttonFriday);
        dayButtons.add(dialogBinding.buttonSaturday);

        View.OnClickListener dayClickListener = v -> {
            for (Button btn : dayButtons) {
                btn.setTextColor(unselectedTextColor);
            }
            Button selectedButton = (Button) v;
            selectedButton.setTextColor(selectedTextColor);
            selectDayButton(selectedButton);
        };

        for (Button btn : dayButtons) {
            btn.setOnClickListener(dayClickListener);
        }

        // Select the current day if weekly
        if (budget.getCycleType().equals("WEEKLY")) {
            Button dayButton = getDayButtonForValue(budget.getCycleValue(), dialogBinding);
            if (dayButton != null) {
                selectDayButton(dayButton);
                dayButton.setTextColor(selectedTextColor);
            }
        }

        // Update Budget Button (US19)
        dialogBinding.crateBudgetBtn.setText("تحديث الميزانية");
        dialogBinding.crateBudgetBtn.setOnClickListener(v -> {
            String budgetName = dialogBinding.budgetName.getText().toString().trim();
            String budgetAmount = dialogBinding.budgetAmount.getText().toString().trim();

            if (budgetName.isEmpty() || budgetAmount.isEmpty()) {
                Toast.makeText(getContext(), "الرجاء ملء جميع الحقول", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(budgetAmount);
                if (amount <= 0) {
                    Toast.makeText(getContext(), "المبلغ يجب أن يكون أكبر من صفر", Toast.LENGTH_SHORT).show();
                    return;
                }

                String cycleType;
                int cycleValue;

                if (dialogBinding.dayOfWeekScrollview.getVisibility() == View.VISIBLE) {
                    cycleType = "WEEKLY";
                    cycleValue = getDayValueFromButton(selectedDayButton);
                } else {
                    cycleType = "MONTHLY";
                    cycleValue = dialogBinding.dayOfMonthPicker.getValue();
                }

                budget.setName(budgetName);
                budget.setAmount(amount);
                budget.setCycleType(cycleType);
                budget.setCycleValue(cycleValue);

                try {
                    if (budgetRepository.updateBudget(budget) > 0) {
                        dialog.dismiss();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "تم تحديث الميزانية", Toast.LENGTH_SHORT).show();
                                loadBudgets();
                            });
                        }
                    } else {
                        Toast.makeText(getContext(), "فشل في تحديث الميزانية", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "حدث خطأ: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "المبلغ غير صحيح", Toast.LENGTH_SHORT).show();
            }
        });

        // US22: Start New Cycle button
        dialogBinding.startNewCycleBtn.setOnClickListener(v -> {
            try {
                if (budgetRepository.startNewCycle(budget)) {
                    dialog.dismiss();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "تم بدء دورة جديدة", Toast.LENGTH_SHORT).show();
                            loadBudgets();
                        });
                    }
                } else {
                    Toast.makeText(getContext(), "فشل في بدء دورة جديدة", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                dialog.dismiss();
                Toast.makeText(getContext(), "حدث خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /**
     * Get day button based on Calendar value
     */
    private Button getDayButtonForValue(int dayValue, DeletebudgetDialogBinding binding) {
        switch (dayValue) {
            case Calendar.SUNDAY: return binding.buttonSunday;
            case Calendar.MONDAY: return binding.buttonMonday;
            case Calendar.TUESDAY: return binding.buttonTuesday;
            case Calendar.WEDNESDAY: return binding.buttonWednesday;
            case Calendar.THURSDAY: return binding.buttonThursday;
            case Calendar.FRIDAY: return binding.buttonFriday;
            case Calendar.SATURDAY: return binding.buttonSaturday;
            default: return binding.buttonSunday;
        }
    }

    /**
     * Get Calendar day value from button
     */
    private int getDayValueFromButton(Button button) {
        if (button == null) return Calendar.SUNDAY;
        
        if (button.getId() == R.id.button_sunday) return Calendar.SUNDAY;
        if (button.getId() == R.id.button_monday) return Calendar.MONDAY;
        if (button.getId() == R.id.button_tuesday) return Calendar.TUESDAY;
        if (button.getId() == R.id.button_wednesday) return Calendar.WEDNESDAY;
        if (button.getId() == R.id.button_thursday) return Calendar.THURSDAY;
        if (button.getId() == R.id.button_friday) return Calendar.FRIDAY;
        if (button.getId() == R.id.button_saturday) return Calendar.SATURDAY;
        
        return Calendar.SUNDAY;
    }

    /**
     * Manages the visual state of the day selection buttons.
     */
    private void selectDayButton(Button buttonToSelect) {
        // 1. Deselect the previously selected button, if it exists
        if (selectedDayButton != null) {
            selectedDayButton.setSelected(false);
            // Hide the dot drawable by setting it to null
            selectedDayButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }

        // 2. Select the new button
        buttonToSelect.setSelected(true);
        // Show the dot drawable by setting the drawable resource
        buttonToSelect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.dot_indicator, 0, 0, 0);

        // 3. Keep track of the currently selected button
        selectedDayButton = buttonToSelect;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Close repository connection
        if (budgetRepository != null) {
            budgetRepository.close();
        }
        
        // NOTE: CRITICAL STEP for fragments to avoid memory leaks.
        // The view is destroyed, so we must release the reference to the binding object.
        binding = null;
    }
}
