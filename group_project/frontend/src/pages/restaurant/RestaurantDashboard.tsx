import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { Package, DollarSign, Utensils, CheckCircle, ArrowRight, Store } from 'lucide-react';
import { restaurantService } from '../../services/restaurantService';
import { foodItemService } from '../../services/foodItemService';
import { orderService } from '../../services/orderService';
import StatCard from '../../components/StatCard';
import { SkeletonStat } from '../../components/Skeleton';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
import { OrderStatus } from '../../types/order';

const RestaurantDashboard: React.FC = () => {
  const { data: restaurantData, isLoading: loadingRest } = useQuery({
    queryKey: ['own-restaurant'],
    queryFn: async () => {
      const res = await restaurantService.getOwnRestaurant();
      return res.responseObject;
    },
    retry: 1,
  });

  const { data: foodItems } = useQuery({
    queryKey: ['restaurant-food-items', restaurantData?.id],
    queryFn: () => foodItemService.getFoodItemsByRestaurant(restaurantData!.id),
    enabled: !!restaurantData?.id,
  });

  const { data: orders } = useQuery({
    queryKey: ['restaurant-orders'],
    queryFn: async () => {
      const res = await orderService.getUserOrders(0, 100);
      return res.content ?? [];
    },
    retry: 1,
  });

  const totalRevenue = orders?.filter(o => o.status === 'DELIVERED')
    .reduce((s, o) => s + Number(o.totalPrice), 0) ?? 0;
  const completedOrders = orders?.filter(o => o.status === 'DELIVERED').length ?? 0;

  // Chart data — orders by status
  const chartData: { name: string; count: number }[] = ['PENDING', 'CONFIRMED', 'DELIVERED', 'CANCELLED'].map(s => ({
    name: s.charAt(0) + s.slice(1).toLowerCase(),
    count: orders?.filter(o => o.status === s as OrderStatus).length ?? 0,
  }));

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-orange-100 rounded-xl flex items-center justify-center">
              <Store size={22} className="text-orange-600" />
            </div>
            <div>
              <h1 className="text-3xl font-black text-gray-900">
                {restaurantData?.name ?? 'Restaurant Dashboard'}
              </h1>
              <p className="text-sm text-gray-500">Welcome back! Here's your overview.</p>
            </div>
          </div>
          {restaurantData && (
            <span className={`px-3 py-1.5 rounded-full text-sm font-semibold ${
              restaurantData.status === 'APPROVED' ? 'badge-approved' :
              restaurantData.status === 'PENDING' ? 'badge-pending' : 'badge-rejected'
            }`}>
              {restaurantData.status}
            </span>
          )}
        </div>

        {/* Pending warning */}
        {restaurantData?.status === 'PENDING' && (
          <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 mb-6">
            <p className="text-amber-800 font-medium">⏳ Your restaurant is pending admin approval. You can set it up meanwhile.</p>
          </div>
        )}

        {/* Stats */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          {loadingRest ? (
            Array.from({ length: 4 }).map((_, i) => <SkeletonStat key={i} />)
          ) : (
            <>
              <StatCard title="Total Revenue" value={`Rs. ${totalRevenue.toLocaleString()}`} icon={<DollarSign size={18} />} color="orange" />
              <StatCard title="Total Orders" value={orders?.length ?? 0} icon={<Package size={18} />} color="blue" />
              <StatCard title="Food Items" value={foodItems?.length ?? 0} icon={<Utensils size={18} />} color="green" />
              <StatCard title="Completed" value={completedOrders} icon={<CheckCircle size={18} />} color="purple" />
            </>
          )}
        </div>

        {/* Chart */}
        <div className="card p-6 mb-6">
          <h2 className="text-lg font-bold text-gray-900 mb-4">Orders by Status</h2>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={chartData} barSize={40}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 13 }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 13 }} />
              <Tooltip
                contentStyle={{ borderRadius: '12px', border: '1px solid #e5e7eb', fontSize: '13px' }}
              />
              <Bar dataKey="count" fill="#f97316" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Link to="/restaurant/food-items" className="card p-6 flex items-center justify-between hover:shadow-md transition-all group">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
                <Utensils size={22} className="text-orange-600" />
              </div>
              <div>
                <h3 className="font-bold text-gray-900">Manage Menu</h3>
                <p className="text-sm text-gray-500">Add, edit or remove food items</p>
              </div>
            </div>
            <ArrowRight size={18} className="text-gray-400 group-hover:text-orange-500 group-hover:translate-x-1 transition-all" />
          </Link>
          <Link to="/restaurant/orders" className="card p-6 flex items-center justify-between hover:shadow-md transition-all group">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
                <Package size={22} className="text-blue-600" />
              </div>
              <div>
                <h3 className="font-bold text-gray-900">View Orders</h3>
                <p className="text-sm text-gray-500">Update order statuses</p>
              </div>
            </div>
            <ArrowRight size={18} className="text-gray-400 group-hover:text-orange-500 group-hover:translate-x-1 transition-all" />
          </Link>
        </div>
      </div>
    </div>
  );
};

export default RestaurantDashboard;
