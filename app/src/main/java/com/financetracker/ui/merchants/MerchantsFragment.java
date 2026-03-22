package com.financetracker.ui.merchants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.financetracker.R;
import com.financetracker.data.db.entity.Merchant;

public class MerchantsFragment extends Fragment {

    private MerchantsViewModel viewModel;
    private MerchantListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merchants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            viewModel = new ViewModelProvider(this).get(MerchantsViewModel.class);

            RecyclerView recyclerView = view.findViewById(R.id.rv_merchants);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

            adapter = new MerchantListAdapter(this::showEditDialog);
            recyclerView.setAdapter(adapter);

            // Observe all merchants
            viewModel.allMerchants.observe(getViewLifecycleOwner(), merchants -> {
                if (adapter != null && merchants != null) {
                    adapter.submitList(merchants);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEditDialog(Merchant merchant) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_merchant, null);
        TextInputEditText etName = dv.findViewById(R.id.et_merchant_name);
        TextInputEditText etNickName = dv.findViewById(R.id.et_merchant_nickname);
        TextInputEditText etCategory = dv.findViewById(R.id.et_merchant_category);

        etName.setText(merchant.name != null ? merchant.name : "");
        etNickName.setText(merchant.nickName != null ? merchant.nickName : "");
        etCategory.setText(merchant.categoryId != null ? merchant.categoryId : "Not assigned");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Edit Merchant")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    String newNickName = etNickName.getText() != null ? etNickName.getText().toString() : "";
                    merchant.nickName = newNickName.isEmpty() ? null : newNickName;
                    viewModel.updateMerchant(merchant);
                })
                .setNeutralButton("Delete", (d, w) -> viewModel.deleteMerchant(merchant.uuid))
                .setNegativeButton("Cancel", null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialog.show();
    }
}


