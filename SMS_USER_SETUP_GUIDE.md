# SMS Transaction Auto-Import Setup Guide

## Feature Overview

The FinanceTracker app can automatically read incoming SMS messages from your bank and convert them into financial transactions. This feature:

✅ **Automatically detects** bank transaction messages  
✅ **Extracts amounts** from SMS text  
✅ **Matches accounts** using last 4 digits  
✅ **Shows notifications** when new transactions arrive  
✅ **Lets you review** before recording in the app  
✅ **Supports categories** for expense/income tracking  

## Prerequisites

- FinanceTracker app installed and updated
- Android 6.0 or higher
- Bank account(s) added in the app
- SMS permission granted to the app

## Step-by-Step Setup

### Step 1: Add Account Numbers to Your Bank Accounts

1. Open FinanceTracker app
2. Go to **Accounts** tab
3. Tap on each bank account to edit
4. Look for field: **"Account Number (last 4 digits)"**
5. Enter the last 4 digits shown in your bank SMS messages
   - Example: If SMS says "A/C •••1234", enter **1234**
   - Example: If SMS says "ACCOUNT XXXX5678", enter **5678**
6. Tap **Save**

**Why?** This allows the app to automatically match incoming SMS with the correct account.

### Step 2: Grant Permissions

When you first open SMS Import feature, you'll see permission requests:

**Required Permissions:**
- ✅ **SMS** - Allows app to read transaction messages
- ✅ **Notifications** - Alerts you about new transactions

**Tap "Allow" for both permissions**

To manually grant permissions:
1. Open phone **Settings**
2. Go to **Apps** → **FinanceTracker**
3. Tap **Permissions**
4. Grant **SMS** and **Notification** permissions

### Step 3: Create Categories (Optional but Recommended)

Categories help organize your expenses and income:

1. Go to **Categories** in FinanceTracker
2. Add categories like:
   - **EXPENSE**: Groceries, Shopping, Bills, Fuel, etc.
   - **INCOME**: Salary, Bonus, Refund, etc.
3. You'll select these when reviewing SMS imports

## How It Works - Step by Step

### When You Receive a Bank SMS

```
You receive SMS from bank:
"Your A/C •••1234 has been debited with Rs.500 on 14 Mar"
        ↓
App automatically:
- Detects it's a transaction
- Extracts amount: Rs.500
- Detects type: EXPENSE
- Extracts account: 1234
- Matches with your account
        ↓
Shows notification:
"SMS Transaction Detected - 1 pending review"
        ↓
You tap notification
        ↓
App shows review screen
```

### Review Screen - What You See

When you tap the notification or open **SMS Imports**, you see:

**Transaction Details:**
- Amount: Rs. 500
- Type: EXPENSE
- SMS Text: Full message for reference

**Your Selections:**
- Account: Auto-filled if matched, or select manually
- Category: Select from available categories (optional)

