# SMS Transaction Auto-Import - Code Snippets & Reference

## Quick Code Reference

### 1. How to Request SMS Permissions at Runtime

```java
// In MainActivity.java or any Activity
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Request SMS permissions if not granted
        if (ContextCompat.checkSelfPermission(this, 
                Manifest.permission.RECEIVE_SMS) != 
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.POST_NOTIFICATIONS
                },
                SMS_PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, 
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("SMS", "Permission granted: " + permissions[i]);
                } else {
                    Log.d("SMS", "Permission denied: " + permissions[i]);
                }
            }
        }
    }
}
```

### 2. How to Add Account Number to Account Entity

```java
// In Account.java - Already implemented, but here's the usage
public class Account {
    @PrimaryKey
    @NonNull
    public String uuid;
    
    public String name;
    public String type;                // CASH, BANK, CREDIT_CARD, WALLET
    public double openingBalance;
    public String currency;
    public String accountNumberLast4;  // ← Add this in settings
    public long createdAt;
    public long updatedAt;
    public boolean deleted;
}

// Creating an account with last 4 digits:
Account account = new Account();
account.uuid = UUID.randomUUID().toString();
account.name = "HDFC Bank";
account.type = "BANK";
account.openingBalance = 50000;
account.currency = "INR";
account.accountNumberLast4 = "1234";  // ← User enters this in UI
account.createdAt = System.currentTimeMillis();
account.updatedAt = System.currentTimeMillis();
account.deleted = false;

// Save to database
accountRepository.insert(account, null);
```

### 3. How to Process a Confirmed SMS Import

```java
// In SmsImportRepository.java - Already implemented
public void confirm(String uuid) {
    executor.execute(() -> {
        // Step 1: Mark as CONFIRMED
        smsImportDao.updateStatus(uuid, "CONFIRMED", System.currentTimeMillis());
        
        // Step 2: Get the SMS import
        SmsImport smsImport = smsImportDao.getById(uuid);
        
        // Step 3: Convert to transaction
        if (smsImport != null) {
            SmsImportConversionService.convertToTransaction(context, smsImport);
        }
    });
}
```

### 4. How to Create a Transaction from SMS Import

```java
// In SmsImportConversionService.java - Already implemented
public static void convertToTransaction(Context context, SmsImport smsImport) {
    // Validate
    if (smsImport == null || !smsImport.status.equals("CONFIRMED")) {
        Log.w(TAG, "Cannot convert non-confirmed SMS import");
        return;
    }
    
    if (smsImport.accountId == null) {
        Log.w(TAG, "SMS import missing accountId");
        return;
    }
    
    // Get DAO
    AppDatabase db = AppDatabase.getInstance(context);
    TransactionDao transactionDao = db.transactionDao();
    
    // Create transaction
    Transaction transaction = new Transaction();
    transaction.uuid = UUID.randomUUID().toString();
    transaction.accountId = smsImport.accountId;           // From SMS import
    transaction.type = smsImport.detectedType;            // EXPENSE or INCOME
    transaction.amount = smsImport.amount;                // Extracted from SMS
    transaction.date = smsImport.date;                    // From SMS
    transaction.categoryId = smsImport.categoryId;        // User selection (can be null)
    transaction.referenceId = smsImport.uuid;            // Link back to SMS
    transaction.note = "Auto-imported from SMS";
    transaction.createdAt = System.currentTimeMillis();
    transaction.updatedAt = System.currentTimeMillis();
    transaction.deleted = false;
    
    // Save to database
    try {
        transactionDao.insert(transaction);
        Log.d(TAG, "Successfully converted SMS to transaction");
    } catch (Exception e) {
        Log.e(TAG, "Failed to convert SMS", e);
    }
}
```

### 5. How to Show Notification on SMS Arrival

```java
// In SmsImportNotificationService.java - Already implemented
public static void notifyPendingImport(Context context, int count) {
    // Create notification channel (Android 13+)
    createNotificationChannel(context);
    
    // Create intent to open app
    Intent intent = new Intent(context, MainActivity.class);
    intent.putExtra("navigate_to", "sms_import");
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    
    PendingIntent pendingIntent = PendingIntent.getActivity(
        context, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );
    
    // Build notification
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("SMS Transaction Detected")
        .setContentText(count + " transaction imports pending review")
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH);
    
    // Show notification
    NotificationManager manager = context.getSystemService(NotificationManager.class);
    if (manager != null) {
        manager.notify(1001, builder.build());
    }
}
```

