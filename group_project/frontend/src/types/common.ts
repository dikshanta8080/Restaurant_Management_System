export interface ApiResponse<T> {
  httpStatus: string;
  message: string;
  responseObject: T;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface RestaurantStatusUpdateRequest {
  restaurantId: number;
  restaurantStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
}
