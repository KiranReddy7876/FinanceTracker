package com.financetracker.ui.smsimport;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.financetracker.R;
import com.financetracker.data.db.entity.SmsImport;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsImportAdapter extends ListAdapter<SmsImport, SmsImportAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(SmsImport smsImport);
    }

    private final OnClickListener listener;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault());

    public SmsImportAdapter(OnClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<SmsImport> DIFF = new DiffUtil.ItemCallback<SmsImport>() {
        @Override
        public boolean areItemsTheSame(@NonNull SmsImport a, @NonNull SmsImport b) {
            return a.uuid.equals(b.uuid);
        }

        @Override
        public boolean areContentsTheSame(@NonNull SmsImport a, @NonNull SmsImport b) {
            return a.updatedAt == b.updatedAt && 
                   a.accountId != null && a.accountId.equals(b.accountId) &&
                   a.categoryId != null && a.categoryId.equals(b.categoryId);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_sms_import, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        SmsImport sms = getItem(position);
        
        h.tvAmount.setText(fmt.format(sms.amount));
        h.tvType.setText(sms.detectedType);
        h.tvDate.setText(sdf.format(new Date(sms.date)));
        
        String preview = sms.smsText.length() > 50 ? 
            sms.smsText.substring(0, 50) + "..." : sms.smsText;
        h.tvPreview.setText(preview);
        
        String accountStatus = sms.accountId != null ? "✓ Matched" : "⚠ Not matched";
        h.tvAccountStatus.setText(accountStatus);
        
        h.itemView.setOnClickListener(v -> listener.onClick(sms));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvType, tvDate, tvPreview, tvAccountStatus;

        ViewHolder(@NonNull View v) {
            super(v);
            tvAmount = v.findViewById(R.id.tv_amount);
            tvType = v.findViewById(R.id.tv_type);
            tvDate = v.findViewById(R.id.tv_date);
            tvPreview = v.findViewById(R.id.tv_preview);
            tvAccountStatus = v.findViewById(R.id.tv_account_status);
        }
    }
}

