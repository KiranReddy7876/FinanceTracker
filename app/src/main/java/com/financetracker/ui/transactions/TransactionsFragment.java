package com.financetracker.ui.transactions;

import android.os.Bundle;
import android.view.*;
import android.widget.SearchView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.financetracker.R;

public class TransactionsFragment extends Fragment {

    private TransactionsViewModel viewModel;
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionsViewModel.class);

        RecyclerView rv = view.findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Pass AppDatabase instance to enable merchant nickName lookup
        adapter = new TransactionAdapter(transaction -> {
            Bundle args = new Bundle();
            args.putString("transactionId", transaction.uuid);
            Navigation.findNavController(view).navigate(R.id.action_transactions_to_addTransaction, args);
        }, com.financetracker.data.db.AppDatabase.getInstance(requireContext()));
        rv.setAdapter(adapter);

        viewModel.allTransactions.observe(getViewLifecycleOwner(), transactions ->
            adapter.submitList(transactions));

        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { return false; }
            @Override public boolean onQueryTextChange(String q) {
                if (q.isEmpty()) {
                    viewModel.allTransactions.observe(getViewLifecycleOwner(), transactions ->
                        adapter.submitList(transactions));
                } else {
                    viewModel.search(q).observe(getViewLifecycleOwner(), transactions ->
                        adapter.submitList(transactions));
                }
                return true;
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v ->
            Navigation.findNavController(view).navigate(R.id.action_transactions_to_addTransaction));
    }
}
