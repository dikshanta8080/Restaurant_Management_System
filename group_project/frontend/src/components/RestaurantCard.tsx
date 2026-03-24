import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { MapPin, Star, ChevronRight } from 'lucide-react';
import { RestaurantResponse } from '../types/restaurant';
import { getImageUrl } from '../utils/imageUtils';

interface RestaurantCardProps {
  restaurant: RestaurantResponse;
}

const statusColors: Record<string, string> = {
  APPROVED: 'badge-approved',
  PENDING: 'badge-pending',
  REJECTED: 'badge-rejected',
};

const RestaurantCard: React.FC<RestaurantCardProps> = ({ restaurant }) => {
  const imageUrl = getImageUrl(restaurant.imageUrl);
  const [brokenImage, setBrokenImage] = useState(false);

  useEffect(() => {
    setBrokenImage(false);
  }, [imageUrl]);

  const locationText = [
    restaurant.street,
    restaurant.city,
    restaurant.district,
    restaurant.province,
  ].filter(Boolean).join(', ');

  const ratingText = typeof restaurant.averageRating === 'number'
    ? restaurant.averageRating.toFixed(1)
    : '—';

  return (
    <Link
      to={`/restaurants/${restaurant.id}`}
      className="card overflow-hidden group hover:-translate-y-1 transition-all duration-300 flex flex-col"
    >
      <div className="relative h-48 bg-gradient-to-br from-orange-100 to-red-100 overflow-hidden">
        {imageUrl && !brokenImage ? (
          <img
            src={imageUrl}
            alt={restaurant.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            onError={() => setBrokenImage(true)}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <div className="text-6xl">🍽️</div>
          </div>
        )}
        <div className="absolute top-3 right-3">
          <span className={statusColors[restaurant.status] || 'badge-pending'}>
            {restaurant.status}
          </span>
        </div>
      </div>

      <div className="p-4 flex flex-col flex-1">
        <div className="flex items-start justify-between mb-2">
          <h3 className="font-bold text-gray-900 text-lg leading-tight group-hover:text-orange-600 transition-colors">
            {restaurant.name}
          </h3>
          <ChevronRight size={16} className="text-gray-400 mt-1 flex-shrink-0 group-hover:text-orange-500 group-hover:translate-x-0.5 transition-all" />
        </div>

        {restaurant.description && (
          <p className="text-sm text-gray-500 mb-3 line-clamp-2 flex-1">{restaurant.description}</p>
        )}

        <div className="flex items-center gap-2 mt-auto pt-2 border-t border-gray-50">
          <div className="flex items-center gap-1 text-yellow-500">
            <Star size={14} className="fill-yellow-400" />
            <span className="text-xs font-medium text-gray-600">{ratingText}</span>
          </div>
          <span className="text-gray-300">•</span>
          <div className="flex items-center gap-1 text-gray-500 text-xs">
            <MapPin size={12} />
            <span>{locationText || restaurant.ownerName}</span>
          </div>
        </div>
      </div>
    </Link>
  );
};

export default RestaurantCard;
