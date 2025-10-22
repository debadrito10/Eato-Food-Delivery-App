const base = process.env.REACT_APP_API_BASE || "http://localhost:8081";

export async function searchRestaurants(q = "", page = 0, size = 10, city = "Kolkata") {
  const params = new URLSearchParams({ q, page, size, city });
  const res = await fetch(`${base}/api/restaurants?` + params.toString(), {
    headers: { "Accept": "application/json" }
  });
  if (!res.ok) throw new Error(`API ${res.status}`);
  return res.json(); // {items, total, page, size}
}

export async function getRestaurant(id) {
  const res = await fetch(`${base}/api/restaurants/${id}`, {
    headers: { "Accept": "application/json" }
  });
  if (!res.ok) throw new Error(`API ${res.status}`);
  return res.json(); // RestaurantDetailDto
}

export async function health() {
  const res = await fetch(`${base}/health`);
  return res.ok;
}