**Actions:**
- **Confirm** - Adds transaction to your records
- **Ignore** - Skips this SMS (won't appear again)
- **Cancel** - Leave as pending (review later)

### After Confirmation

- ✅ Transaction added to your account
- ✅ Amount updated in dashboard
- ✅ Transaction appears in account details
- ✅ SMS marked as processed

## Common Scenarios

### Scenario 1: Account Auto-Matched ✅

```
SMS: "A/C •••1234 debited Rs.500"
Account in app has last4: "1234"
    ↓
Account auto-filled in review
You only need to select category
Tap Confirm
```

### Scenario 2: Account Not Matched ⚠️

```
SMS: "Your account debited Rs.500"
(No account number in SMS)
    ↓
accountId field empty in review
You MUST manually select account
Then select category (optional)
Tap Confirm
```

### Scenario 3: No Category Selected ℹ️

```
You see review screen
Category is optional
Leave it blank to skip
Tap Confirm
    ↓
Transaction created without category
You can add category later by editing transaction
```

### Scenario 4: Unknown Bank Format ⚠️

```
SMS has unusual format
App cannot extract amount or type
    ↓
SMS is not imported
No notification shown
    ↓
You can still manually add transaction
```

## Supported SMS Formats

The app recognizes bank SMS with these account number patterns:

| Format | Example | Extracted |
|--------|---------|-----------|
| Masked bullets | A/C •••1234 | 1234 |
| Masked asterisks | A/C ****1234 | 1234 |
| X format | ACCOUNT XXXX5678 | 5678 |
| Direct numbers | A/C 1234 | 1234 |

## Managing SMS Imports

### View Pending Imports
1. Open FinanceTracker
2. Go to **SMS Imports** tab
3. See all pending SMS transactions

### Batch Review
1. Click on each import to review
2. Confirm or Ignore one by one
3. Confirmed ones become transactions
4. Ignored ones disappear

### Clear Notifications
- Notification auto-clears when you confirm/ignore
- Or open app to dismiss notification

## Troubleshooting

### "SMS not being imported"

**Possible causes:**
- [ ] SMS from bank not detected as transaction message
- [ ] Permissions not granted to app
- [ ] Amount or type cannot be extracted from SMS

**Solutions:**
1. Check permissions (Settings → Apps → FinanceTracker)
2. Grant **SMS** permission
3. Try receiving another SMS from your bank
4. If SMS has unusual format, add it manually

### "Account not auto-matching"

**Possible causes:**
- [ ] Account number last 4 digits not configured
- [ ] Account number in SMS different from what you entered
- [ ] Account deleted or not active

**Solutions:**
1. Go to Accounts → Edit your bank account
2. Enter the correct **last 4 digits** from your SMS
3. Make sure account is not marked as deleted
4. Save and test with new SMS

### "Notification not showing"

**Possible causes:**
- [ ] Notification permission not granted
- [ ] Notifications disabled for app
- [ ] Do Not Disturb mode enabled

**Solutions:**
1. Grant **Notification** permission (Settings → Apps → FinanceTracker)
2. Check app notification settings are enabled
3. Turn off Do Not Disturb if enabled
4. Or open app manually to see SMS Imports

### "Cannot confirm import"

**Possible causes:**
- [ ] No account selected
- [ ] Account field is empty

**Solutions:**
1. Tap account field/spinner
2. Select correct account from list
3. Now you can confirm

## Best Practices

### 1. Keep Account Numbers Updated
- Update account number last 4 digits when you get new bank account
- Delete old accounts so SMS doesn't match incorrectly

### 2. Use Meaningful Categories
- Create categories that match your spending patterns
- Use consistent category names across transaction types

### 3. Review Regularly
- Check SMS Imports tab daily if you receive many SMS
- Confirm or ignore imports promptly
- Don't let pending list get too large

### 4. Verify First Import
- When setting up, test with first SMS
- Confirm account is correctly matched
- Adjust account numbers if needed

### 5. Double-Check Large Amounts
- Always review large transaction SMS
- Make sure amount is correctly extracted
- Verify account is correct before confirming

## Privacy & Security

✅ **Your SMS is:**
- Stored only on your device
- Synced with your Google Drive (if enabled)
- Never sent to external servers
- Only last 4 digits used for matching

✅ **Your data:**
- Encrypted in transit (HTTPS)
- Stays in your account
- Can be deleted anytime
- Not shared with third parties

## FAQ

**Q: Do I need to grant SMS permission?**  
A: Yes, the app needs to read incoming SMS to detect transactions.

**Q: Can I review SMS before confirming?**  
A: Yes! Every SMS import shows a review dialog before being recorded.

**Q: Can I undo a confirmed transaction?**  
A: Yes, go to Transactions tab and delete/edit the transaction.

**Q: What if SMS amount is wrong?**  
A: In review dialog, ignore the SMS. Then manually add correct amount.

**Q: Can I auto-confirm certain SMS?**  
A: Not yet, but it's on the roadmap for future updates.

**Q: Does this work with all banks?**  
A: Works with most banks in India. If your bank's SMS format isn't recognized, you can add manually.

**Q: Can I see the original SMS?**  
A: Yes, it's shown in the review dialog as reference.

**Q: What happens to old SMS records?**  
A: They're kept in app for audit trail. You can see them in transaction details.

## Getting Help

If you encounter issues:

1. **Check the logs** - Open app settings to view logs
2. **Review documentation** - See SMS Implementation Guide for technical details
3. **Test with sample SMS** - Use a test message to verify setup
4. **Verify permissions** - Ensure SMS and Notification permissions are granted
5. **Check account setup** - Ensure account numbers are correctly configured

## Next Steps

1. ✅ Add account numbers to your bank accounts
2. ✅ Grant SMS permission when prompted
3. ✅ Receive a bank SMS (or test)
4. ✅ Tap the notification that appears
5. ✅ Review and confirm the transaction
6. ✅ Watch it appear in your dashboard!

## Feature Roadmap

Coming soon:
- 🔄 Merchant name extraction and matching
- 🤖 Smart category prediction
- ⚡ Auto-confirm rules for trusted transactions
- 🔍 Duplicate transaction detection
- 📊 SMS filtering by sender
- 🔐 Enhanced security controls

Enjoy automatic transaction tracking! 🎉

