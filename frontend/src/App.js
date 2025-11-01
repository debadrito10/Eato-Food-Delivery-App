import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import RestaurantDetail from "./pages/RestaurantDetail";
import Header from "./Header";
import Cart from "./pages/Cart";
import OrdersList from "./pages/OrdersList";
import OrderDetail from "./pages/OrderDetail";
import RequireAuth from "./components/RequireAuth";
export default function App() {
  return (
    <BrowserRouter>
    <Header />
      <Routes>
        <Route path="/" element={<Home/>} />
        <Route path="/restaurant/:id" element={<RestaurantDetail/>} />
          <Route path="/cart" element={<Cart/>} />
        <Route path="/orders" element={
          <RequireAuth><OrdersList/></RequireAuth>
        }/>
        <Route path="/orders/:id" element={
          <RequireAuth><OrderDetail/></RequireAuth>
        }/>
      </Routes>
    </BrowserRouter>
  );
}
