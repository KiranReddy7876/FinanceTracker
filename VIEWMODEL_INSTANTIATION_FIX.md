# ViewModel Instantiation Error - FIXED ✅

## Error
```
Cannot create an instance of class com.financetracker.ui.accounts.AccountsViewModel
```

## Root Cause
The `AccountsViewModel` extends `AndroidViewModel`, which requires the Application context to be passed to its constructor. However, in `AccountsFragment`, it was being instantiated using only `ViewModelProvider(this)`, which doesn't provide the necessary `AndroidViewModelFactory`.

```java
// WRONG - This fails for AndroidViewModel
viewModel = new ViewModelProvider(this).get(AccountsViewModel.class);
```

## Solution Applied ✅

**File:** `AccountsFragment.java` (Line 33-36)

**Changed from:**
```java
viewModel = new ViewModelProvider(this).get(AccountsViewModel.class);
```

**Changed to:**
```java
viewModel = new ViewModelProvider(
    this,
    new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
).get(AccountsViewModel.class);
```

## Why This Works

1. **AndroidViewModel Requirement:** When a ViewModel extends `AndroidViewModel`, it needs the Application context
2. **AndroidViewModelFactory:** This factory knows how to instantiate `AndroidViewModel` classes by providing the Application context
3. **requireActivity().getApplication():** Gets the Application context from the current Activity

## Key Changes

| Component | What Was Changed | Why |
|-----------|-----------------|-----|
| ViewModel Instantiation | Added AndroidViewModelFactory | Required for AndroidViewModel |
| Factory Parameter | Passed Application context | Needed by AndroidViewModel constructor |

## AccountsViewModel Constructor
```java
public AccountsViewModel(Application application) {
    super(application);  // Requires Application context
    accountRepo = new AccountRepository(application);
    accounts = accountRepo.getAllActiveWithBalance();
}
```

The `super(application)` call in the constructor requires the Application instance, which is now properly provided by the factory.

## Verification

✅ **Code compiles without errors**
✅ **No compilation warnings related to ViewModel**
✅ **AccountsFragment properly instantiates AccountsViewModel**

## Testing

After the fix:

1. **Build the project:**
   ```bash
   ./gradlew.bat clean build
   ```

2. **Run the app:**
   - Navigate to Accounts screen
   - Should display list of accounts without errors
   - Accounts should show with calculated balances and account numbers

3. **Test Features:**
   - Create account with account number → "BANK •••1234" ✓
   - Edit account → Account number appears in dialog ✓
   - Delete account → Removed from list ✓
   - Balance calculation → Correct balance displayed ✓

## Similar Fix Needed Elsewhere?

If you're using `AndroidViewModel` in other fragments/activities, apply the same pattern:

```java
// Correct way to instantiate AndroidViewModel
viewModel = new ViewModelProvider(
    this,
    new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
).get(YourAndroidViewModel.class);
```

## Status

✅ **FIXED**
- ViewModel instantiation error resolved
- Code compiles successfully
- Ready to build and test

---

**Fixed Date:** March 15, 2026
**File Modified:** AccountsFragment.java
**Type:** ViewModel Factory Configuration
**Impact:** Accounts screen will now load without errors

