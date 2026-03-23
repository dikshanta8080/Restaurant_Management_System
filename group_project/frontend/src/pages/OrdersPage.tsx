import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Package, Clock, CheckCircle, XCircle, ChevronRight, Loader } from 'lucide-react';
import { orderService } from '../services/orderService';
import { OrderResponse, OrderStatus } from '../types/order';
import Pagination from '../components/Pagination';
import { SkeletonRow } from '../components/Skeleton';

const statusConfig: Record<OrderStatus, { label: string; cls: string; icon: React.ReactNode }> = {
  PENDING: { label: 'Pending', cls: 'badge-pending', icon: <Clock size={12} /> },
  ACCEPTED: { label: 'Accepted', cls: 'badge-confirmed', icon: <Loader size={12} className="animate-spin" /> },
  COMPLETED: { label: 'Completed', cls: 'badge-delivered', icon: <CheckCircle size={12} /> },
  REJECTED: { label: 'Rejected', cls: 'badge-cancelled', icon: <XCircle size={12} /> },
};

const OrdersPage: React.FC = () => {
  const [page, setPage] = useState(1);
  const navigate = useNavigate();

  const { data, isLoading } = useQuery({
    queryKey: ['orders', page],
    queryFn: () => orderService.getUserOrders(page - 1, 10),
  });

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 bg-orange-100 rounded-xl flex items-center justify-center">
            <Package size={22} className="text-orange-600" />
          </div>
          <div>
            <h1 className="text-3xl font-black text-gray-900">My Orders</h1>
            <p className="text-sm text-gray-500">Track all your food orders</p>
          </div>
        </div>

        {isLoading ? (
          <div className="card divide-y">
            {Array.from({ length: 5 }).map((_, i) => <SkeletonRow key={i} />)}
          </div>
        ) : !data?.content?.length ? (
          <div className="card p-16 flex flex-col items-center text-center">
            <div className="w-20 h-20 bg-orange-50 rounded-full flex items-center justify-center mb-5">
              <Package size={36} className="text-orange-300" />
            </div>
            <h2 className="text-2xl font-bold text-gray-700 mb-2">No orders yet</h2>
            <p className="text-gray-400 mb-8">Head over to our restaurants and place your first order!</p>
            <button onClick={() => navigate('/restaurants')} className="btn-primary">Browse Restaurants</button>
          </div>
        ) : (
          <>
            <div className="card divide-y divide-gray-50">
              {data.content.map((order: OrderResponse) => {
                const status = statusConfig[order.status] ?? statusConfig.PENDING;
                return (
                  <button
                    key={order.id}
                    onClick={() => navigate(`/orders/${order.id}`)}
                    className="flex items-center gap-4 p-5 w-full text-left hover:bg-orange-50/50 transition-colors"
                  >
                    <div className="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center flex-shrink-0">
                      <Package size={22} className="text-orange-600" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <p className="font-bold text-gray-900">Order #{order.id}</p>
                        <span className={`${status.cls} flex items-center gap-1`}>
                          {status.icon} {status.label}
                        </span>
                      </div>
                      <p className="text-sm text-gray-500">
                        {order.items?.length ?? 0} item{(order.items?.length ?? 0) !== 1 ? 's' : ''} •{' '}
                        {order.createdAt ? new Date(order.createdAt).toLocaleString() : ''}
                      </p>
                      <p className="text-xs text-gray-400 mt-1">
                        Deliver to: {[order.deliveryStreet, order.deliveryCity].filter(Boolean).join(', ') || '—'}
                      </p>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="font-bold text-orange-600">Rs. {Number(order.totalPrice).toFixed(2)}</span>
                      <ChevronRight size={16} className="text-gray-400" />
                    </div>
                  </button>
                );
              })}
            </div>
            <Pagination currentPage={page} totalPages={data.totalPages} onPageChange={setPage} />
          </>
        )}
      </div>
    </div>
  );
};

export default OrdersPage;
