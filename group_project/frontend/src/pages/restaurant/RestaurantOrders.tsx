import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Package, Clock, CheckCircle, XCircle, Loader2 } from 'lucide-react';
import { orderService } from '../../services/orderService';
import { OrderResponse, OrderStatus } from '../../types/order';
import { SkeletonRow } from '../../components/Skeleton';
import toast from 'react-hot-toast';
import BackButton from '../../components/BackButton';

const statusConfig: Record<OrderStatus, { label: string; cls: string; icon: React.ReactNode }> = {
  PENDING: { label: 'Pending', cls: 'badge-pending', icon: <Clock size={12} /> },
  PAID: { label: 'Paid', cls: 'badge-confirmed', icon: <CheckCircle size={12} /> },
  ACCEPTED: { label: 'Accepted', cls: 'badge-confirmed', icon: <Loader2 size={12} className="animate-spin" /> },
  COMPLETED: { label: 'Completed', cls: 'badge-delivered', icon: <CheckCircle size={12} /> },
  REJECTED: { label: 'Rejected', cls: 'badge-cancelled', icon: <XCircle size={12} /> },
};

const nextStatus: Partial<Record<OrderStatus, OrderStatus>> = {
  PAID: 'ACCEPTED',
  ACCEPTED: 'COMPLETED',
};

const RestaurantOrders: React.FC = () => {
  const queryClient = useQueryClient();
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const { data: ordersData, isLoading } = useQuery({
    queryKey: ['restaurant-orders-manage'],
    queryFn: async () => {
      const res = await orderService.getRestaurantIncomingOrders(0, 100);
      return res.content ?? [];
    },
    refetchInterval: 15000,
    retry: 1,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: OrderStatus }) =>
      orderService.updateOrderStatus(id, status),
    onSuccess: () => {
      toast.success('Order status updated!');
      queryClient.invalidateQueries({ queryKey: ['restaurant-orders-manage'] });
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Failed to update status'),
  });

  const allStatuses = ['ALL', 'PENDING', 'PAID', 'ACCEPTED', 'COMPLETED', 'REJECTED'];
  const filtered = ordersData?.filter((o: OrderResponse) => statusFilter === 'ALL' || o.status === statusFilter) ?? [];

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <BackButton className="mb-6" redirectTo="/restaurant/dashboard" />
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center">
            <Package size={22} className="text-blue-600" />
          </div>
          <div>
            <h1 className="text-3xl font-black text-gray-900">Incoming Orders</h1>
            <p className="text-sm text-gray-500">Refreshes every 15 seconds</p>
          </div>
        </div>

        {/* Status filter tabs */}
        <div className="flex gap-2 flex-wrap mb-6">
          {allStatuses.map(s => {
            const count = s === 'ALL'
              ? (ordersData?.length ?? 0)
              : (ordersData?.filter((o: OrderResponse) => o.status === s).length ?? 0);
            return (
              <button
                key={s}
                onClick={() => setStatusFilter(s)}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all flex items-center gap-1.5 ${
                  statusFilter === s ? 'bg-orange-500 text-white shadow-sm' : 'bg-white border border-gray-200 text-gray-600 hover:border-orange-200 hover:text-orange-600'
                }`}
              >
                {s === 'ALL' ? 'All' : s.charAt(0) + s.slice(1).toLowerCase()}
                <span className={`text-xs px-1.5 py-0.5 rounded-full ${statusFilter === s ? 'bg-white/20' : 'bg-gray-100'}`}>
                  {count}
                </span>
              </button>
            );
          })}
        </div>

        {isLoading ? (
          <div className="card divide-y">
            {Array.from({ length: 5 }).map((_, i) => <SkeletonRow key={i} />)}
          </div>
        ) : filtered.length === 0 ? (
          <div className="card flex flex-col items-center justify-center py-16 text-center text-gray-400">
            <Package size={48} className="mb-3 opacity-30" />
            <p className="font-medium text-lg">No orders{statusFilter !== 'ALL' ? ` with status ${statusFilter.toLowerCase()}` : ''}</p>
          </div>
        ) : (
          <div className="space-y-3">
            {filtered.map((order: OrderResponse) => {
              const cfg = statusConfig[order.status] ?? statusConfig.PENDING;
              const next = nextStatus[order.status];
              return (
                <div key={order.id} className="card p-5">
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <h3 className="font-bold text-gray-900">Order #{order.id}</h3>
                        <span className={`${cfg.cls} flex items-center gap-1`}>
                          {cfg.icon} {cfg.label}
                        </span>
                      </div>
                      <p className="text-xs text-gray-400">
                        {order.createdAt ? new Date(order.createdAt).toLocaleString() : ''}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-lg font-black text-orange-600">Rs. {Number(order.totalPrice).toFixed(2)}</p>
                    </div>
                  </div>

                  {/* Items */}
                  <div className="border border-gray-100 rounded-xl divide-y mb-4 text-sm">
                    {order.items?.map(item => (
                      <div key={item.id} className="flex justify-between px-3 py-2">
                        <span className="text-gray-700">{item.foodItemName} × {item.quantity}</span>
                        <span className="font-medium">Rs. {(Number(item.price) * item.quantity).toFixed(2)}</span>
                      </div>
                    ))}
                  </div>

                  {/* Action */}
                  {(order.status === 'PENDING' || order.status === 'PAID') && (
                    <div className="flex gap-2">
                      <button
                        onClick={() => updateMutation.mutate({ id: order.id, status: 'ACCEPTED' })}
                        disabled={updateMutation.isPending}
                        className="btn-primary flex-1 text-sm py-2"
                      >
                        {order.status === 'PAID' ? 'Accept Paid Order' : 'Accept Order'}
                      </button>
                      <button
                        onClick={() => updateMutation.mutate({ id: order.id, status: 'REJECTED' })}
                        disabled={updateMutation.isPending}
                        className="btn-danger flex-1 text-sm py-2"
                      >
                        Reject Order
                      </button>
                    </div>
                  )}
                  {order.status === 'ACCEPTED' && next && (
                    <button
                      onClick={() => updateMutation.mutate({ id: order.id, status: next })}
                      disabled={updateMutation.isPending}
                      className="btn-primary w-full text-sm py-2"
                    >
                      Mark as Completed
                    </button>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default RestaurantOrders;
