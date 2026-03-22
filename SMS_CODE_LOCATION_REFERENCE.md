# SMS Transaction Review - Code Location Reference

## Quick Reference: Where Everything Is

### 📍 Main Screens

#### **SMS Import Fragment (Main Review List)**
```
File: app/src/main/java/com/financetracker/ui/smsimport/SmsImportFragment.java

Key Methods:
├─ onCreateView() - Loads layout
├─ onViewCreated() - Sets up RecyclerView
│   ├─ Observes pendingSmsImports LiveData
│   ├─ Updates adapter with pending SMS list
│   └─ Shows "No pending" message if empty
└─ showEditDialog(SmsImport) - Opens review dialog
    ├─ Loads dialog_sms_import layout
    ├─ Populates transaction details
    ├─ Sets up account spinner
    ├─ Sets up category spinner
    ├─ Handles user selections
    ├─ Confirm/Ignore/Cancel actions
    └─ Updates database & navigates back

Layout: app/src/main/res/layout/fragment_sms_import.xml
├─ Title: "Pending SMS Transactions"
├─ RecyclerView: rv_sms_imports
└─ Empty message: tv_no_pending
```

#### **Review Dialog**
```
File: Same as above (SmsImportFragment.showEditDialog())

Layout: app/src/main/res/layout/dialog_sms_import.xml
├─ Amount display: tv_amount
├─ Type display: tv_type
├─ SMS text display: tv_sms_text (scrollable)
├─ Account spinner: spinner_account
├─ Category spinner: spinner_category
└─ Buttons: Confirm, Ignore, Cancel
```

#### **SMS Review Fragment (Alternative)**
```
File: app/src/main/java/com/financetracker/ui/smsreview/SmsReviewFragment.java

Key Methods:
├─ onCreateView() - Loads layout
└─ onViewCreated() - Sets up display
    ├─ Observes pendingItems
    ├─ Sets up SmsReviewAdapter
    └─ Updates RecyclerView

Layout: app/src/main/res/layout/fragment_sms_review.xml
├─ RecyclerView: rv_sms_pending
└─ Empty message: tv_empty
```

---

### 🔧 ViewModels

#### **SmsImportViewModel**
```
File: app/src/main/java/com/financetracker/ui/smsimport/SmsImportViewModel.java

LiveData Exposed:
├─ pendingSmsImports - List of pending SMS imports
├─ accounts - All active accounts (for dropdown)
└─ categories - Categories filtered by type

Key Methods:
├─ updateAccountAndCategory(uuid, accountId, categoryId)
│   └─ Updates SMS import with user selections
├─ confirmImport(uuid)
│   └─ Confirms and converts to transaction
├─ ignoreImport(uuid)
│   └─ Marks as ignored
└─ getCategoriesByType(type)
    └─ Returns categories filtered by EXPENSE/INCOME

Usage in Fragment:
├─ viewModel.pendingSmsImports.observe() - Populate list
├─ viewModel.accounts.observe() - Populate account dropdown
├─ viewModel.getCategoriesByType().observe() - Populate category dropdown
├─ viewModel.updateAccountAndCategory() - On selection
├─ viewModel.confirmImport() - On confirm button
└─ viewModel.ignoreImport() - On ignore button
```

#### **SmsReviewViewModel**
```
File: app/src/main/java/com/financetracker/ui/smsreview/SmsReviewViewModel.java

LiveData Exposed:
├─ pendingItems - List of pending SMS imports
└─ accounts - All active accounts

Key Methods:
├─ confirmAndCreate(smsImport, accountId, categoryId)
│   ├─ Creates Transaction from SMS import
│   ├─ Sets account, type, amount, date
│   ├─ Inserts transaction into DB
│   └─ Marks SMS import as confirmed
└─ ignore(uuid)
    └─ Marks SMS import as ignored

Usage in Fragment:
├─ viewModel.accounts.observe() - Populate account spinner
├─ viewModel.pendingItems.observe() - Populate adapter
├─ viewModel.confirmAndCreate() - On confirm
└─ viewModel.ignore() - On ignore
```

---

### 📦 Data Models

