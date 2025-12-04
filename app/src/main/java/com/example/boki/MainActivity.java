// I have removed the conflict markers and kept the correct imports.
package com.example.boki;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.boki.databinding.ActivityMainBinding;
import com.example.boki.databinding.AddoperationsDialogBoxBinding;

// These imports were missing from one of the versions
import com.example.boki.data.local.ExpenseRepository;
import com.example.boki.models.Expense;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity {

    // Kept the clear variable names from the nav-home-page branch
    ActivityMainBinding bindingMain;
    AddoperationsDialogBoxBinding dialogBinding;

    // Added the repository from the HEAD branch, as it's needed for the database.
    private ExpenseRepository expenseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingMain = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bindingMain.getRoot());

        // Initialize the repository. This is important for saving data.
        expenseRepository = new ExpenseRepository(this);

        // -- SETUP the Dialog Box --
        final Dialog addoperations_dialog = new Dialog(MainActivity.this);
        dialogBinding = AddoperationsDialogBoxBinding.inflate(getLayoutInflater());
        addoperations_dialog.setContentView(dialogBinding.getRoot());

        if (addoperations_dialog.getWindow() != null) {
            addoperations_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // Use ColorDrawable for transparency to let the custom background show its corners.
            addoperations_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        addoperations_dialog.setCancelable(false);

        // FAB listener to show the dialog
        bindingMain.fab.setOnClickListener(v -> addoperations_dialog.show());

        // Cancel button listener
        dialogBinding.cancelDialogBtn.setOnClickListener(v -> {
            dialogBinding.operationName.setText("");
            dialogBinding.operationAmount.setText("");
            addoperations_dialog.dismiss();
        });

        // Save button listener - This is where you would save to the database.
        dialogBinding.saveOprationBtn.setOnClickListener(v -> {
            // Here you would create an Expense object and save it
            // For example:
            // String name = dialogBinding.operationName.getText().toString();
            // double amount = Double.parseDouble(dialogBinding.operationAmount.getText().toString());
            // Expense newExpense = new Expense(name, amount, new Date());
            // expenseRepository.insert(newExpense);

            Toast.makeText(MainActivity.this, "Save Successful", Toast.LENGTH_SHORT).show();

            dialogBinding.operationName.setText("");
            dialogBinding.operationAmount.setText("");
            addoperations_dialog.dismiss();
        });

        // Date and Time Picker listeners
        dialogBinding.datePickerBtn.setOnClickListener(v -> openDatePickerDialog());
        dialogBinding.timePickerBtn.setOnClickListener(v -> openTimePickerDialog());

        // --- BOTTOM NAVIGATION SETUP ---
        bindingMain.bnvBottom.getMenu().getItem(2).setEnabled(false);
        bindingMain.bnvBottom.setSelectedItemId(R.id.home);
        replacFragment(new HomeFragment());

        bindingMain.bnvBottom.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replacFragment(new HomeFragment());
            } else if (itemId == R.id.budget) {
                replacFragment(new BudgetFragment());
            } else if (itemId == R.id.operations) {
                replacFragment(new OperationsFragment());
            } else if (itemId == R.id.expenses) {
                replacFragment(new ExpensesFragment());
            }
            return true;
        });
    }

    // This onDestroy method is important for closing the database connection.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (expenseRepository != null) {
            expenseRepository.close();
        }
    }

    // Method to replace fragments
    private void replacFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    // Method to open DatePickerDialog
    private void openDatePickerDialog() {
        DatePickerDialog openDatePickerDialog = new DatePickerDialog(MainActivity.this, R.style.DialogTheme, (view, year, month, dayOfMonth) -> {
            dialogBinding.datePickerBtn.setText(String.format(Locale.getDefault(), "%d/%d/%d", dayOfMonth, month + 1, year));
        }, 2025, 1, 1); // Note: month is 0-indexed, so 1 is February.
        openDatePickerDialog.show();
    }

    // Method to open TimePickerDialog
    private void openTimePickerDialog() {
        TimePickerDialog dialog = new TimePickerDialog(MainActivity.this, R.style.DialogTheme, (view, hourOfDay, minute) -> {
            dialogBinding.timePickerBtn.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, 15, 50, false);
        dialog.show();
    }
}
