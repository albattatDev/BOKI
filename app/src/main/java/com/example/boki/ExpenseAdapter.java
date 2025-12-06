package com.example.boki;import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.boki.models.Expense;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter to display a list of Expense objects in a RecyclerView.
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    // Note 1: The list is initialized here but kept private.
    // It's better to manage the data internally and provide a public method to update it.
    private List<Expense> expenses = new ArrayList<>();

    // Note 2: A public constructor with no arguments is cleaner.
    // The adapter doesn't need the list or context right away.
    public ExpenseAdapter() {
        // This constructor is now empty.
    }

    /**
     * Note 3: This is the standard and best way to create a ViewHolder.
     * We get the context from the 'parent' ViewGroup, which is always available here.
     * This avoids the need to pass Context into the adapter's constructor.
     */
    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_list_item, parent, false);
        return new ExpenseViewHolder(v);
    }

    /**
     * Note 4: This method connects your data to the ViewHolder.
     * It's more efficient to get the current Expense object once.
     */
    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        // Get the current expense item once to avoid multiple lookups.
        Expense currentExpense = expenses.get(position);
        // Call the 'bind' method in the ViewHolder to set the data.
        holder.bind(currentExpense);
    }

    @Override
    public int getItemCount() {
        // returns the number of items in the list.
        return expenses.size();
    }

    /**
     * Note 5: This is a new, essential helper method.
     * It allows your Fragment or Activity to update the list of expenses in the adapter.
     * 'notifyDataSetChanged()' tells the RecyclerView to refresh itself with the new data.
     */
    public void setExpenses(List<Expense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged(); // Refresh the list
    }


    /**
     * The ViewHolder class now includes a 'bind' method.
     * This keeps the logic for populating a view contained within the ViewHolder itself.
     */
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        // These are the views for a single list item.
        TextView operationtitle, amount, date, time;
        ImageView categoryicon;


        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            // Finding the views by their ID from the expense_list_item.xml file.
            operationtitle = itemView.findViewById(R.id.operation_title_id);
            amount = itemView.findViewById(R.id.amount_id);
            date = itemView.findViewById(R.id.date_id);
            time = itemView.findViewById(R.id.time_id);
            categoryicon = itemView.findViewById(R.id.category_icon);
        }

        /**
         * Note 6: This new 'bind' method takes an Expense object and sets the view's content.
         * This makes the onBindViewHolder method cleaner and organizes the code better.
         */
        public void bind(Expense expense) {
            operationtitle.setText(expense.getTitle());
            amount.setText(String.format(Locale.getDefault(), "%.2f", expense.getAmount()));
            date.setText(expense.getDate());
            time.setText(expense.getTime());

            // Get the context from the itemView, which is needed to access resources (colors).
            Context context = itemView.getContext();
            int colorResId;
            String category = expense.getCategory();

            // Use a switch statement to determine the color based on the category string.
            switch (category) {
                case "مطاعم":
                    colorResId = R.color.BOKI_Pink;
                    break;
                case "العائلة":
                    colorResId = R.color.BOKI_Blue;
                    break;
                case "صحة وعناية":
                    colorResId = R.color.BOKI_LightRead;
                    break;
                case "مواصلات":
                    colorResId = R.color.BOKI_lightPurple;
                    break;
                case "اتصالات":
                    colorResId = R.color.BOKI_lightBlue;
                    break;
                case "تعليم":
                    colorResId = R.color.BOKI_Green;
                    break;
                case "ترفية":
                    colorResId = R.color.BOKI_Orange;
                    break;
                default: // This will handle "Other" or any unrecognized category
                    colorResId = R.color.BOKI_TextPrimary; // A default neutral color
                    break;
            }

            // Set the background tint of the circle icon.
            // The background of the ImageView should be a white circle drawable.
            categoryicon.getBackground().setTint(context.getResources().getColor(colorResId, context.getTheme()));

            // You can also change the plus icon color if you want
            categoryicon.setColorFilter(context.getResources().getColor(R.color.BOKI_TextPrimary, context.getTheme())); // Keeps the '+' icon white
        }
        }
    }