#### **SmsImport Entity**
```
File: app/src/main/java/com/financetracker/data/db/entity/SmsImport.java

Fields:
├─ uuid: String (PK)
├─ smsText: String - Full SMS message
├─ amount: double - Extracted amount
├─ detectedType: String - EXPENSE or INCOME
├─ date: long - Timestamp
├─ accountId: String - Matched or user-selected account
├─ categoryId: String - User-selected category (nullable)
├─ status: String - PENDING, CONFIRMED, IGNORED
├─ createdAt: long - Creation timestamp
├─ updatedAt: long - Last update timestamp
└─ deleted: boolean - Soft delete flag

Lifecycle:
├─ Created: status = "PENDING"
├─ After review: status = "CONFIRMED" or "IGNORED"
└─ Auto-import: status = "CONFIRMED" immediately
```

#### **Transaction Entity**
```
File: app/src/main/java/com/financetracker/data/db/entity/Transaction.java

Created from SmsImport with:
├─ uuid: New UUID
├─ accountId: From user selection or auto-match
├─ type: From SmsImport.detectedType
├─ amount: From SmsImport.amount
├─ date: From SmsImport.date
├─ categoryId: From user selection (optional)
├─ note: "Auto-imported from SMS"
├─ referenceId: Links to SmsImport.uuid (audit trail)
├─ createdAt: Current timestamp
├─ updatedAt: Current timestamp
└─ deleted: false
```

---

### 💾 Database Access

#### **SmsImportDao**
```
File: app/src/main/java/com/financetracker/data/db/dao/SmsImportDao.java

Key Queries:
├─ insert(SmsImport) - Create new
├─ update(SmsImport) - Update existing
├─ getPending() - LiveData of PENDING imports
│   └─ SELECT * FROM sms_import WHERE status = 'PENDING'
├─ getPendingCount() - LiveData count of pending
│   └─ SELECT COUNT(*) FROM sms_import WHERE status = 'PENDING'
├─ updateStatus(uuid, status, updatedAt) - Change status
│   └─ UPDATE sms_import SET status = ?, updatedAt = ? WHERE uuid = ?
├─ updateAccountAndCategory(uuid, accountId, categoryId, updatedAt)
│   └─ UPDATE sms_import SET accountId = ?, categoryId = ?, updatedAt = ? WHERE uuid = ?
├─ getById(uuid) - Get single import
│   └─ SELECT * FROM sms_import WHERE uuid = ? LIMIT 1
└─ getConfirmed() - Get all confirmed (for sync)
    └─ SELECT * FROM sms_import WHERE status = 'CONFIRMED' AND deleted = 0
```

#### **SmsImportRepository**
```
File: app/src/main/java/com/financetracker/data/repository/SmsImportRepository.java

Methods:
├─ insert(SmsImport) - Async insert
├─ getPending() - Returns LiveData<List<SmsImport>>
├─ getPendingCount() - Returns LiveData<Integer>
├─ confirm(uuid) - Update to CONFIRMED + convert to transaction
├─ confirmWithoutUserReview(uuid) - Auto-confirm (NEW METHOD)
│   └─ For SMS with account matches
├─ ignore(uuid) - Update to IGNORED
├─ updateAccountAndCategory(uuid, accountId, categoryId) - Update selections
├─ getById(uuid) - Get single import
└─ getConfirmed() - Get confirmed imports
```

---

### 🔄 Adapters

#### **SmsImportAdapter**
```
File: app/src/main/java/com/financetracker/ui/smsimport/SmsImportAdapter.java

Extends: ListAdapter<SmsImport, SmsImportAdapter.ViewHolder>

Key Methods:
├─ onBindViewHolder() - Display SMS import item
│   ├─ Shows amount (currency formatted)
│   ├─ Shows type (EXPENSE/INCOME)
│   └─ Shows SMS preview text
├─ ViewHolder
│   ├─ tvAmount - Amount display
│   ├─ tvType - Type badge
│   └─ tvSmsText - SMS preview
└─ Click Handler - Calls callback to show review dialog

Usage in Fragment:
├─ adapter = new SmsImportAdapter(this::showEditDialog)
├─ rv.setAdapter(adapter)
└─ adapter.submitList(smsImportsList) - When data updates
```

