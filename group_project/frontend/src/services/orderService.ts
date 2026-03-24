import api from './axios';
import { OrderCreateRequest, OrderPlacementResponse, OrderResponse } from '../types/order';

export const orderService = {
  // POST /api/v1/customer/orders → returns OrderPlacementResponse (split by restaurant)
  placeOrder: (request: OrderCreateRequest): Promise<OrderPlacementResponse> =>
    api.post('/api/v1/customer/orders', request).then(r => r.data),

  // GET /api/v1/customer/orders → returns Spring Page<OrderResponse> directly
  getUserOrders: (page = 0, size = 10): Promise<any> =>
    api.get('/api/v1/customer/orders', { params: { page, size } }).then(r => r.data),

  // GET /api/v1/restaurant/orders → owner/admin incoming orders
  getRestaurantIncomingOrders: (page = 0, size = 20): Promise<any> =>
    api.get('/api/v1/restaurant/orders', { params: { page, size } }).then(r => r.data),

  // GET /api/v1/customer/orders/{id}
  getOrderById: (id: number): Promise<OrderResponse> =>
    api.get(`/api/v1/customer/orders/${id}`).then(r => r.data),

  // PUT /api/v1/restaurant/orders/{id}/status?status=ACCEPTED
  updateOrderStatus: (id: number, status: string): Promise<OrderResponse> =>
    api.put(`/api/v1/restaurant/orders/${id}/status`, null, { params: { status } }).then(r => r.data),
};
