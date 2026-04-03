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

    public void saveTransfer(String fromAccountId, String toAccountId, double amount, long date, String note) {
        if (fromAccountId == null || fromAccountId.isEmpty() || toAccountId == null || toAccountId.isEmpty()) {
            errorMessage.setValue("Please select both from and to accounts");
            return;
        }
        if (fromAccountId.equals(toAccountId)) {
            errorMessage.setValue("From and to accounts must be different");
            return;
        }
        if (amount <= 0) {
            errorMessage.setValue("Amount must be greater than zero");
            return;
        }

        if (editingTransaction != null && "TRANSFER".equals(editingTransaction.type)) {
            editingTransaction.accountId = fromAccountId;
            editingTransaction.type = "TRANSFER";
            editingTransaction.amount = amount;
            editingTransaction.date = date;
            editingTransaction.note = note;
            editingTransaction.transferToAccountId = toAccountId;
            transactionRepo.update(editingTransaction, () -> saveSuccess.postValue(true));
        } else {
            Transaction t = new Transaction();
            t.uuid = UUID.randomUUID().toString();
            t.accountId = fromAccountId;
            t.type = "TRANSFER";
            t.amount = amount;
            t.date = date;
            t.note = note;
            t.transferToAccountId = toAccountId;
            t.createdAt = System.currentTimeMillis();
            t.updatedAt = System.currentTimeMillis();
            t.deleted = false;
            transactionRepo.insert(t, () -> saveSuccess.postValue(true));
        }
    }

    public void saveSelfTransfer(String fromAccountId, String toAccountId,
                                double amount, long date, String note) {
        if (fromAccountId == null || fromAccountId.isEmpty() ||
            toAccountId == null || toAccountId.isEmpty()) {
            errorMessage.setValue("Please select both accounts");
            return;
        }
        if (fromAccountId.equals(toAccountId)) {
            errorMessage.setValue("Accounts must be different");
            return;
        }
        if (amount <= 0) {
            errorMessage.setValue("Amount must be greater than zero");
            return;
        }

        if (editingTransaction != null) {
            // ── UPDATE ──────────────────────────────────────────────────────
            editingTransaction.accountId          = fromAccountId;
            editingTransaction.type               = "TRANSFER";
            editingTransaction.transferType       = "SELF";
            editingTransaction.transferToAccountId = toAccountId;
            editingTransaction.recipientName      = null;
            editingTransaction.amount             = amount;
            editingTransaction.date               = date;
            editingTransaction.note               = note;
            transactionRepo.update(editingTransaction, () -> saveSuccess.postValue(true));
        } else {
            // ── INSERT ──────────────────────────────────────────────────────
            Transaction t = new Transaction();
            t.uuid = UUID.randomUUID().toString();
            t.accountId = fromAccountId;
            t.type = "TRANSFER";
            t.transferType = "SELF";
            t.transferToAccountId = toAccountId;
            t.amount = amount;
            t.date = date;
            t.note = note;
            t.createdAt = System.currentTimeMillis();
            t.updatedAt = System.currentTimeMillis();
            t.deleted = false;
            transactionRepo.insert(t, () -> saveSuccess.postValue(true));
        }
    }

    public void saveFriendTransfer(String fromAccountId, String friendName,
                                  String transferSubType, String categoryId,
                                  double amount, long date, String note) {
        if (friendName == null || friendName.isEmpty()) {
            errorMessage.setValue("Friend's name is required");
            return;
        }
        if ("SETTLE_PAYMENT".equals(transferSubType) && (categoryId == null || categoryId.isEmpty())) {
            errorMessage.setValue("Please select expense category for settlement");
            return;
        }
        if (amount <= 0) {
            errorMessage.setValue("Amount must be greater than zero");
            return;
        }

        if (editingTransaction != null) {
            // ── UPDATE ──────────────────────────────────────────────────────
            editingTransaction.accountId           = fromAccountId;
            editingTransaction.type                = "TRANSFER";
            editingTransaction.transferType        = transferSubType;
            editingTransaction.recipientName       = friendName;
            editingTransaction.categoryId          = categoryId.isEmpty() ? null : categoryId;
            editingTransaction.transferToAccountId = null;
            editingTransaction.amount              = amount;
            editingTransaction.date                = date;
            editingTransaction.note                = note;
            transactionRepo.update(editingTransaction, () -> saveSuccess.postValue(true));
        } else {
            // ── INSERT ──────────────────────────────────────────────────────
            Transaction t = new Transaction();
            t.uuid = UUID.randomUUID().toString();
            t.accountId = fromAccountId;
            t.type = "TRANSFER";
            t.transferType = transferSubType;
            t.recipientName = friendName;
            t.categoryId = categoryId;
            t.amount = amount;
            t.date = date;
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

    /**
     * Get categories filtered by transaction type
     * @param type EXPENSE or INCOME
     * @return LiveData list of categories for that type
     */
    public LiveData<List<Category>> getCategoriesByType(String type) {
        return categoryRepo.getByType(type);
    }
}
