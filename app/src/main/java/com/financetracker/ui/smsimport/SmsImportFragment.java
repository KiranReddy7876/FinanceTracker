package com.financetracker.ui.smsimport;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.financetracker.R;
import com.financetracker.data.db.entity.*;
import java.text.NumberFormat;
import java.util.*;

public class SmsImportFragment extends Fragment {

    private SmsImportViewModel viewModel;
    private SmsImportAdapter adapter;
    private TextView tvNoPending;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sms_import, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(
            this,
            new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
        ).get(SmsImportViewModel.class);

        tvNoPending = view.findViewById(R.id.tv_no_pending);
        RecyclerView rv = view.findViewById(R.id.rv_sms_imports);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new SmsImportAdapter(this::showEditDialog);
        rv.setAdapter(adapter);

        // Observe pending SMS imports
        viewModel.pendingSmsImports.observe(getViewLifecycleOwner(), smsImports -> {
            if (smsImports == null || smsImports.isEmpty()) {
                tvNoPending.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                tvNoPending.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                adapter.submitList(smsImports);
            }
        });
    }

    private void showEditDialog(SmsImport smsImport) {
        View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_sms_import, null);

        TextView tvAmount = dialogView.findViewById(R.id.tv_amount);
        TextView tvType = dialogView.findViewById(R.id.tv_type);
        TextView tvSmsText = dialogView.findViewById(R.id.tv_sms_text);
        EditText etMerchant = dialogView.findViewById(R.id.et_merchant);
        Spinner spinnerAccount = dialogView.findViewById(R.id.spinner_account);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_category);

        // Display SMS details
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        tvAmount.setText(fmt.format(smsImport.amount));
        tvType.setText(smsImport.detectedType);
        tvSmsText.setText("SMS: " + smsImport.smsText);

        // Pre-fill merchant if extracted, otherwise leave empty for user to paste
        if (smsImport.merchantName != null && !smsImport.merchantName.isEmpty()) {
            etMerchant.setText(smsImport.merchantName);
        }

        // Load accounts
        viewModel.accounts.observe(getViewLifecycleOwner(), accounts -> {
            if (accounts != null && !accounts.isEmpty()) {
                List<String> names = new ArrayList<>();
                for (Account a : accounts) {
                    names.add(a.name + " (" + a.type + ")");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAccount.setAdapter(adapter);

                // Select account if already matched
                if (smsImport.accountId != null) {
                    for (int i = 0; i < accounts.size(); i++) {
                        if (accounts.get(i).uuid.equals(smsImport.accountId)) {
                            spinnerAccount.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });

        // Store loaded categories in a holder so the Confirm button can access them
        // getCategoriesByType().getValue() on a NEW LiveData always returns null!
        final List<Category>[] loadedCategories = new List[]{new ArrayList<>()};

        // Load categories that match the detected transaction type
        viewModel.getCategoriesByType(smsImport.detectedType).observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                loadedCategories[0] = categories;  // Store reference for Confirm button
                List<String> names = new ArrayList<>();
                names.add("— No Category —");
                for (Category c : categories) {
                    names.add(c.name);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);

                // Select category if already assigned
                if (smsImport.categoryId != null) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).uuid.equals(smsImport.categoryId)) {
                            spinnerCategory.setSelection(i + 1);
                            break;
                        }
                    }
                }
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle("Review SMS Import")
            .setView(dialogView)
            .setPositiveButton("Confirm", (d, w) -> {
                List<Account> accounts = viewModel.accounts.getValue();

                if (accounts == null || accounts.isEmpty()) {
                    Toast.makeText(requireContext(), "No accounts available", Toast.LENGTH_SHORT).show();
                    return;
                }

                int accountPos = spinnerAccount.getSelectedItemPosition();
                String selectedAccountId = accounts.get(accountPos).uuid;

                // Get category from the stored list (NOT from a new LiveData call!)
                String selectedCategoryId = null;
                List<Category> typeCategories = loadedCategories[0];
                int catPos = spinnerCategory.getSelectedItemPosition();
                if (catPos > 0 && catPos - 1 < typeCategories.size()) {
                    selectedCategoryId = typeCategories.get(catPos - 1).uuid;
                }

                // Update merchant if user edited it
                String editedMerchant = etMerchant.getText().toString().trim();
                if (!editedMerchant.isEmpty() && !editedMerchant.equals(smsImport.merchantName)) {
                    viewModel.updateMerchant(smsImport.uuid, editedMerchant);
                }

                // Update SMS import with selected account and category, THEN confirm atomically
                viewModel.updateAndConfirmImport(smsImport.uuid, selectedAccountId, selectedCategoryId);
                
                Toast.makeText(requireContext(), "Transaction recorded", Toast.LENGTH_SHORT).show();
            })
            .setNeutralButton("Ignore", (d, w) -> {
                viewModel.ignoreImport(smsImport.uuid);
                Toast.makeText(requireContext(), "SMS ignored", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Delete", (d, w) -> {
                viewModel.deleteSmsImport(smsImport.uuid);
                Toast.makeText(requireContext(), "SMS deleted", Toast.LENGTH_SHORT).show();
            })
            .setOnDismissListener(d -> {
                // Data will update automatically via LiveData
            })
            .create();
        // Prevent black blink when keyboard appears inside this dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        dialog.show();
    }
}

