package com.financetracker.ui.addtransaction;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.financetracker.R;
import com.financetracker.data.db.entity.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddTransactionFragment extends Fragment {

    private AddTransactionViewModel viewModel;
    private long selectedDate = System.currentTimeMillis();

    private List<Account>   accountList  = new ArrayList<>();
    private List<Category>  categoryList = new ArrayList<>();
    private List<Merchant>  merchantList = new ArrayList<>();

    // Selection indices (0-based; -1 = nothing chosen)
    private int selectedAccountPos  = 0;
    private int selectedCategoryPos = 0;   // 0 = "No Category"
    private int selectedMerchantPos = 0;   // 0 = "No Merchant"

    private String editingTransactionId;
    private Handler mainHandler  = new Handler(Looper.getMainLooper());
    private Runnable populateRunnable;

    private TextInputEditText etAmount, etNote, etDate;
    private AutoCompleteTextView acAccount, acCategory, acMerchant;
    private MaterialButtonToggleGroup toggleType;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddTransactionViewModel.class);

        // With adjustNothing the window never moves, so we shift the NestedScrollView's
        // bottom padding to match the keyboard height — this keeps the focused field visible.
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), imeHeight);
            return insets;
        });

        // Bind views
        etAmount    = view.findViewById(R.id.et_amount);
        etNote      = view.findViewById(R.id.et_note);
        etDate      = view.findViewById(R.id.et_date);
        acAccount   = view.findViewById(R.id.spinner_account);
        acCategory  = view.findViewById(R.id.spinner_category);
        acMerchant  = view.findViewById(R.id.spinner_merchant);
        toggleType  = view.findViewById(R.id.toggle_type);
        Button btnSave   = view.findViewById(R.id.btn_save);
        Button btnDelete = view.findViewById(R.id.btn_delete);

        if (getArguments() != null)
            editingTransactionId = getArguments().getString("transactionId");

        if (editingTransactionId != null) {
            viewModel.loadTransaction(editingTransactionId);
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete", (d, w) -> {
                        viewModel.deleteTransaction(editingTransactionId);
                        Navigation.findNavController(view).popBackStack();
                        Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show());
        }

        // Date field — MaterialDatePicker (bottom-sheet, NO window dim)
        updateDateDisplay();
        View.OnClickListener dateClick = v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(selectedDate)
                    .build();
            picker.addOnPositiveButtonClickListener(sel -> {
                selectedDate = sel;
                updateDateDisplay();
            });
            picker.show(getParentFragmentManager(), "DATE_PICKER");
        };
        etDate.setOnClickListener(dateClick);
        view.findViewById(R.id.til_date).setOnClickListener(dateClick);

        // Account dropdown
        viewModel.accounts.observe(getViewLifecycleOwner(), accounts -> {
            accountList = accounts;
            List<String> names = new ArrayList<>();
            for (Account a : accounts) names.add(a.name + " (" + a.type + ")");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_list_item_1, names);
            acAccount.setAdapter(adapter);
            if (selectedAccountPos < names.size())
                acAccount.setText(adapter.getItem(selectedAccountPos), false);
            acAccount.setOnItemClickListener((parent, v, pos, id) -> selectedAccountPos = pos);
            populateFormIfEditing();
        });

        // Category dropdown
        viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            categoryList = categories;
            List<String> names = new ArrayList<>();
            names.add("— No Category —");
            for (Category c : categories) names.add(c.name);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_list_item_1, names);
            acCategory.setAdapter(adapter);
            if (selectedCategoryPos < names.size())
                acCategory.setText(adapter.getItem(selectedCategoryPos), false);
            acCategory.setOnItemClickListener((parent, v, pos, id) -> selectedCategoryPos = pos);
            populateFormIfEditing();
        });

        // Merchant dropdown
        viewModel.merchants.observe(getViewLifecycleOwner(), merchants -> {
            merchantList = merchants;
            List<String> names = new ArrayList<>();
            names.add("— No Merchant —");
            for (Merchant m : merchants) names.add(m.name);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_list_item_1, names);
            acMerchant.setAdapter(adapter);
            if (selectedMerchantPos < names.size())
                acMerchant.setText(adapter.getItem(selectedMerchantPos), false);
            acMerchant.setOnItemClickListener((parent, v, pos, id) -> selectedMerchantPos = pos);
            populateFormIfEditing();
        });

        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok))
                Navigation.findNavController(view).popBackStack();
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText() != null ? etAmount.getText().toString() : "";
            if (amountStr.isEmpty()) { etAmount.setError("Required"); return; }
            try {
                double amount     = Double.parseDouble(amountStr);
                String type       = getSelectedType();
                String accountId  = accountList.isEmpty() ? "" : accountList.get(selectedAccountPos).uuid;
                String categoryId = (selectedCategoryPos > 0 && selectedCategoryPos - 1 < categoryList.size())
                        ? categoryList.get(selectedCategoryPos - 1).uuid : "";
                String merchantId = (selectedMerchantPos > 0 && selectedMerchantPos - 1 < merchantList.size())
                        ? merchantList.get(selectedMerchantPos - 1).uuid : "";
                String note       = etNote.getText() != null ? etNote.getText().toString() : "";
                viewModel.saveTransaction(accountId, type, amount, selectedDate, categoryId, merchantId, note);
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid amount");
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error saving transaction", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFormIfEditing() {
        if (editingTransactionId == null) return;
        Transaction tx = viewModel.getEditingTransaction();
        if (tx == null) {
            if (populateRunnable != null) mainHandler.removeCallbacks(populateRunnable);
            populateRunnable = this::populateFormIfEditing;
            mainHandler.postDelayed(populateRunnable, 200);
            return;
        }
        if (accountList == null || accountList.isEmpty()) return;

        // Amount
        if (tx.amount > 0) etAmount.setText(String.valueOf(tx.amount));

        // Note
        etNote.setText(tx.note != null ? tx.note : "");

        // Date
        if (tx.date > 0) { selectedDate = tx.date; updateDateDisplay(); }

        // Type
        if ("INCOME".equals(tx.type))        toggleType.check(R.id.btn_income);
        else if ("TRANSFER".equals(tx.type)) toggleType.check(R.id.btn_transfer);
        else                                 toggleType.check(R.id.btn_expense);

        // Account
        for (int i = 0; i < accountList.size(); i++) {
            if (accountList.get(i).uuid.equals(tx.accountId)) {
                selectedAccountPos = i;
                acAccount.setText(accountList.get(i).name + " (" + accountList.get(i).type + ")", false);
                break;
            }
        }

        // Category
        if (tx.categoryId != null && !categoryList.isEmpty()) {
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).uuid.equals(tx.categoryId)) {
                    selectedCategoryPos = i + 1;
                    acCategory.setText(categoryList.get(i).name, false);
                    break;
                }
            }
        }

        // Merchant
        if (tx.merchantId != null && !merchantList.isEmpty()) {
            for (int i = 0; i < merchantList.size(); i++) {
                if (merchantList.get(i).uuid.equals(tx.merchantId)) {
                    selectedMerchantPos = i + 1;
                    acMerchant.setText(merchantList.get(i).name, false);
                    break;
                }
            }
        }
    }

    private void updateDateDisplay() {
        if (etDate != null)
            etDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(new Date(selectedDate)));
    }

    private String getSelectedType() {
        int id = toggleType.getCheckedButtonId();
        if (id == R.id.btn_income)   return "INCOME";
        if (id == R.id.btn_transfer) return "TRANSFER";
        return "EXPENSE";
    }
}
