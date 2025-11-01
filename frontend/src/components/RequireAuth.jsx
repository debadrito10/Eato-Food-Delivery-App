import { Navigate, useLocation } from "react-router-dom";

export default function RequireAuth({ children }) {
  const token = localStorage.getItem("eato_token");
  const loc = useLocation();
  if (!token) return <Navigate to="/" state={{ from: loc }} replace />;
  return children;
}
