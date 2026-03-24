import React, { useEffect, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ShoppingCart, Trash2, Plus, Minus, ArrowRight, Package } from 'lucide-react';
import { cartService } from '../services/cartService';
import { orderService } from '../services/orderService';
import { paymentService } from '../services/paymentService';
import { customerService } from '../services/customerService';
import { useCart } from '../context/CartContext';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { SkeletonRow } from '../components/Skeleton';
import BackButton from '../components/BackButton';
import {
  DEFAULT_STREETS,
  NEPAL_DISTRICT_TO_CITIES,
  NEPAL_DISTRICTS_ALL,
  NEPAL_DISTRICTS_BY_PROVINCE,
  NEPAL_PROVINCES,
  type NepalProvince,
} from '../utils/nepalLocations';

const CartPage: React.FC = () => {
  const { cartItems, setCartItems, removeItem, updateItem, cartTotal } = useCart();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [useSavedAddress, setUseSavedAddress] = useState(true);
  const [overrideAddress, setOverrideAddress] = useState({
    province: '',
    district: '',
    city: '',
    street: '',
  });

  const districtOptions =
    overrideAddress.province &&
    NEPAL_DISTRICTS_BY_PROVINCE[overrideAddress.province as NepalProvince]
      ? NEPAL_DISTRICTS_BY_PROVINCE[overrideAddress.province as NepalProvince]
      : NEPAL_DISTRICTS_ALL;

  const cityOptions = overrideAddress.district
    ? NEPAL_DISTRICT_TO_CITIES[overrideAddress.district] ?? []
    : [];

  const { isLoading } = useQuery({
    queryKey: ['cart'],
    queryFn: async () => {
      const items = await cartService.getCart();
      setCartItems(items);
      return items;
    },
  });

  const { data: profile } = useQuery({
    queryKey: ['profile-for-order'],
    queryFn: async () => {
      const res = await customerService.getProfile();
      return res.responseObject;
    },
    staleTime: 30000,
    retry: 1,
  });

  useEffect(() => {
    if (!profile) return;
    setOverrideAddress({
      province: profile.province ?? '',
      district: profile.district ?? '',
      city: profile.city ?? '',
      street: profile.street ?? '',
    });
  }, [profile]);

  const overrideValid = useSavedAddress
    ? true
    : !!overrideAddress.province.trim() &&
      !!overrideAddress.district.trim() &&
      !!overrideAddress.city.trim() &&
      !!overrideAddress.street.trim();

  const removeMutation = useMutation({
    mutationFn: (id: number) => cartService.removeCartItem(id),
    onSuccess: (_, id) => {
      removeItem(id);
      queryClient.invalidateQueries({ queryKey: ['cart'] });
      toast.success('Item removed');
    },
    onError: () => toast.error('Failed to remove item'),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, qty }: { id: number; qty: number }) => cartService.updateCartItem(id, qty),
    onSuccess: (data, { id }) => {
      updateItem(id, data.quantity);
    },
    onError: () => toast.error('Failed to update quantity'),
  });

  const handleUpdateQty = (id: number, currentQty: number, delta: number) => {
    const newQty = currentQty + delta;
    if (newQty < 1) {
      removeMutation.mutate(id);
    } else {
      updateMutation.mutate({ id, qty: newQty });
    }
  };

  const placeOrderMutation = useMutation({
    mutationFn: () => {
      const base = {
        items: cartItems.map(i => ({ foodItemId: i.foodItemId, quantity: i.quantity })),
      };

      // Use saved address by default; allow override when user edits fields.
      if (useSavedAddress) {
        return orderService.placeOrder({
          ...base,
          addressId: profile?.addressId,
        });
      }

      return orderService.placeOrder({
        ...base,
        province: overrideAddress.province.trim(),
        district: overrideAddress.district.trim(),
        city: overrideAddress.city.trim(),
        street: overrideAddress.street.trim(),
      });
    },
    onSuccess: async (placement) => {
      const orders = placement?.orders ?? [];
      if (!orders.length) {
        toast.error('No orders were created from your cart.');
        return;
      }
      try {
        if (orders.length > 1) {
          toast('Multiple restaurant orders created. Redirecting to payment for the first order.');
        }
        const primaryOrder = orders[0];
        const sessionRes = await paymentService.createCheckoutSession(primaryOrder.id);
        const checkout = sessionRes?.responseObject;
        if (!checkout?.sessionId || !checkout?.checkoutUrl) {
          throw new Error('Stripe checkout session was not returned by backend');
        }
        window.location.href = checkout.checkoutUrl;
      } catch (e: any) {
        const ids = orders.map(o => o.id).join(',');
        toast.error(e?.response?.data?.message || e?.message || 'Payment initialization failed. Your order was created and can be tracked from Orders.');
        navigate(`/payment-cancel?order_ids=${ids}`);
        return;
      }
    },
    onError: (err: any) => toast.error(err?.response?.data?.message || 'Failed to place order'),
  });

  const deliveryFee = cartItems.length > 0 ? 50 : 0;

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <BackButton className="mb-6" />
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 bg-orange-100 rounded-xl flex items-center justify-center">
            <ShoppingCart size={22} className="text-orange-600" />
          </div>
          <div>
            <h1 className="text-3xl font-black text-gray-900">Your Cart</h1>
            <p className="text-gray-500 text-sm">{cartItems.length} item{cartItems.length !== 1 ? 's' : ''}</p>
          </div>
        </div>

        {isLoading ? (
          <div className="card divide-y">
            {Array.from({ length: 3 }).map((_, i) => <SkeletonRow key={i} />)}
          </div>
        ) : cartItems.length === 0 ? (
          <div className="card p-16 flex flex-col items-center text-center">
            <div className="w-24 h-24 bg-orange-50 rounded-full flex items-center justify-center mb-6">
              <ShoppingCart size={40} className="text-orange-300" />
            </div>
            <h2 className="text-2xl font-bold text-gray-700 mb-2">Your cart is empty</h2>
            <p className="text-gray-400 mb-8">Add some delicious food items to get started</p>
            <button onClick={() => navigate('/restaurants')} className="btn-primary">
              Browse Restaurants
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-3">
              {cartItems.map(item => (
                <div key={item.id} className="card p-4 flex items-center gap-4">
                  <div className="w-16 h-16 bg-orange-50 rounded-xl flex items-center justify-center flex-shrink-0 text-2xl">
                    🍛
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-gray-900 truncate">{item.foodItemName}</p>
                    <p className="text-orange-600 font-bold">Rs. {Number(item.price).toFixed(2)}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="flex items-center gap-1 border border-gray-200 rounded-xl overflow-hidden">
                      <button
                        onClick={() => handleUpdateQty(item.id, item.quantity, -1)}
                        className="p-1.5 hover:bg-gray-100 text-gray-600 transition-colors"
                      >
                        <Minus size={14} />
                      </button>
                      <span className="w-8 text-center text-sm font-bold">{item.quantity}</span>
                      <button
                        onClick={() => handleUpdateQty(item.id, item.quantity, 1)}
                        className="p-1.5 hover:bg-gray-100 text-gray-600 transition-colors"
                      >
                        <Plus size={14} />
                      </button>
                    </div>
                    <span className="text-sm font-semibold text-gray-700 w-20 text-right">
                      Rs. {(Number(item.price) * item.quantity).toFixed(2)}
                    </span>
                    <button
                      onClick={() => removeMutation.mutate(item.id)}
                      className="p-1.5 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>
              ))}
            </div>

            <div className="card p-6 h-fit sticky top-24">
              <h2 className="text-lg font-bold text-gray-900 mb-4">Order Summary</h2>
              <div className="bg-gray-50 border border-gray-100 rounded-xl p-4 mb-4">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="text-sm font-semibold text-gray-900">Delivery Address</div>
                    <div className="text-xs text-gray-500 mt-1">
                      {useSavedAddress
                        ? 'Using your saved address'
                        : 'Using the address below'}
                    </div>
                  </div>
                  <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer select-none">
                    <input
                      type="checkbox"
                      checked={useSavedAddress}
                      onChange={(e) => setUseSavedAddress(e.target.checked)}
                    />
                    Use saved
                  </label>
                </div>

                <div className="mt-3 text-sm text-gray-800">
                  {profile && (
                    <div className="space-y-1">
                      <div>
                        <span className="font-semibold">Saved:</span>{' '}
                        {[profile.street, profile.city, profile.district, profile.province].filter(Boolean).join(', ') || '—'}
                      </div>
                      {!useSavedAddress && (
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-3">
                          <div>
                            <label className="label">Province</label>
                            <select
                              value={overrideAddress.province}
                              onChange={(e) =>
                                setOverrideAddress((p) => ({ ...p, province: e.target.value, district: '', city: '' }))
                              }
                              className="input"
                            >
                              <option value="">Select Province</option>
                              {NEPAL_PROVINCES.map(p => (
                                <option key={p} value={p}>
                                  {p}
                                </option>
                              ))}
                            </select>
                          </div>
                          <div>
                            <label className="label">District</label>
                            <select
                              value={overrideAddress.district}
                              onChange={(e) =>
                                setOverrideAddress((p) => ({ ...p, district: e.target.value, city: '' }))
                              }
                              className="input"
                            >
                              <option value="">Select District</option>
                              {districtOptions.map(d => (
                                <option key={d} value={d}>
                                  {d}
                                </option>
                              ))}
                            </select>
                          </div>
                          <div>
                            <label className="label">City</label>
                            <input
                              value={overrideAddress.city}
                              onChange={(e) => setOverrideAddress((p) => ({ ...p, city: e.target.value }))}
                              className="input"
                              list="cart-city-suggestions"
                              placeholder="e.g. Kathmandu"
                            />
                            <datalist id="cart-city-suggestions">
                              {cityOptions.map(c => (
                                <option key={c} value={c} />
                              ))}
                            </datalist>
                          </div>
                          <div>
                            <label className="label">Street</label>
                            <input
                              value={overrideAddress.street}
                              onChange={(e) => setOverrideAddress((p) => ({ ...p, street: e.target.value }))}
                              className="input"
                              list="cart-street-suggestions"
                              placeholder="e.g. Thamel"
                            />
                            <datalist id="cart-street-suggestions">
                              {DEFAULT_STREETS.map(s => (
                                <option key={s} value={s} />
                              ))}
                            </datalist>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </div>

              <div className="space-y-3 text-sm">
                <div className="flex justify-between text-gray-600">
                  <span>Subtotal</span>
                  <span className="font-medium">Rs. {cartTotal.toFixed(2)}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Delivery Fee</span>
                  <span className="font-medium">Rs. {deliveryFee.toFixed(2)}</span>
                </div>
                <div className="border-t border-gray-100 pt-3 flex justify-between font-bold text-gray-900">
                  <span>Total</span>
                  <span className="text-orange-600 text-lg">Rs. {(cartTotal + deliveryFee).toFixed(2)}</span>
                </div>
              </div>
              <button
                onClick={() => placeOrderMutation.mutate()}
                disabled={placeOrderMutation.isPending || !overrideValid}
                className="btn-primary w-full mt-6 flex items-center justify-center gap-2 py-3"
              >
                {placeOrderMutation.isPending ? 'Placing order…' : (
                  <>Pay Now <ArrowRight size={16} /></>
                )}
              </button>
              <button onClick={() => navigate('/restaurants')} className="btn-secondary w-full mt-2 text-sm">
                Continue Shopping
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CartPage;
