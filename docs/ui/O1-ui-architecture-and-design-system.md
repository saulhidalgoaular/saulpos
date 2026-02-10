# O1 UI Architecture and Design System

## Scope
Card `O1` establishes the initial JavaFX client architecture, reusable component primitives, and shared theme tokens for keyboard-first POS workflows.

## Screen Map and Navigation Model
`pos-client` uses a deterministic screen registry in `com.saulpos.client.app.ScreenRegistry`.

Ordered navigation flow:
1. `LOGIN` (`/login`)
2. `SHIFT_CONTROL` (`/shift`)
3. `SELL` (`/sell`)
4. `CHECKOUT` (`/checkout`)
5. `RETURNS` (`/returns`)
6. `BACKOFFICE` (`/backoffice`)
7. `REPORTING` (`/reporting`)
8. `HARDWARE` (`/hardware`)

Each screen definition includes:
- route target,
- display title,
- authentication requirement,
- keyboard order priority,
- short operational description.

## Folder Conventions
`pos-client/src/main/java/com/saulpos/client`
- `app`: screen map, navigation state, app-level route definitions.
- `state`: shared client state store and session model.
- `api`: API client abstraction and HTTP implementation.
- `ui/components`: reusable UI primitives.
- `ui/layout`: screen shell and layout composition.
- `ui/theme`: design tokens and component catalog metadata.

`pos-client/src/main/resources/ui/theme`
- `saulpos-theme.css`: centralized visual tokens/styles.

## State Management Boundaries
- `AppStateStore` is the single mutable state boundary for session/auth state.
- `NavigationState` controls active view routing.
- Views observe state via JavaFX properties/bindings.
- API effects are isolated behind the `PosApiClient` interface.

## API Client Abstraction
`PosApiClient` provides a transport contract for UI workflows:
- `ping()`: backend availability signal.
- `login(...)`: authentication contract (implemented in O2).
- `logout()` and `setAccessToken(...)`: session lifecycle hooks.

`HttpPosApiClient` is the default adapter using Java `HttpClient` and Jackson.

## Reusable Component Library (Initial)
- `PosButton`: styled primary/accent action button.
- `PosTextField`: shared text-input primitive.
- `PosTableView<T>`: table primitive for tabular workflows.
- `PosDialogFactory`: standardized dialog construction.
- `ToastHost`: transient feedback container.

## Design System Tokens
Shared constants in `ThemeTokens` and CSS variables define:
- app/surface backgrounds,
- primary/muted text,
- primary/accent actions,
- borders and danger state,
- shared font family.

## Accessibility Baseline
- Deterministic keyboard order metadata for primitives (`DesignSystemCatalog`).
- Focusability flags for interactive primitives.
- Contrast guardrails validated in tests (`ThemeTokensTest`).

## Testing Added in O1
- `ScreenRegistryTest`: validates navigation flow integrity.
- `DesignSystemCatalogTest`: validates primitive coverage and keyboard order.
- `ThemeTokensTest`: validates color contrast thresholds.
- `AppStateStoreTest`: validates authenticated state transitions.
