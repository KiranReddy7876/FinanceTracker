package com.financetracker.ui.dashboard;

import android.app.Application;
import androidx.lifecycle.*;
import com.financetracker.data.db.entity.*;
import com.financetracker.data.repository.*;
import java.util.Calendar;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SmsImportRepository smsImportRepository;

    public final LiveData<List<Account>> accounts;
    public final LiveData<List<Transaction>> recentTransactions;
    public final LiveData<Integer> pendingSmsCount;
    
    private final MediatorLiveData<Long> dateRangeTrigger = new MediatorLiveData<>();
    private final LiveData<Double> monthlyIncome;
    private final LiveData<Double> monthlyExpense;
    private final LiveData<Double> monthlyTransfer;
    private LiveData<List<Transaction>> currentMonthTransactions;

    public DashboardViewModel(Application application) {
        super(application);
        accountRepository = new AccountRepository(application);
        transactionRepository = new TransactionRepository(application);
        smsImportRepository = new SmsImportRepository(application);

        accounts = accountRepository.getAllActive();
        recentTransactions = transactionRepository.getRecent(5);
        pendingSmsCount = smsImportRepository.getPendingCount();
        
        // Initialize date range trigger
        dateRangeTrigger.setValue(System.currentTimeMillis());
        
        // Setup monthly income using switchMap to always query current month
        monthlyIncome = Transformations.switchMap(dateRangeTrigger, ignored -> {
            long[] dateRange = getMonthDateRange();
            currentMonthTransactions = transactionRepository.getByDateRange(dateRange[0], dateRange[1]);
            return transactionRepository.getTotalIncomeLive(dateRange[0], dateRange[1]);
        });
        
        // Setup monthly expense using switchMap to always query current month
        monthlyExpense = Transformations.switchMap(dateRangeTrigger, ignored -> {
            long[] dateRange = getMonthDateRange();
            return transactionRepository.getTotalExpenseLive(dateRange[0], dateRange[1]);
        });

        // Setup monthly transfer using switchMap to always query current month
        monthlyTransfer = Transformations.switchMap(dateRangeTrigger, ignored -> {
            long[] dateRange = getMonthDateRange();
            return transactionRepository.getTotalTransferLive(dateRange[0], dateRange[1]);
        });
    }

    private long[] getMonthDateRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long start = cal.getTimeInMillis();
        long end = System.currentTimeMillis();
        return new long[]{start, end};
    }

    public void refreshMonthlyData() {
        dateRangeTrigger.setValue(System.currentTimeMillis());
    }

    public LiveData<List<Transaction>> getCurrentMonthTransactions() {
        if (currentMonthTransactions == null) {
            long[] dateRange = getMonthDateRange();
            currentMonthTransactions = transactionRepository.getByDateRange(dateRange[0], dateRange[1]);
        }
        return currentMonthTransactions;
    }

    public LiveData<Double> getMonthlyIncome() {
        return monthlyIncome;
    }

    public LiveData<Double> getMonthlyExpense() {
        return monthlyExpense;
    }

    public LiveData<Double> getMonthlyTransfer() {
        return monthlyTransfer;
    }
}
