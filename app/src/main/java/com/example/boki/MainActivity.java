// I have removed the conflict markers and kept the correct imports.
package com.example.boki;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.LocaleData;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.boki.databinding.ActivityMainBinding;
import com.example.boki.databinding.AddoperationsDialogBoxBinding;

// These imports were missing from one of the versions
import com.example.boki.data.local.ExpenseRepository;
import com.example.boki.databinding.CategorySelectionDialogBinding;
import com.example.boki.models.Expense;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity {

    // Kept the clear variable names from the nav-home-page branch
    ActivityMainBinding bindingMain;
    AddoperationsDialogBoxBinding dialogBinding;

    CategorySelectionDialogBinding categoryBinding;

    // Added the repository from the HEAD branch, as it's needed for the database.
    private ExpenseRepository expenseRepository;

    //inslize the dialog objct
     Dialog addoperations_dialog,category_dialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingMain = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bindingMain.getRoot());

        // Initialize the repository. This is important for saving data.
        expenseRepository = new ExpenseRepository(this);

        // -- DIALOG BOX SETUP --
        //1- ADD OPERATION DIALOG
        addoperations_dialog = new Dialog(MainActivity.this);
        dialogBinding = AddoperationsDialogBoxBinding.inflate(getLayoutInflater());
        addoperations_dialog.setContentView(dialogBinding.getRoot());

        if (addoperations_dialog.getWindow() != null) {
            addoperations_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // Use ColorDrawable for transparency to let the custom background show its corners.
            addoperations_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        addoperations_dialog.setCancelable(false);

        //2- CATEGORY DIALOG
        category_dialog = new Dialog(MainActivity.this);
        categoryBinding = CategorySelectionDialogBinding.inflate(getLayoutInflater());
        category_dialog.setContentView(categoryBinding.getRoot());

        if (category_dialog.getWindow() != null) {
            category_dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            category_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        category_dialog.setCancelable(false);




        // -- LISTENERS SETUP --

        // FAB listener to show the dialog
        bindingMain.fab.setOnClickListener(v -> {
            // Show the dialog
            addoperations_dialog.show();
            //set the date and time to curent date and time
            //set the date and time to current date and time in ISO format
            dialogBinding.datePickerBtn.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));

            // Show AM/PM to user, but keep DB time in 24h with seconds for correct sorting
            String timeUi = new SimpleDateFormat("h:mm a", Locale.US).format(new Date());
            String timeDb = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
            dialogBinding.timePickerBtn.setText(timeUi);
            dialogBinding.timePickerBtn.setTag(timeDb);

            //reset the category button to other
            MaterialButton targetButton = (MaterialButton) dialogBinding.categoryBtn;
            targetButton.setText(categoryBinding.otherBtn.getText());
            targetButton.setTextColor(getResources().getColor(R.color.BOKI_TextPrimary, getTheme())); // Assuming a default color
            targetButton.setBackgroundTintList(getResources().getColorStateList(R.color.BOKI_TextPrimary_More_Transparent, getTheme()));
            targetButton.setStrokeColor(getResources().getColorStateList(R.color.BOKI_TextPrimary_Transparent, getTheme()));

        });

        //Open category dialog
        dialogBinding.categoryBtn.setOnClickListener(v -> {
            category_dialog.show();
        });

        //Cancel button listener
        dialogBinding.cancelDialogBtn.setOnClickListener(v -> {
            closeAddOperationDialog();
        });

        // Save button listener - This is where you would save to the database.
        dialogBinding.saveOprationBtn.setOnClickListener(v -> {
            // Here you would create an Expense object and save it

            //git category
            String category = dialogBinding.categoryBtn.getText().toString();

            //git name
            String name = dialogBinding.operationName.getText().toString();
            //if condition to check this to value are not empty
            if (name.isEmpty()){
                Toast.makeText(MainActivity.this, "Please Enter Operation Name", Toast.LENGTH_SHORT).show();
                return;
            }

            //git amount
            double amount; // Declare amount variable
            String amountStr = dialogBinding.operationAmount.getText().toString();

            // Check if the amount field is empty
            if (amountStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return; // Stop execution
            }

            try {
                // Try to parse the string into a double
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                // This block runs if parsing fails (e.g., input is "abc")
                Toast.makeText(MainActivity.this, "Please enter a valid number for the amount", Toast.LENGTH_SHORT).show();
                return; // Stop execution
            }

             //get date
             String date = dialogBinding.datePickerBtn.getText().toString();
             //get time
            // Get DB time from tag (sortable HH:mm:ss). UI text is AM/PM.
            String time = (String) dialogBinding.timePickerBtn.getTag();
            if (time == null || time.trim().isEmpty()) {
                time = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
            }

            Expense newExpense = new Expense(name, amount,category, null, date, time);
            // Save the expense to the database
            expenseRepository.insertExpense(newExpense);

            // Notify all fragments that an expense was added so they can refresh UI immediately
            Bundle result = new Bundle();
            result.putBoolean("expense_added", true);
            result.putString("expense_added_date", date);
            result.putString("expense_added_time", time);
            getSupportFragmentManager().setFragmentResult("expense_refresh", result);


//             FOT TEST PURPOSE
//            read the data base to check if it saved or not
//             Retrieve all expenses (sorted by date DESC, time DESC)
//            List<Expense> allExpenses = expenseRepository.getAllExpenses();
//
//            // Iterate through expenses
//            for (Expense expense : allExpenses) {
//                Log.d("Database", "Expense: " + expense.getTitle() + " - $" + expense.getAmount()+"category:"+ expense.getCategory()+"date:"+expense.getDate()+"Time:"+expense.getTime()) ;
//            }
//
//            // Check if empty
//            if (allExpenses.isEmpty()) {
//                Log.d("Database", "No expenses found");
//            }

            closeAddOperationDialog();
        });

        //1- CATEGORY DIALOG LISTENER

        //cancel button listener for category dialog
        categoryBinding.cancelDialogBtn.setOnClickListener(v -> {
            category_dialog.dismiss();
        });

        // --- LISTENERS FOR ALL CATEGORY BUTTONS ---

        // 1. Food Button Listener
        categoryBinding.foodBtn.setOnClickListener(v -> {
            // Cast the target button to MaterialButton to access styling methods
            MaterialButton targetButton = (MaterialButton) dialogBinding.categoryBtn;

            // Update Text and Style
            targetButton.setText(categoryBinding.foodBtn.getText());
            targetButton.setTextColor(getResources().getColor(R.color.BOKI_Pink, getTheme()));
            targetButton.setBackgroundTintList(getResources().getColorStateList(R.color.BOKI_Pink_Transparent, getTheme()));
            targetButton.setStrokeColor(getResources().getColorStateList(R.color.BOKI_Pink, getTheme()));

            // Close the category selection dialog
            category_dialog.dismiss();
        });

        // 2. Family Button Listener
        categoryBinding.familyBtn.setOnClickListener(v -> {
            MaterialButton targetButton = (MaterialButton) dialogBinding.categoryBtn;
            targetButton.setText(categoryBinding.familyBtn.getText());
            targetButton.setTextColor(getResources().getColor(R.color.BOKI_Blue, getTheme()));
            targetButton.setBackgroundTintList(getResources().getColorStateList(R.color.BOKI_Blue_Transparent, getTheme()));
            targetButton.setStrokeColor(getResources().getColorStateList(R.color.BOKI_Blue, getTheme()));
            category_dialog.dismiss();
        });

        // 3. Health Button Listener
        categoryBinding.HelthBtn.setOnClickListener(v -> { // Note: ID in XML is Helth_btn
            MaterialButton targetButton = (MaterialButton) dialogBinding.categoryBtn;
            targetButton.setText(categoryBinding.HelthBtn.getText());
            targetButton.setTextColor(getResources().getColor(R.color.BOKI_LightRead, getTheme()));
            targetButton.setBackgroundTintList(getResources().getColorStateList(R.color.BOKI_LightRead_Transparent, getTheme()));
            targetButton.setStrokeColor(getResources().getColorStateList(R.color.BOKI_LightRead, getTheme()));
            category_dialog.dismiss();
        });

        // 4. Transportation Button Listener
        categoryBinding.transportationBtn.setOnClickListener(v -> {
            MaterialButton targetButton = (MaterialButton) dialogBinding.categoryBtn;
            targetButton.setText(categoryBinding.transportationBtn.getText());
            targetButton.setTextColor(getResources().getColor(R.color.BOKI_lightPurple, getTheme()));
            targetButton.setBackgroundTintList(getResources().getColorStateList(R.color.BOKI_lightPurple_Transparent, getTheme()));
            targetButton.setStrokeColor(getResources().getColorStateList(R.color.BOKI_lightPurple, getTheme()));
            category_dialog.dismiss();
        });

        // 5. Communication Button Listener
        categoryBinding.communicationBtn.setOnClickListener(v -> {
            MaterialButton targetButton = (MaterialButton) dialogBinding.categoryBtn;
            targetButton.setText(categoryBinding.communicationBtn.getText());
            targetButton.setTextColor(getResources().getColor(R.color.BOKI_lightBlue, getTheme()));
            targetButton.setBackgroundTintList(getResources().getColorStateList(R.color.BOKI_lightBlue_Transparent, getTheme()));
            targetButton.setStrokeColor(getResources().getColorStateList(R.color.BOKI_lightBlue, getTheme()));
            category_dialog.dismiss();
        });

        // 6. Education Button Listener
        categoryBinding.educationBtn.setOnClickListener(v -> {
            MaterialButton targetButton = (MaterialButton) dialogBinding.categoryBtn;
            targetButton.setText(categoryBinding.educationBtn.getText());
            targetButton.setTextColor(getResources().getColor(R.color.BOKI_Green, getTheme()));
            targetButton.setBackgroundTintList(getResources().getColorStateList(R.color.BOKI_Green_transparent, getTheme()));
            targetButton.setStrokeColor(getResources().getColorStateList(R.color.BOKI_Green, getTheme()));
            category_dialog.dismiss();
        });

        // 7. Entertainment Button Listener
        categoryBinding.entertainmentBtn.setOnClickListener(v -> {
            MaterialButton targetButton = (MaterialButton) dialogBinding.categoryBtn;
            targetButton.setText(categoryBinding.entertainmentBtn.getText());
            targetButton.setTextColor(getResources().getColor(R.color.BOKI_Orange, getTheme())); // Assuming a default color
            targetButton.setBackgroundTintList(getResources().getColorStateList(R.color.BOKI_Orange_Transparent, getTheme()));
            targetButton.setStrokeColor(getResources().getColorStateList(R.color.BOKI_Orange, getTheme()));
            category_dialog.dismiss();
        });

        // 8. Other Button Listener
        categoryBinding.otherBtn.setOnClickListener(v -> {
            MaterialButton targetButton = (MaterialButton) dialogBinding.categoryBtn;
            targetButton.setText(categoryBinding.otherBtn.getText());
            targetButton.setTextColor(getResources().getColor(R.color.BOKI_TextPrimary, getTheme())); // Assuming a default color
            targetButton.setBackgroundTintList(getResources().getColorStateList(R.color.BOKI_TextPrimary_More_Transparent, getTheme()));
            targetButton.setStrokeColor(getResources().getColorStateList(R.color.BOKI_TextPrimary_Transparent, getTheme()));
            category_dialog.dismiss();
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
        // 1. Get an instance of the Calendar to retrieve the current date
        final java.util.Calendar c = java.util.Calendar.getInstance();
        int currentYear = c.get(java.util.Calendar.YEAR);
        int currentMonth = c.get(java.util.Calendar.MONTH);
        int currentDay = c.get(java.util.Calendar.DAY_OF_MONTH);

        // 2. Create the DatePickerDialog, passing the current date as the default
        DatePickerDialog openDatePickerDialog = new DatePickerDialog(MainActivity.this, R.style.DialogTheme, (view, year, month, dayOfMonth) -> {
            dialogBinding.datePickerBtn.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth));
        }, currentYear, currentMonth, currentDay); // <-- Use current date variables here

        openDatePickerDialog.show();
    }


    // Method to open TimePickerDialog
    private void openTimePickerDialog() {
        // Use Calendar to retrieve current time
        final java.util.Calendar c = java.util.Calendar.getInstance();
        int currentHour = c.get(java.util.Calendar.HOUR_OF_DAY);
        int currentMinute = c.get(java.util.Calendar.MINUTE);

        // 2. Create the TimePickerDialog, passing the current time as the default
        // 12-hour picker for user convenience (AM/PM)
        TimePickerDialog dialog = new TimePickerDialog(
                MainActivity.this,
                R.style.timepukerTheme,
                (view, hourOfDay, minute) -> {
                    // 1) DB time (24h + seconds) for correct sorting
                    String timeDb = String.format(Locale.US, "%02d:%02d:00", hourOfDay, minute);

                    // 2) UI time (AM/PM) for better UX
                    int displayHour;
                    if (hourOfDay == 0) {
                        displayHour = 12;
                    } else if (hourOfDay > 12) {
                        displayHour = hourOfDay - 12;
                    } else {
                        displayHour = hourOfDay;
                    }

                    String amPm = (hourOfDay < 12) ? "AM" : "PM";
                    String timeUi = String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm);

                    // Set UI + store DB value in tag
                    dialogBinding.timePickerBtn.setText(timeUi);
                    dialogBinding.timePickerBtn.setTag(timeDb);
                },
                currentHour,
                currentMinute,
                false
        );

        dialog.show();

    }







    //set all value in add operation dialog and close it
    private  void closeAddOperationDialog(){
        dialogBinding.operationName.setText("");
        dialogBinding.operationAmount.setText("");
        dialogBinding.timePickerBtn.setTag(null);
        addoperations_dialog.dismiss();

    }


}
