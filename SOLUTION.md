# Financing Algorithm Implementation

## Key Components

### Core Services

**InvoiceFinancingService**
- This is the main entry point for the financing process
- Manages the overall workflow and transaction boundaries
- Fetches pending invoices and available purchasers
- Pretty straightforward implementation that delegates to specialized services

**PurchaserSelectionService**
- Handles the logic for finding the best purchaser for each invoice
- Uses Java streams to filter eligible purchasers and select the one with the lowest rate
- Applies the sorting and selection logic in memory after loading purchasers

**FinancingCalculationService**
- I put all financial calculations here to keep them in one place
- Handles financing terms, rates, and discount calculations
- Pays special attention to rounding behavior to avoid precision issues

**PurchaserEligibilityService**
- Evaluates whether a purchaser can finance a specific invoice
- Checks minimum term requirements and maximum rates
- Encapsulates all the eligibility rules from the spec

### Processing Components

**InvoiceBatchProcessor**
- Processes invoices sequentially one by one
- Periodically flushes and clears the entity manager every 1000 invoices
- Tracks processing progress and logs status updates

**InvoiceFinancingApplier**
- Applies financing to individual invoices when a suitable purchaser is found
- Persists both the updated invoice and the new financing details record

### Data Access

**EntityManagerUtil**
- Handles persistence context management
- Helps prevent memory issues for large datasets

**Repositories**
- Added custom queries where needed (like finding non-financed invoices)
- Implemented fetch joins to load purchaser settings efficiently in a single query

## Design Decisions

For performance, I implemented:
- Memory management with periodic entity manager flushing
- Optimized queries with fetch joins to load related data efficiently
- Processing of eligibility rules in memory using Java streams

I paid a lot of attention to testing:
- Unit tests for all components
- The provided Integration test passes
- Edge case tests for calculations (including boundary values)

## Algorithm Implementation

The implementation follows the spec:
1. Get all non-financed invoices
2. For each invoice, find eligible purchasers based on:
    - Existing financing settings for the creditor
    - Meeting minimum financing term requirement
    - Not exceeding maximum financing rate
3. Pick the purchaser with the lowest rate
4. Calculate and save the results

## Performance Considerations

The spec mentioned processing 10,000 invoices efficiently, so I implemented:
- Sequential processing with memory management through periodic entity manager flushing
- Eager loading of purchaser settings with fetch joins to avoid N+1 query issues
- In-memory application of eligibility rules using Java streams

## Future Improvements

If I had more time, I would:
- Implement true batch processing instead of sequential processing
- Implement a more robust error handling strategy
- Add metrics for monitoring performance

