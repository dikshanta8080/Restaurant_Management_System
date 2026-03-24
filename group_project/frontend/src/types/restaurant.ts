export type RestaurantStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface RestaurantResponse {
  id: number;
  name: string;
  description: string;
  imageUrl?: string;
  ownerName: string;
  status: RestaurantStatus;
  averageRating?: number;
  reviewCount?: number;

  // Nepal location fields (used for filtering & display)
  addressId?: number;
  province?: string;
  district?: string;
  city?: string;
  street?: string;
}

export interface RestaurantCreateRequest {
  name: string;
  description: string;
  province: string;
  district: string;
  city: string;
  street: string;
  multipartFile?: File;
}

export interface RestaurantCreateResponse {
  id: number;
  name: string;
  description: string;
  imageUrl?: string;
  status: RestaurantStatus;
}
