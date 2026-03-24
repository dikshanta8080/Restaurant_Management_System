import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { foodItemService } from '../services/foodItemService';
import { customerService } from '../services/customerService';
import { reviewService } from '../services/reviewService';
import FoodItemCard from '../components/FoodItemCard';
import StarRating from '../components/StarRating';
import Modal from '../components/Modal';
import { SkeletonCard, SkeletonText } from '../components/Skeleton';
import { Store, Star, MessageSquare, Plus, ChevronDown } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { reviewService as rs } from '../services/reviewService';
import toast from 'react-hot-toast';
import { getImageUrl } from '../utils/imageUtils';
import BackButton from '../components/BackButton';

const RestaurantDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const restaurantId = Number(id);
  const { isAuthenticated, isRole } = useAuth();
  const [reviewModal, setReviewModal] = useState(false);
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [categoryFilter, setCategoryFilter] = useState('ALL');

  const { data: restaurantsData } = useQuery({
    queryKey: ['restaurants'],
    queryFn: async () => {
      const res = await customerService.getAllRestaurants();
      return res.responseObject;
    },
  });
  const restaurant = restaurantsData?.find(r => r.id === restaurantId);

  const { data: foodItems, isLoading: loadingFood } = useQuery({
    queryKey: ['food-items', restaurantId],
    queryFn: () => foodItemService.getFoodItemsByRestaurant(restaurantId),
    enabled: !!restaurantId,
  });

  const { data: reviews, refetch: refetchReviews } = useQuery({
    queryKey: ['reviews', 'restaurant', restaurantId],
    queryFn: () => reviewService.getReviewsForRestaurant(restaurantId),
    enabled: !!restaurantId,
  });

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: async () => {
      const res = await foodItemService.getAllCategories();
      return res.responseObject;
    },
  });

  const categoryMap = Object.fromEntries((categories ?? []).map(c => [c.id, c.categoryName]));

  const uniqueCategories = ['ALL', ...Array.from(new Set(foodItems?.map(f => categoryMap[f.categoryId] ?? 'OTHER') ?? []))];

  const filteredItems = foodItems?.filter(f =>
    categoryFilter === 'ALL' || categoryMap[f.categoryId] === categoryFilter
  ) ?? [];

  const avgRating = reviews && reviews.length > 0
    ? (reviews.reduce((s, r) => s + r.rating, 0) / reviews.length).toFixed(1)
    : '—';

  const handleReviewSubmit = async () => {
    if (!isAuthenticated) { toast.error('Please login to leave a review'); return; }
    setSubmittingReview(true);
    try {
      await rs.createReview({ rating, comment, restaurantId });
      toast.success('Review submitted!');
      setReviewModal(false);
      setComment('');
      setRating(5);
      refetchReviews();
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Failed to submit review');
    } finally {
      setSubmittingReview(false);
    }
  };

  const imageUrl = getImageUrl(restaurant?.imageUrl);

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      {/* Hero */}
      <div className="relative h-64 md:h-96 bg-gradient-to-br from-orange-400 to-red-500 overflow-hidden">
        {imageUrl ? (
          <img src={imageUrl} alt={restaurant?.name} className="w-full h-full object-cover opacity-80" />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-9xl opacity-30">🍽️</div>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
        <div className="absolute bottom-6 left-6 right-6 text-white">
          <h1 className="text-3xl md:text-5xl font-black">{restaurant?.name ?? 'Loading…'}</h1>
          {restaurant?.description && (
            <p className="mt-2 text-white/80 text-lg max-w-2xl">{restaurant.description}</p>
          )}
          <div className="flex items-center gap-4 mt-3 text-sm">
            <span className="flex items-center gap-1">
              <Star size={14} className="fill-yellow-400 text-yellow-400" />
              {avgRating} ({reviews?.length ?? 0} reviews)
            </span>
            <span className="text-white/60">By {restaurant?.ownerName}</span>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <BackButton className="mb-5" />
        {/* Actions bar */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Menu</h2>
            <p className="text-sm text-gray-500 mt-0.5">{foodItems?.length ?? 0} items available</p>
          </div>
          {isAuthenticated && isRole('CUSTOMER') && (
            <button
              onClick={() => setReviewModal(true)}
              className="btn-outline text-sm flex items-center gap-2"
            >
              <MessageSquare size={16} /> Write Review
            </button>
          )}
        </div>

        {/* Category filter */}
        <div className="flex gap-2 flex-wrap mb-6">
          {uniqueCategories.map(cat => (
            <button
              key={cat}
              onClick={() => setCategoryFilter(cat)}
              className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                categoryFilter === cat
                  ? 'bg-orange-500 text-white shadow-sm'
                  : 'bg-white text-gray-600 border border-gray-200 hover:border-orange-200 hover:text-orange-600'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Food Items Grid */}
        {loadingFood ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
          </div>
        ) : filteredItems.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredItems.map(item => (
              <FoodItemCard
                key={item.id}
                item={item}
                categoryName={categoryMap[item.categoryId]}
              />
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-20 text-gray-400">
            <Store size={48} className="mb-3 opacity-30" />
            <p className="font-medium">No food items in this category</p>
          </div>
        )}

        {/* Reviews Section */}
        <div className="mt-14">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">
            Customer Reviews ({reviews?.length ?? 0})
          </h2>
          {reviews && reviews.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {reviews.map(review => (
                <div key={review.id} className="card p-5">
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <div className="w-9 h-9 bg-gradient-to-br from-orange-400 to-red-400 rounded-full flex items-center justify-center text-white font-bold text-sm">
                        {review.userName?.charAt(0).toUpperCase() ?? 'U'}
                      </div>
                      <div>
                        <p className="font-semibold text-gray-900 text-sm">{review.userName}</p>
                        <p className="text-xs text-gray-400">{new Date(review.createdAt).toLocaleDateString()}</p>
                      </div>
                    </div>
                    <StarRating value={review.rating} readonly size={14} />
                  </div>
                  {review.comment && <p className="text-sm text-gray-600 mt-2">{review.comment}</p>}
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-12 text-gray-400">
              <MessageSquare size={40} className="mx-auto mb-3 opacity-30" />
              <p>No reviews yet. Be the first to review!</p>
            </div>
          )}
        </div>
      </div>

      {/* Review Modal */}
      <Modal isOpen={reviewModal} onClose={() => setReviewModal(false)} title="Write a Review">
        <div className="space-y-4">
          <div>
            <label className="label">Your Rating</label>
            <StarRating value={rating} onChange={setRating} size={32} />
          </div>
          <div>
            <label className="label">Comment (optional)</label>
            <textarea
              value={comment}
              onChange={e => setComment(e.target.value)}
              rows={4}
              placeholder="Share your experience…"
              className="input resize-none"
            />
          </div>
          <div className="flex gap-3 pt-2">
            <button onClick={() => setReviewModal(false)} className="btn-secondary flex-1">Cancel</button>
            <button
              onClick={handleReviewSubmit}
              disabled={submittingReview}
              className="btn-primary flex-1"
            >
              {submittingReview ? 'Submitting…' : 'Submit Review'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default RestaurantDetailPage;
