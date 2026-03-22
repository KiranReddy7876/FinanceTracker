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
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM", Locale.getDefault());
    private final AppDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
        if (t == null) {
            return;
        }

        try {
            // Priority: Merchant nickName > Merchant name > Note (SMS text) > Type > Unknown
            String displayText = null;
            
            // First: Try to get merchant nickName (run in background thread to avoid blocking main thread)
            if (t.merchantId != null && db != null) {
                // Set initial display text to note/type while we load merchant in background
                if (t.note != null && !t.note.isEmpty()) {
                    displayText = t.note;
                } else {
                    displayText = t.type != null ? t.type : "Unknown";
                }
                
                // Load merchant in background thread
                executor.execute(() -> {
                    try {
                        Merchant merchant = db.merchantDao().getById(t.merchantId);
                        if (merchant != null) {
                            Log.d(TAG, "Found merchant: " + merchant.name + ", nickName: " + merchant.nickName);
                            String finalDisplayText = null;
                            
                            // Display nickName if available, otherwise display name
                            if (merchant.nickName != null && !merchant.nickName.isEmpty()) {
                                finalDisplayText = merchant.nickName;
                                Log.d(TAG, "Displaying nickName: " + finalDisplayText);
                            } else if (merchant.name != null && !merchant.name.isEmpty()) {
                                finalDisplayText = merchant.name;
                                Log.d(TAG, "Displaying merchant name: " + finalDisplayText);
                            }
                            
                            // Update UI on main thread
                            if (finalDisplayText != null && !finalDisplayText.isEmpty()) {
                                final String textToDisplay = finalDisplayText;
                                mainHandler.post(() -> holder.tvNote.setText(textToDisplay));
                            }
                        } else {
                            Log.d(TAG, "Merchant not found for ID: " + t.merchantId);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error looking up merchant: " + e.getMessage());
                    }
                });
            } else {
                Log.d(TAG, "No merchantId or db for transaction");
                // No merchant, use note or type
                if (t.note != null && !t.note.isEmpty()) {
                    displayText = t.note;
                    Log.d(TAG, "Displaying note: " + displayText.substring(0, Math.min(50, displayText.length())));
                } else {
                    displayText = t.type != null ? t.type : "Unknown";
                    Log.d(TAG, "Displaying type/unknown: " + displayText);
                }
            }
            
            // Set initial display text (will be updated by background thread if merchant found)
            if (displayText != null && !displayText.isEmpty()) {
                holder.tvNote.setText(displayText);
            }
            
            holder.tvDate.setText(dateFmt.format(new Date(t.date)));
            holder.tvAmount.setText(currencyFmt.format(Math.max(0, t.amount)));

            if ("INCOME".equals(t.type)) {
                holder.tvAmount.setTextColor(Color.parseColor("#2E7D32"));
            } else if ("EXPENSE".equals(t.type)) {
                holder.tvAmount.setTextColor(Color.parseColor("#C62828"));
            } else {
                holder.tvAmount.setTextColor(Color.parseColor("#1565C0"));
            }

            holder.tvType.setText(t.type != null && t.type.length() > 0 ? t.type.substring(0, 1) : "?");
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(t);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNote, tvDate, tvAmount, tvType;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNote = itemView.findViewById(R.id.tv_transaction_note);
            tvDate = itemView.findViewById(R.id.tv_transaction_date);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
            tvType = itemView.findViewById(R.id.tv_transaction_type_badge);
        }
    }
}


