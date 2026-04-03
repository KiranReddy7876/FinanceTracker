package com.financetracker.ui.accounts;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.financetracker.R;
import com.financetracker.data.db.entity.Account;
import java.text.NumberFormat;
import java.util.*;

public class AccountsFragment extends Fragment {

    private AccountsViewModel viewModel;
    private AccountAdapter adapter;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accounts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(
            this,
            new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
        ).get(AccountsViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rv_accounts);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AccountAdapter(account -> showEditDialog(account));
        rv.setAdapter(adapter);

        TextView tvNetBalance   = view.findViewById(R.id.tv_net_balance);
        TextView tvTotalSavings = view.findViewById(R.id.tv_total_savings);
        TextView tvTotalDebt    = view.findViewById(R.id.tv_total_debt);

        viewModel.accounts.observe(getViewLifecycleOwner(), accounts -> adapter.submitList(accounts));

        viewModel.netBalance.observe(getViewLifecycleOwner(), val ->
            tvNetBalance.setText(formatAmount(val)));

        viewModel.totalSavings.observe(getViewLifecycleOwner(), val ->
            tvTotalSavings.setText(formatAmount(val)));

        viewModel.totalDebt.observe(getViewLifecycleOwner(), val ->
            tvTotalDebt.setText(formatAmount(val)));

        FloatingActionButton fab = view.findViewById(R.id.fab_add_account);
        fab.setOnClickListener(v -> showAddDialog());
    }

    private String formatAmount(Double value) {
        if (value == null) return fmt.format(0);
        if (value < 0) return "- " + fmt.format(Math.abs(value));
        return fmt.format(value);
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_account, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_account_name);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_account_type);
        TextInputEditText etBalance = dialogView.findViewById(R.id.et_opening_balance);
        TextInputEditText etAccountNumber = dialogView.findViewById(R.id.et_account_number);

        String[] types = {"CASH", "BANK", "CREDIT_CARD", "WALLET"};
        spinnerType.setAdapter(new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, types));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle("Add Account")
            .setView(dialogView)
            .setPositiveButton("Save", (d, w) -> {
                String name = etName.getText() != null ? etName.getText().toString() : "";
                String type = types[spinnerType.getSelectedItemPosition()];
                String balStr = etBalance.getText() != null ? etBalance.getText().toString() : "0";
                String accountNumber = etAccountNumber.getText() != null ? etAccountNumber.getText().toString() : "";
                double balance = balStr.isEmpty() ? 0 : Double.parseDouble(balStr);
                if (!name.isEmpty()) viewModel.addAccount(name, type, balance, "INR", accountNumber);
            })
            .setNegativeButton("Cancel", null)
            .create();
        // Prevent black blink when keyboard appears inside this dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialog.show();
    }

    private void showEditDialog(Account account) {
        View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_account, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_account_name);
        Spinner spinnerType = dialogView.findViewById(R.id.spinner_account_type);
        TextInputEditText etBalance = dialogView.findViewById(R.id.et_opening_balance);
        TextInputEditText etAccountNumber = dialogView.findViewById(R.id.et_account_number);

        etName.setText(account.name);
        etBalance.setText(String.valueOf(account.currentBalance));
        if (account.accountNumberLast4 != null && !account.accountNumberLast4.isEmpty()) {
            etAccountNumber.setText(account.accountNumberLast4);
        }

        String[] types = {"CASH", "BANK", "CREDIT_CARD", "WALLET"};
        spinnerType.setAdapter(new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, types));
        int typeIndex = Arrays.asList(types).indexOf(account.type);
        if (typeIndex >= 0) spinnerType.setSelection(typeIndex);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle("Edit Account")
            .setView(dialogView)
            .setPositiveButton("Save", (d, w) -> {
                String name = etName.getText() != null ? etName.getText().toString() : account.name;
                String type = types[spinnerType.getSelectedItemPosition()];
                String balStr = etBalance.getText() != null ? etBalance.getText().toString() : "0";
                double balance = balStr.isEmpty() ? 0 : Double.parseDouble(balStr);
                String accountNumber = etAccountNumber.getText() != null ? etAccountNumber.getText().toString() : "";
                // Create a NEW copy - do NOT mutate original account object!
                // Mutating the original causes DiffUtil to see no change when Room returns fresh data
                Account updated = new Account(account.uuid, name, type, balance,
                    account.currency != null ? account.currency : "INR");
                updated.accountNumberLast4 = accountNumber;
                updated.createdAt = account.createdAt;
                viewModel.updateAccount(updated);
            })
            .setNeutralButton("Delete", (d, w) -> viewModel.deleteAccount(account.uuid))
            .setNegativeButton("Cancel", null)
            .create();
        // Prevent black blink when keyboard appears inside this dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialog.show();
    }
}
