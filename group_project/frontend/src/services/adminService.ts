import api from './axios';
import { ApiResponse } from '../types/common';
import { RestaurantResponse } from '../types/restaurant';
import { PaymentResponse } from '../types/payment';

export const adminService = {
  // GET /api/v1/admin/restaurants/pending
  getPendingRestaurants: (): Promise<ApiResponse<RestaurantResponse[]>> =>
    api.get('/api/v1/admin/restaurants/pending').then(r => r.data),

  // GET /api/v1/admin/restaurants
  getAllRestaurants: (): Promise<ApiResponse<RestaurantResponse[]>> =>
    api.get('/api/v1/admin/restaurants').then(r => r.data),

  // PUT /api/v1/admin/updateStatus (JSON body: { restaurantId, restaurantStatus })
  updateRestaurantStatus: (request: { restaurantId: number; restaurantStatus: string }): Promise<any> =>
    api.put('/api/v1/admin/updateStatus', request).then(r => r.data),

  // DELETE /api/v1/admin/restaurants/{id}
  deleteRestaurant: (restaurantId: number): Promise<any> =>
    api.delete(`/api/v1/admin/restaurants/${restaurantId}`).then(r => r.data),

  // DELETE /api/v1/admin/deleteUser (uses @ModelAttribute so send as query params)
  deleteUser: (userId: number): Promise<any> =>
    api.delete('/api/v1/admin/deleteUser', { params: { id: userId } }).then(r => r.data),

  // GET /api/v1/admin/customers
  getAllCustomers: (): Promise<any[]> =>
    api.get('/api/v1/admin/customers').then(r => r.data?.responseObject ?? []),

  // GET /api/v1/admin/payments
  getAllPayments: (): Promise<PaymentResponse[]> =>
    api.get('/api/v1/admin/payments').then(r => r.data?.responseObject ?? []),
};
