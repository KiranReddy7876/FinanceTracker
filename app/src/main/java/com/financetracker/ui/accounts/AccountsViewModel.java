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

    /** Total Savings — sum of all account balances that are positive */
    public final LiveData<Double> totalSavings;

    /** Outstanding Debt — sum of absolute values of all account balances that are negative */
    public final LiveData<Double> totalDebt;

    /** Net Balance — total savings minus total outstanding debt (sum of all balances) */
    public final LiveData<Double> netBalance;

    public AccountsViewModel(Application application) {
        super(application);
        accountRepo = new AccountRepository(application);
        accounts = accountRepo.getAllActive();

        totalSavings = Transformations.map(accounts, list -> {
            if (list == null) return 0.0;
            double sum = 0;
            for (Account a : list) {
                if (a.currentBalance > 0) sum += a.currentBalance;
            }
            return sum;
        });

        totalDebt = Transformations.map(accounts, list -> {
            if (list == null) return 0.0;
            double sum = 0;
            for (Account a : list) {
                if (a.currentBalance < 0) sum += Math.abs(a.currentBalance);
            }
            return sum;
        });

        netBalance = Transformations.map(accounts, list -> {
            if (list == null) return 0.0;
            double net = 0;
            for (Account a : list) {
                net += a.currentBalance;
            }
            return net;
        });
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
