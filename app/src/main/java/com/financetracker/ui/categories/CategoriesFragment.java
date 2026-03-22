package com.financetracker.ui.categories;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.financetracker.R;
import com.financetracker.data.db.entity.Category;

public class CategoriesFragment extends Fragment {

    private CategoriesViewModel viewModel;
    private CategoryAdapter adapter;
    private String currentType = "EXPENSE";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rv_categories);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CategoryAdapter(this::showEditDialog);
        rv.setAdapter(adapter);

        TabLayout tabs = view.findViewById(R.id.tabs_category_type);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentType = tab.getPosition() == 0 ? "EXPENSE" : "INCOME";
                observeCategories();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        observeCategories();

        FloatingActionButton fab = view.findViewById(R.id.fab_add_category);
        fab.setOnClickListener(v -> showAddDialog());
    }

    private void observeCategories() {
        if ("EXPENSE".equals(currentType)) {
            viewModel.expenseCategories.observe(getViewLifecycleOwner(), cats -> adapter.submitList(cats));
        } else {
            viewModel.incomeCategories.observe(getViewLifecycleOwner(), cats -> adapter.submitList(cats));
        }
    }

    private void showAddDialog() {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category, null);
        TextInputEditText etName = dv.findViewById(R.id.et_category_name);
        Spinner spinnerType = dv.findViewById(R.id.spinner_category_type);
        spinnerType.setAdapter(new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, new String[]{"EXPENSE", "INCOME"}));
        spinnerType.setSelection("EXPENSE".equals(currentType) ? 0 : 1);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle("Add Category")
            .setView(dv)
            .setPositiveButton("Save", (d, w) -> {
                String name = etName.getText() != null ? etName.getText().toString() : "";
                String type = spinnerType.getSelectedItemPosition() == 0 ? "EXPENSE" : "INCOME";
                if (!name.isEmpty()) viewModel.addCategory(name, type);
            })
            .setNegativeButton("Cancel", null)
            .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialog.show();
    }

    private void showEditDialog(Category category) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category, null);
        TextInputEditText etName = dv.findViewById(R.id.et_category_name);
        etName.setText(category.name);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle("Edit Category")
            .setView(dv)
            .setPositiveButton("Save", (d, w) -> {
                category.name = etName.getText() != null ? etName.getText().toString() : category.name;
                viewModel.updateCategory(category);
            })
            .setNeutralButton("Delete", (d, w) -> viewModel.deleteCategory(category.uuid))
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
