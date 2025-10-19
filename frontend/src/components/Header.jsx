export default function Header({ apiBase }) {
  const showApi = process.env.REACT_APP_SHOW_API === "1";
  return (
    <header style={{
      display: "flex", alignItems: "center", justifyContent: "space-between",
      padding: "14px 20px", borderBottom: "1px solid #eee", background: "#fff",
      position: "sticky", top: 0, zIndex: 10
    }}>
      <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
        <div style={{
          width: 28, height: 28, borderRadius: 6,
          background: "linear-gradient(135deg,#ff7a59,#ffb84d)"
        }} />
        <h1 style={{ fontSize: 18, margin: 0 }}>Eato</h1>
      </div>
      {showApi && <small style={{ color: "#666" }}>API: {apiBase}</small>}
    </header>
  );
}
