package com.financetracker.ui.reports;

import android.app.Application;
import androidx.lifecycle.*;
import com.financetracker.data.db.entity.Category;
import com.financetracker.data.db.entity.Transaction;
import com.financetracker.data.repository.CategoryRepository;
import com.financetracker.data.repository.TransactionRepository;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class ReportsViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepo;
    private final CategoryRepository categoryRepo;
    private final Map<String, String> categoryNameCache = new HashMap<>();

    private final MutableLiveData<Integer> selectedYear = new MutableLiveData<>(Calendar.getInstance().get(Calendar.YEAR));
    private final MutableLiveData<Integer> selectedMonth = new MutableLiveData<>(Calendar.getInstance().get(Calendar.MONTH));

    public ReportsViewModel(Application application) {
        super(application);
        transactionRepo = new TransactionRepository(application);
        categoryRepo = new CategoryRepository(application);
        
        // Load categories into cache on background thread
        Executors.newSingleThreadExecutor().execute(this::loadCategoryCache);
    }

    private void loadCategoryCache() {
        try {
            List<Category> categories = categoryRepo.getAllActiveSync();
            if (categories != null) {
                for (Category c : categories) {
                    categoryNameCache.put(c.uuid, c.name);
                }
            }
        } catch (Exception e) {
            // Silently ignore errors, cache will just be empty
        }
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

    public String getCategoryName(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) {
            return "Uncategorized";
        }
        // Use cached name, avoid blocking database call on main thread
        String name = categoryNameCache.get(categoryId);
        return name != null ? name : "Uncategorized";
    }
}
