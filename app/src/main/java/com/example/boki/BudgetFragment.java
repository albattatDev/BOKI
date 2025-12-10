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
import com.example.boki.databinding.FragmentBudgetBinding;

import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment {

    // NOTE: Declare a variable for the fragment's binding object.
    private FragmentBudgetBinding binding;

    // NOTE: These variables are for managing the state of the day selection buttons in the dialog.
    private List<Button> dayButtons = new ArrayList<>();
    private Button selectedDayButton;

    public BudgetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // NOTE: Inflate the layout for this fragment using View Binding.
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        // NOTE: Return the root view of the binding.
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // NOTE: Access views directly through the binding object.
        binding.addBudgetBtn.setOnClickListener(v -> {
            showAddBudgetDialog();
        });
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

            String cycleType;
            String selectedDay;

            // Check if weekly or monthly is selected
            if (dialogBinding.dayOfWeekScrollview.getVisibility() == View.VISIBLE) {
                cycleType = "أسبوعي";
                selectedDay = selectedDayButton.getText().toString(); // Get text from the selected button
            } else {
                cycleType = "شهري";
                selectedDay = String.valueOf(dialogBinding.dayOfMonthPicker.getValue()); // Get value from NumberPicker
            }

            // Build the message for the Toast
            String message = "الاسم: " + budgetName + "\n" +
                    "المبلغ: " + budgetAmount + "\n" +
                    "الدورة: " + cycleType + "\n" +
                    "اليوم: " + selectedDay;

            // Show the Toast
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

            // You can also close the dialog after creation
             dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
        // NOTE: CRITICAL STEP for fragments to avoid memory leaks.
        // The view is destroyed, so we must release the reference to the binding object.
        binding = null;
    }
}
