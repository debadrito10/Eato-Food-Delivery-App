export default function StatusPill({ status }) {
  const map = {
    ok:   { text: "OK",   bg: "#E6F4EA", fg: "#137333" },
    down: { text: "DOWN", bg: "#FCE8E6", fg: "#C5221F" },
    checking: { text: "CHECKING…", bg: "#E8F0FE", fg: "#174EA6" },
  };
  const s = map[status] || map.checking;
  return (
    <span style={{
      display: "inline-block",
      padding: "4px 10px",
      borderRadius: 999,
      fontSize: 12,
      fontWeight: 600,
      background: s.bg,
      color: s.fg,
      letterSpacing: 0.3
    }}>
      {s.text}
    </span>
  );
}
