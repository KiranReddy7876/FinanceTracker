package com.financetracker.ui.dashboard;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.financetracker.R;
import com.financetracker.ui.transactions.TransactionAdapter;
import java.text.NumberFormat;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private TransactionAdapter transactionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh monthly data when fragment resumes to ensure current date range
        if (viewModel != null) {
            viewModel.refreshMonthlyData();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

            RecyclerView recyclerView = view.findViewById(R.id.rv_recent_transactions);
            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                // Pass AppDatabase instance to enable merchant nickName lookup
                transactionAdapter = new TransactionAdapter(transaction -> {
                    if (transaction != null && transaction.uuid != null) {
                        Bundle args = new Bundle();
                        args.putString("transactionId", transaction.uuid);
                        Navigation.findNavController(view).navigate(R.id.action_dashboard_to_addTransaction, args);
                    }
                }, com.financetracker.data.db.AppDatabase.getInstance(requireContext()));
                recyclerView.setAdapter(transactionAdapter);
            }

            TextView tvIncome = view.findViewById(R.id.tv_monthly_income);
            TextView tvExpense = view.findViewById(R.id.tv_monthly_expense);
            TextView tvSmsBadge = view.findViewById(R.id.tv_sms_badge);

            viewModel.recentTransactions.observe(getViewLifecycleOwner(), transactions -> {
                if (transactionAdapter != null && transactions != null) {
                    transactionAdapter.submitList(transactions);
                }
            });

            NumberFormat currencyFmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

            viewModel.getMonthlyIncome().observe(getViewLifecycleOwner(), income -> {
                if (tvIncome != null && income != null) {
                    tvIncome.setText(currencyFmt.format(income));
                }
            });

            viewModel.getMonthlyExpense().observe(getViewLifecycleOwner(), expense -> {
                if (tvExpense != null && expense != null) {
                    tvExpense.setText(currencyFmt.format(expense));
                }
            });

            viewModel.pendingSmsCount.observe(getViewLifecycleOwner(), count -> {
                if (tvSmsBadge != null) {
                    if (count != null && count > 0) {
                        tvSmsBadge.setVisibility(View.VISIBLE);
                        tvSmsBadge.setText(String.valueOf(count));
                    } else {
                        tvSmsBadge.setVisibility(View.GONE);
                    }
                }
            });

            FloatingActionButton fab = view.findViewById(R.id.fab_add_transaction);
            if (fab != null) {
                fab.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_dashboard_to_addTransaction));
            }

            MaterialButton btnMore = view.findViewById(R.id.btn_more_transactions);
            if (btnMore != null) {
                btnMore.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_dashboard_to_transactions));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
