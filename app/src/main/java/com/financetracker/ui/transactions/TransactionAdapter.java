package com.financetracker.ui.transactions;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.financetracker.R;
import com.financetracker.data.db.AppDatabase;
import com.financetracker.data.db.entity.Category;
import com.financetracker.data.db.entity.Merchant;
import com.financetracker.data.db.entity.Transaction;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.ViewHolder> {

    private static final String TAG = "TransactionAdapter";

    public interface OnItemClickListener {
        void onClick(Transaction transaction);
    }

    private final OnItemClickListener listener;
    private final NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private final AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // In-memory caches to avoid repeated DB hits for same IDs
    private final Map<String, String> merchantCache  = new HashMap<>();
    private final Map<String, String> categoryCache  = new HashMap<>();
    private final Map<String, String> accountCache   = new HashMap<>();

    public TransactionAdapter(OnItemClickListener listener, AppDatabase db) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.db = db;
    }

    // Overload for backward compatibility
    public TransactionAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.db = null;
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<Transaction>() {
            @Override public boolean areItemsTheSame(@NonNull Transaction a, @NonNull Transaction b) {
                return a.uuid != null && b.uuid != null && a.uuid.equals(b.uuid);
            }
            @Override public boolean areContentsTheSame(@NonNull Transaction a, @NonNull Transaction b) {
                return a.updatedAt == b.updatedAt;
            }
        };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = getItem(position);
        if (t == null) return;

        try {
            // ── Date ────────────────────────────────────────────────────────
            String dateStr = dateFmt.format(new Date(t.date));

            // ── Amount (signed + coloured) ───────────────────────────────────
            String amountStr;
            int amountColor;
            if ("INCOME".equals(t.type)) {
                amountStr  = "+ " + currencyFmt.format(t.amount);
                amountColor = Color.parseColor("#2E7D32");
            } else if ("EXPENSE".equals(t.type)) {
                amountStr  = "- " + currencyFmt.format(t.amount);
                amountColor = Color.parseColor("#C62828");
            } else {
                amountStr  = currencyFmt.format(t.amount);
                amountColor = Color.parseColor("#1565C0");
            }
            holder.tvAmount.setText(amountStr);
            holder.tvAmount.setTextColor(amountColor);

            // ── Type badge ───────────────────────────────────────────────────
            holder.tvType.setText(t.type != null && !t.type.isEmpty()
                    ? t.type.substring(0, 1) : "?");

            // ── Primary text: merchant nickName > merchant name > note > type ─
            String initialNote = (t.note != null && !t.note.isEmpty())
                    ? t.note : (t.type != null ? t.type : "Unknown");
            holder.tvNote.setText(initialNote);

            if (t.merchantId != null && !t.merchantId.isEmpty() && db != null) {
                String cached = merchantCache.get(t.merchantId);
                if (cached != null) {
                    holder.tvNote.setText(cached);
                } else {
                    executor.execute(() -> {
                        try {
                            Merchant merchant = db.merchantDao().getById(t.merchantId);
                            if (merchant != null) {
                                String display = (merchant.nickName != null && !merchant.nickName.isEmpty())
                                        ? merchant.nickName : merchant.name;
                                if (display != null && !display.isEmpty()) {
                                    merchantCache.put(t.merchantId, display);
                                    mainHandler.post(() -> holder.tvNote.setText(display));
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Merchant lookup error: " + e.getMessage());
                        }
                    });
                }
            }

            // ── Secondary text ────────────────────────────────────────────────
            // TRANSFER  →  "FromAccount → ToAccount • date"
            // INCOME / EXPENSE  →  "Category • date"
            holder.tvCategoryDate.setText(dateStr); // default until async resolves

            if ("TRANSFER".equals(t.type) && db != null) {
                // Resolve source account name, then target
                resolveAccountName(t.accountId, fromName -> {
                    String to = (t.transferToAccountId != null && !t.transferToAccountId.isEmpty())
                            ? null          // needs async resolve
                            : (t.recipientName != null && !t.recipientName.isEmpty()
                                ? t.recipientName : "?");

                    if (to != null) {
                        // recipient is a plain name — no DB lookup needed
                        holder.tvCategoryDate.setText(fromName + " → " + to + " • " + dateStr);
                    } else {
                        // resolve destination account
                        resolveAccountName(t.transferToAccountId, toName ->
                            holder.tvCategoryDate.setText(fromName + " → " + toName + " • " + dateStr));
                    }
                });

            } else if (t.categoryId != null && !t.categoryId.isEmpty() && db != null) {
                String cachedCat = categoryCache.get(t.categoryId);
                if (cachedCat != null) {
                    holder.tvCategoryDate.setText(cachedCat + " • " + dateStr);
                } else {
                    executor.execute(() -> {
                        try {
                            Category cat = db.categoryDao().getById(t.categoryId);
                            if (cat != null && cat.name != null && !cat.name.isEmpty()) {
                                categoryCache.put(t.categoryId, cat.name);
                                final String label = cat.name + " • " + dateStr;
                                mainHandler.post(() -> holder.tvCategoryDate.setText(label));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Category lookup error: " + e.getMessage());
                        }
                    });
                }
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(t);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Resolves an account UUID to its display name via cache + background DB lookup.
     *  Calls {@code callback} on the main thread once the name is available. */
    private void resolveAccountName(String accountId, java.util.function.Consumer<String> callback) {
        if (accountId == null || accountId.isEmpty()) {
            mainHandler.post(() -> callback.accept("?"));
            return;
        }
        String cached = accountCache.get(accountId);
        if (cached != null) {
            mainHandler.post(() -> callback.accept(cached));
            return;
        }
        executor.execute(() -> {
            try {
                com.financetracker.data.db.entity.Account acc = db.accountDao().getById(accountId);
                String name = (acc != null && acc.name != null && !acc.name.isEmpty())
                        ? acc.name : "?";
                accountCache.put(accountId, name);
                mainHandler.post(() -> callback.accept(name));
            } catch (Exception e) {
                Log.e(TAG, "Account lookup error: " + e.getMessage());
                mainHandler.post(() -> callback.accept("?"));
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNote, tvCategoryDate, tvAmount, tvType;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNote         = itemView.findViewById(R.id.tv_transaction_note);
            tvCategoryDate = itemView.findViewById(R.id.tv_transaction_category_date);
            tvAmount       = itemView.findViewById(R.id.tv_transaction_amount);
            tvType         = itemView.findViewById(R.id.tv_transaction_type_badge);
        }
    }
}
