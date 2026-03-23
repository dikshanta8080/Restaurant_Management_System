import React, { useEffect, useState } from 'react';
import { ShoppingCart, Plus, Minus } from 'lucide-react';
import { FoodItemResponse } from '../types/foodItem';
import { cartService } from '../services/cartService';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { getImageUrl } from '../utils/imageUtils';

interface FoodItemCardProps {
  item: FoodItemResponse;
  categoryName?: string;
}

const FoodItemCard: React.FC<FoodItemCardProps> = ({ item, categoryName }) => {
  const { isAuthenticated } = useAuth();
  const { addItem } = useCart();
  const navigate = useNavigate();
  const [qty, setQty] = useState(1);
  const [adding, setAdding] = useState(false);
  const [imageFailed, setImageFailed] = useState(false);

  const imageUrl = getImageUrl(item.imageUrl);
  useEffect(() => {
    setImageFailed(false);
  }, [imageUrl]);

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    setAdding(true);
    try {
      const cartItem = await cartService.addToCart({ foodItemId: item.id, quantity: qty });
      addItem(cartItem);
      toast.success(`${item.name} added to cart!`);
      setQty(1);
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Failed to add to cart');
    } finally {
      setAdding(false);
    }
  };

  return (
    <div className={`card overflow-hidden group transition-all duration-300 hover:-translate-y-0.5 ${!item.available ? 'opacity-60' : ''}`}>
      {/* Image */}
      <div className="relative h-44 bg-gradient-to-br from-orange-50 to-amber-50 overflow-hidden">
        {imageUrl && !imageFailed ? (
          <img
            src={imageUrl}
            alt={item.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            onError={() => setImageFailed(true)}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-5xl">🍛</div>
        )}
        {!item.available && (
          <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
            <span className="bg-white text-gray-800 text-xs font-semibold px-3 py-1.5 rounded-full">Unavailable</span>
          </div>
        )}
        {categoryName && (
          <div className="absolute top-2 left-2">
            <span className="bg-white/90 backdrop-blur-sm text-xs font-medium px-2 py-1 rounded-full text-gray-700">
              {categoryName}
            </span>
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-4">
        <div className="flex justify-between items-start mb-1">
          <h3 className="font-bold text-gray-900 leading-tight">{item.name}</h3>
          <span className="text-orange-600 font-bold text-lg ml-2 flex-shrink-0">
            Rs. {Number(item.price).toFixed(2)}
          </span>
        </div>
        {item.description && (
          <p className="text-sm text-gray-500 mb-3 line-clamp-2">{item.description}</p>
        )}

        {item.available && (
          <div className="flex items-center gap-2 mt-3">
            {/* Qty controls */}
            <div className="flex items-center gap-1 border border-gray-200 rounded-xl overflow-hidden">
              <button
                onClick={() => setQty(q => Math.max(1, q - 1))}
                className="p-1.5 hover:bg-gray-100 text-gray-600 transition-colors"
              >
                <Minus size={14} />
              </button>
              <span className="w-7 text-center text-sm font-semibold">{qty}</span>
              <button
                onClick={() => setQty(q => q + 1)}
                className="p-1.5 hover:bg-gray-100 text-gray-600 transition-colors"
              >
                <Plus size={14} />
              </button>
            </div>
            <button
              onClick={handleAddToCart}
              disabled={adding}
              className="flex-1 btn-primary text-sm py-2 flex items-center justify-center gap-1.5"
            >
              <ShoppingCart size={15} />
              {adding ? 'Adding…' : 'Add to Cart'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default FoodItemCard;
