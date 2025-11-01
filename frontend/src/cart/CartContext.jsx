import React, { createContext, useContext, useMemo, useState } from "react";

const CartCtx = createContext(null);

export function CartProvider({ children }) {
  const [items, setItems] = useState([]); // { dishId, name, price, qty, restaurantId }

  function addItem(dish) {
    setItems(prev => {
      const i = prev.findIndex(d => d.dishId === dish.dishId);
      if (i >= 0) {
        const copy = [...prev];
        copy[i] = { ...copy[i], qty: copy[i].qty + (dish.qty || 1) };
        return copy;
      }
      return [...prev, { ...dish, qty: dish.qty || 1 }];
    });
  }

  function setQty(dishId, qty) {
    setItems(prev => prev.map(d => d.dishId === dishId ? { ...d, qty } : d).filter(d => d.qty > 0));
  }

  function removeItem(dishId) {
    setItems(prev => prev.filter(d => d.dishId !== dishId));
  }

  function clear() { setItems([]); }

  const totalClient = useMemo(() => items.reduce((sum, d) => sum + d.price * d.qty, 0), [items]);

  const value = { items, addItem, setQty, removeItem, clear, totalClient };
  return <CartCtx.Provider value={value}>{children}</CartCtx.Provider>;
}

export function useCart() {
  const ctx = useContext(CartCtx);
  if (!ctx) throw new Error("useCart must be used within CartProvider");
  return ctx;
}
