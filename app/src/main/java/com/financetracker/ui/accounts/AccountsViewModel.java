package com.financetracker.ui.accounts;

import android.app.Application;
import androidx.lifecycle.*;
import com.financetracker.data.db.entity.Account;
import com.financetracker.data.repository.AccountRepository;
import java.util.List;
import java.util.UUID;

public class AccountsViewModel extends AndroidViewModel {

    private final AccountRepository accountRepo;
    public final LiveData<List<Account>> accounts;

    public AccountsViewModel(Application application) {
        super(application);
        accountRepo = new AccountRepository(application);
        accounts = accountRepo.getAllActive();
    }

    public void addAccount(String name, String type, double currentBalance, String currency, String accountNumberLast4) {
        Account a = new Account(UUID.randomUUID().toString(), name, type, currentBalance, currency);
        a.accountNumberLast4 = accountNumberLast4;
        accountRepo.insert(a, null);
    }

    public void updateAccount(Account account) {
        accountRepo.update(account, null);
    }

    public void deleteAccount(String uuid) {
        accountRepo.delete(uuid, null);
    }
}
