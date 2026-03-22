package com.financetracker.ui.smsreview;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.financetracker.R;
import com.financetracker.data.db.entity.Account;
import com.financetracker.data.db.entity.Category;
import com.financetracker.data.db.entity.SmsImport;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SmsReviewAdapter extends ListAdapter<SmsImport, SmsReviewAdapter.ViewHolder> {

    public interface OnConfirmListener {
        void onConfirm(SmsImport item, String accountId, String categoryId);
    }
    public interface OnIgnoreListener {
        void onIgnore(String uuid);
    }

    private List<Account> accounts;
    private List<Category> categories;
    private final OnConfirmListener confirmListener;
    private final OnIgnoreListener ignoreListener;
    private final NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public SmsReviewAdapter(List<Account> accounts,
                             List<Category> categories,
                             OnConfirmListener confirmListener,
                             OnIgnoreListener ignoreListener) {
        super(DIFF_CALLBACK);
        this.accounts = new ArrayList<>(accounts);
        this.categories = new ArrayList<>(categories);
        this.confirmListener = confirmListener;
        this.ignoreListener = ignoreListener;
    }

    public void updateAccounts(List<Account> newAccounts) {
        this.accounts = new ArrayList<>(newAccounts);
        notifyDataSetChanged();
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = new ArrayList<>(newCategories);
        notifyDataSetChanged();
    }

    private static final DiffUtil.ItemCallback<SmsImport> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<SmsImport>() {
            @Override public boolean areItemsTheSame(@NonNull SmsImport a, @NonNull SmsImport b) {
                return a.uuid.equals(b.uuid);
            }
            @Override public boolean areContentsTheSame(@NonNull SmsImport a, @NonNull SmsImport b) {
                return a.status.equals(b.status);
            }
        };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_sms_review, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SmsImport item = getItem(position);

        holder.tvSmsText.setText(item.smsText);
        holder.tvAmount.setText(currencyFmt.format(item.amount));
        holder.tvDate.setText(dateFmt.format(new Date(item.date)));
        holder.tvType.setText(item.detectedType);

        // Merchant name row
        if (item.merchantName != null && !item.merchantName.isEmpty()) {
            holder.tvMerchant.setVisibility(View.VISIBLE);
            holder.tvMerchant.setText("Merchant: " + item.merchantName);
        } else {
            holder.tvMerchant.setVisibility(View.GONE);
        }

        // Account spinner
        List<String> accountNames = new ArrayList<>();
        for (Account a : accounts) accountNames.add(a.name);
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(
            holder.itemView.getContext(), android.R.layout.simple_spinner_item, accountNames);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerAccount.setAdapter(accountAdapter);

        // Pre-select matched account
        if (item.accountId != null) {
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).uuid.equals(item.accountId)) {
                    holder.spinnerAccount.setSelection(i);
                    break;
                }
            }
        }

        // Category spinner – prepend "None" option
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("-- Select Category --");
        for (Category c : categories) categoryNames.add(c.name);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
            holder.itemView.getContext(), android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerCategory.setAdapter(categoryAdapter);

        // Pre-select known category (auto-filled from known merchant)
        if (item.categoryId != null) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).uuid.equals(item.categoryId)) {
                    holder.spinnerCategory.setSelection(i + 1); // +1 because of "None" at index 0
                    break;
                }
            }
        }

        holder.btnConfirm.setOnClickListener(v -> {
            int accPos = holder.spinnerAccount.getSelectedItemPosition();
            String accountId = (accPos >= 0 && accPos < accounts.size()) ? accounts.get(accPos).uuid : "";

            int catPos = holder.spinnerCategory.getSelectedItemPosition();
            // catPos 0 = "-- Select Category --" → no category
            String categoryId = (catPos > 0 && catPos - 1 < categories.size())
                ? categories.get(catPos - 1).uuid : "";

            confirmListener.onConfirm(item, accountId, categoryId);
        });

        holder.btnIgnore.setOnClickListener(v -> ignoreListener.onIgnore(item.uuid));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSmsText, tvAmount, tvDate, tvType, tvMerchant;
        Spinner spinnerAccount, spinnerCategory;
        Button btnConfirm, btnIgnore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSmsText = itemView.findViewById(R.id.tv_sms_text);
            tvAmount = itemView.findViewById(R.id.tv_sms_amount);
            tvDate = itemView.findViewById(R.id.tv_sms_date);
            tvType = itemView.findViewById(R.id.tv_sms_type);
            tvMerchant = itemView.findViewById(R.id.tv_sms_merchant);
            spinnerAccount = itemView.findViewById(R.id.spinner_sms_account);
            spinnerCategory = itemView.findViewById(R.id.spinner_sms_category);
            btnConfirm = itemView.findViewById(R.id.btn_sms_confirm);
            btnIgnore = itemView.findViewById(R.id.btn_sms_ignore);
        }
    }
}
