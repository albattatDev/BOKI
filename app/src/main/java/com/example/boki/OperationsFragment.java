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
import com.example.boki.data.local.ExpenseRepository;
import com.example.boki.models.Expense;
import java.util.List;

public class OperationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ExpenseAdapter expenseAdapter;
    private ExpenseRepository expenseRepository;

    public OperationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Repository and Adapter.
        expenseRepository = new ExpenseRepository(requireContext());
        // Pass the repository and a method reference for reloading data TO the adapter.
        expenseAdapter = new ExpenseAdapter(expenseRepository, this::loadAndDisplayData);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_operations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Standard RecyclerView setup.
        recyclerView = view.findViewById(R.id.expenses_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(expenseAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load data when the fragment becomes visible.
        loadAndDisplayData();
    }

    // Public method to be called by the adapter after an update.
    public void loadAndDisplayData() {
        List<Expense> allExpenses = expenseRepository.getAllExpenses();
        expenseAdapter.setExpenses(allExpenses);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (expenseRepository != null) {
            expenseRepository.close();
        }
    }
}
