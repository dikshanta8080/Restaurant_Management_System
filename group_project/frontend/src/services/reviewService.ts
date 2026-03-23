import api from './axios';
import { ReviewRequest, ReviewResponse } from '../types/review';

export const reviewService = {
  // POST /api/reviews → returns ReviewResponse (CREATED)
  createReview: (request: ReviewRequest): Promise<ReviewResponse> =>
    api.post('/api/reviews', request).then(r => r.data),

  // PUT /api/reviews/{id}
  updateReview: (id: number, request: ReviewRequest): Promise<ReviewResponse> =>
    api.put(`/api/reviews/${id}`, request).then(r => r.data),

  // DELETE /api/reviews/{id}
  deleteReview: (id: number): Promise<void> =>
    api.delete(`/api/reviews/${id}`).then(() => undefined),

  // GET /api/reviews/restaurants/{restaurantId}?page=0&size=10
  getReviewsForRestaurant: (restaurantId: number, page = 0, size = 10): Promise<ReviewResponse[]> =>
    api.get(`/api/reviews/restaurants/${restaurantId}`, { params: { page, size } }).then(r => r.data),

  // GET /api/reviews/food-items/{foodItemId}?page=0&size=10
  getReviewsForFoodItem: (foodItemId: number, page = 0, size = 10): Promise<ReviewResponse[]> =>
    api.get(`/api/reviews/food-items/${foodItemId}`, { params: { page, size } }).then(r => r.data),
};
