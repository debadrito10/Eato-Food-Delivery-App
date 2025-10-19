// src/App.js
import { useEffect, useMemo, useState } from "react";
import { health } from "./api";
import Header from "./components/Header";
import StatusPill from "./components/StatusPill";
import Card from "./components/Card";

export default function App() {
  const apiBase = useMemo(() => process.env.REACT_APP_API || "http://localhost:8081", []);
  const [status, setStatus] = useState("checking");
  const [latencyMs, setLatencyMs] = useState(null);
  const [error, setError] = useState("");

  async function ping() {
    setStatus("checking");
    setError("");
    const t0 = performance.now();
    try {
      const d = await health();
      const t1 = performance.now();
      setLatencyMs(Math.round(t1 - t0));
      setStatus(d?.status === "ok" ? "ok" : "down");
    } catch (e) {
      setLatencyMs(null);
      setStatus("down");
      setError(e?.message || "Request failed");
    }
  }

  useEffect(() => {
    ping();
    const id = setInterval(ping, 15000); // auto-refresh every 15s
    return () => clearInterval(id);
  }, []);

  return (
    <div style={{ minHeight: "100svh", background: "#f7f7f8" }}>
      <Header apiBase={apiBase} />
      <main style={{ maxWidth: 960, margin: "24px auto", padding: "0 16px", display: "grid", gap: 16 }}>
        <Card
          title="Backend Health"
          extra={<StatusPill status={status} />}
        >
          <p style={{ margin: "6px 0 12px", color: "#555" }}>
            We’re polling the backend <code style={codeStyle}>/health</code> endpoint every 15s.
          </p>

          <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}>
            <button onClick={ping} style={btnStyle} disabled={status === "checking"}>
              {status === "checking" ? "Checking…" : "Check now"}
            </button>
            <span style={{ color: "#666" }}>
              Latency: {latencyMs === null ? "—" : `${latencyMs} ms`}
            </span>
          </div>

          {error && (
            <div style={alertStyle}>
              <strong>Request Error:</strong> {error}
            </div>
          )}
        </Card>

    
      </main>
      <footer style={{ textAlign: "center", color: "#888", fontSize: 12, padding: "24px 0" }}>
        © {new Date().getFullYear()} Eato
      </footer>
    </div>
  );
}

const btnStyle = {
  padding: "8px 14px",
  borderRadius: 8,
  border: "1px solid #ddd",
  background: "#fff",
  cursor: "pointer",
  fontWeight: 600,
};

const codeStyle = {
  background: "#f0f1f3",
  padding: "2px 6px",
  borderRadius: 6,
  fontFamily: "ui-monospace, SFMono-Regular, Menlo, Consolas, monospace",
  fontSize: 12,
};

const alertStyle = {
  marginTop: 12,
  padding: "8px 12px",
  background: "#fff7e6",
  border: "1px solid #ffe8b3",
  color: "#8a6d3b",
  borderRadius: 8,
};
