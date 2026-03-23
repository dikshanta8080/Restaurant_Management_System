export interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: string;
  profileImagePath?: string;
}

export interface UserProfileUpdateRequest {
  name?: string;
  multipartFile?: File;
}
