import React, { useEffect, useState } from "react";
import { listOrders } from "../api";
import { Link } from "react-router-dom";

export default function OrdersList() {
  const [data, setData] = useState(null);
  const [page, setPage] = useState(0);
  const size = 10;

  useEffect(() => {
    let ignore = false;
    listOrders({ page, size })
      .then(res => { if (!ignore) setData(res); })
      .catch(e => { if (!ignore) setData({ error: e.message || "Failed to load" }); });
    return () => { ignore = true; };
  }, [page]);

  if (!data) return <p>Loading…</p>;
  if (data.error) return <p style={{color:"crimson"}}>Error: {data.error}</p>;

  const canNext = (data.page + 1) * data.size < data.total;

  return (
    <div style={{maxWidth:800, margin:"24px auto", padding:16}}>
      <h2>My Orders</h2>
      {data.items?.length === 0 ? (
        <p>No orders yet.</p>
      ) : (
        <ul>
          {data.items.map(o => (
            <li key={o.id}>
              <Link to={`/orders/${o.id}`}>#{o.id}</Link> — ₹{o.total} — {new Date(o.createdAt).toLocaleString()}
            </li>
          ))}
        </ul>
      )}
      <div style={{display:"flex", gap:8, alignItems:"center"}}>
        <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>Prev</button>
        <span>Page {page + 1}</span>
        <button onClick={() => setPage(p => p + 1)} disabled={!canNext}>Next</button>
      </div>
    </div>
  );
}
