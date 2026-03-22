package com.financetracker.ui.accounts;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.financetracker.R;
import com.financetracker.data.db.entity.AccountWithBalance;
import java.text.NumberFormat;
import java.util.Locale;

public class AccountAdapter extends ListAdapter<AccountWithBalance, AccountAdapter.ViewHolder> {

    public interface OnClickListener { void onClick(AccountWithBalance accountWithBalance); }
    private final OnClickListener listener;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public AccountAdapter(OnClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<AccountWithBalance> DIFF = new DiffUtil.ItemCallback<AccountWithBalance>() {
        @Override public boolean areItemsTheSame(@NonNull AccountWithBalance a, @NonNull AccountWithBalance b) { 
            return a.account.uuid.equals(b.account.uuid); 
        }
        @Override public boolean areContentsTheSame(@NonNull AccountWithBalance a, @NonNull AccountWithBalance b) { 
            return a.account.updatedAt == b.account.updatedAt && 
                   a.totalIncome == b.totalIncome && 
                   a.totalExpense == b.totalExpense; 
        }
    };

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_account, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AccountWithBalance awb = getItem(position);
        h.tvName.setText(awb.account.name);
        String typeWithNumber = awb.account.type;
        if (awb.account.accountNumberLast4 != null && !awb.account.accountNumberLast4.isEmpty()) {
            typeWithNumber += " •••" + awb.account.accountNumberLast4;
        }
        h.tvType.setText(typeWithNumber);
        h.tvBalance.setText(fmt.format(awb.getCurrentBalance()));
        h.itemView.setOnClickListener(v -> listener.onClick(awb));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvBalance;
        ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_account_name);
            tvType = v.findViewById(R.id.tv_account_type);
            tvBalance = v.findViewById(R.id.tv_account_balance);
        }
    }
}
