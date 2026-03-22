SMS Reading Issue - Debugging Checklist
========================================

## Quick Verification Steps

### 1. Permission Check
```
Device Settings → Apps → Finance Tracker → Permissions
- SMS: Should show "Allowed"
- Notifications: Should show "Allowed"
```

If not allowed:
- Go back to app → Allow permissions when prompted
- OR grant manually in settings

### 2. Check Logcat Logs
Filter by "SmsReceiver" tag:

```
Expected logs when SMS arrives:
D/SmsReceiver: === BroadcastReceiver.onReceive() called ===
D/SmsReceiver: Action: android.provider.Telephony.SMS_RECEIVED
D/SmsReceiver: Received SMS with X part(s), format: 3gpp
D/SmsReceiver: Received SMS from: +XXX..., body length: XXX
D/SmsReceiver: Parsed SMS: amount=XXX, type=CREDIT/DEBIT, merchant=BANK NAME
```

If you DON'T see these logs:
- Permissions not granted
- Phone number not set in emulator
- Broadcast receiver not registered

### 3. Verify App Installation
```
adb shell pm list packages | grep financetracker
```

Should return:
```
package:com.financetracker
```

### 4. Check if Receiver is Registered
```
adb shell dumpsys package com.financetracker | grep -A 10 "SmsReceiver"
```

Should show the receiver is registered

### 5. Send Test SMS

**On Emulator:**
```
telnet localhost 5554
sms send +1234567890 "Test transaction: CREDITCARD: Amount: 500"
```

**On Real Device:**
- Use another phone to send SMS
- Use Android Studio's SMS simulator

### 6. Monitor Real-time Logs
```
adb logcat -s "SmsReceiver:D,SmsProcessingService:D,FinanceTrackerApp:D" | grep ".*"
```

Then send SMS and watch logs appear

## Expected Behavior After Fix

### First Launch:
1. ✅ App shows "Allow SMS permissions?" dialog
2. ✅ User grants permissions
3. ✅ Logcat shows: "SMS permissions granted - SMS reading is now enabled"

### SMS Arrives:
1. ✅ SmsReceiver.onReceive() called (check logcat)
2. ✅ SMS parsed and transaction details extracted
3. ✅ JobIntentService queues SMS processing work
4. ✅ SMS appears in pending transactions (if merchant not categorized)
5. ✅ OR transaction created directly (if merchant is categorized)

## Troubleshooting

### Issue: "BroadcastReceiver.onReceive() NOT called"
**Possible Causes:**
- [ ] Permissions not granted (check in Settings)
- [ ] SMS not being sent to correct number
- [ ] Phone number not configured in emulator
- [ ] App not installed or wrong version
- [ ] Receiver not registered in manifest

**Solution:**
1. Check permissions: `adb shell dumpsys package com.financetracker | grep -i permission`
2. Verify phone number: In emulator, check "More" → "Advanced" → Phone number
3. Reinstall app: `adb uninstall com.financetracker` then rebuild

### Issue: "Permission dialog doesn't appear"
**Possible Causes:**
- [ ] Permissions already granted
- [ ] Device running API < 23 (but we handle this)
- [ ] Permissions are system permissions and can't be requested

**Solution:**
1. Reset app permissions: Settings → Apps → Finance Tracker → Permissions → Reset
2. Force close app: `adb shell am force-stop com.financetracker`
3. Reinstall app

### Issue: "SMS Received but not showing in pending transactions"
**Possible Causes:**
- [ ] SMS parsing failed
- [ ] Merchant categorization issue
- [ ] Database error
- [ ] SmsImportNotificationService failed

**Solution:**
1. Check logs for parse errors: `adb logcat -s "SmsParser:D"`
2. Check SmsProcessingService logs: `adb logcat -s "SmsProcessingService:D"`
3. Verify database: Check DatabaseInitializer logs
4. Check if already processed: Query database for duplicate records

## Key Logcat Filters

### All SMS-related logs:
```
adb logcat -s "SmsReceiver,SmsProcessingService,SmsParser,PermissionManager" -v time
```

### Permission-related logs:
```
adb logcat -s "PermissionManager:D,MainActivity:D" -v time
```

### Full app startup:
```
adb logcat -s "FinanceTrackerApp:D,MainActivity:D,PermissionManager:D" -v time
```

## Permission States in Logcat

### ✅ Correct:
```
D/PermissionManager: SMS Permissions - READ_SMS: true, RECEIVE_SMS: true
D/PermissionManager: SMS permissions already granted
D/MainActivity: ✓ SMS permissions granted - SMS reading is now enabled
```

### ❌ Wrong:
```
D/PermissionManager: SMS Permissions - READ_SMS: false, RECEIVE_SMS: false
D/PermissionManager: Requesting SMS permissions from user
D/MainActivity: ✗ SMS permissions denied - SMS reading will not work
```

## Build Verification

### Verify no compilation errors:
```
./gradlew assembleDebug
```

Should end with:
```
BUILD SUCCESSFUL in XXs
```

### Check if APK contains new files:
```
unzip -l app/build/outputs/apk/debug/app-debug.apk | grep PermissionManager
```

Should show:
```
com/financetracker/utils/PermissionManager.class
```

## After Testing

If SMS is now working:
1. ✅ Verify all pending transactions appear correctly
2. ✅ Test merchant categorization works
3. ✅ Check database has correct records
4. ✅ Verify notifications are sent
5. ✅ Test auto-confirmation with categorized merchants

If issues persist:
1. Check all logs from above
2. Verify manifest changes applied
3. Ensure PermissionManager imported correctly
4. Check MainActivity has permission methods
5. Clear app data and reinstall

