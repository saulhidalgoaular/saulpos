package com.saulpos.client.ui.i18n;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UiI18n {

    private static final Pattern NAVIGATION_PATTERN = Pattern.compile("^Navigated to (.+)$");
    private static final Pattern SESSION_ACTIVE_PATTERN = Pattern.compile("^Session active for (.+)\\.$");
    private static final Pattern SHIFT_STATUS_PATTERN = Pattern.compile("^Shift loaded with status (.+)\\.$");
    private static final Pattern SEARCH_RETURNED_PATTERN = Pattern.compile("^Search returned (\\d+) products\\.$");
    private static final Pattern PARKED_LOADED_PATTERN = Pattern.compile("^Loaded (\\d+) parked carts\\.$");
    private static final Pattern RECEIPT_GENERATED_PATTERN = Pattern.compile("^Checkout completed\\. Receipt (.+) generated\\.$");
    private static final Pattern RECEIPT_LOADED_PATTERN = Pattern.compile("^Receipt loaded\\. (\\d+) lines eligible for return review\\.$");
    private static final Pattern RETURN_REFERENCE_PATTERN = Pattern.compile("^Return submitted\\. Reference (.+) created\\.$");
    private static final Pattern CATALOG_LOADED_PATTERN = Pattern.compile("^Catalog loaded with (\\d+) product\\(s\\)\\.$");
    private static final Pattern CUSTOMER_LIST_LOADED_PATTERN = Pattern.compile("^Customer list loaded with (\\d+) record\\(s\\)\\.$");
    private static final Pattern CUSTOMER_LOOKUP_PATTERN = Pattern.compile("^Customer lookup returned (\\d+) record\\(s\\)\\.$");
    private static final Pattern INVENTORY_BALANCE_LOADED_PATTERN = Pattern.compile("^Loaded (\\d+) inventory balance record\\(s\\)\\.$");
    private static final Pattern RESOLVED_PRICE_PATTERN = Pattern.compile("^Resolved price (.+) from (.+)\\.$");
    private static final Pattern SUPPLIER_RETURN_STATUS_PATTERN = Pattern.compile("^Supplier return loaded: (.+) \\((.+)\\)\\.$");
    private static final Pattern SUPPLIER_RETURN_CREATED_PATTERN = Pattern.compile("^Supplier return created: (.+)$");
    private static final Pattern SUPPLIER_RETURN_APPROVED_PATTERN = Pattern.compile("^Supplier return approved: (.+)\\.$");
    private static final Pattern SUPPLIER_RETURN_POSTED_PATTERN = Pattern.compile("^Supplier return posted: (.+)\\.$");
    private static final Pattern RECEIPT_PRINT_QUEUED_PATTERN = Pattern.compile("^Receipt print queued for (.+)\\.$");
    private static final Pattern RECEIPT_REPRINT_QUEUED_PATTERN = Pattern.compile("^Receipt reprint queued for (.+)\\.$");
    private static final Pattern DRAWER_QUEUED_PATTERN = Pattern.compile("^Drawer open queued for terminal (.+)\\.$");
    private static final Pattern JOURNAL_LOADING_PATTERN = Pattern.compile("^Loading receipt journal for (.+)\\.$");
    private static final Pattern JOURNAL_LOADING_SALE_PATTERN = Pattern.compile("^Loading receipt journal for sale (.+)\\.$");
    private static final Pattern JOURNAL_LOADED_PATTERN = Pattern.compile("^Receipt journal loaded for (.+)\\.$");
    private static final Pattern JOURNAL_LOADED_SALE_PATTERN = Pattern.compile("^Receipt journal loaded for sale (.+)\\.$");
    private static final Pattern SALES_EXPORT_READY_PATTERN = Pattern.compile("^Sales/returns CSV export ready: (\\d+) chars\\.$");
    private static final Pattern INVENTORY_EXPORT_READY_PATTERN = Pattern.compile("^Inventory movement CSV export ready: (\\d+) chars\\.$");
    private static final Pattern CASH_EXPORT_READY_PATTERN = Pattern.compile("^Cash shift CSV export ready: (\\d+) chars\\.$");
    private static final Pattern EXCEPTION_EXPORT_READY_PATTERN = Pattern.compile("^Exception CSV export ready: (\\d+) chars\\.$");
    private static final Pattern REQUEST_FAILED_PATTERN = Pattern.compile("^Request failed: (.+)$");

    private static final Map<String, String> EXACT_ES = new LinkedHashMap<>();
    private static final Map<String, String> TOKEN_ES = new LinkedHashMap<>();

    static {
        EXACT_ES.put("SaulPOS v2 Client", "Cliente SaulPOS v2");
        EXACT_ES.put("Language", "Idioma");
        EXACT_ES.put("English", "Ingles");
        EXACT_ES.put("Espanol", "Espanol");
        EXACT_ES.put("Navigated to", "Navegaste a");

        EXACT_ES.put("Sign In", "Iniciar sesion");
        EXACT_ES.put("Shift", "Turno");
        EXACT_ES.put("Sell", "Venta");
        EXACT_ES.put("Checkout", "Cobro");
        EXACT_ES.put("Returns", "Devoluciones");
        EXACT_ES.put("Backoffice", "Backoffice");
        EXACT_ES.put("Reporting", "Reportes");
        EXACT_ES.put("Hardware", "Hardware");

        EXACT_ES.put("Authenticate cashier and load server-side auth context", "Autenticar cajero y cargar el contexto de autenticacion del servidor");
        EXACT_ES.put("Open/close shift and perform paid-in/paid-out cash controls", "Abrir/cerrar turno y registrar entradas/salidas de caja");
        EXACT_ES.put("Main barcode/search/cart workstation", "Puesto principal de escaneo/busqueda/carrito");
        EXACT_ES.put("Tender allocation, rounding visibility, and sale commit", "Asignacion de pagos, visibilidad de redondeo y cierre de venta");
        EXACT_ES.put("Return/refund workflows with approval context", "Flujos de devolucion/reembolso con contexto de aprobacion");
        EXACT_ES.put("Catalog, pricing, customer, and supplier maintenance", "Mantenimiento de catalogo, precios, clientes y proveedores");
        EXACT_ES.put("Operational reports and CSV exports", "Reportes operativos y exportaciones CSV");
        EXACT_ES.put("Receipt print/reprint and drawer controls", "Impresion/reimpresion de recibos y control de caja");

        EXACT_ES.put("Session: AUTHENTICATED", "Sesion: AUTENTICADA");
        EXACT_ES.put("Session: GUEST", "Sesion: INVITADO");
        EXACT_ES.put("Token expiry: ", "Expiracion de token: ");
        EXACT_ES.put("Connectivity: ONLINE", "Conectividad: EN LINEA");
        EXACT_ES.put("Connectivity: OFFLINE", "Conectividad: SIN LINEA");
        EXACT_ES.put("Retry Connectivity", "Reintentar conectividad");
        EXACT_ES.put("Sign Out", "Cerrar sesion");
        EXACT_ES.put("n/a", "n/d");

        EXACT_ES.put("Username", "Usuario");
        EXACT_ES.put("Password", "Contrasena");
        EXACT_ES.put("Enter credentials to start or resume a cashier session.", "Ingrese credenciales para iniciar o reanudar una sesion de cajero.");

        EXACT_ES.put("Shift lifecycle controls", "Controles de ciclo de turno");
        EXACT_ES.put("Load existing shift", "Cargar turno existente");
        EXACT_ES.put("Open shift", "Abrir turno");
        EXACT_ES.put("Cash movements", "Movimientos de caja");
        EXACT_ES.put("Close and reconcile", "Cerrar y conciliar");
        EXACT_ES.put("Shift ID must be numeric.", "El ID de turno debe ser numerico.");
        EXACT_ES.put("Cashier/terminal/opening cash are required.", "Se requiere cajero/terminal/efectivo de apertura.");
        EXACT_ES.put("Movement amount is required.", "Se requiere monto de movimiento.");
        EXACT_ES.put("Counted close cash is required.", "Se requiere efectivo contado al cierre.");
        EXACT_ES.put("Shift ID", "ID de turno");
        EXACT_ES.put("Load Shift", "Cargar turno");
        EXACT_ES.put("Cashier user ID", "ID de usuario cajero");
        EXACT_ES.put("Terminal device ID", "ID de terminal");
        EXACT_ES.put("Opening float (e.g. 120.00)", "Fondo inicial (ej. 120.00)");
        EXACT_ES.put("Open Shift", "Abrir turno");
        EXACT_ES.put("Cash movement amount", "Monto de movimiento de caja");
        EXACT_ES.put("Paid-in / paid-out reason", "Motivo de entrada/salida de caja");
        EXACT_ES.put("Paid-In", "Entrada");
        EXACT_ES.put("Paid-Out", "Salida");
        EXACT_ES.put("Counted close cash", "Efectivo contado al cierre");
        EXACT_ES.put("Close note / variance reason", "Nota de cierre / motivo de diferencia");
        EXACT_ES.put("Close Shift", "Cerrar turno");

        EXACT_ES.put("Sell workstation: create/load cart, scan, search, and edit lines", "Puesto de venta: crear/cargar carrito, escanear, buscar y editar lineas");
        EXACT_ES.put("Cart context", "Contexto del carrito");
        EXACT_ES.put("Barcode scanner flow", "Flujo de escaner de codigo de barras");
        EXACT_ES.put("Product search", "Busqueda de productos");
        EXACT_ES.put("Cart lines", "Lineas del carrito");
        EXACT_ES.put("Edit line quantity", "Editar cantidad de linea");
        EXACT_ES.put("Remove line", "Eliminar linea");
        EXACT_ES.put("Suspended sales", "Ventas suspendidas");
        EXACT_ES.put("Sensitive line controls", "Controles sensibles de linea");

        EXACT_ES.put("Store location ID", "ID de sucursal");
        EXACT_ES.put("Create Cart", "Crear carrito");
        EXACT_ES.put("Load cart by ID", "Cargar carrito por ID");
        EXACT_ES.put("Load Cart", "Cargar carrito");
        EXACT_ES.put("Merchant ID", "ID de comercio");
        EXACT_ES.put("Scan barcode and press Enter", "Escanee codigo de barras y presione Enter");
        EXACT_ES.put("Scan quantity (default 1)", "Cantidad de escaneo (por defecto 1)");
        EXACT_ES.put("Search products", "Buscar productos");
        EXACT_ES.put("Page", "Pagina");
        EXACT_ES.put("Search", "Buscar");
        EXACT_ES.put("Prev", "Anterior");
        EXACT_ES.put("Next", "Siguiente");
        EXACT_ES.put("Quick add quantity", "Cantidad rapida a agregar");
        EXACT_ES.put("Add Selected", "Agregar seleccionado");
        EXACT_ES.put("Line ID", "ID de linea");
        EXACT_ES.put("New quantity", "Nueva cantidad");
        EXACT_ES.put("Update Line", "Actualizar linea");
        EXACT_ES.put("Line ID to remove", "ID de linea a eliminar");
        EXACT_ES.put("Remove Line", "Eliminar linea");
        EXACT_ES.put("Recalculate Totals", "Recalcular totales");
        EXACT_ES.put("Load Sell Permissions", "Cargar permisos de venta");
        EXACT_ES.put("Park note (optional)", "Nota de estacionamiento (opcional)");
        EXACT_ES.put("Park Cart", "Estacionar carrito");
        EXACT_ES.put("List Parked", "Listar estacionados");
        EXACT_ES.put("Parked cart ID", "ID de carrito estacionado");
        EXACT_ES.put("Resume Parked", "Reanudar estacionado");
        EXACT_ES.put("Line ID for void/override", "ID de linea para anular/sobrescribir");
        EXACT_ES.put("Override reason code", "Codigo de motivo de sobrescritura");
        EXACT_ES.put("Override note (optional)", "Nota de sobrescritura (opcional)");
        EXACT_ES.put("Override unit price", "Precio unitario sobrescrito");
        EXACT_ES.put("Void Line", "Anular linea");
        EXACT_ES.put("Override Price", "Sobrescribir precio");
        EXACT_ES.put("Go To Checkout", "Ir a cobro");

        EXACT_ES.put("Checkout workstation: capture tenders, verify due/change, and commit sale", "Puesto de cobro: capturar pagos, verificar saldo/cambio y confirmar venta");
        EXACT_ES.put("Checkout context", "Contexto de cobro");
        EXACT_ES.put("Tenders", "Pagos");
        EXACT_ES.put("Cash amount", "Monto en efectivo");
        EXACT_ES.put("Cash tendered", "Efectivo entregado");
        EXACT_ES.put("Card amount", "Monto en tarjeta");
        EXACT_ES.put("Card reference", "Referencia de tarjeta");
        EXACT_ES.put("Complete Checkout", "Completar cobro");
        EXACT_ES.put("Back To Sell", "Volver a venta");

        EXACT_ES.put("Manager approval required: have a manager sign in and retry this return.", "Se requiere aprobacion de gerente: pida que un gerente inicie sesion y reintente esta devolucion.");
        EXACT_ES.put("Receipt number", "Numero de recibo");
        EXACT_ES.put("Lookup Receipt", "Buscar recibo");
        EXACT_ES.put("Sale line ID", "ID de linea de venta");
        EXACT_ES.put("Return quantity", "Cantidad a devolver");
        EXACT_ES.put("Reason code (e.g. DAMAGED)", "Codigo de motivo (ej. DAMAGED)");
        EXACT_ES.put("Refund tender type: CASH or CARD", "Tipo de reembolso: CASH o CARD");
        EXACT_ES.put("Refund reference (optional)", "Referencia de reembolso (opcional)");
        EXACT_ES.put("Note (optional)", "Nota (opcional)");
        EXACT_ES.put("Use Selected Line", "Usar linea seleccionada");
        EXACT_ES.put("Submit Return", "Enviar devolucion");
        EXACT_ES.put("Returns workstation: lookup receipt, review eligible lines, and submit refunds", "Puesto de devoluciones: buscar recibo, revisar lineas elegibles y enviar reembolsos");

        EXACT_ES.put("Catalog merchant ID", "ID de comercio de catalogo");
        EXACT_ES.put("Catalog search (SKU/name)", "Busqueda de catalogo (SKU/nombre)");
        EXACT_ES.put("Load Products", "Cargar productos");
        EXACT_ES.put("Product ID (for update)", "ID de producto (para actualizar)");
        EXACT_ES.put("SKU", "SKU");
        EXACT_ES.put("Name", "Nombre");
        EXACT_ES.put("Base price", "Precio base");
        EXACT_ES.put("Barcode(s), comma-separated", "Codigo(s) de barras, separados por coma");
        EXACT_ES.put("Create Product", "Crear producto");
        EXACT_ES.put("Update Product", "Actualizar producto");
        EXACT_ES.put("Customer merchant ID", "ID de comercio de clientes");
        EXACT_ES.put("Load Customers", "Cargar clientes");
        EXACT_ES.put("Lookup document type", "Tipo de documento de busqueda");
        EXACT_ES.put("Lookup document value", "Valor de documento de busqueda");
        EXACT_ES.put("Lookup email", "Correo de busqueda");
        EXACT_ES.put("Lookup phone", "Telefono de busqueda");
        EXACT_ES.put("Lookup Customers", "Buscar clientes");
        EXACT_ES.put("Customer ID (for update)", "ID de cliente (para actualizar)");
        EXACT_ES.put("Display name", "Nombre para mostrar");
        EXACT_ES.put("Invoice required (true/false)", "Factura requerida (true/false)");
        EXACT_ES.put("Credit enabled (true/false)", "Credito habilitado (true/false)");
        EXACT_ES.put("Document type (optional)", "Tipo de documento (opcional)");
        EXACT_ES.put("Document value (optional)", "Valor de documento (opcional)");
        EXACT_ES.put("Primary email (optional)", "Correo principal (opcional)");
        EXACT_ES.put("Primary phone (optional)", "Telefono principal (opcional)");
        EXACT_ES.put("Create Customer", "Crear cliente");
        EXACT_ES.put("Update Customer", "Actualizar cliente");
        EXACT_ES.put("Product ID", "ID de producto");
        EXACT_ES.put("Customer ID (optional)", "ID de cliente (opcional)");
        EXACT_ES.put("Resolve Store Price", "Resolver precio de sucursal");
        EXACT_ES.put("Lot balance store location ID", "ID de sucursal para saldo por lote");
        EXACT_ES.put("Lot balance product ID (optional)", "ID de producto para saldo por lote (opcional)");
        EXACT_ES.put("Lot-level balances", "Saldos por lote");
        EXACT_ES.put("Load Lot/Expiry Balances", "Cargar saldos de lote/vencimiento");
        EXACT_ES.put("Supplier return ID", "ID de devolucion a proveedor");
        EXACT_ES.put("Supplier ID", "ID de proveedor");
        EXACT_ES.put("Return store location ID", "ID de sucursal de devolucion");
        EXACT_ES.put("Return product ID", "ID de producto devuelto");
        EXACT_ES.put("Return quantity", "Cantidad de devolucion");
        EXACT_ES.put("Return unit cost", "Costo unitario de devolucion");
        EXACT_ES.put("Supplier return note", "Nota de devolucion a proveedor");
        EXACT_ES.put("Create Supplier Return", "Crear devolucion a proveedor");
        EXACT_ES.put("Load Supplier Return", "Cargar devolucion a proveedor");
        EXACT_ES.put("Approve Return", "Aprobar devolucion");
        EXACT_ES.put("Post Return", "Registrar devolucion");
        EXACT_ES.put("Backoffice workspace: catalog, pricing, customers, lot/expiry inventory, and supplier returns", "Espacio backoffice: catalogo, precios, clientes, inventario por lote/vencimiento y devoluciones a proveedor");
        EXACT_ES.put("Catalog", "Catalogo");
        EXACT_ES.put("Customers", "Clientes");
        EXACT_ES.put("Pricing", "Precios");
        EXACT_ES.put("Lot/Expiry inventory", "Inventario por lote/vencimiento");
        EXACT_ES.put("Supplier returns", "Devoluciones a proveedor");

        EXACT_ES.put("From (ISO-8601, optional)", "Desde (ISO-8601, opcional)");
        EXACT_ES.put("To (ISO-8601, optional)", "Hasta (ISO-8601, opcional)");
        EXACT_ES.put("Store location ID (optional)", "ID de sucursal (opcional)");
        EXACT_ES.put("Terminal device ID (optional)", "ID de terminal (opcional)");
        EXACT_ES.put("Cashier user ID (optional)", "ID de usuario cajero (opcional)");
        EXACT_ES.put("Category ID (optional)", "ID de categoria (opcional)");
        EXACT_ES.put("Tax group ID (optional)", "ID de grupo fiscal (opcional)");
        EXACT_ES.put("Supplier ID (inventory optional)", "ID de proveedor (opcional para inventario)");
        EXACT_ES.put("Exception reason code (optional)", "Codigo de motivo de excepcion (opcional)");
        EXACT_ES.put("Exception event type (optional)", "Tipo de evento de excepcion (opcional)");
        EXACT_ES.put("Load Sales/Returns", "Cargar ventas/devoluciones");
        EXACT_ES.put("Load Inventory Moves", "Cargar movimientos de inventario");
        EXACT_ES.put("Load Cash Shifts", "Cargar turnos de caja");
        EXACT_ES.put("Load Exceptions", "Cargar excepciones");
        EXACT_ES.put("Export Sales CSV", "Exportar ventas CSV");
        EXACT_ES.put("Export Inventory CSV", "Exportar inventario CSV");
        EXACT_ES.put("Export Cash CSV", "Exportar caja CSV");
        EXACT_ES.put("Export Exceptions CSV", "Exportar excepciones CSV");
        EXACT_ES.put("Reporting workspace: filtered report loads with streaming preview and CSV export actions", "Espacio de reportes: cargas filtradas con vista previa en streaming y exportacion CSV");

        EXACT_ES.put("Refresh Hardware Access", "Actualizar acceso a hardware");
        EXACT_ES.put("Print as copy", "Imprimir como copia");
        EXACT_ES.put("Print Receipt", "Imprimir recibo");
        EXACT_ES.put("Journal lookup receipt number", "Numero de recibo para bitacora");
        EXACT_ES.put("Journal lookup sale ID", "ID de venta para bitacora");
        EXACT_ES.put("Lookup by Receipt", "Buscar por recibo");
        EXACT_ES.put("Lookup by Sale", "Buscar por venta");
        EXACT_ES.put("Reprint Receipt", "Reimprimir recibo");
        EXACT_ES.put("Receipt reprint is hidden because this user is not authorized.", "La reimpresion de recibo esta oculta porque este usuario no esta autorizado.");
        EXACT_ES.put("Reason code", "Codigo de motivo");
        EXACT_ES.put("Reference number (optional)", "Numero de referencia (opcional)");
        EXACT_ES.put("Open Drawer", "Abrir cajon");
        EXACT_ES.put("Drawer controls are hidden because this user is not authorized.", "Los controles de cajon estan ocultos porque este usuario no esta autorizado.");
        EXACT_ES.put("Hardware workspace: receipt print status and role-gated drawer controls", "Espacio de hardware: estado de impresion y controles de cajon segun rol");
        EXACT_ES.put("Receipt print", "Impresion de recibo");
        EXACT_ES.put("Receipt journal + reprint", "Bitacora de recibos + reimpresion");
        EXACT_ES.put("Cash drawer", "Cajon de efectivo");

        EXACT_ES.put("No cart loaded.", "No hay carrito cargado.");
        EXACT_ES.put("No shift loaded.", "No hay turno cargado.");
        EXACT_ES.put("No completed checkout yet.", "Aun no hay cobro completado.");
        EXACT_ES.put("No receipt lookup loaded.", "No hay busqueda de recibo cargada.");
        EXACT_ES.put("No return submitted yet.", "Aun no hay devolucion enviada.");
        EXACT_ES.put("No price resolution requested yet.", "Aun no se solicito resolucion de precio.");
        EXACT_ES.put("No supplier return selected.", "No hay devolucion a proveedor seleccionada.");
        EXACT_ES.put("No receipt journal lookup loaded.", "No hay consulta de bitacora de recibos cargada.");
        EXACT_ES.put("Print status: idle.", "Estado de impresion: inactivo.");
        EXACT_ES.put("Print status: queued.", "Estado de impresion: en cola.");
        EXACT_ES.put("Drawer status: idle.", "Estado de cajon: inactivo.");
        EXACT_ES.put("Drawer status: queued.", "Estado de cajon: en cola.");

        EXACT_ES.put("Please sign in.", "Por favor inicie sesion.");
        EXACT_ES.put("Cannot sign in while offline. Reconnect to continue.", "No se puede iniciar sesion sin conexion. Reconecte para continuar.");
        EXACT_ES.put("Signed out.", "Sesion cerrada.");
        EXACT_ES.put("Authentication is required for this screen.", "Se requiere autenticacion para esta pantalla.");
        EXACT_ES.put("Session expired. Please sign in again.", "La sesion expiro. Inicie sesion nuevamente.");
        EXACT_ES.put("Session refreshed.", "Sesion actualizada.");
        EXACT_ES.put("Invalid username or password.", "Usuario o contrasena invalidos.");
        EXACT_ES.put("Account is temporarily locked.", "La cuenta esta bloqueada temporalmente.");
        EXACT_ES.put("Account is disabled.", "La cuenta esta deshabilitada.");
        EXACT_ES.put("Unable to sign in. Verify server connectivity and try again.", "No se pudo iniciar sesion. Verifique conectividad y reintente.");
        EXACT_ES.put("Session refresh failed. Please sign in again.", "Fallo la renovacion de sesion. Inicie sesion nuevamente.");

        EXACT_ES.put("No active shift loaded.", "No hay turno activo cargado.");
        EXACT_ES.put("Open or load a shift before closing.", "Abra o cargue un turno antes de cerrar.");
        EXACT_ES.put("Shift closed.", "Turno cerrado.");
        EXACT_ES.put("Open or load an active shift first.", "Abra o cargue primero un turno activo.");
        EXACT_ES.put("Shift open and ready for cash controls.", "Turno abierto y listo para controles de caja.");
        EXACT_ES.put("Shift request failed. Verify inputs and connectivity.", "Fallo la solicitud de turno. Verifique datos y conectividad.");
        EXACT_ES.put("Paid-in recorded.", "Entrada de caja registrada.");
        EXACT_ES.put("Paid-out recorded.", "Salida de caja registrada.");

        EXACT_ES.put("Create or load a cart to begin selling.", "Cree o cargue un carrito para comenzar a vender.");
        EXACT_ES.put("Cart changes are unavailable offline. Reconnect and try again.", "Los cambios de carrito no estan disponibles sin conexion. Reconecte e intente de nuevo.");
        EXACT_ES.put("Create or load an ACTIVE cart before scanning.", "Cree o cargue un carrito ACTIVO antes de escanear.");
        EXACT_ES.put("Create or load an ACTIVE cart before adding products.", "Cree o cargue un carrito ACTIVO antes de agregar productos.");
        EXACT_ES.put("Create or load an ACTIVE cart before editing lines.", "Cree o cargue un carrito ACTIVO antes de editar lineas.");
        EXACT_ES.put("Create or load an ACTIVE cart before removing lines.", "Cree o cargue un carrito ACTIVO antes de eliminar lineas.");
        EXACT_ES.put("Create or load an ACTIVE cart before recalculation.", "Cree o cargue un carrito ACTIVO antes de recalcular.");
        EXACT_ES.put("Create or load an ACTIVE cart before parking.", "Cree o cargue un carrito ACTIVO antes de estacionar.");
        EXACT_ES.put("Create or load an ACTIVE cart before line void.", "Cree o cargue un carrito ACTIVO antes de anular linea.");
        EXACT_ES.put("Create or load an ACTIVE cart before price override.", "Cree o cargue un carrito ACTIVO antes de sobrescribir precio.");
        EXACT_ES.put("Create or load an ACTIVE cart before checkout.", "Cree o cargue un carrito ACTIVO antes de cobrar.");
        EXACT_ES.put("Sale cannot be completed offline. Reconnect to finalize payment.", "La venta no se puede completar sin conexion. Reconecte para finalizar el pago.");

        EXACT_ES.put("Lookup a receipt to begin a return.", "Busque un recibo para comenzar una devolucion.");
        EXACT_ES.put("Receipt number is required.", "Se requiere numero de recibo.");
        EXACT_ES.put("Lookup a receipt before submitting a return.", "Busque un recibo antes de enviar una devolucion.");
        EXACT_ES.put("Sale line and quantity greater than zero are required.", "Se requiere linea de venta y cantidad mayor que cero.");
        EXACT_ES.put("Reason code is required.", "Se requiere codigo de motivo.");
        EXACT_ES.put("Refund tender type is required.", "Se requiere tipo de reembolso.");
        EXACT_ES.put("Select a return line first.", "Seleccione primero una linea de devolucion.");
        EXACT_ES.put("Return request failed. Verify inputs and connectivity.", "Fallo la solicitud de devolucion. Verifique datos y conectividad.");

        EXACT_ES.put("Backoffice ready: manage catalog, pricing, customers, lots, and supplier returns.", "Backoffice listo: gestione catalogo, precios, clientes, lotes y devoluciones a proveedor.");
        EXACT_ES.put("Backoffice operation failed. Verify inputs and connectivity.", "Fallo la operacion de backoffice. Verifique datos y conectividad.");

        EXACT_ES.put("No report loaded.", "No hay reporte cargado.");
        EXACT_ES.put("Reporting ready: load filters and run a report.", "Reportes listos: cargue filtros y ejecute un reporte.");
        EXACT_ES.put("Sales/returns report loaded.", "Reporte de ventas/devoluciones cargado.");
        EXACT_ES.put("Reporting operation failed. Verify filters and connectivity.", "Fallo la operacion de reportes. Verifique filtros y conectividad.");

        EXACT_ES.put("Hardware ready: use receipt print and drawer controls.", "Hardware listo: use impresion de recibos y controles de cajon.");
        EXACT_ES.put("Receipt print request completed.", "Solicitud de impresion completada.");
        EXACT_ES.put("Receipt reprint request completed.", "Solicitud de reimpresion completada.");
        EXACT_ES.put("Drawer open request completed.", "Solicitud de apertura de cajon completada.");
        EXACT_ES.put("Hardware failed. Verify connectivity and permissions.", "Fallo de hardware. Verifique conectividad y permisos.");

        TOKEN_ES.put("Cart #", "Carrito #");
        TOKEN_ES.put("Shift #", "Turno #");
        TOKEN_ES.put("Sale #", "Venta #");
        TOKEN_ES.put("Return #", "Devolucion #");
        TOKEN_ES.put("SupplierReturn #", "DevolucionProveedor #");
        TOKEN_ES.put("Store=", "Sucursal=");
        TOKEN_ES.put("No cart loaded.", "No hay carrito cargado.");
        TOKEN_ES.put("No shift loaded.", "No hay turno cargado.");
        TOKEN_ES.put("No completed checkout yet.", "Aun no hay cobro completado.");
        TOKEN_ES.put("No receipt lookup loaded.", "No hay busqueda de recibo cargada.");
        TOKEN_ES.put("No return submitted yet.", "Aun no hay devolucion enviada.");
        TOKEN_ES.put("No price resolution requested yet.", "Aun no se solicito resolucion de precio.");
        TOKEN_ES.put("No supplier return selected.", "No hay devolucion a proveedor seleccionada.");
        TOKEN_ES.put("No receipt journal lookup loaded.", "No hay consulta de bitacora de recibos cargada.");
        TOKEN_ES.put("No cart loaded.", "No hay carrito cargado.");
        TOKEN_ES.put("Payable=", "Pagable=");
        TOKEN_ES.put("Allocated=", "Asignado=");
        TOKEN_ES.put("Due=", "Pendiente=");
        TOKEN_ES.put("Est. Change=", "Cambio est.=");
        TOKEN_ES.put("Print status:", "Estado de impresion:");
        TOKEN_ES.put("Drawer status:", "Estado de cajon:");
        TOKEN_ES.put(" | status=", " | estado=");
        TOKEN_ES.put(" | lines=", " | lineas=");
        TOKEN_ES.put(" | subtotal=", " | subtotal=");
        TOKEN_ES.put(" | tax=", " | impuesto=");
        TOKEN_ES.put(" | rounding=", " | redondeo=");
        TOKEN_ES.put(" | payable=", " | pagable=");
        TOKEN_ES.put(" | opening=", " | apertura=");
        TOKEN_ES.put(" | paidIn=", " | entradas=");
        TOKEN_ES.put(" | paidOut=", " | salidas=");
        TOKEN_ES.put(" | expectedClose=", " | cierreEsperado=");
        TOKEN_ES.put(" | counted=", " | contado=");
        TOKEN_ES.put(" | variance=", " | diferencia=");
        TOKEN_ES.put(" | receipt=", " | recibo=");
        TOKEN_ES.put(" | allocated=", " | asignado=");
        TOKEN_ES.put(" | tendered=", " | entregado=");
        TOKEN_ES.put(" | change=", " | cambio=");
        TOKEN_ES.put(" | soldAt=", " | vendidoEn=");
        TOKEN_ES.put(" | ref=", " | ref=");
        TOKEN_ES.put(" | tender=", " | pago=");
        TOKEN_ES.put(" | gross=", " | bruto=");
        TOKEN_ES.put(" | product=", " | producto=");
        TOKEN_ES.put(" | resolved=", " | resuelto=");
        TOKEN_ES.put(" | source=", " | origen=");
        TOKEN_ES.put(" | sourceId=", " | idOrigen=");
        TOKEN_ES.put(" | totalCost=", " | costoTotal=");
        TOKEN_ES.put(" | store=", " | sucursal=");
        TOKEN_ES.put(" | terminal=", " | terminal=");
        TOKEN_ES.put(" | cashier=", " | cajero=");
        TOKEN_ES.put(" | adapter=", " | adaptador=");
        TOKEN_ES.put(" | retryable=", " | reintentable=");
        TOKEN_ES.put(" | eventId=", " | idEvento=");
        TOKEN_ES.put(" | line=", " | linea=");
        TOKEN_ES.put(" | sold=", " | vendido=");
        TOKEN_ES.put(" | returned=", " | devuelto=");
        TOKEN_ES.put(" | available=", " | disponible=");
        TOKEN_ES.put(" | qty=", " | cant=");
        TOKEN_ES.put(" | unit=", " | unit=");
        TOKEN_ES.put(" | total=", " | total=");
        TOKEN_ES.put(" | parkedAt=", " | estacionadoEn=");
        TOKEN_ES.put(" | invoiceRequired=", " | facturaRequerida=");
        TOKEN_ES.put(" | creditEnabled=", " | creditoHabilitado=");
        TOKEN_ES.put(" | lot=", " | lote=");
        TOKEN_ES.put(" | expiry=", " | vencimiento=");
        TOKEN_ES.put(" | state=", " | estado=");
        TOKEN_ES.put("ACTIVE", "ACTIVO");
        TOKEN_ES.put("INACTIVE", "INACTIVO");
        TOKEN_ES.put(" enabled", " habilitado");
        TOKEN_ES.put(" restricted", " restringido");
        TOKEN_ES.put("yes", "si");
    }

    private final ObjectProperty<UiLanguage> language = new SimpleObjectProperty<>(UiLanguage.ENGLISH);

    public ObjectProperty<UiLanguage> languageProperty() {
        return language;
    }

    public UiLanguage language() {
        return language.get();
    }

    public void setLanguage(UiLanguage language) {
        this.language.set(language == null ? UiLanguage.ENGLISH : language);
    }

    public StringBinding bindTranslated(ObservableValue<String> source) {
        return Bindings.createStringBinding(() -> translate(source.getValue()), source, language);
    }

    public String translate(String source) {
        if (source == null) {
            return null;
        }
        if (language() == UiLanguage.ENGLISH) {
            return source;
        }
        return translateSpanish(source);
    }

    private String translateSpanish(String source) {
        String exact = EXACT_ES.get(source);
        if (exact != null) {
            return exact;
        }

        Matcher matcher = NAVIGATION_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Navegaste a " + translateNavigationTarget(matcher.group(1));
        }

        matcher = SESSION_ACTIVE_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Sesion activa para " + matcher.group(1) + ".";
        }

        matcher = SHIFT_STATUS_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Turno cargado con estado " + matcher.group(1) + ".";
        }

        matcher = SEARCH_RETURNED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "La busqueda devolvio " + matcher.group(1) + " productos.";
        }

        matcher = PARKED_LOADED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Se cargaron " + matcher.group(1) + " carritos estacionados.";
        }

        matcher = RECEIPT_GENERATED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Cobro completado. Recibo " + matcher.group(1) + " generado.";
        }

        matcher = RECEIPT_LOADED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Recibo cargado. " + matcher.group(1) + " lineas elegibles para revision de devolucion.";
        }

        matcher = RETURN_REFERENCE_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Devolucion enviada. Referencia " + matcher.group(1) + " creada.";
        }

        matcher = CATALOG_LOADED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Catalogo cargado con " + matcher.group(1) + " producto(s).";
        }

        matcher = CUSTOMER_LIST_LOADED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Listado de clientes cargado con " + matcher.group(1) + " registro(s).";
        }

        matcher = CUSTOMER_LOOKUP_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "La busqueda de clientes devolvio " + matcher.group(1) + " registro(s).";
        }

        matcher = INVENTORY_BALANCE_LOADED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Se cargaron " + matcher.group(1) + " registro(s) de saldo de inventario.";
        }

        matcher = RESOLVED_PRICE_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Precio resuelto " + matcher.group(1) + " desde " + matcher.group(2) + ".";
        }

        matcher = SUPPLIER_RETURN_STATUS_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Devolucion a proveedor cargada: " + matcher.group(1) + " (" + matcher.group(2) + ").";
        }

        matcher = SUPPLIER_RETURN_CREATED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Devolucion a proveedor creada: " + matcher.group(1);
        }

        matcher = SUPPLIER_RETURN_APPROVED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Devolucion a proveedor aprobada: " + matcher.group(1) + ".";
        }

        matcher = SUPPLIER_RETURN_POSTED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Devolucion a proveedor registrada: " + matcher.group(1) + ".";
        }

        matcher = RECEIPT_PRINT_QUEUED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Impresion de recibo en cola para " + matcher.group(1) + ".";
        }

        matcher = RECEIPT_REPRINT_QUEUED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Reimpresion de recibo en cola para " + matcher.group(1) + ".";
        }

        matcher = DRAWER_QUEUED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Apertura de cajon en cola para terminal " + matcher.group(1) + ".";
        }

        matcher = JOURNAL_LOADING_SALE_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Cargando bitacora de recibo para venta " + matcher.group(1) + ".";
        }

        matcher = JOURNAL_LOADING_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Cargando bitacora de recibo para " + matcher.group(1) + ".";
        }

        matcher = JOURNAL_LOADED_SALE_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Bitacora de recibo cargada para venta " + matcher.group(1) + ".";
        }

        matcher = JOURNAL_LOADED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Bitacora de recibo cargada para " + matcher.group(1) + ".";
        }

        matcher = SALES_EXPORT_READY_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Exportacion CSV de ventas/devoluciones lista: " + matcher.group(1) + " caracteres.";
        }

        matcher = INVENTORY_EXPORT_READY_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Exportacion CSV de movimientos de inventario lista: " + matcher.group(1) + " caracteres.";
        }

        matcher = CASH_EXPORT_READY_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Exportacion CSV de turnos de caja lista: " + matcher.group(1) + " caracteres.";
        }

        matcher = EXCEPTION_EXPORT_READY_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Exportacion CSV de excepciones lista: " + matcher.group(1) + " caracteres.";
        }

        matcher = REQUEST_FAILED_PATTERN.matcher(source);
        if (matcher.matches()) {
            return "Solicitud fallida: " + matcher.group(1);
        }

        String translated = source;
        for (Map.Entry<String, String> entry : TOKEN_ES.entrySet()) {
            translated = translated.replace(entry.getKey(), entry.getValue());
        }
        return translated;
    }

    private String translateNavigationTarget(String name) {
        if ("LOGIN".equals(name)) {
            return "Iniciar sesion";
        }
        if ("SHIFT_CONTROL".equals(name)) {
            return "Turno";
        }
        if ("SELL".equals(name)) {
            return "Venta";
        }
        if ("CHECKOUT".equals(name)) {
            return "Cobro";
        }
        if ("RETURNS".equals(name)) {
            return "Devoluciones";
        }
        if ("BACKOFFICE".equals(name)) {
            return "Backoffice";
        }
        if ("REPORTING".equals(name)) {
            return "Reportes";
        }
        if ("HARDWARE".equals(name)) {
            return "Hardware";
        }
        return name;
    }
}
