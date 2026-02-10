import http from "k6/http";
import { check, sleep } from "k6";

const baseUrl = __ENV.BASE_URL || "http://localhost:8080";
const authToken = __ENV.AUTH_TOKEN || "";
const cashierUserId = __ENV.CASHIER_USER_ID || "1";
const storeLocationId = __ENV.STORE_LOCATION_ID || "1";
const terminalDeviceId = __ENV.TERMINAL_DEVICE_ID || "1";
const productId = __ENV.PRODUCT_ID || "1";
const merchantId = __ENV.MERCHANT_ID || "1";

function headersWithAuth(extra = {}) {
  const base = {
    "Content-Type": "application/json",
    ...extra,
  };
  if (authToken) {
    base.Authorization = `Bearer ${authToken}`;
  }
  return base;
}

export const options = {
  stages: [
    { duration: "20s", target: 20 },
    { duration: "40s", target: 20 },
    { duration: "20s", target: 0 },
  ],
  thresholds: {
    "http_req_duration{endpoint:lookup}": ["p(95)<1000"],
    "http_req_duration{endpoint:checkout}": ["p(95)<1800"],
  },
};

export default function () {
  const lookup = http.get(
    `${baseUrl}/api/catalog/products/search?merchantId=${merchantId}&q=load&active=true&page=0&size=10`,
    { headers: headersWithAuth(), tags: { endpoint: "lookup" } },
  );
  check(lookup, {
    "lookup status 200": (r) => r.status === 200,
  });

  const createCartPayload = JSON.stringify({
    cashierUserId: Number(cashierUserId),
    storeLocationId: Number(storeLocationId),
    terminalDeviceId: Number(terminalDeviceId),
    pricingAt: "2026-02-10T10:00:00Z",
  });
  const createCart = http.post(
    `${baseUrl}/api/sales/carts`,
    createCartPayload,
    { headers: headersWithAuth() },
  );
  check(createCart, {
    "create cart status 201": (r) => r.status === 201,
  });
  if (createCart.status !== 201) {
    sleep(0.2);
    return;
  }

  const cartId = createCart.json("id");
  const addLinePayload = JSON.stringify({
    lineKey: `k6-line-${__VU}-${__ITER}`,
    productId: Number(productId),
    quantity: 1,
  });
  const addLine = http.post(
    `${baseUrl}/api/sales/carts/${cartId}/lines`,
    addLinePayload,
    { headers: headersWithAuth() },
  );
  check(addLine, {
    "add line status 200": (r) => r.status === 200,
  });

  const checkoutPayload = JSON.stringify({
    cartId: Number(cartId),
    cashierUserId: Number(cashierUserId),
    terminalDeviceId: Number(terminalDeviceId),
    payments: [
      {
        tenderType: "CASH",
        amount: 5.5,
        tenderedAmount: 5.5,
      },
    ],
  });
  const checkout = http.post(
    `${baseUrl}/api/sales/checkout`,
    checkoutPayload,
    {
      headers: headersWithAuth({
        "Idempotency-Key": `k6-checkout-${__VU}-${__ITER}`,
      }),
      tags: { endpoint: "checkout" },
    },
  );
  check(checkout, {
    "checkout status 200": (r) => r.status === 200,
  });

  sleep(0.1);
}
