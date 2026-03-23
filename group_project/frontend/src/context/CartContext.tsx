import React, { createContext, useContext, useState, useCallback } from 'react';
import { CartItemResponse } from '../types/cart';

interface CartContextType {
  cartItems: CartItemResponse[];
  setCartItems: (items: CartItemResponse[]) => void;
  cartCount: number;
  cartTotal: number;
  addItem: (item: CartItemResponse) => void;
  removeItem: (id: number) => void;
  updateItem: (id: number, quantity: number) => void;
  clearCart: () => void;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [cartItems, setCartItemsState] = useState<CartItemResponse[]>([]);

  const setCartItems = useCallback((items: CartItemResponse[]) => {
    setCartItemsState(items);
  }, []);

  const addItem = useCallback((item: CartItemResponse) => {
    setCartItemsState(prev => {
      const existing = prev.find(i => i.id === item.id);
      if (existing) {
        return prev.map(i => i.id === item.id ? { ...i, quantity: i.quantity + item.quantity } : i);
      }
      return [...prev, item];
    });
  }, []);

  const removeItem = useCallback((id: number) => {
    setCartItemsState(prev => prev.filter(i => i.id !== id));
  }, []);

  const updateItem = useCallback((id: number, quantity: number) => {
    setCartItemsState(prev => prev.map(i => i.id === id ? { ...i, quantity } : i));
  }, []);

  const clearCart = useCallback(() => setCartItemsState([]), []);

  const cartCount = cartItems.reduce((sum, i) => sum + i.quantity, 0);
  const cartTotal = cartItems.reduce((sum, i) => sum + i.price * i.quantity, 0);

  return (
    <CartContext.Provider value={{ cartItems, setCartItems, cartCount, cartTotal, addItem, removeItem, updateItem, clearCart }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used inside CartProvider');
  return ctx;
};
