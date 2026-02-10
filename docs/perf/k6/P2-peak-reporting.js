import http from "k6/http";
import { check, sleep } from "k6";

const baseUrl = __ENV.BASE_URL || "http://localhost:8080";
const authToken = __ENV.AUTH_TOKEN || "";
const from = __ENV.FROM || "2026-02-01T00:00:00Z";
const to = __ENV.TO || "2026-02-10T23:59:59Z";
const storeLocationId = __ENV.STORE_LOCATION_ID || "";

function headersWithAuth() {
  if (!authToken) {
    return {};
  }
  return { Authorization: `Bearer ${authToken}` };
}

function withOptionalStore(path) {
  if (!storeLocationId) {
    return `${baseUrl}${path}?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`;
  }
  return `${baseUrl}${path}?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&storeLocationId=${encodeURIComponent(storeLocationId)}`;
}

export const options = {
  stages: [
    { duration: "20s", target: 10 },
    { duration: "40s", target: 10 },
    { duration: "20s", target: 0 },
  ],
};

export default function () {
  const sales = http.get(withOptionalStore("/api/reports/sales"), { headers: headersWithAuth() });
  check(sales, { "sales report 200": (r) => r.status === 200 });

  const inventory = http.get(withOptionalStore("/api/reports/inventory/movements"), { headers: headersWithAuth() });
  check(inventory, { "inventory report 200": (r) => r.status === 200 });

  const cash = http.get(withOptionalStore("/api/reports/cash/shifts"), { headers: headersWithAuth() });
  check(cash, { "cash report 200": (r) => r.status === 200 });

  const exceptions = http.get(withOptionalStore("/api/reports/exceptions"), { headers: headersWithAuth() });
  check(exceptions, { "exceptions report 200": (r) => r.status === 200 });

  sleep(0.2);
}
