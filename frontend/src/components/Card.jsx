// src/components/Card.jsx
export default function Card({ title, extra, children }) {
  return (
    <div style={{
      background: "#fff", border: "1px solid #eee", borderRadius: 12,
      padding: 16, boxShadow: "0 1px 3px rgba(0,0,0,0.04)"
    }}>
      {(title || extra) && (
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 10 }}>
          <h2 style={{ fontSize: 16, margin: 0 }}>{title}</h2>
          {extra}
        </div>
      )}
      {children}
    </div>
  );
}
