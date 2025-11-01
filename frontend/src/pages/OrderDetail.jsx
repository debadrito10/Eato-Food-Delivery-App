import React, { useEffect, useState } from "react";
import { getOrder } from "../api";
import { useParams } from "react-router-dom";

export default function OrderDetail() {
  const { id } = useParams();
  const [data, setData] = useState(null);
  const [err, setErr] = useState("");

  useEffect(() => {
    let ignore = false;
    getOrder(id)
      .then(res => { if (!ignore) setData(res); })
      .catch(e => { if (!ignore) setErr(e.message || "Failed to load"); });
    return () => { ignore = true; };
  }, [id]);

  if (err) return <p style={{color:"crimson"}}>Error: {err}</p>;
  if (!data) return <p>Loading…</p>;

  return (
    <div style={{maxWidth:800, margin:"24px auto", padding:16}}>
      <h2>Order #{data.id}</h2>
      <p>Total: ₹{data.total}</p>
      <p>When: {new Date(data.createdAt).toLocaleString()}</p>
      <h3>Items</h3>
      <ul>
        {data.items?.map(it => (
          <li key={`${it.dishId}-${it.priceAtOrder}`}>
            {it.name} × {it.qty} — ₹{it.priceAtOrder}
          </li>
        ))}
      </ul>
      {data.address && <pre>{JSON.stringify(JSON.parse(data.address), null, 2)}</pre>}
    </div>
  );
}