#### **SmsReviewAdapter**
```
File: app/src/main/java/com/financetracker/ui/smsreview/SmsReviewAdapter.java

Extends: RecyclerView.Adapter

Key Methods:
├─ onBindViewHolder() - Display SMS import
│   ├─ Shows details from SmsImport
│   ├─ Shows account spinner
│   ├─ Shows category spinner
│   └─ Buttons for confirm/ignore
└─ Click Handlers
    ├─ Confirm → confirmCallback.onConfirm()
    └─ Ignore → ignoreCallback.onIgnore()

Usage in Fragment:
├─ adapter = new SmsReviewAdapter(accounts, confirmCallback, ignoreCallback)
├─ rv.setAdapter(adapter)
└─ adapter.submitList(pendingItems)
```

---

### 🔗 Related Services

#### **SmsReceiver (Receives SMS)**
```
File: app/src/main/java/com/financetracker/service/SmsReceiver.java

Flow:
├─ onReceive() - Called when SMS arrives
├─ Parse SMS message
├─ Check if transaction SMS
├─ Extract transaction details (amount, type, date)
├─ Extract account number (last 4 digits)
├─ Match account via AccountRepository
├─ Create SmsImport record
├─ Insert into database
├─ NEW: Auto-confirm if account matched
│   └─ Call smsImportRepo.confirmWithoutUserReview()
│   └─ Skip notification & manual review
└─ OR: Show notification if no match
    └─ User sees badge & review screen
```

#### **SmsImportConversionService**
```
File: app/src/main/java/com/financetracker/service/SmsImportConversionService.java

Purpose: Convert confirmed SMS imports to transactions

Key Methods:
├─ convertToTransaction(context, smsImport)
│   ├─ Validates SmsImport status = "CONFIRMED"
│   ├─ Validates accountId != null
│   ├─ Creates new Transaction record
│   ├─ Maps SmsImport fields to Transaction
│   ├─ Inserts into transactions table
│   └─ Logs result
└─ processAllConfirmed(context) - Batch processing
    ├─ Get all confirmed SMS imports
    ├─ Convert each to transaction
    └─ Run on background thread
```

---

### 📺 UI Flow Code Path

#### **From Menu to SMS Import Screen**
```
1. User clicks Menu
   └─ MainActivity navigates via NavController

2. NavController routes to SmsImportFragment
   └─ Fragment.onCreateView() called
   └─ Inflates fragment_sms_import.xml

3. SmsImportFragment.onViewCreated()
   ├─ Creates SmsImportViewModel
   ├─ Sets up RecyclerView with SmsImportAdapter
   ├─ Observes viewModel.pendingSmsImports
   │   └─ On data change: adapter.submitList(items)
   └─ Shows list or empty message

4. SmsImportAdapter displays items
   ├─ Each item shows amount, type, SMS preview
   └─ setOnClickListener on each item

5. User clicks item
   ├─ Adapter calls callback: this::showEditDialog
   ├─ SmsImportFragment.showEditDialog(smsImport) runs
   ├─ Inflates dialog_sms_import.xml
   ├─ Populates spinners with data
   ├─ Shows AlertDialog
   └─ Waits for user action

6. User selects account & category
   ├─ Account spinner selection listener
   ├─ Category spinner selection listener
   └─ Selections stored in local variables

7. User clicks Confirm
   ├─ viewModel.updateAccountAndCategory() - Save selections
   ├─ viewModel.confirmImport(uuid) - Confirm SMS
   ├─ Repository updates status to CONFIRMED
   ├─ SmsImportConversionService creates transaction
   ├─ Dialog closes
   ├─ Toast: "Transaction recorded"
   ├─ List updates (item removed)
   └─ Dashboard updates via LiveData

8. User sees updated dashboard
   ├─ Recent Transactions updated
   ├─ Amount reflected in totals
   └─ Transaction visible in account details
```

---

### 🧪 Testing Code Locations

#### **Unit Tests** (Where to add)
```
app/src/test/java/com/financetracker/
├─ data/db/dao/SmsImportDaoTest.java
├─ data/repository/SmsImportRepositoryTest.java
├─ ui/smsimport/SmsImportViewModelTest.java
├─ service/SmsImportConversionServiceTest.java
└─ service/SmsReceiverTest.java
```

#### **Integration Tests** (Where to add)
```
app/src/androidTest/java/com/financetracker/
├─ SmsImportFragmentTest.java - Test UI flow
├─ SmsImportEndToEndTest.java - Full workflow
└─ SmsNotificationIntegrationTest.java - Notification + review
```

