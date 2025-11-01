import React, { useState } from "react";
import { useCart } from "../cart/CartContext";
import { priceCart, createOrder } from "../api";


export default function Cart() {
  const { items, setQty, removeItem, totalClient, clear } = useCart();
  const [serverTotal, setServerTotal] = useState(null);
  const [checking, setChecking] = useState(false);
  const [ordering, setOrdering] = useState(false);
  const [address, setAddress] = useState({ city: "Kolkata", pincode: "700001" });
  const [message, setMessage] = useState("");

  const payloadItems = items.map(d => ({ dishId: d.dishId, qty: d.qty }));

  async function previewPrice() {
    setChecking(true); setMessage("");
    try {
      const res = await priceCart(payloadItems);
      setServerTotal(res.total);
    } catch (e) {
      setMessage(e.message);
    } finally { setChecking(false); }
  }

  async function checkout() {
    setOrdering(true); setMessage("");
    const idem = crypto.randomUUID ? crypto.randomUUID() : String(Date.now());
    try {
      const res = await createOrder(payloadItems, JSON.stringify(address), idem);
      setMessage(`Order placed. #${res.orderId}`);
      clear();
      setServerTotal(null);
    } catch (e) {
      setMessage(e.message);
    } finally { setOrdering(false); }
  }

  return (
    <div className="cart">
      <h2>Cart</h2>
      {items.length === 0 && <p>No items yet.</p>}
      {items.map(d => (
        <div key={d.dishId} className="cart-row">
          <div>{d.name}</div>
          <div>₹{d.price}</div>
          <input type="number" min="1" value={d.qty}
                 onChange={e => setQty(d.dishId, parseInt(e.target.value || "1", 10))}/>
          <button onClick={() => removeItem(d.dishId)}>Remove</button>
        </div>
      ))}
      <div className="totals">
        <div>Client total: ₹{totalClient}</div>
        <div>Server total: {serverTotal != null ? `₹${serverTotal}` : "—"}</div>
      </div>

      <div className="address">
        <h4>Address (optional)</h4>
        <input placeholder="City" value={address.city||""}
               onChange={e => setAddress(a => ({...a, city: e.target.value}))}/>
        <input placeholder="Pincode" value={address.pincode||""}
               onChange={e => setAddress(a => ({...a, pincode: e.target.value}))}/>
      </div>

      <div className="actions">
        <button disabled={!items.length || checking} onClick={previewPrice}>
          {checking ? "Checking..." : "Preview Price"}
        </button>
        <button disabled={!items.length || ordering} onClick={checkout}>
          {ordering ? "Placing..." : "Checkout"}
        </button>
      </div>
      {message && <p>{message}</p>}
    </div>
  );
}
