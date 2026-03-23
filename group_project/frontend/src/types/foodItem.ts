export type FoodCategory = 'VEG' | 'NONVEG' | 'DRINKS' | 'FASTFOODS' | 'BEVERAGE';

export interface CategoryResponse {
  id: number;
  categoryName: FoodCategory;
}

export interface FoodItemResponse {
  id: number;
  name: string;
  description: string;
  price: number;
  available: boolean;
  imageUrl?: string;
  categoryId: number;
  restaurantId: number;
}

export interface FoodItemRequest {
  name: string;
  description: string;
  price: number;
  available: boolean;
  categoryId: number;
  multipartFile?: File;
}
