package com.example.boki;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boki.models.ExpenseCategorySummary;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseCategoryAdapter
        extends RecyclerView.Adapter<ExpenseCategoryAdapter.ViewHolder> {

    private final List<ExpenseCategorySummary> items = new ArrayList<>();

    public void submitList(List<ExpenseCategorySummary> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName;
        TextView amount;
        TextView percentage;
        MaterialButton categoryIcon;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.operation_title_id);
            amount = itemView.findViewById(R.id.amount_id);
            percentage = itemView.findViewById(R.id.percentage_title_id);
            categoryIcon = itemView.findViewById(R.id.category_icon);
        }

        void bind(ExpenseCategorySummary item) {
            Context context = itemView.getContext();

            categoryName.setText(item.getCategory());
            amount.setText(String.format(
                    Locale.getDefault(),
                    "%.2f",
                    item.getTotalAmount()
            ));

            // RTL Arabic percentage → %33.92
            percentage.setText(
                    String.format(
                            Locale.getDefault(),
                            "%%%1$.2f",
                            item.getPercentage()
                    )
            );

            // Solid color for text
            int textColorRes = resolveCategoryColor(item.getCategory());
            int textColor = context.getColor(textColorRes);

            categoryName.setTextColor(textColor);
            percentage.setTextColor(textColor);
            if (categoryIcon != null) {
                // MaterialButton icon tint (tints the icon drawable set via app:icon)
                categoryIcon.setIconTint(context.getColorStateList(textColorRes));
            }

            // Transparent color for card/item background
            int bgColorRes = resolveCategoryBackgroundColor(item.getCategory());
            itemView.setBackgroundTintList(
                    context.getColorStateList(bgColorRes)
            );
        }

        private int resolveCategoryColor(String category) {
            switch (category) {
                case "مطاعم":
                    return R.color.BOKI_Pink;
                case "العائلة":
                    return R.color.BOKI_Blue;
                case "صحة وعناية":
                    return R.color.BOKI_LightRead;
                case "مواصلات":
                    return R.color.BOKI_lightPurple;
                case "اتصالات":
                    return R.color.BOKI_lightBlue;
                case "تعليم":
                    return R.color.BOKI_Green;
                case "ترفية":
                    return R.color.BOKI_Orange;
                default:
                    return R.color.BOKI_TextPrimary;
            }
        }

        private int resolveCategoryBackgroundColor(String category) {
            switch (category) {
                case "مطاعم":
                    return R.color.BOKI_Pink_Transparent;
                case "العائلة":
                    return R.color.BOKI_Blue_Transparent;
                case "صحة وعناية":
                    return R.color.BOKI_LightRead_Transparent;
                case "مواصلات":
                    return R.color.BOKI_lightPurple_Transparent;
                case "اتصالات":
                    return R.color.BOKI_lightBlue_Transparent;
                case "تعليم":
                    return R.color.BOKI_Green_transparent;
                case "ترفية":
                    return R.color.BOKI_Orange_Transparent;
                default:
                    // A subtle neutral background for unknown categories
                    return R.color.BOKI_TextPrimary_Transparent;
            }
        }
    }
}