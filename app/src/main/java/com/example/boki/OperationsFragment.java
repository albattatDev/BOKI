package com.example.boki;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.boki.data.local.ExpenseRepository;
import com.example.boki.models.Expense;
import com.example.boki.databinding.CategorySelectionDialogBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.android.material.button.MaterialButton;


/**
 * A fragment to display a list of all operations (expenses).
 */
public class OperationsFragment extends Fragment {

    // Note 1: Declare your views and adapter here.
    // They will be initialized later in the correct lifecycle methods.
    private RecyclerView recyclerView;
    private ExpenseAdapter expenseAdapter;
    private ExpenseRepository expenseRepository;

    public OperationsFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Note 2: onCreate is for non-view setup.
        // Initialize your repository and adapter here.
        // The adapter now uses an empty constructor.
        expenseRepository = new ExpenseRepository(requireContext());
        expenseAdapter = new ExpenseAdapter();
        // Item click (and/or action) listener from adapter
        expenseAdapter.setOnExpenseActionListener(new ExpenseAdapter.OnExpenseActionListener() {
            @Override
            public void onExpenseClick(Expense expense) {
                openDeleteOrEditDialog(expense);
            }

            @Override
            public void onExpenseLongClick(Expense expense) {
                openDeleteOrEditDialog(expense);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Note 3: This is the correct place to inflate your layout file.
        // It returns the view that the fragment will manage.
        return inflater.inflate(R.layout.fragment_operations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Note 4: onViewCreated is the perfect place for all view-related setup.
        // 'view' is the non-null root view returned by onCreateView.
        recyclerView = view.findViewById(R.id.expenses_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(expenseAdapter); // Set the adapter on the RecyclerView.

        //refresh the data
        getParentFragmentManager().setFragmentResultListener(
                "expense_refresh",
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    boolean added = bundle.getBoolean("expense_added", false);
                    boolean deleted = bundle.getBoolean("expense_deleted", false);
                    boolean updated = bundle.getBoolean("expense_updated", false);
                    if (added || deleted || updated) {
                        loadExpenses();
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        // Note 5: onResume is a good place to load data.
        // It's called every time the fragment becomes visible to the user,
        // so the list will refresh if new data was added.
        loadAndDisplayData();
    }

    private void loadAndDisplayData() {
        // Note 6: This is the correct way to load and display data.
        // Fetch the list from the repository.
        List<Expense> allExpenses = expenseRepository.getAllExpenses();

        if (allExpenses.isEmpty()) {
            Toast.makeText(getContext(), "No Expenses Found", Toast.LENGTH_SHORT).show();
        } else {
            // Use the adapter's 'setExpenses' method to update the RecyclerView.
            expenseAdapter.setExpenses(allExpenses);
        }
    }

    // Convert DB time (HH:mm:ss) -> UI time (h:mm a)
    private String formatTimeForUi(String dbTime) {
        if (dbTime == null) return "";
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
            SimpleDateFormat uiFormat = new SimpleDateFormat("h:mm a", Locale.US);
            Date parsed = dbFormat.parse(dbTime);
            return (parsed != null) ? uiFormat.format(parsed) : dbTime;
        } catch (ParseException e) {
            // Fallback: show raw string if parsing fails
            return dbTime;
        }
    }

    // Apply category button text + color + background tint + stroke (same as MainActivity)
    private void applyCategoryStyle(MaterialButton targetButton, String categoryText) {
        if (targetButton == null) return;

        int textColor;
        int bgTint;
        int strokeColor;

        if (categoryText == null) categoryText = "";

        switch (categoryText) {
            case "مطاعم":
                textColor = R.color.BOKI_Pink;
                bgTint = R.color.BOKI_Pink_Transparent;
                strokeColor = R.color.BOKI_Pink;
                break;
            case "العائلة":
                textColor = R.color.BOKI_Blue;
                bgTint = R.color.BOKI_Blue_Transparent;
                strokeColor = R.color.BOKI_Blue;
                break;
            case "صحة وعناية":
                textColor = R.color.BOKI_LightRead;
                bgTint = R.color.BOKI_LightRead_Transparent;
                strokeColor = R.color.BOKI_LightRead;
                break;
            case "مواصلات":
                textColor = R.color.BOKI_lightPurple;
                bgTint = R.color.BOKI_lightPurple_Transparent;
                strokeColor = R.color.BOKI_lightPurple;
                break;
            case "اتصالات":
                textColor = R.color.BOKI_lightBlue;
                bgTint = R.color.BOKI_lightBlue_Transparent;
                strokeColor = R.color.BOKI_lightBlue;
                break;
            case "تعليم":
                textColor = R.color.BOKI_Green;
                bgTint = R.color.BOKI_Green_transparent;
                strokeColor = R.color.BOKI_Green;
                break;
            case "ترفية":
                textColor = R.color.BOKI_Orange;
                bgTint = R.color.BOKI_Orange_Transparent;
                strokeColor = R.color.BOKI_Orange;
                break;
            default:
                textColor = R.color.BOKI_TextPrimary;
                bgTint = R.color.BOKI_TextPrimary_More_Transparent;
                strokeColor = R.color.BOKI_TextPrimary_Transparent;
                break;
        }

        targetButton.setText(categoryText);
        targetButton.setTextColor(getResources().getColor(textColor, requireContext().getTheme()));
        targetButton.setBackgroundTintList(getResources().getColorStateList(bgTint, requireContext().getTheme()));
        targetButton.setStrokeColor(getResources().getColorStateList(strokeColor, requireContext().getTheme()));
    }


    private void openDeleteOrEditDialog(Expense expense) {
        // Dialog shows expense info; trash icon deletes; save just closes (edit wiring later).
        Dialog dialog = new Dialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.delete_operations_dialog, null, false);
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.setCancelable(true);

        Button cancelBtn = dialogView.findViewById(R.id.cancel_dialog_btn);
        MaterialButton deleteBtn = dialogView.findViewById(R.id.delete_btn);
        Button saveBtn = dialogView.findViewById(R.id.save_opration_btn);

        EditText nameEt = dialogView.findViewById(R.id.operation_name);
        EditText amountEt = dialogView.findViewById(R.id.operation_amount);
        Button dateBtn = dialogView.findViewById(R.id.date_picker_btn);
        Button timeBtn = dialogView.findViewById(R.id.time_picker_btn);
        MaterialButton categoryBtn = dialogView.findViewById(R.id.category_btn);

        // Prefill
        if (nameEt != null) nameEt.setText(expense.getTitle());
        if (amountEt != null) amountEt.setText(String.valueOf(expense.getAmount()));
        if (dateBtn != null) dateBtn.setText(expense.getDate());
        // Time: show UI (AM/PM) but keep DB time in tag (HH:mm:ss)
        if (timeBtn != null) {
            String dbTime = expense.getTime();
            timeBtn.setTag(dbTime);
            timeBtn.setText(formatTimeForUi(dbTime));
        }

        // Category: set text + style
        if (categoryBtn != null) {
            applyCategoryStyle(categoryBtn, expense.getCategory());
        }


        if (cancelBtn != null) {
            cancelBtn.setOnClickListener(v -> dialog.dismiss());
        }


        // -------- Category picker dialog (same UI as MainActivity) --------
        Dialog categoryDialog = new Dialog(requireContext());
        CategorySelectionDialogBinding categoryBinding = CategorySelectionDialogBinding.inflate(getLayoutInflater());
        categoryDialog.setContentView(categoryBinding.getRoot());

        if (categoryDialog.getWindow() != null) {
            categoryDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            categoryDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        categoryDialog.setCancelable(true);

        if (categoryBtn != null) {
            categoryBtn.setOnClickListener(v -> categoryDialog.show());
        }

        categoryBinding.cancelDialogBtn.setOnClickListener(v -> categoryDialog.dismiss());

        categoryBinding.foodBtn.setOnClickListener(v -> { applyCategoryStyle(categoryBtn, categoryBinding.foodBtn.getText().toString()); categoryDialog.dismiss(); });
        categoryBinding.familyBtn.setOnClickListener(v -> { applyCategoryStyle(categoryBtn, categoryBinding.familyBtn.getText().toString()); categoryDialog.dismiss(); });
        categoryBinding.HelthBtn.setOnClickListener(v -> { applyCategoryStyle(categoryBtn, categoryBinding.HelthBtn.getText().toString()); categoryDialog.dismiss(); });
        categoryBinding.transportationBtn.setOnClickListener(v -> { applyCategoryStyle(categoryBtn, categoryBinding.transportationBtn.getText().toString()); categoryDialog.dismiss(); });
        categoryBinding.communicationBtn.setOnClickListener(v -> { applyCategoryStyle(categoryBtn, categoryBinding.communicationBtn.getText().toString()); categoryDialog.dismiss(); });
        categoryBinding.educationBtn.setOnClickListener(v -> { applyCategoryStyle(categoryBtn, categoryBinding.educationBtn.getText().toString()); categoryDialog.dismiss(); });
        categoryBinding.entertainmentBtn.setOnClickListener(v -> { applyCategoryStyle(categoryBtn, categoryBinding.entertainmentBtn.getText().toString()); categoryDialog.dismiss(); });
        categoryBinding.otherBtn.setOnClickListener(v -> { applyCategoryStyle(categoryBtn, categoryBinding.otherBtn.getText().toString()); categoryDialog.dismiss(); });

        // -------- Date picker --------
        if (dateBtn != null) {
            dateBtn.setOnClickListener(v -> {
                java.util.Calendar c = java.util.Calendar.getInstance();
                int year = c.get(java.util.Calendar.YEAR);
                int month = c.get(java.util.Calendar.MONTH);
                int day = c.get(java.util.Calendar.DAY_OF_MONTH);

                DatePickerDialog dp = new DatePickerDialog(
                        requireContext(),
                        R.style.DialogTheme,
                        (view1, y, m, d) -> {
                            String iso = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                            dateBtn.setText(iso);
                        },
                        year, month, day
                );
                dp.show();
            });
        }

        // -------- Time picker (UI AM/PM, DB HH:mm:ss in tag) --------
        if (timeBtn != null) {
            timeBtn.setOnClickListener(v -> {
                java.util.Calendar c = java.util.Calendar.getInstance();
                int currentHour = c.get(java.util.Calendar.HOUR_OF_DAY);
                int currentMinute = c.get(java.util.Calendar.MINUTE);

                TimePickerDialog tp = new TimePickerDialog(
                        requireContext(),
                        R.style.timepukerTheme,
                        (view12, hourOfDay, minute) -> {
                            String timeDb = String.format(Locale.US, "%02d:%02d:00", hourOfDay, minute);

                            int displayHour;
                            if (hourOfDay == 0) displayHour = 12;
                            else if (hourOfDay > 12) displayHour = hourOfDay - 12;
                            else displayHour = hourOfDay;

                            String amPm = (hourOfDay < 12) ? "AM" : "PM";
                            String timeUi = String.format(Locale.US, "%d:%02d %s", displayHour, minute, amPm);

                            timeBtn.setText(timeUi);
                            timeBtn.setTag(timeDb);
                        },
                        currentHour,
                        currentMinute,
                        false
                );
                tp.show();
            });
        }


        if (saveBtn != null) {
            if (saveBtn != null) {
                saveBtn.setOnClickListener(v -> {

                    String newTitle = nameEt.getText().toString().trim();
                    String amountStr = amountEt.getText().toString().trim();
                    String newDate = dateBtn.getText().toString().trim();
                    String newTime = (String) timeBtn.getTag();
                    if (newTime == null || newTime.trim().isEmpty()) {
                        newTime = expense.getTime();
                    }
                    String newCategory = categoryBtn.getText().toString().trim();

                    if (newTitle.isEmpty()) {
                        Toast.makeText(getContext(), "Please Enter Operation Name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double newAmount;
                    try {
                        newAmount = Double.parseDouble(amountStr);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Please enter a valid number for the amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // نفس الـ ID مهم عشان update يشتغل على نفس السجل
                    Expense updatedExpense = new Expense(
                            expense.getId(),
                            newTitle,
                            newAmount,
                            newCategory,
                            expense.getNote(),   // ما عندنا تعديل note حالياً
                            newDate,
                            newTime
                    );

                    int rows = expenseRepository.updateExpense(updatedExpense);

                    if (rows > 0) {
                        loadExpenses();

                        Bundle result = new Bundle();
                        result.putBoolean("expense_updated", true);
                        result.putLong("expense_updated_id", expense.getId());
                        getParentFragmentManager().setFragmentResult("expense_refresh", result);

                        Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        if (deleteBtn != null) {
            deleteBtn.setOnClickListener(v -> {
                View confirmView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.delete_confirmation_dialog, null, false);

                AlertDialog confirmDialog = new AlertDialog.Builder(requireContext())
                        .setView(confirmView)
                        .create();

                if (confirmDialog.getWindow() != null) {
                    confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }

                Button confirmDeleteBtn = confirmView.findViewById(R.id.Delete_btn);
                Button confirmCancelBtn = confirmView.findViewById(R.id.cancel_dialog_btn);

                confirmCancelBtn.setOnClickListener(x -> confirmDialog.dismiss());
                confirmDeleteBtn.setOnClickListener(x -> {
                    boolean ok = expenseRepository.deleteExpense(expense.getId());
                    confirmDialog.dismiss();
                    dialog.dismiss();

                    if (ok) {
                        // Refresh this list
                        loadExpenses();

                        // Notify other screens (remaining balance, etc.)
                        Bundle result = new Bundle();
                        result.putBoolean("expense_deleted", true);
                        result.putLong("expense_deleted_id", expense.getId());
                        getParentFragmentManager().setFragmentResult("expense_refresh", result);

                        Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                });

                confirmDialog.show();
            });
        }
        dialog.show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Note 7: It's good practice to close the database connection
        // when the fragment is destroyed to prevent memory leaks.
        if (expenseRepository != null) {
            expenseRepository.close();
        }
    }

    private void loadExpenses() {
        List<Expense> list = expenseRepository.getAllExpenses();
        expenseAdapter.setExpenses(list);
    }
}
