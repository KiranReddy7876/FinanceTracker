# Fix: Income Categories Not Showing When Adding Income Transaction

## Problem
When adding a transaction as INCOME, the category dropdown was showing EXPENSE categories instead of INCOME categories. This was a critical bug that prevented users from properly categorizing income transactions.

## Root Cause
The AddTransactionFragment was observing `viewModel.categories` which returned **ALL categories** from the database without filtering by transaction type. The categories need to be filtered based on whether the user selected INCOME or EXPENSE transaction type.

## Solution Implemented

### 1. Added Method to ViewModel (AddTransactionViewModel.java)
```java
/**
 * Get categories filtered by transaction type
 * @param type EXPENSE or INCOME
 * @return LiveData list of categories for that type
 */
public LiveData<List<Category>> getCategoriesByType(String type) {
    return categoryRepo.getByType(type);
}
```

This method leverages the existing `CategoryRepository.getByType()` method to filter categories by type.

### 2. Updated Fragment Category Observer (AddTransactionFragment.java)

**Before:**
```java
viewModel.categories.observe(getViewLifecycleOwner(), categories -> {
    // Load ALL categories - WRONG!
    categoryList = categories;
    // ... populate dropdown
});
```

**After:**
```java
String initialType = getSelectedType();
viewModel.getCategoriesByType(initialType).observe(getViewLifecycleOwner(), categories -> {
    // Load ONLY categories matching the selected type - CORRECT!
    categoryList = categories;
    // ... populate dropdown
});
```

### 3. Updated Type Toggle Listener (AddTransactionFragment.java)

**Before:**
```java
toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
    if (isChecked) {
        updateUIForTransactionType(view, btnSave);
        // No category reload when type changes
    }
});
```

**After:**
```java
toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
    if (isChecked) {
        updateUIForTransactionType(view, btnSave);
        
        // Reload categories for the new transaction type
        String selectedType = getSelectedType();
        selectedCategoryPos = 0;  // Reset to "No Category"
        viewModel.getCategoriesByType(selectedType).observe(getViewLifecycleOwner(), categories -> {
            categoryList = categories;
            List<String> names = new ArrayList<>();
            names.add("— No Category —");
            for (Category c : categories) names.add(c.name);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_list_item_1, names);
            acCategory.setAdapter(adapter);
            acSettleCategory.setAdapter(adapter);
            acCategory.setText(adapter.getItem(0), false);  // Reset to "No Category"
        });
    }
});
```

## How It Works Now

### When Adding INCOME Transaction
1. User selects INCOME button in transaction type toggle
2. UI updates to show INCOME-related fields
3. **Categories dropdown automatically reloads with ONLY INCOME categories:**
   - Salary
   - Bonus
   - Interest
   - Dividend
   - Miscellaneous
   - (Custom INCOME categories if any)

### When Adding EXPENSE Transaction
1. User selects EXPENSE button in transaction type toggle
2. UI updates to show EXPENSE-related fields
3. **Categories dropdown automatically reloads with ONLY EXPENSE categories:**
   - Groceries
   - Utilities
   - Entertainment
   - Transport
   - Healthcare
   - Shopping
   - Miscellaneous
   - (Custom EXPENSE categories if any)

### When Adding TRANSFER Transaction
- Categories dropdown is hidden (TRANSFER type doesn't use categories for main transaction)
- Settlement Payment transfers still use expense categories for tracking

## Files Modified
1. `app/src/main/java/com/financetracker/ui/addtransaction/AddTransactionViewModel.java`
   - Added `getCategoriesByType(String type)` method

2. `app/src/main/java/com/financetracker/ui/addtransaction/AddTransactionFragment.java`
   - Updated category observer to use `getCategoriesByType()` with initial type
   - Updated type toggle listener to reload categories when type changes

## Testing Checklist

- [x] Add INCOME transaction → Shows only INCOME categories
- [x] Add EXPENSE transaction → Shows only EXPENSE categories
- [x] Switch type while editing → Categories update correctly
- [x] Edit existing INCOME transaction → Shows correct INCOME categories
- [x] Edit existing EXPENSE transaction → Shows correct EXPENSE categories
- [x] Custom categories appear in correct type dropdown

## Benefits
✅ Correct category filtering by transaction type
✅ Better user experience - no confusion about available categories
✅ Data integrity - ensures transactions are categorized with correct type
✅ Follows system design pattern from SmsImportFragment

## Code Quality
- Uses existing repository methods (no duplication)
- Maintains consistent pattern with other features
- No breaking changes to existing functionality
- Minimal code changes for maximum impact

---
**Date Fixed:** March 31, 2026
**Status:** ✅ COMPLETE

