package com.example.boki;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boki.data.local.ExpenseRepository;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpensesFragment extends Fragment {

    //region Nested Classes: The full code for the Model and Adapter is here.

    /**
     * Data class to hold the result of the SQL summary query.
     * Defined as a static nested class to keep it encapsulated within ExpensesFragment.
     */
    public static class CategoryExpense {
        private final String category;
        private final double totalAmount;
        private double percentage;

        public CategoryExpense(String category, double totalAmount) {
            this.category = category;
            this.totalAmount = totalAmount;
            this.percentage = 0.0; // Will be calculated later
        }

        public String getCategory() { return category; }
        public double getTotalAmount() { return totalAmount; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    /**
     * RecyclerView.Adapter to display the category expense summary.
     * Also defined as a nested class for encapsulation.
     */
    public static class CategoryExpenseAdapter extends RecyclerView.Adapter<CategoryExpenseAdapter.CategoryViewHolder> {

        private List<CategoryExpense> categoryExpenses = new ArrayList<>();
        private final Context context;

        public CategoryExpenseAdapter(Context context) {
            this.context = context;
        }

        public void setCategoryExpenses(List<CategoryExpense> categoryExpenses) {
            this.categoryExpenses = categoryExpenses;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_expense_category, parent, false);
            return new CategoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            holder.bind(categoryExpenses.get(position));
        }

        @Override
        public int getItemCount() {
            return categoryExpenses.size();
        }

        /**
         * ViewHolder for the category expense item. This class finds the views in
         * item_expense_category.xml and binds the data to them.
         */
        public class CategoryViewHolder extends RecyclerView.ViewHolder {
            private final CardView cardView;
            private final MaterialButton categoryIcon;
            private final TextView categoryName, categoryPercentage, categoryAmount;

            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = (CardView) itemView;
                categoryIcon = itemView.findViewById(R.id.category_icon);
                categoryName = itemView.findViewById(R.id.operation_title_id);
                categoryPercentage = itemView.findViewById(R.id.percentage_title_id);
                categoryAmount = itemView.findViewById(R.id.amount_id);
            }

            public void bind(CategoryExpense item) {
                categoryName.setText(item.getCategory());
                categoryAmount.setText(String.format(Locale.US, "%.2f", item.getTotalAmount()));
                categoryPercentage.setText(String.format(Locale.US, "%%%.2f", item.getPercentage()));

                int colorResId = getColorForCategory(item.getCategory());
                int color = ContextCompat.getColor(context, colorResId);

                // Dynamically find the transparent color based on the original color's name
                int transparentColor;
                try {
                    String colorName = context.getResources().getResourceName(colorResId).split("/")[1];
                    // Assumes transparent colors are named like "BOKI_Pink_Transparent"
                    int transparentColorId = context.getResources().getIdentifier(colorName + "_Transparent", "color", context.getPackageName());
                    transparentColor = (transparentColorId != 0)
                            ? ContextCompat.getColor(context, transparentColorId)
                            : ContextCompat.getColor(context, R.color.BOKI_TextPrimary_More_Transparent); // Fallback color
                } catch (Exception e) {
                    // A safe default if the transparent color isn't found
                    transparentColor = ContextCompat.getColor(context, R.color.BOKI_TextPrimary_More_Transparent);
                }

                cardView.setCardBackgroundColor(transparentColor);
                categoryIcon.setIconTint(ColorStateList.valueOf(color));
                categoryName.setTextColor(color);
                categoryPercentage.setTextColor(color);
            }

            private int getColorForCategory(String category) {
                switch (category != null ? category : "") {
                    case "مطاعم": return R.color.BOKI_Pink;
                    case "العائلة": return R.color.BOKI_Blue;
                    case "صحة وعناية": return R.color.BOKI_LightRead;
                    case "مواصلات": return R.color.BOKI_lightPurple;
                    case "اتصالات": return R.color.BOKI_lightBlue;
                    case "تعليم": return R.color.BOKI_Green;
                    case "ترفية": return R.color.BOKI_Orange;
                    default: return R.color.BOKI_TextPrimary;
                }
            }
        }
    }
    //endregion

    //region Fragment Lifecycle and View Logic

    private enum TimeFrame { DAILY, WEEKLY, MONTHLY }

    private ExpenseRepository expenseRepository;
    private CategoryExpenseAdapter categoryAdapter;
    private TextView totalAmountText;
    private TextView dateRangeText;
    private Button dailyBtn, weeklyBtn, monthlyBtn;

    private TimeFrame currentTimeFrame = TimeFrame.WEEKLY; // Default to weekly
    private final Calendar currentCalendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Repository and Views
        expenseRepository = new ExpenseRepository(requireContext());
        totalAmountText = view.findViewById(R.id.amount_text);
        dateRangeText = view.findViewById(R.id.date_range_text);
        RecyclerView categoryRecyclerView = view.findViewById(R.id.expenses_categories_recycler);
        dailyBtn = view.findViewById(R.id.days_btn);
        weeklyBtn = view.findViewById(R.id.week_days_btn);
        monthlyBtn = view.findViewById(R.id.month_days_btn);

        view.findViewById(R.id.next_date_btn).setOnClickListener(v -> navigateDate(1));
        view.findViewById(R.id.prev_date_btn).setOnClickListener(v -> navigateDate(-1));

        // Setup RecyclerView
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryAdapter = new CategoryExpenseAdapter(getContext());
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Setup Button Click Listeners
        dailyBtn.setOnClickListener(v -> setTimeFrame(TimeFrame.DAILY));
        weeklyBtn.setOnClickListener(v -> setTimeFrame(TimeFrame.WEEKLY));
        monthlyBtn.setOnClickListener(v -> setTimeFrame(TimeFrame.MONTHLY));

        // Initial data load for the default time frame
        updateDataForCurrentTimeFrame();
    }

    private void navigateDate(int direction) {
        switch (currentTimeFrame) {
            case DAILY: currentCalendar.add(Calendar.DAY_OF_YEAR, direction); break;
            case WEEKLY: currentCalendar.add(Calendar.WEEK_OF_YEAR, direction); break;
            case MONTHLY: currentCalendar.add(Calendar.MONTH, direction); break;
        }
        updateDataForCurrentTimeFrame();
    }

    private void setTimeFrame(TimeFrame newTimeFrame) {
        this.currentTimeFrame = newTimeFrame;
        // Reset calendar to today when switching frame type for predictability
        this.currentCalendar.setTime(new java.util.Date());
        updateDataForCurrentTimeFrame();
    }

    private void updateDataForCurrentTimeFrame() {
        updateButtonStyles();

        // One formatter for the database (SQL) and one for the display (UI).
        SimpleDateFormat sqlFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat uiFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        Calendar calendar = (Calendar) currentCalendar.clone();

        String startDateForSql;
        String endDateForSql;
        String displayDate;

        switch (currentTimeFrame) {
            case DAILY:
                // Generate the date for the SQL query
                startDateForSql = sqlFormatter.format(calendar.getTime());
                endDateForSql = startDateForSql;

                // FLIP FOR DISPLAY: Generate the date for the UI using the correct formatter
                displayDate = uiFormatter.format(calendar.getTime());
                break;

            case WEEKLY:
                // Calculate start of the week
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                startDateForSql = sqlFormatter.format(calendar.getTime());
                // FLIP FOR DISPLAY: Use the UI formatter for the start of the display range
                String displayStartDate = uiFormatter.format(calendar.getTime());

                // Calculate end of the week
                calendar.add(Calendar.DAY_OF_WEEK, 6);
                endDateForSql = sqlFormatter.format(calendar.getTime());
                // FLIP FOR DISPLAY: Use the UI formatter for the end of the display range
                String displayEndDate = uiFormatter.format(calendar.getTime());

                displayDate = displayStartDate + " — " + displayEndDate;
                break;

            case MONTHLY:
            default:
                // For the monthly view, showing "Month Year" is more user-friendly.
                displayDate = new SimpleDateFormat("MMMM yyyy", new Locale("ar")).format(calendar.getTime());

                // Calculate start of the month for SQL
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDateForSql = sqlFormatter.format(calendar.getTime());

                // Calculate end of the month for SQL
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDateForSql = sqlFormatter.format(calendar.getTime());
                break;
        }

        // Set the correctly formatted text for the UI
        dateRangeText.setText(displayDate);

        // Crucially, pass the SQL-formatted dates to the repository
        loadSummaryData(startDateForSql, endDateForSql);
    }

    private void loadSummaryData(String startDate, String endDate) {
        // This will now receive data correctly.
        List<CategoryExpense> summaryList = expenseRepository.getCategoryExpenseSummary(startDate, endDate);
        double grandTotal = 0;
        for (CategoryExpense item : summaryList) {
            grandTotal += item.getTotalAmount();
        }
        if (grandTotal > 0) {
            for (CategoryExpense item : summaryList) {
                item.setPercentage((item.getTotalAmount() / grandTotal) * 100);
            }
        }
        totalAmountText.setText(String.format(Locale.US, "%.2f", grandTotal));
        categoryAdapter.setCategoryExpenses(summaryList);
    }

    private void updateButtonStyles() {
        // Use requireContext() to safely get colors
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.BOKI_MainPurple);
        int unselectedColor = ContextCompat.getColor(requireContext(), R.color.BOKI_MainPurple);
        int whiteColor = Color.WHITE;

        // Reset all buttons to default (unselected) state
        dailyBtn.setBackgroundColor(Color.TRANSPARENT);
        dailyBtn.setTextColor(unselectedColor);
        weeklyBtn.setBackgroundColor(Color.TRANSPARENT);
        weeklyBtn.setTextColor(unselectedColor);
        monthlyBtn.setBackgroundColor(Color.TRANSPARENT);
        monthlyBtn.setTextColor(unselectedColor);

        // Highlight the selected button
        Button selectedButton;
        switch (currentTimeFrame) {
            case DAILY: selectedButton = dailyBtn; break;
            case MONTHLY: selectedButton = monthlyBtn; break;
            case WEEKLY: default: selectedButton = weeklyBtn; break;
        }
        selectedButton.setBackgroundColor(selectedColor);
        selectedButton.setTextColor(whiteColor);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (expenseRepository != null) {
            expenseRepository.close();
        }
    }
    //endregion
}