---

### 📱 Layout Files Reference

```
app/src/main/res/layout/
├─ fragment_sms_import.xml - Main review list screen
│   ├─ Title
│   ├─ RecyclerView (rv_sms_imports)
│   └─ Empty message
├─ dialog_sms_import.xml - Review & approve dialog
│   ├─ Amount & Type display
│   ├─ SMS text (scrollable)
│   ├─ Account spinner
│   ├─ Category spinner
│   └─ Buttons (Confirm, Ignore, Cancel)
├─ fragment_sms_review.xml - Alternative review
│   ├─ RecyclerView
│   └─ Empty message
└─ item_sms_import.xml - Single SMS item view
    ├─ Amount display
    ├─ Type display
    └─ SMS preview

app/src/main/res/drawable/
├─ badge_background.xml - Badge styling
└─ outlined_bg.xml - Spinner background
```

---

### 🔑 Key Constants

```java
// SmsReceiver
private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
private static final String TAG = "SmsReceiver";

// SmsImportFragment
// Fragment ID for navigation
R.id.action_dashboard_to_smsImport
R.id.action_menu_to_smsImport

// Status values
"PENDING"   - Awaiting review
"CONFIRMED" - Approved and converted
"IGNORED"   - Discarded by user

// Transaction types
"EXPENSE" - Money out
"INCOME"  - Money in

// Database table names
"sms_import"     - SMS imports table
"transactions"   - Transactions table
"accounts"       - Accounts table
"categories"     - Categories table
```

---

### 🔍 Debug Points

If SMS review not working, check:

1. **SMS reaching app:**
   ```
   Logcat filter: "SmsReceiver"
   Look for: "SMS imported successfully" or "Error processing SMS"
   ```

2. **SMS stored in database:**
   ```
   Database Inspector (Android Studio)
   Check: sms_import table for new records
   ```

3. **Fragment appearing:**
   ```
   Logcat filter: "SmsImportFragment"
   Look for: "onViewCreated" and "onBindViewHolder" logs
   ```

4. **Data displayed:**
   ```
   Check: RecyclerView has adapter with items
   Check: Spinners populate correctly
   Check: Transaction details shown in dialog
   ```

5. **Confirmation working:**
   ```
   After confirm click:
   - SMS should move to CONFIRMED status
   - Transaction should appear in transactions table
   - Recent Transactions should update
   ```

---

## File Directory Structure

```
FinanceTracker/
app/src/main/java/com/financetracker/
├─ ui/
│   ├─ smsimport/
│   │   ├─ SmsImportFragment.java ← Main review list
│   │   ├─ SmsImportViewModel.java
│   │   ├─ SmsImportAdapter.java
│   │   └─ ...
│   ├─ smsreview/
│   │   ├─ SmsReviewFragment.java ← Alternative review
│   │   ├─ SmsReviewViewModel.java
│   │   ├─ SmsReviewAdapter.java
│   │   └─ ...
│   └─ ...
├─ data/
│   ├─ db/
│   │   ├─ entity/
│   │   │   ├─ SmsImport.java
│   │   │   ├─ Transaction.java
│   │   │   └─ ...
│   │   ├─ dao/
│   │   │   ├─ SmsImportDao.java
│   │   │   └─ ...
│   │   └─ AppDatabase.java
│   └─ repository/
│       ├─ SmsImportRepository.java
│       └─ ...
├─ service/
│   ├─ SmsReceiver.java ← Receives SMS
│   ├─ SmsImportConversionService.java ← Converts to transaction
│   └─ ...
└─ ...

app/src/main/res/layout/
├─ fragment_sms_import.xml ← Main screen
├─ dialog_sms_import.xml ← Review dialog
├─ fragment_sms_review.xml ← Alternative screen
└─ ...
```

---

**Quick Navigation:**
- 🎯 **Want to review SMS?** → Open `SmsImportFragment.java`
- 🔧 **Want to modify review logic?** → Edit `SmsImportViewModel.java`
- 💾 **Want to change database queries?** → Edit `SmsImportDao.java`
- 🎨 **Want to change dialog UI?** → Edit `dialog_sms_import.xml`
- 📋 **Want to see all pending?** → Check `pendingSmsImports` LiveData