### 6. How to Extract Account Number from SMS

```java
// In SmsAccountNumberExtractor.java - Already implemented
public static String extractLast4Digits(String smsText) {
    if (smsText == null || smsText.isEmpty()) {
        return null;
    }
    
    // Pattern 1: Masked format •••1234 or ****1234
    Pattern maskedPattern = Pattern.compile("[•*]{3,4}(\\d{4})");
    Matcher maskedMatcher = maskedPattern.matcher(smsText);
    if (maskedMatcher.find()) {
        return maskedMatcher.group(1);
    }
    
    // Pattern 2: A/C format
    Pattern accountPattern = Pattern.compile(
        "(?:A/C|ACCOUNT|ACC|ACCT)[\\s.:]*[X•]*[X•]*[X•]*[X•]?(\\d{4})",
        Pattern.CASE_INSENSITIVE
    );
    Matcher accountMatcher = accountPattern.matcher(smsText);
    if (accountMatcher.find()) {
        return accountMatcher.group(1);
    }
    
    // Pattern 3: xxxx format
    Pattern xxxPattern = Pattern.compile("[Xx]{4}(\\d{4})");
    Matcher xxxMatcher = xxxPattern.matcher(smsText);
    if (xxxMatcher.find()) {
        return xxxMatcher.group(1);
    }
    
    return null;
}

// Usage:
String sms = "Your A/C •••1234 debited Rs. 500";
String last4 = SmsAccountNumberExtractor.extractLast4Digits(sms);
// Result: "1234"
```

### 7. How to Parse SMS Transaction Details

```java
// In SmsParser.java - Already implemented
public static ParsedTransaction parse(String body) {
    if (body == null) return null;
    
    // Extract amount using regex pattern
    Pattern amountPattern = Pattern.compile(
        "(?:Rs\\.?|INR|USD|₹|EUR)\\s*([\\d,]+(?:\\.\\d{1,2})?)",
        Pattern.CASE_INSENSITIVE
    );
    Matcher amountMatcher = amountPattern.matcher(body);
    if (!amountMatcher.find()) return null;
    
    String amountStr = amountMatcher.group(1).replace(",", "");
    double amount = Double.parseDouble(amountStr);
    
    // Detect transaction type
    String type = detectType(body); // EXPENSE or INCOME
    
    // Extract date
    long date = extractDate(body);
    
    // Extract merchant
    String merchant = extractMerchant(body);
    
    // Return parsed result
    ParsedTransaction result = new ParsedTransaction();
    result.amount = amount;
    result.type = type;
    result.date = date > 0 ? date : System.currentTimeMillis();
    result.merchant = merchant;
    result.rawText = body;
    return result;
}

// Usage:
String sms = "Your A/C •••1234 debited Rs.500 on 14-Mar-2024";
SmsParser.ParsedTransaction parsed = SmsParser.parse(sms);
// Result: amount=500, type=EXPENSE, date=<timestamp>, merchant=null
```

### 8. How to Filter Categories by Type

```java
// In SmsImportViewModel.java - Already implemented
public LiveData<List<Category>> getCategoriesByType(String type) {
    return categoryRepo.getByType(type);
}

// Usage in SmsImportFragment:
viewModel.getCategoriesByType("EXPENSE").observe(getViewLifecycleOwner(), categories -> {
    if (categories != null) {
        // Show only EXPENSE categories in spinner
        List<String> names = new ArrayList<>();
        names.add("— No Category —");
        for (Category c : categories) {
            names.add(c.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, names);
        spinnerCategory.setAdapter(adapter);
    }
});
```

### 9. How to Match Account by Last 4 Digits

```java
// In AccountRepository.java - Already implemented
public Account findByAccountNumber(String last4Digits) {
    return accountDao.getByAccountNumber(last4Digits);
}

// In AccountDao.java - Already implemented
@Query("SELECT * FROM accounts WHERE accountNumberLast4 = :last4 AND deleted = 0 LIMIT 1")
Account getByAccountNumber(String last4);

// Usage in SmsReceiver:
AccountRepository accountRepo = new AccountRepository(context);
Account matchedAccount = accountRepo.findByAccountNumber("1234");
if (matchedAccount != null) {
    String accountId = matchedAccount.uuid;
    // Use this account ID in SMS import
}
```

### 10. How to Handle SMS in BroadcastReceiver

