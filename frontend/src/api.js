const base = process.env.REACT_APP_API_BASE || "http://localhost:8081";

// --- token handling ---
let token = null;
export function setToken(t) { token = t; if (t) localStorage.setItem("eato_token", t); else localStorage.removeItem("eato_token"); }
export function getToken() { return token || localStorage.getItem("eato_token") || null; }
function authHeaders() { const t = getToken(); return t ? { Authorization: `Bearer ${t}` } : {}; }


// --- small helper for better errors ---
async function asJsonOrThrow(res) {
  if (!res.ok) {
    let msg = `${res.status}`;
    try {
      const data = await res.json();
      if (data && (data.error || data.msg)) msg = data.error || data.msg;
    } catch { /* ignore */ }
    throw new Error(msg);
  }
  return res.json();
}

// --- auth APIs ---
export async function register(email, password) {
  const res = await fetch(`${base}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type":"application/json" },
    body: JSON.stringify({ email, password })
  });
    return asJsonOrThrow(res); // {token,email,userId}

}
export async function login(email, password) {
  const res = await fetch(`${base}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type":"application/json" },
    body: JSON.stringify({ email, password })
  });
  return asJsonOrThrow(res); // {token,email,userId}

}
export async function me() {
  const res = await fetch(`${base}/api/me`, { headers: { ...authHeaders() } });
  return asJsonOrThrow(res); // {id,email}
}

export async function searchRestaurants(q = "", page = 0, size = 10, city = "Kolkata") {
  const params = new URLSearchParams({ q, page, size, city });
  const res = await fetch(`${base}/api/restaurants?` + params.toString(), {
    headers: { "Accept": "application/json" }
  });
    return asJsonOrThrow(res); // {items,total, page,size}

}

export async function getRestaurant(id) {
  const res = await fetch(`${base}/api/restaurants/${id}`, {
    headers: { "Accept": "application/json" }
  });
  return asJsonOrThrow(res); // {restaurantdetails}

}

export async function health() {
  const res = await fetch(`${base}/health`);
  return res.ok;
}
// --- orders (new) ---
/** preview server-authoritative total for cart */
export async function priceCart(items /* [{dishId, qty}] */) {
  const res = await fetch(`${base}/api/cart/price`, {
    method: "POST",
    headers: { "Content-Type":"application/json" },
    body: JSON.stringify({ items })
  });
  return asJsonOrThrow(res); // { total }
}

/** create order (JWT required). address can be a JS object; we stringify here */
export async function createOrder(items, addressObj = null, idemKey /* optional */) {
  const headers = { "Content-Type":"application/json", ...authHeaders() };
  if (idemKey) headers["Idempotency-Key"] = idemKey;

  const body = {
    items,
    address: addressObj ? JSON.stringify(addressObj) : undefined
  };

  const res = await fetch(`${base}/api/orders`, {
    method: "POST",
    headers,
    body: JSON.stringify(body)
  });
  return asJsonOrThrow(res); // { orderId, total }
}

/** list current user's orders (JWT required) */
export async function listOrders({ page = 0, size = 10 } = {}) {
  const params = new URLSearchParams({ page, size });
  const res = await fetch(`${base}/api/orders?${params.toString()}`, {
    headers: { "Accept":"application/json", ...authHeaders() }
  });
  return asJsonOrThrow(res); // PageDto<OrderListItem>
}

/** get detail of one order (JWT required) */
export async function getOrder(id) {
  const res = await fetch(`${base}/api/orders/${id}`, {
    headers: { "Accept":"application/json", ...authHeaders() }
  });
  return asJsonOrThrow(res); // OrderDetail
}