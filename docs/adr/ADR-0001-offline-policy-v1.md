# ADR-0001: Offline Policy Definition (K1)

## Status
Accepted - February 10, 2026

## Context
SaulPOS v2 is server-authoritative for pricing, tax, inventory, and checkout writes.  
For v1, there is no durable client-side transaction queue and no server-side replay buffer for disconnected writes.

We need an explicit offline policy that:
- defines what is and is not supported when connectivity is lost,
- maps the policy to technical controls, and
- defines user-facing behavior/messages.

## Decision
SaulPOS v2 adopts a constrained offline policy for v1:

1. Transactional operations are `ONLINE_ONLY`.
2. Read-only cached reference views are `DEGRADED_READ_ONLY` when the client has previously synced data.
3. No offline write queue is supported in v1.

Policy contract is published by `GET /api/system/offline-policy`.

## Supported Operation Modes
| Operation | Mode | Technical Control | User-facing Behavior |
|---|---|---|---|
| `AUTH_LOGIN` | `ONLINE_ONLY` | Client blocks login submission while disconnected. | "Cannot sign in while offline. Reconnect to continue." |
| `CART_MUTATION` | `ONLINE_ONLY` | Client disables add/update/remove cart actions while disconnected. | "Cart changes are unavailable offline. Reconnect and try again." |
| `CHECKOUT` | `ONLINE_ONLY` | Checkout is blocked unless server round-trip is available. | "Sale cannot be completed offline. Reconnect to finalize payment." |
| `CATALOG_REFERENCE_VIEW` | `DEGRADED_READ_ONLY` | Client may show last-synced catalog reference but keeps transactional actions disabled. | "You can view cached catalog data, but sales actions stay disabled until reconnect." |

## Consequences
- Positive:
  - predictable behavior in connectivity loss scenarios,
  - avoids silent data-loss risk from unsynced writes,
  - policy is machine-readable for client UX.
- Tradeoffs:
  - true offline checkout is not available in v1,
  - operators must restore connectivity before completing sales.

## Follow-ups
- `K2`: add idempotency/replay protection for duplicate submissions.
- Future enhancement: introduce queued offline write strategy only with explicit conflict/reconciliation model.
