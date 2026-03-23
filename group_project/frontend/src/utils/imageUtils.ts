export function getImageUrl(imageUrl: string | null | undefined): string | null {
  if (!imageUrl) return null;
  // Already absolute — e.g. a cloud CDN URL
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }
  // Backend upload URLs are typically relative (either "/uploads/..." or "uploads/...").
  // The dev server proxies `/uploads` automatically, so we should not hardcode a host/port.
  return imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`;
}
