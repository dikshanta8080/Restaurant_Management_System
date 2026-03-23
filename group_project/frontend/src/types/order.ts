export type OrderStatus = 'PENDING' | 'ACCEPTED' | 'COMPLETED' | 'REJECTED';

export interface OrderItemRequest {
  foodItemId: number;
  quantity: number;
}

export interface OrderCreateRequest {
  items: OrderItemRequest[];
  addressId?: number;
}

export interface OrderItemResponse {
  id: number;
  foodItemId: number;
  foodItemName: string;
  quantity: number;
  price: number;
}

export interface OrderResponse {
  id: number;
  totalPrice: number;
  status: OrderStatus;
  items: OrderItemResponse[];
  createdAt: string;
  userId: number;
}
