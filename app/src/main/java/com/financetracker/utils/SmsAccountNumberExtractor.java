package com.financetracker.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to extract account numbers from SMS transaction messages
 * Handles various bank SMS formats and extracts the last 4 digits
 */
public class SmsAccountNumberExtractor {
    
    /**
     * Extract the last 4 digits of an account number from SMS text
     * Looks for common patterns like "XXXX1234", "A/C XXXXXX1234", "•••1234", etc.
     * 
     * @param smsText The SMS message text
     * @return Last 4 digits of account number, or null if not found
     */
    public static String extractLast4Digits(String smsText) {
        if (smsText == null || smsText.isEmpty()) {
            return null;
        }
        
        // Pattern 1: Look for •••XXXX or ****XXXX pattern (already masked)
        // Example: "A/C •••1234 debited Rs.100"
        Pattern maskedPattern = Pattern.compile("[•*]{3,4}(\\d{4})");
        Matcher maskedMatcher = maskedPattern.matcher(smsText);
        if (maskedMatcher.find()) {
            return maskedMatcher.group(1);
        }
        
        // Pattern 1.5: Look for "A/c no. XX" or "A/C no. XX" followed by 4 digits
        // Example: "A/c no. XX8996" (Axis Bank format)
        Pattern acNoXxPattern = Pattern.compile(
            "A/c\\s+no\\.?\\s+[A-Za-z]{2}(\\d{4})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher acNoXxMatcher = acNoXxPattern.matcher(smsText);
        if (acNoXxMatcher.find()) {
            return acNoXxMatcher.group(1);
        }
        
        // Pattern 2: Look for Credit Card or Debit Card followed by x and 4 digits
        // Example: "Credit Card x9477", "Debit Card X1234"
        Pattern creditCardPattern = Pattern.compile(
            "(?:Credit|Debit)\\s+Card\\s+[Xx](\\d{4})",
            Pattern.CASE_INSENSITIVE
        );
        Matcher creditCardMatcher = creditCardPattern.matcher(smsText);
        if (creditCardMatcher.find()) {
            return creditCardMatcher.group(1);
        }
        
        // Pattern 3: Look for A/C or ACCOUNT or ACC followed by numbers with Xs or bullets
        // Example: "A/C XXXXXX1234 spent", "ACCOUNT 1234 debited"
        Pattern accountPattern = Pattern.compile(
            "(?:A/C|ACCOUNT|ACC|ACCT)[\\s.:]*[X•]*[X•]*[X•]*[X•]?(\\d{4})", 
            Pattern.CASE_INSENSITIVE
        );
        Matcher accountMatcher = accountPattern.matcher(smsText);
        if (accountMatcher.find()) {
            return accountMatcher.group(1);
        }
        
        // Pattern 4: Look for "xxxx1234" format (4 Xs followed by 4 digits)
        // Example: "xxxx5678 has been debited"
        Pattern xxxPattern = Pattern.compile("[Xx]{4}(\\d{4})");
        Matcher xxxMatcher = xxxPattern.matcher(smsText);
        if (xxxMatcher.find()) {
            return xxxMatcher.group(1);
        }
        
        // Pattern 5: Look for any sequence of 10-16 digits and extract last 4
        // This is a fallback for account numbers without masking
        // Example: "1234567890123456" -> "3456"
        Pattern digitPattern = Pattern.compile("\\b(\\d{10,16})\\b");
        Matcher digitMatcher = digitPattern.matcher(smsText);
        if (digitMatcher.find()) {
            String digits = digitMatcher.group(1);
            return digits.substring(digits.length() - 4);
        }
        
        return null;
    }
    
    /**
     * Check if an SMS likely contains account number information
     * Used to identify which SMS messages should be scanned for account numbers
     * 
     * @param smsText The SMS message text
     * @return true if SMS likely contains account number
     */
    public static boolean likelyContainsAccountNumber(String smsText) {
        if (smsText == null) return false;
        
        String lower = smsText.toLowerCase();
        
        // Check for common patterns that indicate account numbers
        return lower.contains("a/c") || 
               lower.contains("account") ||
               lower.contains("acct") ||
               lower.contains("credit card") ||
               lower.contains("debit card") ||
               lower.contains("•••") ||
               lower.contains("****") ||
               lower.contains("xxxx") ||
               lower.contains("debit") ||
               lower.contains("credit") ||
               lower.contains("transfer");
    }
    
    /**
     * Validate that extracted account number is valid (4 digits)
     * 
     * @param accountNumber The account number to validate
     * @return true if valid (4 digits), false otherwise
     */
    public static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && 
               accountNumber.length() == 4 && 
               accountNumber.matches("\\d{4}");
    }
}