```java
// In SmsReceiver.java - Already implemented
public class SmsReceiver extends BroadcastReceiver {
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // Check action
        if (!SMS_RECEIVED.equals(intent.getAction())) return;
        
        // Extract SMS data
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;
        
        Object[] pdus = (Object[]) bundle.get("pdus");
        String format = bundle.getString("format");
        if (pdus == null) return;
        
        // Reconstruct SMS content
        StringBuilder fullMessage = new StringBuilder();
        for (Object pdu : pdus) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
            if (sms == null) continue;
            fullMessage.append(sms.getMessageBody());
        }
        
        String body = fullMessage.toString();
        
        // Process in background
        new Thread(() -> processSmsInBackground(context, body)).start();
    }
    
    private void processSmsInBackground(Context context, String body) {
        // Parse SMS
        // Match account
        // Create SmsImport
        // Show notification
    }
}
```

### 11. Database Query Examples

```java
// Get pending SMS imports
LiveData<List<SmsImport>> pending = smsImportDao.getPending();

// Get count of pending imports
LiveData<Integer> count = smsImportDao.getPendingCount();

// Get confirmed imports (for sync)
List<SmsImport> confirmed = smsImportDao.getConfirmed();

// Match account by last 4 digits
Account account = accountDao.getByAccountNumber("1234");

// Get categories by type
LiveData<List<Category>> expenses = categoryDao.getByType("EXPENSE");

// Create new SMS import
SmsImport smsImport = new SmsImport();
smsImport.uuid = UUID.randomUUID().toString();
smsImport.smsText = "...";
smsImport.amount = 500;
smsImport.detectedType = "EXPENSE";
smsImport.date = System.currentTimeMillis();
smsImport.accountId = "account-uuid";
smsImport.categoryId = "category-uuid";
smsImport.status = "PENDING";
smsImport.createdAt = System.currentTimeMillis();
smsImport.updatedAt = System.currentTimeMillis();
smsImport.deleted = false;

// Insert SMS import
smsImportDao.insert(smsImport);

// Update status
smsImportDao.updateStatus(uuid, "CONFIRMED", System.currentTimeMillis());

// Update account and category
smsImportDao.updateAccountAndCategory(uuid, accountId, categoryId, System.currentTimeMillis());
```

### 12. Logging Examples

```java
// In SmsReceiver
Log.d("SmsReceiver", "SMS received from: " + sender);
Log.d("SmsReceiver", "SMS imported successfully - Amount: " + parsed.amount);
Log.e("SmsReceiver", "Error processing SMS", exception);

// In SmsImportConversionService
Log.d("SmsImportConversion", "Successfully converted SMS to transaction");
Log.w("SmsImportConversion", "Cannot convert non-confirmed SMS import");
Log.e("SmsImportConversion", "Failed to convert SMS", exception);

// In SmsImportNotificationService
Log.d("SmsImportNotification", "Showing notification for " + count + " imports");

// Usage
if (BuildConfig.DEBUG) {
    Log.d(TAG, "Debug information: " + details);
}
```

### 13. Common Error Scenarios & Handling

```java
// Error 1: SMS is not a transaction message
if (!SmsParser.isTransactionSms(body, sender)) {
    Log.d(TAG, "Not a transaction SMS, discarding");
    return;  // Discard silently
}

// Error 2: Cannot parse amount
SmsParser.ParsedTransaction parsed = SmsParser.parse(body);
if (parsed == null) {
    Log.w(TAG, "Could not parse transaction details from SMS");
    return;  // Discard silently
}

// Error 3: Cannot match account
if (extractedAccountNumber == null) {
    Log.d(TAG, "No account number in SMS, will prompt user");
    matchedAccountId = null;  // User must select
}

// Error 4: Account number doesn't match any account
Account matchedAccount = accountRepo.findByAccountNumber(extractedAccountNumber);
if (matchedAccount == null) {
    Log.d(TAG, "Account number not found, will prompt user");
    matchedAccountId = null;  // User must select
}

// Error 5: Missing required fields
if (smsImport.accountId == null) {
    Log.w(TAG, "SMS import missing accountId, cannot convert");
    return;  // Wait for user to provide
}

// Error 6: Database error
try {
    transactionDao.insert(transaction);
    Log.d(TAG, "Transaction inserted successfully");
} catch (Exception e) {
    Log.e(TAG, "Failed to insert transaction", e);
    // SMS import remains PENDING for retry
}
```

### 14. Unit Test Examples

