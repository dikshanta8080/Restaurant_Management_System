export interface ReviewRequest {
  rating: number;
  comment?: string;
  restaurantId?: number;
  foodItemId?: number;
}

export interface ReviewResponse {
  id: number;
  rating: number;
  comment?: string;
  userId: number;
  userName: string;
  restaurantId?: number;
  foodItemId?: number;
  createdAt: string;
}
