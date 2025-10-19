import axios from "axios";

const api = axios.create({
  baseURL: process.env.REACT_APP_API || "http://localhost:8081",
  timeout: 8000,
});

export const health = async () => {
  const res = await api.get("/health");
  return res.data; // { status: "ok" }
};

export default api;
