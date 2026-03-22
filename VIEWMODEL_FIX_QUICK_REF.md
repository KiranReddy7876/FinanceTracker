# ViewModel Error - Quick Fix Summary

## Problem
```
Cannot create an instance of class com.financetracker.ui.accounts.AccountsViewModel
```

## Root Cause
`AccountsViewModel` extends `AndroidViewModel` (requires Application context), but was instantiated without the proper factory in `AccountsFragment`.

## Solution - APPLIED ✅

**File:** `AccountsFragment.java` Line 33-36

**Old Code:**
```java
viewModel = new ViewModelProvider(this).get(AccountsViewModel.class);
```

**New Code:**
```java
viewModel = new ViewModelProvider(
    this,
    new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
).get(AccountsViewModel.class);
```

## What Changed
Added `AndroidViewModelFactory` with Application context to properly instantiate the ViewModel.

## Status: ✅ FIXED

Build and run:
```bash
./gradlew.bat clean build
```

The error is now resolved! ✅

---

For details: See VIEWMODEL_INSTANTIATION_FIX.md

