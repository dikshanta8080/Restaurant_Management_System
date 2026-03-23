export interface CartItemRequest {
  foodItemId: number;
  quantity: number;
}

export interface CartItemResponse {
  id: number;
  foodItemId: number;
  foodItemName: string;
  price: number;
  quantity: number;
  restaurantId: number;
}
