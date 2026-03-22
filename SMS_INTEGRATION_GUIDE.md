# Account Number Feature - SMS Integration Guide

## How to Integrate with SMS Import Feature

When you're ready to integrate this with the SMS import feature, use the `SmsAccountNumberExtractor` utility:

### Step 1: In SMS Import Processing

```java
// When processing an SMS transaction
String smsText = "Your A/C XXXX1234 debited Rs.100 on 15-Mar-2026";

// Check if SMS contains account information
if (SmsAccountNumberExtractor.likelyContainsAccountNumber(smsText)) {
    String accountNumber = SmsAccountNumberExtractor.extractLast4Digits(smsText);
    
    if (SmsAccountNumberExtractor.isValidAccountNumber(accountNumber)) {
        // Account number extracted successfully
        // Match to account in database
    }
}
```

### Step 2: Find Matching Account

```java
// In AccountRepository, add this method:
public Account findByAccountNumber(String last4Digits) {
    return accountDao.getByAccountNumber(last4Digits);
}

// In AccountDao, add this query:
@Query("SELECT * FROM accounts WHERE accountNumberLast4 = :last4 AND deleted = 0 LIMIT 1")
Account getByAccountNumber(String last4);
```

### Step 3: Auto-Populate in SMS Import Dialog

```java
// In SMS Import Fragment
String extractedAccountNumber = SmsAccountNumberExtractor.extractLast4Digits(smsText);

// Find matching account
Account matchingAccount = accountRepo.findByAccountNumber(extractedAccountNumber);

if (matchingAccount != null) {
    // Auto-select the matching account in the dropdown
    int position = accountList.indexOf(matchingAccount);
    accountSpinner.setSelection(position);
}
```

### Step 4: Display Account Info

```java
// Show which account was matched
String hint = "Auto-matched to: " + matchingAccount.name + " •••" + matchingAccount.accountNumberLast4;
Toast.makeText(context, hint, Toast.LENGTH_SHORT).show();
```

## Supported SMS Patterns

The extractor handles these SMS patterns:

### Pattern 1: Masked with Bullets
```
"Your A/C •••1234 debited Rs.100"
```
Extracts: `1234`

### Pattern 2: Masked with X's
```
"Account XXXX5678 has been credited"
```
Extracts: `5678`

### Pattern 3: Masked Format (xxxx)
```
"xxxx9012 spent $50 today"
```
Extracts: `9012`

### Pattern 4: Full Account Number
```
"Account 1234567890123456 transfer done"
```
Extracts: `3456` (last 4 digits)

### Pattern 5: International Format
```
"A/C XXXXXX1234 money sent"
```
Extracts: `1234`

## Example SMS Scenarios

### Bank of India
```
"Your A/C XXX1234 has been debited with Rs.100.00 on 15/03/2026"
```
→ Extracts: `1234`
→ Matches: Checking (BANK •••1234)

### HDFC Bank
```
"Your A/C •••5678 credited with Rs.500 Transfer successful."
```
→ Extracts: `5678`
→ Matches: Savings (BANK •••5678)

### Credit Card SMS
```
"Dear Customer, Your Credit Card ending with 9012 has been charged Rs.2,500 on 15-Mar-2026"
```
→ Extracts: `9012`
→ Matches: Credit (CREDIT_CARD •••9012)

### International Transfer
```
"Transfer to ACC XXXXXX7890 completed. Amount: $100. Ref: TXN123"
```
→ Extracts: `7890`
→ Matches: Global (BANK •••7890)

## Integration Code Template

```java
public class SmsImportProcessor {
    
    private AccountRepository accountRepo;
    
    public Account matchAccountFromSms(String smsText) {
        // Step 1: Check if SMS has account info
        if (!SmsAccountNumberExtractor.likelyContainsAccountNumber(smsText)) {
            return null;
        }
        
        // Step 2: Extract account number
        String accountNumber = SmsAccountNumberExtractor.extractLast4Digits(smsText);
        
        // Step 3: Validate
        if (!SmsAccountNumberExtractor.isValidAccountNumber(accountNumber)) {
            return null;
        }
        
        // Step 4: Find matching account
        Account matchedAccount = accountRepo.findByAccountNumber(accountNumber);
        
        return matchedAccount; // May be null if no match found
    }
    
    public void processSmsWithAutoMatch(String smsText, double amount, String type) {
        Account account = matchAccountFromSms(smsText);
        
        if (account != null) {
            // Auto-populate the account
            smsImportData.accountId = account.uuid;
            smsImportData.accountName = account.name + " •••" + account.accountNumberLast4;
        }
    }
}
```

## AccountDao Query to Add

```java
/**
 * Find account by last 4 digits of account number
 * Used to auto-match SMS transactions to accounts
 */
@Query("SELECT * FROM accounts WHERE accountNumberLast4 = :last4 AND deleted = 0 LIMIT 1")
Account getByAccountNumber(String last4);
```

## AccountRepository Method to Add

```java
/**
 * Find account by last 4 digits from SMS
 */
public Account findByAccountNumber(String last4Digits) {
    return accountDao.getByAccountNumber(last4Digits);
}
```

## Handling Edge Cases

### Multiple Accounts with Same Last 4 Digits
If user has multiple accounts ending with same 4 digits:
```java
@Query("SELECT * FROM accounts WHERE accountNumberLast4 = :last4 AND deleted = 0")
List<Account> getByAccountNumber(String last4); // Returns list instead of single

// Then ask user to select the correct one
if (matchingAccounts.size() > 1) {
    showAccountSelectionDialog(matchingAccounts);
}
```

### No Account Found
```java
Account account = accountRepo.findByAccountNumber(extractedNumber);
if (account == null) {
    // Allow user to manually select account
    showAccountSelectionDropdown();
}
```

### Invalid Account Number Format
```java
String extracted = SmsAccountNumberExtractor.extractLast4Digits(smsText);
if (!SmsAccountNumberExtractor.isValidAccountNumber(extracted)) {
    // Log warning and proceed without auto-match
    Log.w("SMS", "Invalid account number extracted: " + extracted);
}
```

## Testing Integration

```java
// Unit test example
@Test
public void testSmsAccountMatching() {
    // Create test account
    Account testAccount = new Account("uuid1", "Checking", "BANK", 5000, "INR");
    testAccount.accountNumberLast4 = "1234";
    accountRepo.insert(testAccount);
    
    // Test SMS with that account number
    String sms = "Your A/C •••1234 debited Rs.100";
    String extracted = SmsAccountNumberExtractor.extractLast4Digits(sms);
    
    assertEquals("1234", extracted);
    
    // Verify matching works
    Account matched = accountRepo.findByAccountNumber(extracted);
    assertNotNull(matched);
    assertEquals("1234", matched.accountNumberLast4);
}
```

## Performance Tips

1. **Cache Account Numbers** - Load all account numbers once and cache them
2. **Index in Database** - Add index to `accountNumberLast4` for faster queries
3. **Batch Processing** - Process multiple SMS texts in parallel
4. **Lazy Loading** - Only extract when needed (not for every SMS)

## Debugging

Enable logging to debug SMS extraction:

```java
String sms = "Your A/C XXXX1234 debited Rs.100";

if (SmsAccountNumberExtractor.likelyContainsAccountNumber(sms)) {
    Log.d("SMS", "SMS contains account info");
}

String extracted = SmsAccountNumberExtractor.extractLast4Digits(sms);
if (extracted != null) {
    Log.d("SMS", "Extracted account number: " + extracted);
} else {
    Log.w("SMS", "Failed to extract account number from: " + sms);
}
```

---

**Ready for SMS Integration** ✅

