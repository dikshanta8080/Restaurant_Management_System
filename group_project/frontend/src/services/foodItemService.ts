import api from './axios';
import { FoodItemRequest, FoodItemResponse, CategoryResponse } from '../types/foodItem';
import { ApiResponse, Page } from '../types/common';

export const foodItemService = {
  // POST /api/v1/restaurant/restaurants/{restaurantId}/food-items (multipart)
  addFoodItem: (restaurantId: number, request: FoodItemRequest): Promise<FoodItemResponse> => {
    const form = new FormData();
    form.append('name', request.name);
    form.append('description', request.description ?? '');
    form.append('price', String(request.price));
    form.append('available', String(request.available ?? true));
    form.append('categoryId', String(request.categoryId));
    if (request.multipartFile) form.append('multipartFile', request.multipartFile);
    return api.post(`/api/v1/restaurant/restaurants/${restaurantId}/food-items`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data);
  },

  // PUT /api/v1/restaurant/food-items/{id} (JSON body)
  updateFoodItem: (id: number, request: Partial<FoodItemRequest>): Promise<FoodItemResponse> =>
    api.put(`/api/v1/restaurant/food-items/${id}`, request).then(r => r.data),

  // DELETE /api/v1/restaurant/food-items/{id}
  deleteFoodItem: (id: number): Promise<void> =>
    api.delete(`/api/v1/restaurant/food-items/${id}`).then(() => undefined),

  // GET /api/v1/restaurant/restaurants/{restaurantId}/food-items
  getFoodItemsByRestaurant: (restaurantId: number): Promise<FoodItemResponse[]> =>
    api.get(`/api/v1/restaurant/restaurants/${restaurantId}/food-items`).then(r => r.data),

  // GET /api/v1/restaurant/food-items (paginated)
  getAllFoodItems: (page = 1, size = 10, categoryId?: number): Promise<Page<FoodItemResponse>> =>
    api.get('/api/v1/restaurant/food-items', {
      params: { pageNo: page, pageSize: size, ...(categoryId ? { categoryId } : {}) },
    }).then(r => r.data),

  // GET /api/v1/restaurant/allCategoriws  (typo is in the backend!)
  getAllCategories: (): Promise<ApiResponse<CategoryResponse[]>> =>
    api.get('/api/v1/restaurant/allCategoriws').then(r => r.data),
};
