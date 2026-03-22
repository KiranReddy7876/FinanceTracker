package com.financetracker.ui.transactions;

import android.app.Application;
import androidx.lifecycle.*;
import com.financetracker.data.db.entity.Transaction;
import com.financetracker.data.repository.TransactionRepository;
import java.util.List;

public class TransactionsViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepo;
    public final LiveData<List<Transaction>> allTransactions;
    private MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    public TransactionsViewModel(Application application) {
        super(application);
        transactionRepo = new TransactionRepository(application);
        allTransactions = transactionRepo.getAllActive();
    }

    public LiveData<List<Transaction>> search(String query) {
        searchQuery.setValue(query);
        return transactionRepo.search(query);
    }

    public LiveData<List<Transaction>> filterByAccount(String accountId) {
        return transactionRepo.getByAccount(accountId);
    }

    public void deleteTransaction(String uuid) {
        transactionRepo.delete(uuid, null);
    }
}
