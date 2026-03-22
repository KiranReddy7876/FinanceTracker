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
import java.util.*;

public class AccountsFragment extends Fragment {

    private AccountsViewModel viewModel;
    private AccountAdapter adapter;

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
        adapter = new AccountAdapter(accountWithBalance -> showEditDialog(accountWithBalance.account));
        rv.setAdapter(adapter);

        viewModel.accounts.observe(getViewLifecycleOwner(), accountsWithBalance -> adapter.submitList(accountsWithBalance));

        FloatingActionButton fab = view.findViewById(R.id.fab_add_account);
        fab.setOnClickListener(v -> showAddDialog());
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
        etBalance.setText(String.valueOf(account.openingBalance));
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
                account.name = etName.getText() != null ? etName.getText().toString() : account.name;
                account.type = types[spinnerType.getSelectedItemPosition()];
                String balStr = etBalance.getText() != null ? etBalance.getText().toString() : "0";
                account.openingBalance = balStr.isEmpty() ? 0 : Double.parseDouble(balStr);
                account.accountNumberLast4 = etAccountNumber.getText() != null ? etAccountNumber.getText().toString() : "";
                viewModel.updateAccount(account);
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
