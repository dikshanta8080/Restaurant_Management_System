import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Package, ArrowLeft, Clock, CheckCircle, XCircle, Loader2 } from 'lucide-react';
import { orderService } from '../services/orderService';
import { OrderStatus } from '../types/order';
import { SkeletonText } from '../components/Skeleton';

const statusSteps: OrderStatus[] = ['PENDING', 'ACCEPTED', 'COMPLETED'];

const statusConfig: Record<OrderStatus, { label: string; color: string; icon: React.ReactNode }> = {
  PENDING: { label: 'Pending', color: 'text-yellow-600 bg-yellow-50', icon: <Clock size={20} className="text-yellow-500" /> },
  ACCEPTED: { label: 'Accepted / Preparing', color: 'text-blue-600 bg-blue-50', icon: <Loader2 size={20} className="text-blue-500 animate-spin" /> },
  COMPLETED: { label: 'Completed', color: 'text-green-600 bg-green-50', icon: <CheckCircle size={20} className="text-green-500" /> },
  REJECTED: { label: 'Rejected', color: 'text-red-600 bg-red-50', icon: <XCircle size={20} className="text-red-500" /> },
};

const OrderDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: order, isLoading } = useQuery({
    queryKey: ['order', id],
    queryFn: () => orderService.getOrderById(Number(id)),
    refetchInterval: 10000, // Poll every 10 seconds
  });

  if (isLoading) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-10">
        <div className="card p-8 space-y-4"><SkeletonText lines={8} /></div>
      </div>
    );
  }

  if (!order) return (
    <div className="max-w-3xl mx-auto px-4 py-10 text-center text-gray-400">
      <p className="text-xl font-semibold">Order not found</p>
    </div>
  );

  const cfg = statusConfig[order.status] ?? statusConfig.PENDING;
  const currentStep = statusSteps.indexOf(order.status);

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <button
          onClick={() => navigate('/orders')}
          className="flex items-center gap-2 text-gray-500 hover:text-orange-600 font-medium mb-6 transition-colors"
        >
          <ArrowLeft size={16} /> Back to Orders
        </button>

        <div className="card p-6 mb-6">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-2xl font-black text-gray-900">Order #{order.id}</h1>
              <p className="text-sm text-gray-500 mt-1">
                {order.createdAt ? new Date(order.createdAt).toLocaleString() : ''}
              </p>
            </div>
            <div className={`flex items-center gap-2 px-4 py-2 rounded-xl ${cfg.color} font-semibold`}>
              {cfg.icon} {cfg.label}
            </div>
          </div>

            <div className="bg-gray-50 border border-gray-100 rounded-xl p-4 mb-5">
              <div className="text-xs text-gray-500">Deliver to</div>
              <div className="mt-1 text-sm text-gray-800">
                {[order.deliveryStreet, order.deliveryCity, order.deliveryDistrict, order.deliveryProvince]
                  .filter(Boolean)
                  .join(', ') || '—'}
              </div>
            </div>

          {/* Progress tracker (only for non-rejected) */}
          {order.status !== 'REJECTED' && (
            <div className="mb-8">
              <div className="flex items-center justify-between relative">
                <div className="absolute left-0 right-0 top-5 h-0.5 bg-gray-200 -z-0" />
                <div
                  className="absolute left-0 top-5 h-0.5 bg-orange-500 transition-all duration-700 -z-0"
                  style={{ width: currentStep < 0 ? '0%' : `${(currentStep / (statusSteps.length - 1)) * 100}%` }}
                />
                {statusSteps.map((step, i) => {
                  const done = i <= currentStep;
                  const active = i === currentStep;
                  return (
                    <div key={step} className="flex flex-col items-center z-10">
                      <div className={`w-10 h-10 rounded-full flex items-center justify-center transition-all ${
                        done ? 'bg-orange-500 text-white shadow-sm shadow-orange-200' : 'bg-gray-200 text-gray-400'
                      } ${active ? 'ring-4 ring-orange-100' : ''}`}>
                        {i === 0 && <Clock size={18} />}
                        {i === 1 && <Loader2 size={18} className={active ? 'animate-spin' : ''} />}
                        {i === 2 && <CheckCircle size={18} />}
                      </div>
                      <p className={`text-xs font-medium mt-2 ${done ? 'text-orange-600' : 'text-gray-400'}`}>
                        {statusConfig[step].label.split('/')[0].trim()}
                      </p>
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Items */}
          <div className="border border-gray-100 rounded-xl overflow-hidden">
            <div className="bg-gray-50 px-4 py-3 flex justify-between text-xs font-semibold text-gray-500 uppercase tracking-wide">
              <span>Item</span>
              <span>Subtotal</span>
            </div>
            {order.items?.map(item => (
              <div key={item.id} className="flex justify-between items-center px-4 py-3 border-t border-gray-50">
                <div>
                  <p className="font-medium text-gray-900">{item.foodItemName}</p>
                  <p className="text-sm text-gray-500">Rs. {Number(item.price).toFixed(2)} × {item.quantity}</p>
                </div>
                <p className="font-semibold text-gray-700">Rs. {(Number(item.price) * item.quantity).toFixed(2)}</p>
              </div>
            ))}
            <div className="border-t border-gray-100 bg-orange-50 px-4 py-4 flex justify-between items-center">
              <span className="font-bold text-gray-900">Total</span>
              <span className="text-xl font-black text-orange-600">Rs. {Number(order.totalPrice).toFixed(2)}</span>
            </div>
          </div>
        </div>

        <p className="text-center text-xs text-gray-400">Status updates automatically every 10 seconds</p>
      </div>
    </div>
  );
};

export default OrderDetailPage;
