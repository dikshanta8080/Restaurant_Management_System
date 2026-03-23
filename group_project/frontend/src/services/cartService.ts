import api from './axios';
import { CartItemRequest, CartItemResponse } from '../types/cart';

export const cartService = {
  // GET /api/v1/customer/cart → returns List<CartItemResponse> directly (no ApiResponse wrapper)
  getCart: (): Promise<CartItemResponse[]> =>
    api.get('/api/v1/customer/cart').then(r => r.data),

  // POST /api/v1/customer/cart → body: { foodItemId, quantity }
  addToCart: (request: CartItemRequest): Promise<CartItemResponse> =>
    api.post('/api/v1/customer/cart', request).then(r => r.data),

  // PUT /api/v1/customer/cart/{cartItemId}?quantity=N
  updateCartItem: (cartItemId: number, quantity: number): Promise<CartItemResponse> =>
    api.put(`/api/v1/customer/cart/${cartItemId}?quantity=${quantity}`).then(r => r.data),

  // DELETE /api/v1/customer/cart/{cartItemId}
  removeCartItem: (cartItemId: number): Promise<void> =>
    api.delete(`/api/v1/customer/cart/${cartItemId}`).then(() => undefined),
};
