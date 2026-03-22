package com.financetracker.ui.categories;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.financetracker.R;
import com.financetracker.data.db.entity.Category;

public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.ViewHolder> {

    public interface OnClickListener { void onClick(Category category); }
    private final OnClickListener listener;

    public CategoryAdapter(OnClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Category> DIFF = new DiffUtil.ItemCallback<Category>() {
        @Override public boolean areItemsTheSame(@NonNull Category a, @NonNull Category b) { return a.uuid.equals(b.uuid); }
        @Override public boolean areContentsTheSame(@NonNull Category a, @NonNull Category b) { return a.updatedAt == b.updatedAt; }
    };

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Category c = getItem(position);
        h.tvName.setText(c.name);
        h.tvType.setText(c.type);
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType;
        ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_category_name);
            tvType = v.findViewById(R.id.tv_category_type);
        }
    }
}