```java
// Test SMS parsing
@Test
public void testSmsParser_validExpense() {
    String sms = "Your A/C •••1234 debited Rs.500 on 14-Mar-2024";
    SmsParser.ParsedTransaction parsed = SmsParser.parse(sms);
    
    assertEquals(500, parsed.amount, 0.01);
    assertEquals("EXPENSE", parsed.type);
    assertNotNull(parsed.date);
}

// Test account matching
@Test
public void testAccountMatching_withLast4Digits() {
    // Create account
    Account account = new Account();
    account.uuid = "test-uuid";
    account.name = "Test Account";
    account.accountNumberLast4 = "1234";
    accountDao.insert(account);
    
    // Match
    Account found = accountDao.getByAccountNumber("1234");
    assertNotNull(found);
    assertEquals("test-uuid", found.uuid);
}

// Test SMS import creation
@Test
public void testSmsImportCreation() {
    SmsImport smsImport = new SmsImport();
    smsImport.uuid = UUID.randomUUID().toString();
    smsImport.amount = 500;
    smsImport.detectedType = "EXPENSE";
    smsImport.status = "PENDING";
    
    smsImportDao.insert(smsImport);
    
    List<SmsImport> pending = smsImportDao.getPending().getValue();
    assertNotNull(pending);
    assertEquals(1, pending.size());
}

// Test transaction conversion
@Test
public void testTransactionConversion() {
    // Create and confirm SMS import
    SmsImport smsImport = createSmsImport("1234", "EXPENSE", 500);
    smsImportDao.insert(smsImport);
    smsImportDao.updateStatus(smsImport.uuid, "CONFIRMED", System.currentTimeMillis());
    
    // Convert
    SmsImportConversionService.convertToTransaction(context, smsImport);
    
    // Verify transaction created
    List<Transaction> transactions = transactionDao.getAllActive().getValue();
    assertNotNull(transactions);
    assertEquals(1, transactions.size());
    assertEquals(500, transactions.get(0).amount, 0.01);
}
```

### 15. Integration Test Examples

```java
// End-to-end integration test
@Test
public void testSmsToDashboard_fullFlow() {
    // 1. Create account with last 4 digits
    Account account = new Account();
    account.uuid = "account-1";
    account.accountNumberLast4 = "1234";
    accountDao.insert(account);
    
    // 2. Simulate SMS receipt
    String sms = "Your A/C •••1234 debited Rs.500";
    SmsParser.ParsedTransaction parsed = SmsParser.parse(sms);
    
    // 3. Create SMS import
    SmsImport smsImport = new SmsImport();
    smsImport.uuid = "sms-1";
    smsImport.amount = parsed.amount;
    smsImport.detectedType = parsed.type;
    smsImport.accountId = "account-1";  // Auto-matched
    smsImportDao.insert(smsImport);
    
    // 4. Confirm SMS import
    smsImportDao.updateStatus("sms-1", "CONFIRMED", System.currentTimeMillis());
    
    // 5. Convert to transaction
    SmsImportConversionService.convertToTransaction(context, smsImport);
    
    // 6. Verify in dashboard
    List<Transaction> transactions = transactionDao.getAllActive().getValue();
    assertTrue(transactions.size() > 0);
    assertEquals(500, transactions.get(0).amount, 0.01);
}
```

---

## Quick Tips & Tricks

### Tips
1. Always check if object is null before using it
2. Use background threads for database operations
3. Test with different SMS formats (bank variations)
4. Log all important operations for debugging
5. Clear old/expired SMS imports periodically
6. Use transactions for multi-step operations
7. Handle permissions properly for Android 6.0+

### Debugging Tips
1. Enable logging: `adb logcat | grep -E "SmsReceiver|SmsImport|Transaction"`
2. Check database directly: `adb shell sqlite3 /data/data/com.financetracker/databases/finance_tracker.db`
3. Test with command: `adb shell am broadcast -a android.provider.Telephony.SMS_RECEIVED ...`
4. Use Android Studio debugger with breakpoints
5. Add temporary Toast messages for quick feedback

### Common Pitfalls
1. ❌ Accessing database on main thread → Use background threads
2. ❌ Not handling permissions → Request at runtime
3. ❌ Null pointer exceptions → Always check null
4. ❌ Regex patterns too strict → Test with real SMS formats
5. ❌ Not logging errors → Always log exceptions

---

This code reference should help you understand and work with the SMS transaction auto-import feature!

