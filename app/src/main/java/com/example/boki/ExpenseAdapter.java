package com.example.boki;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boki.data.local.ExpenseRepository;
import com.example.boki.models.Expense;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private final ExpenseRepository repository;
    private final Runnable refreshDataCallback;

    public ExpenseAdapter(ExpenseRepository repository, Runnable refreshDataCallback) {
        this.repository = repository;
        this.refreshDataCallback = refreshDataCallback;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_list_item, parent, false);
        return new ExpenseViewHolder(v, repository, refreshDataCallback);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.bind(expenses.get(position));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void setExpenses(List<Expense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView operationtitle, amount, date, time;
        ImageView categoryicon;
        private final ExpenseRepository repository;
        private final Runnable refreshDataCallback;
        private final Context context;
        private final String[] categories = {
                "مطاعم", "العائلة", "صحة وعناية", "مواصلات",
                "اتصالات", "تعليم", "ترفية", "أخرى"
        };

        public ExpenseViewHolder(@NonNull View itemView, ExpenseRepository repository, Runnable refreshDataCallback) {
            super(itemView);
            this.context = itemView.getContext();
            this.repository = repository;
            this.refreshDataCallback = refreshDataCallback;
            operationtitle = itemView.findViewById(R.id.operation_title_id);
            amount = itemView.findViewById(R.id.amount_id);
            date = itemView.findViewById(R.id.date_id);
            time = itemView.findViewById(R.id.time_id);
            categoryicon = itemView.findViewById(R.id.category_icon);
        }

        public void bind(final Expense expense) {
            operationtitle.setText(expense.getTitle());
            amount.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()));
            date.setText(expense.getDate());
            time.setText(expense.getTime());
            categoryicon.setOnClickListener(v -> showUpdateDialog(expense));

            int colorResId;
            switch (expense.getCategory() != null ? expense.getCategory() : "") {
                case "مطاعم": colorResId = R.color.BOKI_Pink; break;
                case "العائلة": colorResId = R.color.BOKI_Blue; break;
                case "صحة وعناية": colorResId = R.color.BOKI_LightRead; break;
                case "مواصلات": colorResId = R.color.BOKI_lightPurple; break;
                case "اتصالات": colorResId = R.color.BOKI_lightBlue; break;
                case "تعليم": colorResId = R.color.BOKI_Green; break;
                case "ترفية": colorResId = R.color.BOKI_Orange; break;
                default: colorResId = R.color.BOKI_TextPrimary; break;
            }
            categoryicon.getBackground().setTint(context.getResources().getColor(colorResId, context.getTheme()));
            categoryicon.setColorFilter(context.getResources().getColor(R.color.BOKI_TextPrimary, context.getTheme()));
        }

        private void showUpdateDialog(final Expense expenseToUpdate) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);

            View dialogView = inflater.inflate(R.layout.update_expense_dialog, null);
            builder.setView(dialogView);

            final EditText editTitle = dialogView.findViewById(R.id.operation_name);
            final EditText editAmount = dialogView.findViewById(R.id.operation_amount);
            final MaterialButton categoryBtn = dialogView.findViewById(R.id.category_btn);
            final Button datePickerBtn = dialogView.findViewById(R.id.date_picker_btn);
            final Button timePickerBtn = dialogView.findViewById(R.id.time_picker_btn);
            final Button saveButton = dialogView.findViewById(R.id.save_opration_btn);
            final Button cancelButton = dialogView.findViewById(R.id.cancel_dialog_btn);
            final ImageButton deleteButton = dialogView.findViewById(R.id.delete_expense_btn);

            editTitle.setText(expenseToUpdate.getTitle());
            editAmount.setText(String.format(Locale.US, "%.2f", expenseToUpdate.getAmount()));
            categoryBtn.setText(expenseToUpdate.getCategory());
            datePickerBtn.setText(expenseToUpdate.getDate());
            timePickerBtn.setText(expenseToUpdate.getTime());
            saveButton.setText("تحديث");

            final AlertDialog dialog = builder.create();

            // Apply timepukerTheme for the Date Picker (White numbers)
            datePickerBtn.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        R.style.timepukerTheme,
                        (view, year, month, dayOfMonth) -> {
                            String selectedDate = year + "-" + String.format(Locale.US, "%02d", month + 1) + "-" + String.format(Locale.US, "%02d", dayOfMonth);
                            datePickerBtn.setText(selectedDate);
                        },
                        c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            });

            // Apply TimePickerNumbersTheme for the Time Picker (Black clock numbers)
            timePickerBtn.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                        R.style.TimePickerNumbersTheme,
                        (view, hourOfDay, minute) -> {
                            String amPm = hourOfDay < 12 ? "AM" : "PM";
                            int hourIn12 = hourOfDay > 12 ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
                            String selectedTime = String.format(Locale.US, "%02d:%02d %s", hourIn12, minute, amPm);
                            timePickerBtn.setText(selectedTime);
                        },
                        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            });

            // Apply DialogTheme for the Category Picker
            categoryBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(context, R.style.DialogTheme)
                        .setTitle("Select a Category")
                        .setItems(categories, (d, which) -> {
                            String selectedCategory = categories[which];
                            categoryBtn.setText(selectedCategory);
                            d.dismiss();
                        })
                        .show();
            });

            // Apply DialogTheme for the Delete Confirmation Dialog
            deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context, R.style.DialogTheme)
                        .setTitle("مسح العملية")
                        .setMessage("هل انت متأكد انك بتمسح العملية؟ لانك ما تقدر ترجعه!")
                        .setPositiveButton("مسح", (d, which) -> {
                            boolean deleted = repository.deleteExpense(expenseToUpdate.getId());
                            if (deleted) {
                                Toast.makeText(context, "Expense Deleted", Toast.LENGTH_SHORT).show();
                                refreshDataCallback.run();
                            } else {
                                Toast.makeText(context, "Delete Failed", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });

            saveButton.setOnClickListener(v -> {
                String newTitle = editTitle.getText().toString().trim();
                String newAmountStr = editAmount.getText().toString().trim();
                String newCategory = categoryBtn.getText().toString();
                String newDate = datePickerBtn.getText().toString();
                String newTime = timePickerBtn.getText().toString();
                if (newTitle.isEmpty() || newAmountStr.isEmpty()) {
                    Toast.makeText(context, "Title and Amount cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    expenseToUpdate.setTitle(newTitle);
                    expenseToUpdate.setAmount(Double.parseDouble(newAmountStr));
                    expenseToUpdate.setCategory(newCategory);
                    expenseToUpdate.setDate(newDate);
                    expenseToUpdate.setTime(newTime);
                    expenseToUpdate.setNote(expenseToUpdate.getNote());
                    int rows = repository.updateExpense(expenseToUpdate);
                    if (rows > 0) {
                        Toast.makeText(context, "Expense Updated!", Toast.LENGTH_SHORT).show();
                        refreshDataCallback.run();
                    } else {
                        Toast.makeText(context, "Update Failed!", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(context, "Invalid Amount!", Toast.LENGTH_SHORT).show();
                }
            });

            cancelButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }
}

