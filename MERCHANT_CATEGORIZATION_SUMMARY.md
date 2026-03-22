# Merchant ID & Auto-Categorization Implementation

## Overview
Implemented complete merchant detection, display, and auto-categorization workflow for SMS-imported transactions.

---

## Features Implemented

### 1. **Merchant Extraction from SMS** ✅
**File**: `SmsParser.java`
- Enhanced `extractMerchant()` with 5 regex patterns for real-world SMS formats:
  - **UPI/VPA patterns**: `UPI/merchant@bank`, `VPA: xyz@upi`
  - **Payment patterns**: `paid to`, `payment to`, `transferred to`, `sent to`
  - **POS swipe**: `at Merchant`, `towards Merchant`
  - **Labeled patterns**: `Merchant:`, `Shop:`, `MCC:`
  - **Credit patterns**: `from Merchant`

### 2. **Merchant Storage in Database** ✅
**Files**: `SmsImport.java`, `AppDatabase.java`
- Added `merchantName` field to `SmsImport` entity
- Database version bumped: **6 → 7**
- Merchant name is now stored with every SMS import

### 3. **SMS Review Screen - Merchant Display** ✅
**Files**: `item_sms_review.xml`, `SmsReviewAdapter.java`
- **Merchant TextView** (line 64-71 in layout)
  - Shows only when merchant name is extracted
  - Hidden by default, visible on-demand
- **Display Logic**:
  ```java
  if (item.merchantName != null && !item.merchantName.isEmpty()) {
      holder.tvMerchant.setVisibility(View.VISIBLE);
      holder.tvMerchant.setText("Merchant: " + item.merchantName);
  }
  ```

### 4. **Category Selection in Review Screen** ✅
**Files**: `item_sms_review.xml`, `SmsReviewAdapter.java`, `SmsReviewFragment.java`, `SmsReviewViewModel.java`
- **Category Spinner** added below Account Spinner
- **Features**:
  - "-- Select Category --" default option
  - All active categories loaded from database
  - **Pre-selection**: if merchant's category is known, automatically selects it
  - User can override if needed

### 5. **Auto-Categorization Workflow** ✅
**File**: `SmsReceiver.java`

When SMS arrives:
1. Extract merchant name from SMS text
2. Look up merchant in DB:
   - If found with `categoryId` → auto-assign that category
3. Check for auto-confirm conditions:
   - If **account matched** AND **category auto-assigned** → **auto-confirm + create transaction silently**
   - Otherwise → **stay PENDING** for user review with pre-filled merchant & category

**Logging examples**:
```
Auto-categorized via known merchant 'AMAZON PAY' → categoryId: cat-001
SMS auto-confirmed and converted to transaction. Merchant: AMAZON PAY, Account: acc-123, Category: cat-001
SMS import PENDING - Account: acc-456, Merchant: STARBUCKS, Category: not known
```

### 6. **Merchant-Category Mapping (Learning)** ✅
**Files**: `MerchantRepository.java`, `SmsReviewViewModel.java`

When user confirms a pending SMS:
1. Transaction is created
2. **Merchant-category mapping is saved**: 
   - If merchant exists in DB → update its `categoryId`
   - If merchant doesn't exist → create new merchant record with `categoryId`
3. **Future SMS** from same merchant → automatically categorized

Example flow:
- Day 1: User reviews "Starbucks" SMS → selects "Coffee" category → merchant saved
- Day 2: New "Starbucks" SMS arrives → auto-categorized to "Coffee" → auto-confirmed → transaction created

---

## Data Flow Diagram

```
SMS Received
    ↓
[SmsParser] Extract merchant name
    ↓
[SmsReceiver] 
  ├─ Match account (via last 4 digits)
  ├─ Look up merchant in DB
  │   └─ Found with categoryId → use it
  │   └─ Not found → stay null
  ├─ Check: account matched + category known?
  │   ├─ YES → auto-confirm → create transaction silently
  │   └─ NO → PENDING → notify user
    ↓
[SmsReviewScreen]
  ├─ Display merchant name
  ├─ Show pre-selected category (if known)
  └─ User can override & confirm
    ↓
[SmsReviewViewModel]
  ├─ Create transaction
  └─ Save merchant → category mapping
    ↓
[MerchantRepository]
  └─ Update/Insert merchant with categoryId
    ↓
Future SMS from same merchant → Auto-categorized ✅
```

---

## Database Schema Changes

### SmsImport Table
```sql
CREATE TABLE sms_import (
    uuid TEXT PRIMARY KEY,
    smsText TEXT,
    amount REAL,
    detectedType TEXT,
    date INTEGER,
    accountId TEXT,
    categoryId TEXT,
    merchantName TEXT,        -- ← NEW FIELD
    status TEXT,
    createdAt INTEGER,
    updatedAt INTEGER,
    deleted INTEGER
);
```

### Merchants Table (Unchanged, Enhanced Use)
```sql
CREATE TABLE merchants (
    uuid TEXT PRIMARY KEY,
    name TEXT UNIQUE,
    categoryId TEXT,          -- ← Used for auto-categorization
    createdAt INTEGER,
    updatedAt INTEGER,
    deleted INTEGER
);
```

---

## Files Modified

| File | Changes |
|------|---------|
| `SmsImport.java` | Added `merchantName` field |
| `AppDatabase.java` | Version 6 → 7 |
| `SmsParser.java` | Enhanced `extractMerchant()` with 5 patterns |
| `SmsReceiver.java` | Look up & auto-assign merchant category, auto-confirm logic |
| `MerchantRepository.java` | Added `saveMerchantCategorySync()` |
| `SmsReviewViewModel.java` | Added categories LiveData, save merchant mapping on confirm |
| `SmsReviewFragment.java` | Observe categories, pass to adapter |
| `SmsReviewAdapter.java` | Display merchant name, category spinner, pre-selection |
| `item_sms_review.xml` | Added merchant TextView + category spinner |

---

## Testing Checklist

- [ ] Receive SMS → merchant extracted correctly
- [ ] Review screen shows merchant name (when extracted)
- [ ] Category spinner shows all categories
- [ ] Unknown merchant → category pre-selected to "Select Category"
- [ ] Known merchant → category pre-selected automatically
- [ ] Confirm pending SMS → merchant-category mapping saved
- [ ] Next SMS from same merchant → auto-categorized & auto-confirmed
- [ ] Build succeeds: `./gradlew clean assembleDebug`

---

## Build Status

✅ **BUILD SUCCESSFUL** (clean build on March 20, 2026)

All changes compile without errors. Database migration uses `fallbackToDestructiveMigration()`.

