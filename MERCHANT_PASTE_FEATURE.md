# Editable Merchant Field - Manual Paste Feature

## Overview
Implemented editable merchant field that allows users to select text from SMS and paste merchant name into the field.

---

## Features

### 1. **Editable Merchant Field** ✅
**File**: `dialog_sms_import.xml`

Changed to `TextInputEditText`:
```xml
<com.google.android.material.textfield.TextInputEditText
    android:id="@+id/et_merchant"
    android:layout_width="match_parent"
    android:layout_height="44dp"
    android:inputType="text"
    android:hint="Merchant name (optional)"
    android:padding="8dp"
    android:background="@drawable/outlined_bg"/>
```

**Features:**
- Editable text field
- Pre-filled with auto-extracted merchant (if available)
- User can manually select SMS text and paste
- Users can type/edit merchant name freely
- Optional field (hint text "Merchant name (optional)")

### 2. **Auto-Fill Pre-Extraction** ✅
**File**: `SmsImportFragment.java`

```java
// Pre-fill merchant if extracted, otherwise leave empty for user to paste
if (smsImport.merchantName != null && !smsImport.merchantName.isEmpty()) {
    etMerchant.setText(smsImport.merchantName);
}
```

- If merchant was auto-extracted → field populated
- If no merchant detected → field empty, ready for paste

### 3. **Save Edited Merchant on Confirm** ✅
**Files**: `SmsImportFragment.java`, `SmsImportViewModel.java`, `SmsImportRepository.java`, `SmsImportDao.java`

```java
// Save edited merchant if user changed it
String editedMerchant = etMerchant.getText().toString().trim();
if (!editedMerchant.isEmpty() && !editedMerchant.equals(smsImport.merchantName)) {
    viewModel.updateMerchant(smsImport.uuid, editedMerchant);
}
```

- Only saves if user changed the merchant
- Updates database before creating transaction
- Edited merchant enables auto-categorization for future SMS

---

## User Workflow

### Scenario 1: Auto-Extracted Merchant
```
1. SMS arrives: "Cr. to gpay-12190167465@okbizaxis"
2. Merchant auto-extracted: "gpay-12190167465@okbizaxis"
3. Dialog opens
4. Merchant field pre-filled: "gpay-12190167465@okbizaxis" ✅
5. User accepts (or edits if needed)
6. User clicks Confirm
7. Transaction created with merchant
```

### Scenario 2: No Merchant Detected - User Pastes
```
1. SMS arrives: "Debit of Rs.500 for service charge"
2. Merchant NOT detected: empty
3. Dialog opens
4. Merchant field empty
5. User:
   a. Selects text from SMS: "service charge"
   b. Long-press → Copy
   c. Long-press merchant field → Paste ✅
6. Field populated: "service charge"
7. User clicks Confirm
8. Transaction created with merchant
```

### Scenario 3: User Manually Types
```
1. SMS arrives with unclear merchant
2. Dialog opens
3. Merchant field empty
4. User types: "Starbucks" ✅
5. User clicks Confirm
6. Transaction created with "Starbucks" as merchant
```

---

## Dialog Layout

```
Review SMS Import
├─ Amount: ₹35.00 | Type: EXPENSE
├─ SMS: [full SMS text]
├─ Merchant: [Editable text field]  ← User can paste/type here
├─ Select Account: [dropdown]
├─ Select Category: [dropdown]
└─ [IGNORE] [DELETE] [CONFIRM]
```

---

## Files Modified

| File | Changes |
|------|---------|
| `dialog_sms_import.xml` | Changed merchant to `TextInputEditText` |
| `SmsImportFragment.java` | Added `TextInputEditText` import, updated display logic, save edited merchant |
| `SmsImportViewModel.java` | `updateMerchant()` method |
| `SmsImportRepository.java` | `updateMerchant()` method |
| `SmsImportDao.java` | `updateMerchant()` SQL query |

---

## Build Status

✅ **BUILD SUCCESSFUL** - Ready to use!

---

## Benefits

✅ **No "Copy" Button** - Clean, minimal UI  
✅ **Manual Selection** - User selects exactly what they want  
✅ **Standard Copy-Paste** - Works like any Android text field  
✅ **Pre-Filled When Possible** - Auto-extraction still works  
✅ **Flexible** - Type or paste, user's choice  
✅ **Persistent** - Edited merchant saved to database  
✅ **Auto-Categorization** - Edited merchant triggers merchant-category mapping  

---

## User Instructions

### To Paste Merchant from SMS:
1. **Read the SMS** displayed in the dialog
2. **Select the merchant name** from the SMS text
3. **Copy** it (long-press → Copy)
4. **Tap the Merchant field**
5. **Paste** (long-press → Paste)
6. **Click Confirm**

Or simply:
- **Type** the merchant name manually if you know it

---

## Integration with Auto-Categorization

When user pastes/enters merchant and confirms:
1. Edited merchant saved to `SmsImport.merchantName`
2. Category selected and confirmed
3. `MerchantRepository.saveMerchantCategorySync()` creates/updates merchant record
4. **Future SMS from same merchant** → Auto-categorized + Auto-confirmed

Example:
- Day 1: User pastes "Starbucks" → Category "Coffee" → Confirm
- Day 2: SMS arrives with "Starbucks"
- Day 2: Auto-categorized to "Coffee" → Auto-confirmed → Transaction created ✅

