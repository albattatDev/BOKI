package com.example.boki;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.boki.data.local.ExpenseRepository;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.DecimalFormat;
import androidx.recyclerview.widget.ConcatAdapter;

import com.example.boki.databinding.FragmentExpensesBinding;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class ExpensesFragment extends Fragment {

    // NOTE 1: Declare binding variable. It will be initialized in onCreateView.
    private FragmentExpensesBinding binding;

    private enum TimeView { DAILY, WEEKLY, MONTHLY }
    private TimeView currentTimeView = TimeView.WEEKLY;
    private final Calendar anchorCal = Calendar.getInstance();

    private ExpenseRepository expenseRepository;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private final DecimalFormat amountFormat = new DecimalFormat("0.##");



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
        resetAnchorToToday();
        expenseRepository = new ExpenseRepository(requireContext());
        setupButtonClickListeners();
        updateRangeAndTotalUI();


        // Listen for "expense_saved" events from Add Expense screen/dialog
        getParentFragmentManager().setFragmentResultListener(
                "expense_refresh",
                getViewLifecycleOwner(),
                (requestKey, result) -> refreshUiAfterExpenseChange()
        );
        getParentFragmentManager().setFragmentResultListener(
                "expense_saved",
                getViewLifecycleOwner(),
                (requestKey, result) -> refreshUiAfterExpenseChange()
        );

        setupButtonClickListeners();
        updateRangeAndTotalUI();

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
        TextView dateRangeText = binding.dateRangeText;
        View prevBtn = binding.prevDateBtn; // prev_date_btn
        View nextBtn = binding.nextDateBtn; // next_date_btn
        TextView amountText = binding.amountText; // amount_text


        monthButton.setOnClickListener(v -> {
            // Update UI for Monthly view
            updateButtonState(monthButton, selectedBgColor, selectedTextColor, weekButton, dayButton);

            currentTimeView = TimeView.MONTHLY;
            resetAnchorToToday();
            updateRangeAndTotalUI();        });

        weekButton.setOnClickListener(v -> {
            // Update UI for Weekly view
            updateButtonState(weekButton, selectedBgColor, selectedTextColor, monthButton, dayButton);

            currentTimeView = TimeView.WEEKLY;
            resetAnchorToToday();
            updateRangeAndTotalUI();        });

        dayButton.setOnClickListener(v -> {
            // Update UI for Daily view
            updateButtonState(dayButton, selectedBgColor, selectedTextColor, monthButton, weekButton);

            currentTimeView = TimeView.DAILY;
            resetAnchorToToday();
            updateRangeAndTotalUI();
        });

        prevBtn.setOnClickListener(v -> {
            moveToPreviousPeriod();
            updateRangeAndTotalUI();
        });

        nextBtn.setOnClickListener(v -> {
            moveToNextPeriod();
            updateRangeAndTotalUI();
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

    private void resetAnchorToToday() {
        Calendar now = Calendar.getInstance();
        anchorCal.setTimeInMillis(now.getTimeInMillis());
    }

    private void moveToPreviousPeriod() {
        switch (currentTimeView) {
            case DAILY:
                anchorCal.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case WEEKLY:
                anchorCal.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case MONTHLY:
                anchorCal.add(Calendar.MONTH, -1);
                break;
        }
    }

    private void moveToNextPeriod() {
        switch (currentTimeView) {
            case DAILY:
                anchorCal.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEKLY:
                anchorCal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case MONTHLY:
                anchorCal.add(Calendar.MONTH, 1);
                break;
        }
    }

    private String getRangeTextForCurrentState() {
        switch (currentTimeView) {
            case DAILY:
                return getTodayText();
            case WEEKLY:
                return getCurrentWeekRangeText();
            case MONTHLY:
                return getCurrentMonthRangeText();
            default:
                return getTodayText();
        }
    }

    // Helper methods for formatting dates

    private static class DateRange {
        final Date start;
        final Date end;
        DateRange(Date start, Date end) {
            this.start = start;
            this.end = end;
        }
    }

    private void updateRangeAndTotalUI() {
        if (binding == null) return;

        // 1) Update date range label (Arabic)
        binding.dateRangeText.setText(getRangeTextForCurrentState());

        // 2) Compute start/end in ISO for DB filtering
        DateRange range = getCurrentRangeDates();
        final String startIso = formatIso(range.start);
        final String endIso = formatIso(range.end);

        // 3) Query DB off the UI thread
        dbExecutor.execute(() -> {
            double total = 0.0;
            if (expenseRepository != null) {
                total = expenseRepository.getTotalAmountBetween(startIso, endIso);
            }
            final double finalTotal = total;
            requireActivity().runOnUiThread(() -> {
                if (binding != null) {
                    // رقم فقط (بدون عملة)
                    binding.amountText.setText(amountFormat.format(finalTotal));
                }
            });
        });
    }

    /**
     * Call this whenever an expense is added/edited/deleted to refresh totals (and list if you have one).
     */
    private void refreshUiAfterExpenseChange() {
        // Update totals/date range
        updateRangeAndTotalUI();

        // If you have a RecyclerView/Adapter for expenses, refresh it here.
        // Example (when you add your adapter):
        // if (expenseAdapter != null) expenseAdapter.notifyDataSetChanged();
        // Or re-fetch data from DB and submit to adapter.
    }


    private DateRange getCurrentRangeDates() {
        switch (currentTimeView) {
            case DAILY: {
                Date d = ((Calendar) anchorCal.clone()).getTime();
                return new DateRange(d, d);
            }
            case WEEKLY: {
                Calendar cal = (Calendar) anchorCal.clone();
                cal.setFirstDayOfWeek(Calendar.SUNDAY);

                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                int diffToSunday = dayOfWeek - Calendar.SUNDAY;
                cal.add(Calendar.DAY_OF_MONTH, -diffToSunday);
                Date start = cal.getTime();

                cal.add(Calendar.DAY_OF_MONTH, 6);
                Date end = cal.getTime();

                return new DateRange(start, end);
            }
            case MONTHLY: {
                Calendar cal = (Calendar) anchorCal.clone();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                Date start = cal.getTime();

                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                Date end = cal.getTime();

                return new DateRange(start, end);
            }
            default: {
                Date d = anchorCal.getTime();
                return new DateRange(d, d);
            }
        }
    }

    private String formatIso(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(date);
    }

    private String formatDateArabic(Date date) {
        // عرض عربي للمستخدم
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("ar"));
        return sdf.format(date);
    }

    private String getTodayText() {
        return formatDateArabic(anchorCal.getTime());
    }

    private String getCurrentWeekRangeText() {
        Calendar cal = (Calendar) anchorCal.clone();
        cal.setFirstDayOfWeek(Calendar.SUNDAY);

        // Move to Sunday
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diffToSunday = dayOfWeek - Calendar.SUNDAY; // Sunday=1
        cal.add(Calendar.DAY_OF_MONTH, -diffToSunday);
        Date start = cal.getTime();

        // Move to Saturday
        cal.add(Calendar.DAY_OF_MONTH, 6);
        Date end = cal.getTime();

        return formatRange(start, end);
    }

    private String getCurrentMonthRangeText() {
        Calendar cal = (Calendar) anchorCal.clone();

        // First day of month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date start = cal.getTime();

        // Last day of month
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date end = cal.getTime();

        return formatRange(start, end);
    }

    private String formatRange(Date start, Date end) {
        return formatDateArabic(start) + " - " + formatDateArabic(end);
    }

    private String formatDate(Date date) {
        // Example: 09 Dec 2025
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Safety refresh when coming back to this tab
        refreshUiAfterExpenseChange();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (expenseRepository != null) {
            expenseRepository.close();
            expenseRepository = null;
        }
        dbExecutor.shutdown();
        // NOTE 4: CRITICAL step to avoid memory leaks in Fragments.
        binding = null;
    }
}
