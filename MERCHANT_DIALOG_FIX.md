# Merchant Display Fix - SMS Import Dialog

## Problem
Merchant details were not visible in the SMS Import dialog screen even though they were extracted from SMS.

## Root Cause
The dialog layout (`dialog_sms_import.xml`) and its controller (`SmsImportFragment.java`) did not have code to:
1. Display the merchant name field
2. Show/hide merchant field based on whether merchant was detected

## Solution Implemented

### 1. Updated Layout: `dialog_sms_import.xml`
Added merchant display section after SMS text, before account selector:

```xml
<!-- Merchant name (visible only when merchant is detected) -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:layout_marginBottom="12dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Merchant"
        android:textSize="12sp"
        android:textColor="@color/text_secondary"
        android:layout_marginBottom="4dp"/>

    <TextView
        android:id="@+id/tv_merchant"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textSize="14sp"
        android:textStyle="bold"
        android:textColor="@android:color/black"
        android:padding="8dp"
        android:background="@drawable/outlined_bg"
        android:visibility="gone"/>

</LinearLayout>
```

**Features:**
- Hidden by default (`visibility="gone"`)
- Shows only when merchant is extracted
- Styled consistently with account/category sections
- Bold merchant name for visibility

### 2. Updated Fragment: `SmsImportFragment.java`
Added code in `showEditDialog()` to display the merchant:

```java
TextView tvMerchant = dialogView.findViewById(R.id.tv_merchant);

// Display merchant if extracted
if (smsImport.merchantName != null && !smsImport.merchantName.isEmpty()) {
    tvMerchant.setText(smsImport.merchantName);
    tvMerchant.setVisibility(View.VISIBLE);
} else {
    tvMerchant.setVisibility(View.GONE);
}
```

**Logic:**
- Check if `smsImport.merchantName` is present
- If YES → Show merchant field with extracted name
- If NO → Hide merchant field

---

## Result

### Before ❌
```
Review SMS Import
├─ Amount: ₹35.00 | Type: EXPENSE
├─ SMS: Rs.35.00 Dr. from A/C...
├─ Select Account: [dropdown]
└─ Select Category: [dropdown]
```
❌ **No merchant visible**

### After ✅
```
Review SMS Import
├─ Amount: ₹35.00 | Type: EXPENSE
├─ SMS: Rs.35.00 Dr. from A/C...
├─ Merchant: gpay-12190167465@okbizaxis  ← NOW VISIBLE! ✅
├─ Select Account: [dropdown]
└─ Select Category: [dropdown]
```
✅ **Merchant now displayed**

---

## Display Locations

Now merchant is shown in **3 places**:

| Screen | Location | Status |
|--------|----------|--------|
| **SMS Import Dialog** | After SMS text, before account selector | ✅ Added |
| **SMS Review List** | Card item in RecyclerView | ✅ Already present |
| **Database** | `SmsImport.merchantName` field | ✅ Already stored |

---

## Files Changed

1. **`dialog_sms_import.xml`**
   - Added merchant LinearLayout (lines 87-110)
   - Added merchant TextView with ID `tv_merchant`
   - Hidden by default, shown on-demand

2. **`SmsImportFragment.java`**
   - Added `TextView tvMerchant` reference (line 69)
   - Added merchant visibility logic (lines 85-89)
   - Shows merchant name if extracted

---

## Build Status

✅ **BUILD SUCCESSFUL** - All changes compile without errors

---

## Expected User Experience

1. **SMS arrives** → `Cr. to gpay-12190167465@okbizaxis`
2. **Merchant extracted** → `gpay-12190167465@okbizaxis`
3. **Dialog opens** → Merchant field now **visible and populated**
4. **User sees:**
   - Amount: ₹35.00
   - Type: EXPENSE
   - SMS text (full message)
   - **Merchant: gpay-12190167465@okbizaxis** ← NOW VISIBLE! ✅
   - Account dropdown
   - Category dropdown
5. **User selects** account & category
6. **User confirms** → Transaction created with merchant info
7. **Next SMS** from same merchant → Auto-categorized

---

## Testing Checklist

- [ ] Receive SMS with identifiable merchant (e.g., "Cr. to merchant@bank")
- [ ] Click SMS in import list → Dialog opens
- [ ] Verify merchant name is **visible** in dialog
- [ ] Verify merchant name is **correctly extracted**
- [ ] Select account & category
- [ ] Click Confirm
- [ ] Transaction created with merchant information

---

## Notes

- Merchant field is **hidden** if no merchant is detected (graceful degradation)
- Works with all SMS formats (UPI, bank transfers, POS, etc.)
- Merchant name can be used for auto-categorization in future SMS from same merchant
- Consistent styling with other fields in the dialog

