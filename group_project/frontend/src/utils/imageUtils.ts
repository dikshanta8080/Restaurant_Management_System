/**
 * Returns a fully-qualified image URL for backend-served uploads.
 *
 * The backend stores paths like "/uploads/restaurant/abc.jpg".
 * During development the frontend runs on port 5173 while the backend
 * runs on port 8080, so we must prepend the backend base URL explicitly.
 * Update BACKEND_BASE if you deploy to a different host/port.
 */
const BACKEND_BASE = 'http://localhost:8080';

export function getImageUrl(imageUrl: string | null | undefined): string | null {
  if (!imageUrl) return null;
  // Already absolute — e.g. a cloud CDN URL
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }
  // Relative path from the backend — prepend the base URL
  return `${BACKEND_BASE}${imageUrl}`;
}
