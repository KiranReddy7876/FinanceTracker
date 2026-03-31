package com.financetracker.service;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

public class SmsParser {

    private static final String TAG = "SmsParser";

    // Amount patterns: Rs.500, Rs.500.00, INR 500, ₹500, EUR 100, USD 50
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
        "(?:Rs\\.?|INR|USD|₹|EUR)\\s*([\\d,]+(?:\\.\\d{1,2})?)",
        Pattern.CASE_INSENSITIVE
    );

    // Date patterns: 12-Jan-2024, 12/01/2024, 12-01-2024
    private static final Pattern DATE_PATTERN = Pattern.compile(
        "(\\d{1,2})[/-](\\w{3}|\\d{1,2})[/-](\\d{2,4})"
    );

    private static final List<String> EXPENSE_KEYWORDS = Arrays.asList(
        "debited", "deducted", "withdrawn", "spent", "paid", "purchase",
        "payment of", "charged", "debit","dr."
    );

    private static final List<String> INCOME_KEYWORDS = Arrays.asList(
        "credited", "received", "deposited", "refund", "cashback", "credit","cr."
    );

    private static final List<String> TRANSFER_KEYWORDS = Arrays.asList(
        "credit card payment", "cc payment", "cc bill", "credit card bill", "card payment",
        "minimum due", "bill payment", "payment to credit", "payment towards credit",
        "transfer to", "money transfer", "account transfer", "cash withdrawal", "atm withdrawal",
        "cash transfer"
    );

    // UPI Transfer Keywords - MUST BE CHECKED BEFORE EXPENSE KEYWORDS
    // UPI/P2A = UPI Peer-to-Account, UPI/P2P = UPI Peer-to-Peer
    private static final List<String> UPI_KEYWORDS = Arrays.asList(
        "upi/p2a", "upi/p2p",  // UPI transfer types (P2M is EXPENSE, not transfer)
        "upi transfer", "upi payment", "upi sent", "upi received",
        "neft", "rtgs", "imps",  // Bank transfer types
        "inter-account transfer", "inter account transfer"
    );

    private static final List<String> TRANSACTION_KEYWORDS = new ArrayList<>();

    static {
        TRANSACTION_KEYWORDS.addAll(EXPENSE_KEYWORDS);
        TRANSACTION_KEYWORDS.addAll(INCOME_KEYWORDS);
        TRANSACTION_KEYWORDS.addAll(TRANSFER_KEYWORDS);
        TRANSACTION_KEYWORDS.addAll(UPI_KEYWORDS);
        TRANSACTION_KEYWORDS.add("transaction");
        TRANSACTION_KEYWORDS.add("transfer");
        TRANSACTION_KEYWORDS.add("balance");
    }

    public static boolean isTransactionSms(String body, String sender) {
        if (body == null || body.trim().isEmpty()) {
            Log.w(TAG, "isTransactionSms: Body is null or empty");
            return false;
        }
        String lower = body.toLowerCase();
        for (String keyword : TRANSACTION_KEYWORDS) {
            if (lower.contains(keyword)) {
                Log.d(TAG, "✓ Transaction SMS detected - Found keyword: '" + keyword + "'");
                return true;
            }
        }
        Log.w(TAG, "✗ Not a transaction SMS - No keywords found in: " + body);
        Log.d(TAG, "   Required keywords: " + TRANSACTION_KEYWORDS);
        return false;
    }

    public static ParsedTransaction parse(String body) {
        if (body == null) {
            Log.w(TAG, "parse: Body is null");
            return null;
        }

        Log.d(TAG, "Parsing SMS: " + body);

        Matcher amountMatcher = AMOUNT_PATTERN.matcher(body);
        if (!amountMatcher.find()) {
            Log.w(TAG, "✗ Could not find amount pattern in: " + body);
            Log.d(TAG, "   Expected formats: Rs.500, Rs.500.00, INR 500, ₹500, EUR 100, USD 50");
            return null;
        }

        Log.d(TAG, "✓ Amount found: " + amountMatcher.group(0));

        String amountStr = amountMatcher.group(1).replace(",", "");
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            Log.d(TAG, "✓ Amount parsed: " + amount);
        } catch (NumberFormatException e) {
            Log.e(TAG, "✗ Could not parse amount: " + amountStr, e);
            return null;
        }

        String type = detectType(body);
        long date = extractDate(body);
        String merchant = extractMerchant(body);

        Log.d(TAG, "✓ Parsed - Amount: " + amount + ", Type: " + type + ", Merchant: " + merchant);

        ParsedTransaction result = new ParsedTransaction();
        result.amount = amount;
        result.type = type;
        result.date = date > 0 ? date : System.currentTimeMillis();
        result.merchant = merchant;
        result.rawText = body;
        return result;
    }

    private static String detectType(String body) {
        String lower = body.toLowerCase();
        
        // Check for UPI/P2M (Peer-to-Merchant) FIRST - it's an EXPENSE, not a transfer
        if (lower.contains("upi/p2m")) {
            Log.d(TAG, "✓ Detected UPI/P2M - Classified as EXPENSE");
            return "EXPENSE";
        }
        
        // IMPORTANT: Check for UPI transfers (P2A, P2P) FIRST, before EXPENSE keywords!
        // This ensures that "debited" in "INR 35000 debited ... UPI/P2A" is classified as TRANSFER
        if (isUpiTransfer(lower)) {
            Log.d(TAG, "✓ Detected UPI TRANSFER");
            return "TRANSFER";
        }
        
        // Check for other transfer/bill payment keywords
        for (String kw : TRANSFER_KEYWORDS) {
            if (lower.contains(kw)) return "TRANSFER";
        }
        
        for (String kw : EXPENSE_KEYWORDS) {
            if (lower.contains(kw)) return "EXPENSE";
        }
        for (String kw : INCOME_KEYWORDS) {
            if (lower.contains(kw)) return "INCOME";
        }
        return "EXPENSE";
    }

    /**
     * Detect if SMS is a UPI transfer (P2A, P2P) or bank transfer (NEFT, RTGS, IMPS)
     * UPI/P2A = UPI Peer-to-Account (self transfer to own account)
     * UPI/P2P = UPI Peer-to-Peer (transfer to another person)
     * Note: UPI/P2M (Peer-to-Merchant) is classified as EXPENSE, not TRANSFER
     */
    public static boolean isUpiTransfer(String body) {
        if (body == null) return false;
        String lower = body.toLowerCase();
        
        // Check for UPI patterns: "UPI/P2A/" or "UPI/P2P/" only (NOT P2M - that's an expense)
        if (lower.contains("upi/p2a") || lower.contains("upi/p2p")) {
            return true;
        }
        
        // Check for bank transfer patterns
        for (String kw : UPI_KEYWORDS) {
            if (lower.contains(kw)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Extract UPI reference number from transfer SMS
     * Pattern: "UPI/P2A/639760815755/PAGALA" -> Returns "639760815755"
     */
    public static String extractUpiReference(String body) {
        if (body == null) return null;
        
        // Pattern: "UPI/P2A/12345678901234/MERCHANT" or similar
        Pattern upiPattern = Pattern.compile(
            "UPI/[Pp]2[AaPpMm]/([A-Za-z0-9]{12,14})/",
            Pattern.CASE_INSENSITIVE
        );
        Matcher upiMatcher = upiPattern.matcher(body);
        if (upiMatcher.find()) {
            return upiMatcher.group(1);
        }
        
        return null;
    }

    /**
     * Detect if SMS is a credit card bill payment
     * Patterns: "credit card payment", "cc bill", "card payment", "bill payment"
     */
    public static boolean isCreditCardPayment(String body) {
        if (body == null) return false;
        String lower = body.toLowerCase();
        return lower.contains("credit card") && 
               (lower.contains("payment") || lower.contains("bill") || lower.contains("due"));
    }

    /**
     * Detect if SMS is a cash withdrawal or transfer
     * Patterns: "atm withdrawal", "cash withdrawal", "cash transfer"
     */
    public static boolean isCashTransaction(String body) {
        if (body == null) return false;
        String lower = body.toLowerCase();
        return (lower.contains("cash") || lower.contains("atm")) && 
               (lower.contains("withdrawal") || lower.contains("withdraw") || lower.contains("transfer"));
    }

    /**
     * Extract destination account number from transfer SMS
     * Looks for account numbers mentioned after "credit card", "cc", "to account", etc.
     */
    public static String extractTransferDestinationAccount(String body) {
        if (body == null) return null;
        
        // Pattern 1: "towards your Credit Card x9477" or "to Credit Card X9012"
        Pattern creditCardPattern = Pattern.compile(
            "(?:towards|to)\\s+(?:your\\s+)?credit\\s+card\\s+[Xx](\\d{4})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher ccMatcher = creditCardPattern.matcher(body);
        if (ccMatcher.find()) {
            return ccMatcher.group(1);
        }
        
        // Pattern 2: "towards your A/C X9012" or "to account 9012"
        Pattern acPattern = Pattern.compile(
            "(?:towards|to)\\s+(?:your\\s+)?(?:A/C|account)\\s+(?:X+)?(\\d{4})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher acMatcher = acPattern.matcher(body);
        if (acMatcher.find()) {
            return acMatcher.group(1);
        }
        
        return null;
    }

    private static long extractDate(String body) {
        Matcher m = DATE_PATTERN.matcher(body);
        if (!m.find()) return -1;
        String raw = m.group(0);
        String[] formats = {"dd-MMM-yyyy", "dd/MM/yyyy", "dd-MM-yyyy", "d-MMM-yy"};
        for (String fmt : formats) {
            try {
                return new SimpleDateFormat(fmt, Locale.ENGLISH).parse(raw).getTime();
            } catch (ParseException ignored) {}
        }
        return -1;
    }

    private static String extractMerchant(String body) {
        // Skip merchant extraction for transfer messages
        String lower = body.toLowerCase();
        if (isCreditCardPayment(lower) || isCashTransaction(lower)) {
            Log.d(TAG, "extractMerchant: Skipping merchant extraction for TRANSFER type SMS");
            return null;
        }
        
        // Ordered patterns – first match wins
        Pattern[] patterns = {
            // 1. Direct UPI VPA: "xyz@upi" or "xyz@bank" anywhere in message
            Pattern.compile("([a-zA-Z0-9._\\-]+@[a-zA-Z0-9]+(?:\\.[a-zA-Z]{2,})?)", Pattern.CASE_INSENSITIVE),
            
            // 2. "Cr. to <Name>" / "Dr. to <Name>" (common bank format)
            Pattern.compile("(?:Cr\\.?|Dr\\.?)\\s+to\\s+([a-zA-Z0-9 &.'\\-/+]{2,50}?)(?:\\s+Ref|\\s+on|\\s+via|\\s+a/c|\\.|,|$)", Pattern.CASE_INSENSITIVE),
            
            // 3. "paid to", "payment to", "transferred to", "sent to" (with flexible case)
            Pattern.compile("(?:paid\\s+to|payment\\s+to|tran(?:sferred)?\\s+to|sent\\s+to)\\s+([a-zA-Z0-9 &.'\\-/+]{2,50}?)(?:\\s+on|\\s+via|\\s+ref|\\s+a/c|\\.|,|$)", Pattern.CASE_INSENSITIVE),
            
            // 4. "at <Merchant>" (POS / swipe) - flexible case
            Pattern.compile("\\bat\\s+([a-zA-Z0-9 &.'\\-/+]{2,50}?)(?:\\s+on|\\s+via|\\s+ref|\\s+a/c|\\.|,|$)", Pattern.CASE_INSENSITIVE),
            
            // 5. "towards <Name>" - but NOT for transfers
            Pattern.compile("\\btowards\\s+([a-zA-Z0-9 &.'\\-/+]{2,50}?)(?:\\s+on|\\s+via|\\s+ref|\\s+a/c|\\.|,|$)", Pattern.CASE_INSENSITIVE),
            
            // 6. "from <Name>" for credits
            Pattern.compile("\\bfrom\\s+([a-zA-Z0-9 &.'\\-/+]{2,50}?)(?:\\s+on|\\s+via|\\s+ref|\\s+a/c|\\s+A/C|\\.|,|$)", Pattern.CASE_INSENSITIVE),
            
            // 7. "Merchant:", "Shop:", "Business:" labels
            Pattern.compile("(?:Merchant|Shop|Business|Account|Payee)\\s*[:\\-]\\s*([a-zA-Z0-9 &.'\\-/+]{2,50})", Pattern.CASE_INSENSITIVE),
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(body);
            if (m.find()) {
                String merchant = m.group(1).trim();
                // Remove trailing noise words
                merchant = merchant.replaceAll("(?i)\\s+(on|via|ref|using|thru|through|a/c|account|call)$", "").trim();
                // Filter out generic words that aren't merchants
                if (!merchant.isEmpty() && !merchant.matches("(?i)^(and|or|the|a|an)$")) {
                    return merchant;
                }
            }
        }
        return null;
    }

    public static class ParsedTransaction {
        public double amount;
        public String type;
        public long date;
        public String merchant;
        public String rawText;
    }
}
