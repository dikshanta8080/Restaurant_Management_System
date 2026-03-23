import api from './axios';
import { ApiResponse } from '../types/common';
import { RestaurantResponse, RestaurantCreateRequest } from '../types/restaurant';
import { UserResponse, UserProfileUpdateRequest } from '../types/user';

export const customerService = {
  // GET /api/v1/customer/profile
  getProfile: (): Promise<ApiResponse<UserResponse>> =>
    api.get('/api/v1/customer/profile').then(r => r.data),

  // PUT /api/v1/customer/profileUpdate (multipart)
  updateProfile: (request: UserProfileUpdateRequest): Promise<ApiResponse<UserResponse>> => {
    const form = new FormData();
    form.append('name', request.name);
    if (request.multipartFile) form.append('multipartFile', request.multipartFile);
    return api.put('/api/v1/customer/profileUpdate', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data);
  },

  // GET /api/v1/customer/allRestaurants
  getAllRestaurants: (): Promise<ApiResponse<RestaurantResponse[]>> =>
    api.get('/api/v1/customer/allRestaurants').then(r => r.data),

  // POST /api/v1/customer/createRestaurant (multipart)
  createRestaurant: (request: RestaurantCreateRequest & { multipartFile?: File }): Promise<ApiResponse<any>> => {
    const form = new FormData();
    form.append('name', request.name);
    form.append('description', request.description ?? '');
    if (request.province) form.append('province', request.province);
    if (request.district) form.append('district', request.district);
    if (request.city) form.append('city', request.city);
    if (request.street) form.append('street', request.street);
    if (request.multipartFile) form.append('multipartFile', request.multipartFile);
    return api.post('/api/v1/customer/createRestaurant', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data);
  },
};
