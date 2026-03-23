import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Search, Store, SlidersHorizontal } from 'lucide-react';
import { customerService } from '../services/customerService';
import RestaurantCard from '../components/RestaurantCard';
import { SkeletonCard } from '../components/Skeleton';

const RestaurantsPage: React.FC = () => {
  const [search, setSearch] = useState('');

  const { data, isLoading } = useQuery({
    queryKey: ['restaurants'],
    queryFn: async () => {
      const res = await customerService.getAllRestaurants();
      return res.responseObject.filter(r => r.status === 'APPROVED');
    },
  });

  const filtered = data?.filter(r =>
    (r.name ?? '').toLowerCase().includes(search.toLowerCase()) ||
    r.description?.toLowerCase().includes(search.toLowerCase()) ||
    r.ownerName?.toLowerCase().includes(search.toLowerCase())
  ) ?? [];

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      {/* Header */}
      <div className="bg-white border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
          <h1 className="text-4xl font-black text-gray-900 mb-2">Explore Restaurants</h1>
          <p className="text-gray-500">Find your next favourite meal from our verified restaurants</p>
          {/* Search */}
          <div className="mt-6 flex gap-3">
            <div className="relative flex-1 max-w-lg">
              <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                value={search}
                onChange={e => setSearch(e.target.value)}
                placeholder="Search restaurants…"
                className="input pl-11"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Grid */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        {isLoading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
          </div>
        ) : filtered.length > 0 ? (
          <>
            <p className="text-sm text-gray-500 mb-6">{filtered.length} restaurant{filtered.length !== 1 ? 's' : ''} found</p>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {filtered.map(r => <RestaurantCard key={r.id} restaurant={r} />)}
            </div>
          </>
        ) : (
          <div className="flex flex-col items-center justify-center py-24 text-gray-400">
            <Store size={64} className="mb-4 opacity-30" />
            <p className="text-xl font-semibold">No restaurants found</p>
            {search && <p className="text-sm mt-1">Try a different search term</p>}
          </div>
        )}
      </div>
    </div>
  );
};

export default RestaurantsPage;
