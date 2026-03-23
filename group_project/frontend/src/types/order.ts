export type OrderStatus = 'PENDING' | 'ACCEPTED' | 'COMPLETED' | 'REJECTED';

export interface OrderItemRequest {
  foodItemId: number;
  quantity: number;
}

export interface OrderCreateRequest {
  items: OrderItemRequest[];
  addressId?: number;
  // Optional delivery address override (when user wants a different address).
  province?: string;
  district?: string;
  city?: string;
  street?: string;
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

  deliveryAddressId?: number;
  deliveryProvince?: string;
  deliveryDistrict?: string;
  deliveryCity?: string;
  deliveryStreet?: string;
}
