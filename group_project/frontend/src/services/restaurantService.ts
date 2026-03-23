import api from './axios';
import { ApiResponse } from '../types/common';
import { RestaurantResponse, RestaurantCreateRequest } from '../types/restaurant';

interface RestaurantCreateResponse {
  id: number;
  name: string;
  description: string;
  status: string;
  imageUrl?: string;
}

export const restaurantService = {
  // GET /api/v1/restaurant/getRestaurant (gets own restaurant for logged-in RESTAURANT user)
  getOwnRestaurant: (): Promise<ApiResponse<RestaurantResponse>> =>
    api.get('/api/v1/restaurant/getRestaurant').then(r => r.data),

  // PUT /api/v1/restaurant/updateRestaurant/{id} (multipart)
  updateRestaurant: (id: number, request: RestaurantCreateRequest): Promise<any> => {
    const form = new FormData();
    form.append('name', request.name);
    form.append('description', request.description ?? '');
    if (request.province) form.append('province', request.province);
    if (request.district) form.append('district', request.district);
    if (request.city) form.append('city', request.city);
    if (request.street) form.append('street', request.street);
    if (request.multipartFile) form.append('multipartFile', request.multipartFile);
    return api.put(`/api/v1/restaurant/updateRestaurant/${id}`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data);
  },

  // DELETE /api/v1/restaurant/deleteRestaurant/{id}
  deleteRestaurant: (id: number): Promise<any> =>
    api.delete(`/api/v1/restaurant/deleteRestaurant/${id}`).then(r => r.data),
};
