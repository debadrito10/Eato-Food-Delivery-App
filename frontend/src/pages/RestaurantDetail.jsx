import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { getRestaurant } from "../api";

export default function RestaurantDetail() {
  const { id } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState("");

  useEffect(() => {
    let ignore = false;
    setLoading(true); setErr("");
    getRestaurant(id)
      .then(res => { if (!ignore) setData(res); })
      .catch(e => { if (!ignore) setErr(e.message || "Failed to load"); })
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, [id]);

  if (loading) return <div style={{padding:16}}>Loading…</div>;
  if (err) return <div style={{padding:16, color:"crimson"}}>{err}</div>;
  if (!data) return <div style={{padding:16}}>Not found</div>;

  return (
    <div style={{maxWidth: 800, margin: "24px auto", padding: 16}}>
      <Link to="/" style={{textDecoration:"none"}}>← Back</Link>
      <h2 style={{margin:"12px 0 8px"}}>{data.name}</h2>
      <div style={{color:"#666", marginBottom:12}}>
        {data.cuisine} · {data.city} {data.pincode || ""}
      </div>

      <div style={{border:"1px solid #eee", borderRadius:8, padding:12, marginBottom:16}}>
        <div style={{fontWeight:600, marginBottom:8}}>Location</div>
        <div>Lat: {data.lat} · Lng: {data.lng}</div>
        <a href={`https://www.google.com/maps?q=${data.lat},${data.lng}`} target="_blank" rel="noreferrer">
          Open in Google Maps
        </a>
      </div>

      <div>
        <div style={{fontWeight:600, marginBottom:8}}>Menu</div>
        {(!data.dishes || data.dishes.length===0) ? <div>No dishes yet</div> : (
          <ul style={{listStyle:"none", padding:0, margin:0, display:"grid", gap:8}}>
            {data.dishes.map(d => (
              <li key={d.id} style={{border:"1px solid #eee", borderRadius:8, padding:"8px 12px", display:"flex", justifyContent:"space-between"}}>
                <span>{d.name}</span>
                <span>₹{d.price}</span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
