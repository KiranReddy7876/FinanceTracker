package com.financetracker.ui.smsreview;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import com.financetracker.R;
import com.financetracker.data.db.entity.Account;
import com.financetracker.data.db.entity.Category;
import java.util.ArrayList;
import java.util.List;

public class SmsReviewFragment extends Fragment {

    private SmsReviewViewModel viewModel;
    private SmsReviewAdapter adapter;
    private List<Account> latestAccounts = new ArrayList<>();
    private List<Category> latestCategories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sms_review, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SmsReviewViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rv_sms_pending);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        TextView tvEmpty = view.findViewById(R.id.tv_empty);

        // Initialize adapter once
        adapter = new SmsReviewAdapter(
            latestAccounts,
            latestCategories,
            (smsImport, accountId, categoryId) ->
                viewModel.confirmAndCreate(smsImport, accountId, categoryId),
            uuid -> viewModel.ignore(uuid)
        );
        rv.setAdapter(adapter);

        // Observe accounts
        viewModel.accounts.observe(getViewLifecycleOwner(), accounts -> {
            latestAccounts.clear();
            if (accounts != null) latestAccounts.addAll(accounts);
            adapter.updateAccounts(latestAccounts);
        });

        // Observe categories
        viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            latestCategories.clear();
            if (categories != null) latestCategories.addAll(categories);
            adapter.updateCategories(latestCategories);
        });

        // Observe pending SMS imports
        viewModel.pendingItems.observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            tvEmpty.setVisibility((items == null || items.isEmpty()) ? View.VISIBLE : View.GONE);
        });
    }
}
