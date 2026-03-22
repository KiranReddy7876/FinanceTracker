package com.financetracker.ui.reports;

import android.app.Application;
import androidx.lifecycle.*;
import com.financetracker.data.db.entity.Transaction;
import com.financetracker.data.repository.TransactionRepository;
import java.util.Calendar;
import java.util.List;

public class ReportsViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepo;

    private final MutableLiveData<Integer> selectedYear = new MutableLiveData<>(Calendar.getInstance().get(Calendar.YEAR));
    private final MutableLiveData<Integer> selectedMonth = new MutableLiveData<>(Calendar.getInstance().get(Calendar.MONTH));

    public ReportsViewModel(Application application) {
        super(application);
        transactionRepo = new TransactionRepository(application);
    }

    public LiveData<List<Transaction>> getTransactionsForSelectedMonth() {
        return Transformations.switchMap(selectedMonth, month -> {
            int year = selectedYear.getValue() != null ? selectedYear.getValue() : Calendar.getInstance().get(Calendar.YEAR);
            Calendar start = Calendar.getInstance();
            start.set(year, month, 1, 0, 0, 0);
            start.set(Calendar.MILLISECOND, 0);
            Calendar end = Calendar.getInstance();
            end.set(year, month, start.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
            return transactionRepo.getByDateRange(start.getTimeInMillis(), end.getTimeInMillis());
        });
    }

    public void setMonth(int year, int month) {
        selectedYear.setValue(year);
        selectedMonth.setValue(month);
    }

    public int getSelectedYear() { return selectedYear.getValue() != null ? selectedYear.getValue() : Calendar.getInstance().get(Calendar.YEAR); }
    public int getSelectedMonth() { return selectedMonth.getValue() != null ? selectedMonth.getValue() : Calendar.getInstance().get(Calendar.MONTH); }

    public double sumExpenses(List<Transaction> transactions) {
        double total = 0;
        for (Transaction t : transactions) if ("EXPENSE".equals(t.type)) total += t.amount;
        return total;
    }

    public double sumIncome(List<Transaction> transactions) {
        double total = 0;
        for (Transaction t : transactions) if ("INCOME".equals(t.type)) total += t.amount;
        return total;
    }
}
