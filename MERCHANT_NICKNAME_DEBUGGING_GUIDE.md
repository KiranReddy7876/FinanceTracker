# 🔍 DEBUGGING GUIDE: Merchant NickName Not Showing

The feature is now enhanced with **detailed logging** to help identify the exact issue. 

---

## What Was Added

Updated **TransactionAdapter.java** with comprehensive logging to track:
1. Whether `merchantId` exists on transaction
2. Whether database instance is available
3. Whether merchant lookup succeeds
4. What merchant name/nickName is found
5. Which display priority is being used

---

## How to Debug

### Step 1: Build APK with Logging
```bash
./gradlew assembleDebug
```

### Step 2: Install on Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Watch Logcat Logs
```bash
adb logcat | grep "TransactionAdapter"
```

### Step 4: Open Dashboard/Transactions
When the list appears, you'll see logs like:

```
TransactionAdapter: No merchantId or db for transaction
TransactionAdapter: Found merchant: AMAZON INDIA, nickName: Amazon
TransactionAdapter: Displaying nickName: Amazon
```

---

## Expected Log Output Examples

### ✅ When Working (NickName Should Show)
```
Found merchant: AMAZON INDIA PVT LTD, nickName: Amazon
Displaying nickName: Amazon
```

### ❌ When Merchant Not Linked
```
No merchantId or db for transaction
Displaying note: Your A/C •••1234 debited Rs.500...
```

### ❌ When Merchant Has No NickName
```
Found merchant: AMAZON INDIA, nickName: null
Displaying merchant name: AMAZON INDIA
```

### ❌ When Merchant Not Found
```
Merchant not found for ID: abc-xyz-123
Displaying note: Your A/C debited...
```

### ❌ When Database Lookup Fails
```
Error looking up merchant: Error details...
Displaying note: Your A/C debited...
```

---

## Debugging Checklist

Follow these steps to identify the issue:

### 1. Check if Merchants Exist
- [ ] Open Merchants screen
- [ ] Verify merchants are created
- [ ] Verify at least one merchant has a nickName set

### 2. Check if SMS Imports Link Merchants
- [ ] Receive/simulate SMS
- [ ] Review SMS in SMS Review screen
- [ ] Check if merchant name is extracted
- [ ] Confirm SMS when merchant name appears

### 3. Check Logcat Output
- [ ] Build APK
- [ ] Run with adb logcat
- [ ] Open Dashboard
- [ ] Look for "TransactionAdapter" logs
- [ ] What do the logs say?

**If logs say**: `"No merchantId or db for transaction"`
→ Problem: Transaction isn't linked to merchant

**If logs say**: `"Merchant not found for ID: ..."`
→ Problem: Merchant was deleted or doesn't exist

**If logs say**: `"Found merchant ... nickName: null"`
→ Problem: Merchant exists but nickName not set

**If logs say**: `"Displaying nickName: Amazon"`
→ SUCCESS! Feature is working!

---

## Common Issues & Solutions

### Issue 1: "No merchantId or db for transaction"
**Problem**: Transaction doesn't have merchantId, or database isn't passed

**Solutions**:
1. Check if SMS actually linked a merchant when importing
2. Verify DashboardFragment passes database: 
   ```java
   new TransactionAdapter(listener, AppDatabase.getInstance(context))
   ```
3. Verify TransactionsFragment passes database:
   ```java
   new TransactionAdapter(listener, AppDatabase.getInstance(context))
   ```

### Issue 2: "Merchant not found for ID"
**Problem**: Merchant was deleted or ID is invalid

**Solutions**:
1. Re-create merchant in Merchants screen
2. Re-import SMS that links to the merchant
3. Verify merchant UUID in database

### Issue 3: "Found merchant ... nickName: null"
**Problem**: Merchant exists but nickName not set

**Solutions**:
1. Edit merchant in Merchants screen
2. Set the nickName field (e.g., "Amazon")
3. Save the merchant
4. Restart app

### Issue 4: No logs appearing
**Problem**: Adapter isn't being called, or filtering isn't working

**Solutions**:
```bash
# View all logs
adb logcat

# Filter to see everything
adb logcat *:V | grep TransactionAdapter

# Or use
adb logcat | grep -i nickname
```

---

## Step-by-Step Testing

### Test Scenario 1: Create Merchant with NickName
1. Open Merchants screen
2. Create merchant: "AMAZON INDIA PVT LTD"
3. Set nickName: "Amazon"
4. Save

### Test Scenario 2: Create Transaction Linked to Merchant
1. Open Transactions
2. Add new transaction
3. Select merchant: "AMAZON INDIA PVT LTD"
4. Fill other details and save

### Test Scenario 3: Check Display
1. Go back to Dashboard or Transactions
2. Look for the transaction
3. Should display: "Amazon" (the nickName)
4. Check logcat for: `"Displaying nickName: Amazon"`

### Test Scenario 4: Check Detail View
1. Tap the transaction
2. Open detail view
3. Note field should show full context
4. Merchant should link correctly

---

## Database Inspection

If logs show merchant not found, check database:

```bash
# Open database shell
adb shell

# Connect to database
sqlite3 /data/data/com.financetracker/databases/finance_tracker.db

# Check merchants
SELECT uuid, name, nickName FROM merchants LIMIT 10;

# Check transaction merchant links
SELECT uuid, merchantId, note FROM transactions LIMIT 10;

# Match IDs
SELECT m.name, m.nickName FROM merchants m 
WHERE m.uuid IN (SELECT merchantId FROM transactions WHERE merchantId IS NOT NULL);
```

---

## Expected Behavior

### ✅ Working Correctly
- Dashboard shows merchant nickNames
- Transactions screen shows merchant nickNames
- Detail view shows full SMS text in note field
- Logcat shows `"Displaying nickName: ..."` messages

### ❌ Not Working
- Dashboard shows SMS text (not nickName)
- Logcat shows `"No merchantId or db"` or `"Merchant not found"`
- Check logs to identify which step is failing

---

## Next Steps

1. **Build & Deploy** the updated APK
2. **Monitor Logcat** while using the app
3. **Check Logs** against the examples above
4. **Identify** where the issue is
5. **Report** the specific log message

---

## Log Message Reference

| Log Message | Meaning | Action |
|---|---|---|
| `"No merchantId or db"` | No merchant linked | Link merchant to transaction |
| `"Found merchant ... nickName: ..."` | Merchant found | Check if nickName is null |
| `"Displaying nickName: ..."` | ✅ Working | Feature is working! |
| `"Displaying merchant name: ..."` | No nickName set | Edit merchant, set nickName |
| `"Merchant not found"` | ID doesn't match | Re-link merchant |
| `"Error looking up merchant"` | Database error | Check database connection |
| `"Displaying note:"` | Fallback (no merchant) | Link transaction to merchant |

---

**Build Status**: ✅ SUCCESS
**Logging Added**: ✅ YES
**Ready to Debug**: ✅ YES

Build the APK, run it, and check logcat logs to identify the exact issue!


