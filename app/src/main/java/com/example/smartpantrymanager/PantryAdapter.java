package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Binds pantry records to the RecyclerView row layout.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {
    public interface OnItemActionListener {
        void onEdit(PantryItem item);

        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final DecimalFormat quantityFormat;
    private final OnItemActionListener actionListener;

    public PantryAdapter(OnItemActionListener actionListener) {
        this.actionListener = actionListener;
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        quantityFormat = new DecimalFormat("0.####", symbols);
    }

    public void setItems(List<PantryItem> pantryItems) {
        items.clear();
        if (pantryItems != null) {
            items.addAll(pantryItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.nameTextView.setText(item.getName());
        holder.quantityTextView.setText(holder.itemView.getContext().getString(
                R.string.pantry_quantity_format,
                quantityFormat.format(item.getQuantity()),
                item.getUnit()));

        String expiryDate = item.getExpiryDate();
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            holder.expiryTextView.setText(R.string.expiry_date_not_set);
        } else {
            holder.expiryTextView.setText(holder.itemView.getContext().getString(
                    R.string.pantry_expiry_format, expiryDate));
        }

        holder.editButton.setOnClickListener(view -> actionListener.onEdit(item));
        holder.deleteButton.setOnClickListener(view -> actionListener.onDelete(item));
        holder.editButton.setContentDescription(holder.itemView.getContext().getString(
                R.string.edit_ingredient_action, item.getName()));
        holder.deleteButton.setContentDescription(holder.itemView.getContext().getString(
                R.string.delete_ingredient_action, item.getName()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView quantityTextView;
        private final TextView expiryTextView;
        private final View editButton;
        private final View deleteButton;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.pantryItemNameTextView);
            quantityTextView = itemView.findViewById(R.id.pantryItemQuantityTextView);
            expiryTextView = itemView.findViewById(R.id.pantryItemExpiryTextView);
            editButton = itemView.findViewById(R.id.editPantryItemButton);
            deleteButton = itemView.findViewById(R.id.deletePantryItemButton);
        }
    }
}
