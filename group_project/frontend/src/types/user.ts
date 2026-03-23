export interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: string;
  profileImageUrl?: string;
  addressId?: number;
  province?: string;
  district?: string;
  city?: string;
  street?: string;
}

export interface UserProfileUpdateRequest {
  name?: string;
  multipartFile?: File;
}
