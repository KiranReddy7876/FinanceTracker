package com.financetracker.ui.addtransaction;

import android.app.Application;
import androidx.lifecycle.*;
import com.financetracker.data.db.entity.*;
import com.financetracker.data.repository.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTransactionViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepo;
    private final AccountRepository accountRepo;
    private final CategoryRepository categoryRepo;
    private final MerchantRepository merchantRepo;

    public final LiveData<List<Account>> accounts;
    public final LiveData<List<Category>> categories;
    public final LiveData<List<Merchant>> merchants;

    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Transaction> editingTransactionLive = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LiveData<Boolean> getSaveSuccess() { return saveSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Transaction> getEditingTransactionLive() { return editingTransactionLive; }

    // Holds currently loaded transaction when editing
    private Transaction editingTransaction;

    public AddTransactionViewModel(Application application) {
        super(application);
        transactionRepo = new TransactionRepository(application);
        accountRepo = new AccountRepository(application);
        categoryRepo = new CategoryRepository(application);
        merchantRepo = new MerchantRepository(application);

        accounts = accountRepo.getAllActive();
        categories = categoryRepo.getAllActive();
        merchants = merchantRepo.getAllActive();
    }

    public void saveTransaction(String accountId, String type, double amount, long date,
                                String categoryId, String merchantId, String note) {
        if (accountId == null || accountId.isEmpty()) {
            errorMessage.setValue("Please select an account");
            return;
        }
        if (amount <= 0) {
            errorMessage.setValue("Amount must be greater than zero");
            return;
        }

        if (editingTransaction != null) {
            editingTransaction.accountId = accountId;
            editingTransaction.type = type;
            editingTransaction.amount = amount;
            editingTransaction.date = date;
            editingTransaction.categoryId = categoryId;
            editingTransaction.merchantId = merchantId;
            editingTransaction.note = note;
            transactionRepo.update(editingTransaction, () -> saveSuccess.postValue(true));
        } else {
            Transaction t = new Transaction();
            t.uuid = UUID.randomUUID().toString();
            t.accountId = accountId;
            t.type = type;
            t.amount = amount;
            t.date = date;
            t.categoryId = categoryId.isEmpty() ? null : categoryId;
            t.merchantId = merchantId.isEmpty() ? null : merchantId;
            t.note = note;
            t.createdAt = System.currentTimeMillis();
            t.updatedAt = System.currentTimeMillis();
            t.deleted = false;
            transactionRepo.insert(t, () -> saveSuccess.postValue(true));
        }
    }

    public void setEditingTransaction(Transaction t) {
        this.editingTransaction = t;
    }

    public Transaction getEditingTransaction() { return editingTransaction; }

    public void loadTransaction(String uuid) {
        executor.execute(() -> {
            Transaction t = transactionRepo.getById(uuid);
            if (t != null) {
                editingTransaction = t;
                editingTransactionLive.postValue(t);
            }
        });
    }

    public void deleteTransaction(String uuid) {
        executor.execute(() -> {
            transactionRepo.delete(uuid, () -> saveSuccess.postValue(true));
        });
    }
}
