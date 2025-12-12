package com.example.boki;

import android.os.Bundle;
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
import java.util.List;

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
                    if (added) {
                        loadExpenses(); // refresh list
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
