import React, { useEffect, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Search, Store, Star, SlidersHorizontal, MapPin } from 'lucide-react';
import { customerService } from '../services/customerService';
import { foodItemService } from '../services/foodItemService';
import { FoodCategory, FoodItemResponse, CategoryResponse } from '../types/foodItem';
import { RestaurantResponse } from '../types/restaurant';
import RestaurantCard from '../components/RestaurantCard';
import FoodItemCard from '../components/FoodItemCard';
import { SkeletonCard } from '../components/Skeleton';

type SortKey = 'priceLow' | 'priceHigh' | 'ratingHigh' | 'nameAZ';
type District = string | 'ALL';
type City = string | 'ALL';

type FoodFeedItem = FoodItemResponse & {
  categoryName?: FoodCategory;
  restaurantName?: string;
  restaurantRating?: number;
  restaurantDistrict?: string;
  restaurantCity?: string;
  restaurantProvince?: string;
};

const PAGE_SIZE = 50;

const HomePage: React.FC = () => {
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState<'ALL' | FoodCategory>('ALL');
  const [minRating, setMinRating] = useState(0);
  const [district, setDistrict] = useState<District>('ALL');
  const [city, setCity] = useState<City>('ALL');
  const [sortKey, setSortKey] = useState<SortKey>('ratingHigh');

  const [priceMin, setPriceMin] = useState<number>(0);
  const [priceMax, setPriceMax] = useState<number>(0);

  const { data: feed, isLoading, isError } = useQuery({
    queryKey: ['home-feed'],
    queryFn: async () => {
      const [restaurantsRes, categoriesRes] = await Promise.all([
        customerService.getAllRestaurants({ sortBy: 'rating', sortDir: 'desc' }),
        foodItemService.getAllCategories(),
      ]);

      const restaurants: RestaurantResponse[] = restaurantsRes.responseObject;

      const categories: CategoryResponse[] = categoriesRes.responseObject ?? [];
      const categoryById = new Map<number, FoodCategory>(
        categories.map(c => [c.id, c.categoryName]),
      );

      const restaurantById = new Map<number, RestaurantResponse>(
        restaurants.map(r => [r.id, r]),
      );

      const allFoodItems: FoodFeedItem[] = [];
      let pageNo = 0;

      while (true) {
        const page = await foodItemService.getAllFoodItems(pageNo, PAGE_SIZE);
        for (const fi of page.content ?? []) {
          const r = restaurantById.get(fi.restaurantId);
          allFoodItems.push({
            ...fi,
            categoryName: categoryById.get(fi.categoryId),
            restaurantName: r?.name,
            restaurantRating: r?.averageRating,
            restaurantDistrict: r?.district,
            restaurantCity: r?.city,
            restaurantProvince: r?.province,
          });
        }
        if (page.last) break;
        pageNo += 1;
      }

      return {
        restaurants,
        items: allFoodItems,
      };
    },
    staleTime: 300000,
  });

  useEffect(() => {
    if (!feed?.items?.length) return;
    const prices = feed.items.map(i => i.price).filter(p => typeof p === 'number' && !Number.isNaN(p));
    if (!prices.length) return;
    setPriceMin(Math.min(...prices));
    setPriceMax(Math.max(...prices));
  }, [feed?.items?.length]);

  const districtOptions = useMemo(() => {
    if (!feed?.restaurants) return [];
    const set = new Set(feed.restaurants.map(r => r.district).filter(Boolean) as string[]);
    return ['ALL', ...Array.from(set).sort((a, b) => a.localeCompare(b))] as District[];
  }, [feed?.restaurants]);

  const cityOptions = useMemo(() => {
    if (!feed?.restaurants) return [];
    const filtered = feed.restaurants.filter(r => (district === 'ALL' ? true : r.district === district));
    const set = new Set(filtered.map(r => r.city).filter(Boolean) as string[]);
    return ['ALL', ...Array.from(set).sort((a, b) => a.localeCompare(b))] as City[];
  }, [feed?.restaurants, district]);

  const filteredItems = useMemo(() => {
    if (!feed?.items) return [];

    const q = search.trim().toLowerCase();

    let result = feed.items;

    if (q) {
      result = result.filter(i => {
        const foodMatch = i.name?.toLowerCase().includes(q);
        const restaurantMatch = i.restaurantName?.toLowerCase().includes(q);
        return !!foodMatch || !!restaurantMatch;
      });
    }

    if (category !== 'ALL') {
      result = result.filter(i => i.categoryName === category);
    }

    if (minRating > 0) {
      result = result.filter(i => (i.restaurantRating ?? 0) >= minRating);
    }

    if (district !== 'ALL') {
      result = result.filter(i => i.restaurantDistrict === district);
    }

    if (city !== 'ALL') {
      result = result.filter(i => i.restaurantCity === city);
    }

    if (priceMax > 0) {
      result = result.filter(i => i.price >= priceMin && i.price <= priceMax);
    }

    result = [...result].sort((a, b) => {
      switch (sortKey) {
        case 'priceLow':
          return a.price - b.price;
        case 'priceHigh':
          return b.price - a.price;
        case 'ratingHigh':
          return (b.restaurantRating ?? 0) - (a.restaurantRating ?? 0);
        case 'nameAZ':
          return (a.name ?? '').localeCompare(b.name ?? '');
        default:
          return 0;
      }
    });

    return result;
  }, [feed?.items, search, category, minRating, district, city, priceMin, priceMax, sortKey]);

  const filteredRestaurants = useMemo(() => {
    if (!feed?.restaurants) return [];
    const q = search.trim().toLowerCase();

    let result = feed.restaurants;

    if (q) {
      result = result.filter(r => r.name?.toLowerCase().includes(q));
    }

    if (minRating > 0) {
      result = result.filter(r => (r.averageRating ?? 0) >= minRating);
    }

    if (district !== 'ALL') {
      result = result.filter(r => r.district === district);
    }

    if (city !== 'ALL') {
      result = result.filter(r => r.city === city);
    }

    return [...result].sort((a, b) => (b.averageRating ?? 0) - (a.averageRating ?? 0));
  }, [feed?.restaurants, search, minRating, district, city]);

  const categoryOptions: Array<'ALL' | FoodCategory> = ['ALL', 'VEG', 'NONVEG', 'DRINKS', 'FASTFOODS', 'BEVERAGE'];

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="bg-gradient-to-br from-orange-500 via-orange-600 to-red-600 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 md:py-16">
          <div className="flex flex-col lg:flex-row lg:items-end lg:justify-between gap-8">
            <div>
              <div className="inline-flex items-center gap-2 bg-white/15 backdrop-blur-sm rounded-full px-4 py-2 mb-6">
                <Store size={16} />
                <span className="text-sm font-medium">Nepal Food Delivery</span>
              </div>
              <h1 className="text-4xl md:text-6xl font-black leading-tight">
                Discover <span className="text-yellow-200">Nepali</span> Flavors
              </h1>
              <p className="text-white/80 mt-4 text-base md:text-lg max-w-2xl leading-relaxed">
                Browse restaurants, explore menus, and order your favorites with instant payment confirmation.
              </p>
            </div>

            <div className="w-full lg:max-w-xl">
              <label className="label text-white/90">Search food or restaurant</label>
              <div className="relative">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-white/60" size={18} />
                <input
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  className="input pl-11 bg-white/10 border-white/20 text-white placeholder-white/60 focus:ring-orange-200"
                  placeholder="e.g. momo, thakali, Pokhara…"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="card p-5 md:p-6 mb-8">
          <div className="flex flex-col lg:flex-row lg:items-end gap-4 lg:gap-6">
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-2 text-gray-700">
                <SlidersHorizontal size={18} className="text-orange-600" />
                <span className="font-semibold">Filters</span>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div>
                  <label className="label">Category</label>
                  <select value={category} onChange={e => setCategory(e.target.value as any)} className="input">
                    {categoryOptions.map(c => (
                      <option key={c} value={c}>
                        {c}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="label">Min Rating</label>
                  <select
                    value={minRating}
                    onChange={e => setMinRating(Number(e.target.value))}
                    className="input"
                  >
                    {[0, 3, 4].map(v => (
                      <option key={v} value={v}>
                        {v === 0 ? 'Any' : `${v}+`}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="label">District</label>
                  <select value={district} onChange={e => setDistrict(e.target.value as District)} className="input">
                    {districtOptions.map(d => (
                      <option key={d} value={d}>
                        {d}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="label">City</label>
                  <select value={city} onChange={e => setCity(e.target.value as City)} className="input">
                    {cityOptions.map(c => (
                      <option key={c} value={c}>
                        {c}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div>
                  <label className="label">Min Price</label>
                  <input
                    type="number"
                    value={priceMin}
                    onChange={e => setPriceMin(Number(e.target.value))}
                    className="input"
                    step="1"
                    min={0}
                  />
                </div>
                <div>
                  <label className="label">Max Price</label>
                  <input
                    type="number"
                    value={priceMax}
                    onChange={e => setPriceMax(Number(e.target.value))}
                    className="input"
                    step="1"
                    min={0}
                  />
                </div>
                <div className="sm:col-span-2 lg:col-span-2">
                  <label className="label">Sort by</label>
                  <select value={sortKey} onChange={e => setSortKey(e.target.value as SortKey)} className="input">
                    <option value="ratingHigh">Rating (high to low)</option>
                    <option value="priceLow">Price (low to high)</option>
                    <option value="priceHigh">Price (high to low)</option>
                    <option value="nameAZ">Name (A-Z)</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="mb-8">
          <div className="flex items-start justify-between gap-4 mb-4">
            <div>
              <h2 className="text-2xl font-black text-gray-900">Restaurants near you</h2>
              <p className="text-sm text-gray-500 mt-1 flex items-center gap-2">
                <MapPin size={14} className="text-orange-600" />
                <span>District: {district === 'ALL' ? 'All Nepal' : district} • City: {city === 'ALL' ? 'All cities' : city}</span>
              </p>
            </div>
          </div>

          <div className="flex gap-4 overflow-x-auto pb-2">
            {isLoading
              ? Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)
              : filteredRestaurants.map(r => (
                  <div key={r.id} className="min-w-[320px] flex-shrink-0">
                    <RestaurantCard restaurant={r} />
                  </div>
                ))}
          </div>

          {!isLoading && filteredRestaurants.length === 0 && (
            <div className="flex flex-col items-center justify-center py-10 text-gray-400">
              <Store size={56} className="mb-3 opacity-30" />
              <p className="font-semibold">No restaurants match your filters</p>
            </div>
          )}
        </div>

        <div className="mb-8">
          <div className="flex items-start justify-between gap-4 mb-4">
            <div>
              <h2 className="text-2xl font-black text-gray-900">All Food Items</h2>
              <p className="text-sm text-gray-500 mt-1">
                {filteredItems.length} item{filteredItems.length === 1 ? '' : 's'} • search matches food name or restaurant name
              </p>
            </div>
          </div>

          {isError && (
            <div className="card p-8 text-center text-red-600">
              Failed to load menu. Please refresh and try again.
            </div>
          )}

          {isLoading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {Array.from({ length: 12 }).map((_, i) => <SkeletonCard key={i} />)}
            </div>
          ) : filteredItems.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {filteredItems.map(item => (
                <FoodItemCard
                  key={item.id}
                  item={item}
                  categoryName={item.categoryName}
                  restaurantName={item.restaurantName}
                  restaurantRating={item.restaurantRating}
                />
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-16 text-gray-400">
              <Star size={56} className="mb-3 opacity-30" />
              <p className="font-semibold">No food items available</p>
              {search.trim() && <p className="text-sm mt-1">Try a different search term</p>}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default HomePage;

