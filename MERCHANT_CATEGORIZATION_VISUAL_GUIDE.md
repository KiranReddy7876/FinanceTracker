# Merchant Categorization - Visual Workflow Guide

## Flow Diagram 1: SMS Import Pending Check

```
┌─────────────────────────────────────────────────────────┐
│ New SMS Received from Merchant                          │
│ Parse merchant name: "Starbucks"                        │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│ Database Query: getPending()                            │
│ ├─ Check: WHERE status = 'PENDING'                      │
│ ├─ Check: AND deleted = 0                               │
│ └─ Check: LEFT JOIN merchants                           │
└────────────────────┬────────────────────────────────────┘
                     │
          ┌──────────┴──────────┐
          │                     │
          ▼                     ▼
    ┌─────────────┐     ┌──────────────────┐
    │ Merchant    │     │ Merchant exists? │
    │ Exists?     │     │ Has categoryId?  │
    └─────────────┘     └──────────────────┘
          │                     │
    ┌─────┴─────┐          ┌────┴────┐
    │           │          │         │
   NO          YES        YES       NO
    │           │          │        │
    ▼           ▼          ▼        ▼
 SHOW      SHOW  ?   DON'T SHOW   SHOW
```

## Flow Diagram 2: Detailed Query Logic

```
SMS Import Record (merchantName = "Amazon")
          │
          ▼
┌──────────────────────────────────────────────┐
│ LEFT JOIN merchants                          │
│ ON LOWER(s.merchantName) = LOWER(m.name)    │
│ AND m.deleted = 0                            │
└──────────────────────────────────────────────┘
          │
    ┌─────┴──────────────────────┐
    │                            │
    ▼                            ▼
Found "Amazon" Merchant     No Match Found
categoryId = "shopping-123"  (m is NULL)
    │                            │
    ▼                            ▼
Test: m.categoryId IS NULL  Test: m.categoryId IS NULL
FALSE (it's not null)       TRUE (m is null)
    │                            │
    ▼                            ▼
EXCLUDE from pending         INCLUDE in pending
❌ Won't show                ✅ Will show
```

## Scenario 1: First Time Merchant (New SMS)

```
Timeline:
─────────────────────────────────────────────────────

Day 1: SMS from "Pizza Hut" arrives
       │
       ├─ Merchant "Pizza Hut" does NOT exist in DB
       ├─ LEFT JOIN finds no match
       ├─ Condition: m.categoryId IS NULL (true, m is NULL)
       │
       └─> ✅ SMS appears in PENDING QUEUE
          User sees: [Review] "Pizza Hut" transaction
          │
          └─> User categorizes: "Pizza Hut" → "Restaurants"

Day 2: SMS from "Pizza Hut" arrives again
       │
       ├─ Merchant "Pizza Hut" NOW EXISTS in DB
       ├─ categoryId = "restaurants-uuid"
       ├─ LEFT JOIN finds the merchant
       ├─ Condition: m.categoryId IS NULL (false, has category)
       │
       └─> ❌ SMS does NOT appear in PENDING QUEUE
          User doesn't see it again
```

## Scenario 2: Already Categorized Merchant

```
Timeline:
─────────────────────────────────────────────────────

Day 1: User manually creates merchant "Amazon"
       and assigns category "Shopping"

Day 2: SMS from "Amazon" arrives
       │
       ├─ Merchant "Amazon" EXISTS in DB
       ├─ categoryId = "shopping-123" (already assigned)
       ├─ LEFT JOIN finds the merchant
       ├─ Condition: m.categoryId IS NULL (FALSE)
       │
       └─> ❌ SMS does NOT appear in PENDING QUEUE
          User never sees it in review
          (Could be auto-categorized in future)
```

## Scenario 3: Merchant Without Category

```
Timeline:
─────────────────────────────────────────────────────

Previous: Merchant "Starbucks" created
          but categoryId = NULL (no category assigned)

Day X: SMS from "Starbucks" arrives
       │
       ├─ Merchant "Starbucks" EXISTS in DB
       ├─ categoryId = NULL (not yet categorized)
       ├─ LEFT JOIN finds the merchant
       ├─ Condition: m.categoryId IS NULL (TRUE)
       │
       └─> ✅ SMS appears in PENDING QUEUE
          User sees it for categorization
```

## Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│ SMS Receiver                                                │
│ ├─ Receive SMS text message                                │
│ ├─ Extract amount, date, merchant name                     │
│ └─ Insert into sms_import table (status='PENDING')        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ SmsImportRepository.getPending()                            │
│ ├─ Calls SmsImportDao.getPending()                         │
│ └─ Returns LiveData<List<SmsImport>>                       │
└────────────────────┬────────────────────────────────────────┘
                     │
         ┌───────────┼───────────┐
         │           │           │
         ▼           ▼           ▼
    ┌────────┐ ┌──────────┐ ┌──────────┐
    │SMS     │ │SMS       │ │Dashboard │
    │Review  │ │Import    │ │View Model│
    │View    │ │View      │ │          │
    │Model   │ │Model     │ │(count)   │
    └────────┘ └──────────┘ └──────────┘
         │           │           │
         ▼           ▼           ▼
    ┌─────────────────────────────────┐
    │ UI Updates with Filtered List   │
    │ Only non-categorized merchants  │
    └─────────────────────────────────┘
```

## Query Execution Details

### Step 1: Join Operation
```sql
SELECT s.* FROM sms_import s
LEFT JOIN merchants m ON 
    LOWER(s.merchantName) = LOWER(m.name) 
    AND m.deleted = 0
```
Result for "Amazon" SMS:
```
s.uuid          = "sms-001"
s.merchantName  = "Amazon"
s.status        = "PENDING"
m.uuid          = "merchant-123"
m.name          = "Amazon"
m.categoryId    = "shopping-uuid"  ← Found!
```

### Step 2: Filter Conditions
```sql
WHERE s.status = 'PENDING'
  AND s.deleted = 0
  AND (s.merchantName IS NULL 
       OR s.merchantName = '' 
       OR m.categoryId IS NULL)
```

For "Amazon" SMS:
```
✓ s.status = 'PENDING'           → TRUE
✓ s.deleted = 0                  → TRUE
✓ s.merchantName IS NULL         → FALSE
✓ s.merchantName = ''            → FALSE
✓ m.categoryId IS NULL           → FALSE (m.categoryId = "shopping-uuid")
         ↓
Overall: FALSE OR FALSE OR FALSE = FALSE
         ↓
Result: ❌ EXCLUDED from result set
```

## Decision Tree

```
                    SMS Received
                         │
                         ▼
              Does merchant exist?
                    ╱        ╲
                  NO          YES
                  │           │
                  ▼           ▼
              SHOW      Has category?
              ✅        ╱        ╲
                      YES        NO
                      │          │
                      ▼          ▼
                   HIDE         SHOW
                   ❌           ✅
```

## Pending Queue Content

### Before Fix (Old Behavior)
```
Pending SMS Imports:
├─ [Review] Amazon - $19.99 - Need to categorize
├─ [Review] Amazon - $45.00 - Need to categorize (DUPLICATE!)
├─ [Review] Starbucks - $5.50 - Need to categorize
├─ [Review] Starbucks - $6.00 - Need to categorize (DUPLICATE!)
└─ [Review] Pizza Hut - $25.99 - Need to categorize
```

### After Fix (New Behavior)
```
Pending SMS Imports:
├─ [Review] Pizza Hut - $25.99 - Need to categorize
└─ [Review] Unknown Merchant - $12.34 - Need to categorize
                               ↑
                        No category assigned yet
```

Note: Amazon and Starbucks don't appear because they're already categorized.

---

## Test Checklist

- [ ] Create merchant "TestCorp" without category
- [ ] Simulate SMS from "TestCorp"
- [ ] Verify SMS appears in pending list ✅
- [ ] Assign category to "TestCorp"
- [ ] Simulate new SMS from "TestCorp"
- [ ] Verify SMS does NOT appear in pending list ✅
- [ ] Test case insensitivity ("testcorp" vs "TestCorp") ✅
- [ ] Test with empty merchant name ✅
- [ ] Test with NULL merchant name ✅
- [ ] Verify pending count updates correctly ✅

