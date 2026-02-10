# P5 User Guide (Cashier, Manager, Admin)

Card ID: P5  
Title: Documentation and Handover  
Audience: Store Operations Users

## 1. Cashier Daily Workflow

### Start Shift
1. Sign in with cashier credentials.
2. Open shift with terminal + opening float.
3. Confirm shift status is `OPEN` before selling.

### Sell and Checkout
1. Scan barcode or search product.
2. Confirm quantity, price, and discounts.
3. Proceed to checkout and collect tender (`CASH`, `CARD`, or split).
4. Confirm receipt number is returned after successful checkout.

### Returns
1. Lookup original sale by receipt number.
2. Select eligible lines and return quantity.
3. Enter return reason and refund method.
4. If blocked by policy window, escalate to manager approval flow.

### End Shift
1. Record paid-in/paid-out movements with reasons when needed.
2. Close shift with counted cash.
3. Confirm expected vs counted variance is captured.

## 2. Manager Daily Workflow

### Approvals and Exceptions
1. Review pending approvals (price overrides, restricted returns, high adjustments).
2. Validate reason codes before approving sensitive actions.
3. Monitor exception reports for voids, overrides, no-sale drawer opens, and refund anomalies.

### Operational Controls
1. Validate receipt reprint requests and authorize where required.
2. Review cash shift variance and resolve discrepancies.
3. Verify lot/expiry warnings and supplier return approvals.

## 3. Admin Workflow

### Master Data and Configuration
1. Maintain merchants, stores, terminals, and user assignments.
2. Maintain role and permission mappings.
3. Maintain product, pricing, supplier, and customer data.

### Environment and Operations
1. Follow deployment and operations runbooks in `docs/ops/`.
2. Execute backup/restore rehearsals per schedule.
3. Validate environment configuration and secret hardening checklist.

## 4. Troubleshooting Quick Reference

- Login failures: verify account lock status and credentials.
- Checkout blocked: verify shift is open, permissions, and product policy constraints.
- Reprint blocked: verify `RECEIPT_REPRINT` permission.
- Report/export denied: verify role has report permissions.

## 5. Training and Handover Checklist

- [x] Cashier flow walkthrough completed.
- [x] Manager approval/exception flow walkthrough completed.
- [x] Admin configuration workflow walkthrough completed.
- [x] Recovery workflows (reprint, backup/restore escalation path) reviewed.
