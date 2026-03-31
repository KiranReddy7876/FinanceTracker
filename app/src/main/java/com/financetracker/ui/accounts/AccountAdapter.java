package com.financetracker.ui.accounts;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.financetracker.R;
import com.financetracker.data.db.entity.Account;
import java.text.NumberFormat;
import java.util.Locale;

public class AccountAdapter extends ListAdapter<Account, AccountAdapter.ViewHolder> {

    public interface OnClickListener { void onClick(Account account); }
    private final OnClickListener listener;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public AccountAdapter(OnClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Account> DIFF = new DiffUtil.ItemCallback<Account>() {
        @Override public boolean areItemsTheSame(@NonNull Account a, @NonNull Account b) { 
            return a.uuid.equals(b.uuid); 
        }
        @Override public boolean areContentsTheSame(@NonNull Account a, @NonNull Account b) { 
            return a.updatedAt == b.updatedAt && a.currentBalance == b.currentBalance; 
        }
    };

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_account, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Account account = getItem(position);
        h.tvName.setText(account.name);
        String typeWithNumber = account.type;
        if (account.accountNumberLast4 != null && !account.accountNumberLast4.isEmpty()) {
            typeWithNumber += " •••" + account.accountNumberLast4;
        }
        h.tvType.setText(typeWithNumber);
        
        // Handle negative balance display properly
        double balance = account.currentBalance;
        String balanceText;
        if (balance < 0) {
            // For negative balances, format the absolute value and add negative sign
            balanceText = "- " + fmt.format(Math.abs(balance));
        } else {
            balanceText = fmt.format(balance);
        }
        h.tvBalance.setText(balanceText);
        h.itemView.setOnClickListener(v -> listener.onClick(account));
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


