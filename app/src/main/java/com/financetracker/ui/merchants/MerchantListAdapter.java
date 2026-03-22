package com.financetracker.ui.merchants;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.financetracker.R;
import com.financetracker.data.db.entity.Merchant;

public class MerchantListAdapter extends ListAdapter<Merchant, MerchantListAdapter.ViewHolder> {

    public interface OnMerchantClickListener {
        void onMerchantClick(Merchant merchant);
    }

    private final OnMerchantClickListener listener;

    public MerchantListAdapter(OnMerchantClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Merchant> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Merchant>() {
                @Override
                public boolean areItemsTheSame(@NonNull Merchant a, @NonNull Merchant b) {
                    return a.uuid.equals(b.uuid);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Merchant a, @NonNull Merchant b) {
                    return (a.name != null ? a.name.equals(b.name) : b.name == null) &&
                           (a.nickName != null ? a.nickName.equals(b.nickName) : b.nickName == null) &&
                           (a.categoryId != null ? a.categoryId.equals(b.categoryId) : b.categoryId == null);
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_merchant_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Merchant merchant = getItem(position);
        holder.bind(merchant, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_merchant_name);
        }

        void bind(Merchant merchant, OnMerchantClickListener listener) {
            // Display nickName if available, otherwise display name
            String displayName = (merchant.nickName != null && !merchant.nickName.isEmpty())
                    ? merchant.nickName
                    : (merchant.name != null ? merchant.name : "Unknown");
            tvName.setText(displayName);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMerchantClick(merchant);
                }
            });
        }
    }
}

