import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { getToken, setToken, login, register, me } from "./api";
import { useCart } from "./cart/CartContext";

export default function Header() {
  const { items /*, clear*/ } = useCart();
  const cartCount = useMemo(() => items.reduce((n, d) => n + d.qty, 0), [items]);

  const [user, setUser] = useState(null);
  const [showAuth, setShowAuth] = useState(false);
  const [mode, setMode] = useState("login"); // "login" | "register"
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");

  const loc = useLocation();
  const navigate = useNavigate();

  // Rehydrate user when token exists (on load and when route changes)
  useEffect(() => {
    const t = getToken();
    if (!t) { setUser(null); return; }
    me().then(setUser).catch(() => setUser(null));
  }, [loc.pathname]);

  async function submit(e) {
    e.preventDefault(); setErr("");
    try {
      const resp = (mode === "login")
        ? await login(email.trim(), password)
        : await register(email.trim(), password);
      setToken(resp.token);
      const u = await me();
      setUser(u);
      setShowAuth(false);
      setEmail(""); setPassword("");
    } catch (er) {
      setErr(er.message || "Auth failed");
    }
  }

  function logout() {
    setToken(null);
    setUser(null);
    // clear?.(); // if you want to empty the cart on logout, uncomment
    navigate("/");
  }

  return (
    <header style={{
      display:"flex", alignItems:"center", gap:16,
      padding:"10px 16px", borderBottom:"1px solid #eee",
      position:"sticky", top:0, background:"#fff", zIndex:10
    }}>
      <Link to="/" style={{textDecoration:"none", fontWeight:700, color:"#222"}}>Eato</Link>

      <nav style={{display:"flex", gap:12}}>
        <Link to="/">Home</Link>
        <Link to="/orders">My Orders</Link>
        <Link to="/cart">Cart{cartCount > 0 ? ` (${cartCount})` : ""}</Link>
      </nav>

      <div style={{marginLeft:"auto"}}>
        {user ? (
          <span>
            {user.email}{" "}
            <button onClick={logout} style={{marginLeft:8}}>Logout</button>
          </span>
        ) : (
          <button onClick={() => { setMode("login"); setErr(""); setShowAuth(true); }}>
            Login / Sign up
          </button>
        )}
      </div>

      {showAuth && (
        <div style={{position:"fixed", inset:0, background:"rgba(0,0,0,0.35)"}}>
          <div style={{
            maxWidth:380, margin:"10% auto", background:"#fff",
            padding:16, borderRadius:10, boxShadow:"0 10px 30px rgba(0,0,0,0.2)"
          }}>
            <div style={{display:"flex", gap:8, marginBottom:12}}>
              <button onClick={()=>{setMode("login"); setErr("");}} disabled={mode==="login"}>Login</button>
              <button onClick={()=>{setMode("register"); setErr("");}} disabled={mode==="register"}>Sign up</button>
            </div>

            <form onSubmit={submit}>
              <input
                value={email} onChange={e=>setEmail(e.target.value)}
                type="email" required placeholder="Email"
                style={{width:"100%", padding:8, margin:"6px 0"}}
              />
              <input
                value={password} onChange={e=>setPassword(e.target.value)}
                type="password" required minLength={6} placeholder="Password (min 6)"
                style={{width:"100%", padding:8, margin:"6px 0"}}
              />
              {err && <div style={{color:"crimson"}}>{err}</div>}

              <div style={{display:"flex", justifyContent:"flex-end", gap:8, marginTop:8}}>
                <button type="button" onClick={()=>setShowAuth(false)}>Cancel</button>
                <button type="submit">{mode==="login" ? "Login" : "Sign up"}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </header>
  );
}
