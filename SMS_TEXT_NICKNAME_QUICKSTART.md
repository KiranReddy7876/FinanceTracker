# SMS Text & Merchant NickName Feature - Quick Reference

## What's New ✨

### Feature 1: SMS Text Stored in Transaction Note
When you import a transaction from SMS, the **raw SMS message** is now saved in the transaction's note field.

**Example**:
- SMS: "Your A/C •••1234 debited Rs.500 to AMAZON INDIA PVT LTD on 14-Mar-2024"
- Transaction Note: "Your A/C •••1234 debited Rs.500 to AMAZON INDIA PVT LTD on 14-Mar-2024"

**Where it appears**:
- Recent Transactions list (shows the SMS text)
- Transaction detail view (full audit trail)

---

### Feature 2: Merchant NickName Display
If a merchant has a **nickName** set, it appears in the transaction list as a fallback.

**Example**:
- Merchant name: "AMAZON INDIA PVT LTD"
- Merchant nickName: "Amazon"
- Transaction without custom note → Shows "Amazon"

**Where it appears**:
- Recent Transactions list (when no note is set)
- Takes priority over full merchant name

---

## How SMS Imports Work Now

```
SMS Received
    ↓
Parsed (amount, merchant, date extracted)
    ↓
User Reviews in SMS Review Screen
    ↓
User Confirms + Selects Account & Category
    ↓
Transaction Created with:
  - note = FULL SMS TEXT ← NEW!
  - merchantId = Linked merchant
  - amount, date, type, etc.
    ↓
Recent Transactions List Shows:
  Priority: SMS Text → NickName → Name → Type
```

---

## Recent Transactions Display Priority

**Display Text** is determined by this priority order:

1. **SMS Text** (Transaction note)
   - If available, this is shown first
   - Users see the original SMS message

2. **Merchant NickName**
   - If no note and merchant exists with nickName
   - Quick identification without opening transaction

3. **Merchant Name**
   - If no note and no nickName
   - Full merchant name

4. **Transaction Type**
   - If no note and no merchant
   - Shows: EXPENSE, INCOME, or TRANSFER

5. **"Unknown"**
   - Last resort (should rarely happen)

---

## Examples in Recent Transactions

### Example 1: SMS Import
```
Transaction Date: 14 Mar
Display Text: "Your A/C •••1234 debited Rs.500 to AMAZON INDIA..."
Amount: Rs. -500
Type: EXPENSE
```

### Example 2: Manual Transaction with Merchant NickName
```
Transaction Date: 15 Mar
Display Text: "Amazon"  (merchant nickName)
Amount: Rs. -299
Type: EXPENSE
```

### Example 3: Manual Transaction with Custom Note
```
Transaction Date: 16 Mar
Display Text: "Coffee with client - client discussion"
Amount: Rs. -150
Type: EXPENSE
```

### Example 4: Transfer without Note
```
Transaction Date: 17 Mar
Display Text: "TRANSFER"  (type fallback)
Amount: Rs. -5,000
Type: TRANSFER
```

---

## For Users

### To Set Merchant NickName:
1. Go to Merchants Management
2. Find or create merchant (e.g., "AMAZON INDIA PVT LTD")
3. Edit merchant → Set NickName (e.g., "Amazon")
4. Save

### To View Full SMS Text:
1. Tap any transaction in Recent Transactions
2. View full transaction details
3. See the complete SMS text in the Note field

---

## For Developers

### Files Changed:
1. **SmsImportConversionService.java** - Line 96: `transaction.note = smsImport.smsText;`
2. **SmsReviewViewModel.java** - Line 65: `t.note = smsImport.smsText;`
3. **TransactionAdapter.java** - Lines 60-98: New display priority logic

### Key Classes:
- **Transaction**: `note` field holds SMS text or custom notes
- **Merchant**: `nickName` field for quick identification
- **SmsImport**: `smsText` field contains raw SMS message

### Build Status:
✅ All files compile successfully
✅ No database changes needed
✅ Backward compatible

---

## Testing Scenarios

| Scenario | Expected Behavior |
|----------|-------------------|
| Import SMS with merchant | Shows SMS text in transaction |
| Merchant with nickName, transaction has note | Shows note |
| Merchant with nickName, no transaction note | Shows nickName |
| Manual transaction with custom note | Shows custom note |
| No note, no merchant | Shows transaction type (EXPENSE/INCOME/TRANSFER) |

---

## FAQ

**Q: Will my existing transactions change?**
A: No, existing transactions keep their current notes. Only new SMS imports will have the SMS text.

**Q: Can I edit the note after import?**
A: Yes, you can tap the transaction and edit the note if needed.

**Q: What if I don't want SMS text as note?**
A: You can edit the transaction and change the note to something else.

**Q: Does this affect manually created transactions?**
A: No, manual transactions work the same. You can add custom notes or leave them empty.

**Q: How is this different from before?**
A: Before, SMS notes showed "SMS Import - Merchant Name". Now they show the actual SMS text for full context.


