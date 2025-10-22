import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { searchRestaurants } from "../api";

export default function Home() {
  const [params, setParams] = useSearchParams();
  const [data, setData] = useState({ items: [], total: 0, page: 0, size: 10 });
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState("");

  const q = params.get("q") || "";
  const page = parseInt(params.get("page") || "0", 10);
  const size = parseInt(params.get("size") || "10", 10);

  const [qInput, setQInput] = useState(q);
  useEffect(() => setQInput(q), [q]);
  const debouncedQ = useDebounce(qInput, 300);

  useEffect(() => {
    let ignore = false;
    setLoading(true); setErr("");
    searchRestaurants(debouncedQ, page, size, "Kolkata")
      .then(r => { if (!ignore) setData(r); })
      .catch(e => { if (!ignore) setErr(e.message || "Failed to load"); })
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, [debouncedQ, page, size]);

  function onSearchChange(val) {
    setQInput(val);
    const p = new URLSearchParams(params);
    p.set("q", val);
    p.set("page", "0");
    p.set("size", String(size));
    setParams(p);
  }

  const totalPages = useMemo(
    () => Math.max(1, Math.ceil((data.total || 0) / size)),
    [data.total, size]
  );

  return (
    <div style={{maxWidth: 900, margin: "24px auto", padding: 16}}>
      <h1 style={{marginBottom: 12}}>Eato — Kolkata restaurants</h1>

      <div style={{display:"flex", gap:12, marginBottom:16}}>
        <input
          value={qInput}
          onChange={e => onSearchChange(e.target.value)}
          placeholder="Search by name or cuisine (e.g., biryani, Bengali)"
          style={{flex:1, padding:"8px 12px"}}
        />
      </div>

      {loading && <div>Loading…</div>}
      {err && <div style={{color:"crimson"}}>{err}</div>}
      {!loading && !err && data.items.length === 0 && <div>No results</div>}

      <ul style={{listStyle:"none", padding:0, margin:0, display:"grid", gap:12}}>
        {data.items.map(r => (
          <li key={r.id} style={{border:"1px solid #e5e5e5", borderRadius:8, padding:12}}>
            <div style={{display:"flex", justifyContent:"space-between", alignItems:"center", gap:12}}>
              <div>
                <div style={{fontWeight:600}}>{r.name}</div>
                <div style={{fontSize:14, color:"#666"}}>{r.cuisine} · {r.city} {r.pincode || ""}</div>
              </div>
              <Link to={`/restaurant/${r.id}`} style={{textDecoration:"none"}}>View →</Link>
            </div>
          </li>
        ))}
      </ul>

      <Pager
        page={page}
        totalPages={totalPages}
        onPrev={() => navTo(page-1)}
        onNext={() => navTo(page+1)}
        disabledPrev={page<=0}
        disabledNext={page>=totalPages-1}
      />
    </div>
  );

  function navTo(np) {
    const p = new URLSearchParams(params);
    p.set("page", String(np));
    setParams(p);
  }
}

function Pager({ page, totalPages, onPrev, onNext, disabledPrev, disabledNext }) {
  if (totalPages <= 1) return null;
  return (
    <div style={{display:"flex", gap:8, marginTop:16, alignItems:"center"}}>
      <button disabled={disabledPrev} onClick={onPrev}>Prev</button>
      <span>Page {page+1} / {totalPages}</span>
      <button disabled={disabledNext} onClick={onNext}>Next</button>
    </div>
  );
}

function useDebounce(value, delay) {
  const [v, setV] = useState(value);
  useEffect(() => { const t = setTimeout(()=>setV(value), delay); return ()=>clearTimeout(t); }, [value, delay]);
  return v;
}
