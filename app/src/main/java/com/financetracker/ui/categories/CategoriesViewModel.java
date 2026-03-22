package com.financetracker.ui.categories;

import android.app.Application;
import androidx.lifecycle.*;
import com.financetracker.data.db.entity.Category;
import com.financetracker.data.repository.CategoryRepository;
import java.util.List;
import java.util.UUID;

public class CategoriesViewModel extends AndroidViewModel {

    private final CategoryRepository categoryRepo;
    public final LiveData<List<Category>> allCategories;
    public final LiveData<List<Category>> expenseCategories;
    public final LiveData<List<Category>> incomeCategories;

    public CategoriesViewModel(Application application) {
        super(application);
        categoryRepo = new CategoryRepository(application);
        allCategories = categoryRepo.getAllActive();
        expenseCategories = categoryRepo.getByType("EXPENSE");
        incomeCategories = categoryRepo.getByType("INCOME");
    }

    public void addCategory(String name, String type) {
        Category c = new Category(UUID.randomUUID().toString(), name, type);
        categoryRepo.insert(c, null);
    }

    public void updateCategory(Category category) {
        categoryRepo.update(category, null);
    }

    public void deleteCategory(String uuid) {
        categoryRepo.delete(uuid, null);
    }
}
