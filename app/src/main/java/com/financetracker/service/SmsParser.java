package com.financetracker.service;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

public class SmsParser {

    private static final String TAG = "SmsParser";

    // ...existing code...
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

    private static final List<String> TRANSACTION_KEYWORDS = new ArrayList<>();

    static {
        TRANSACTION_KEYWORDS.addAll(EXPENSE_KEYWORDS);
        TRANSACTION_KEYWORDS.addAll(INCOME_KEYWORDS);
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
        for (String kw : EXPENSE_KEYWORDS) {
            if (lower.contains(kw)) return "EXPENSE";
        }
        for (String kw : INCOME_KEYWORDS) {
            if (lower.contains(kw)) return "INCOME";
        }
        return "EXPENSE";
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
            
            // 5. "towards <Name>"
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
