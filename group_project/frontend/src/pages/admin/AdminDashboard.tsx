import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ShieldCheck, Store, Users, Clock, ArrowRight } from 'lucide-react';
import { adminService } from '../../services/adminService';
import StatCard from '../../components/StatCard';
import { SkeletonStat } from '../../components/Skeleton';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const COLORS = ['#f97316', '#3b82f6', '#22c55e', '#ef4444'];

const AdminDashboard: React.FC = () => {
  const { data: allRestaurants, isLoading: loadingRest } = useQuery({
    queryKey: ['admin-restaurants'],
    queryFn: async () => {
      const res = await adminService.getAllRestaurants();
      return res.responseObject;
    },
  });

  const { data: pending } = useQuery({
    queryKey: ['admin-pending'],
    queryFn: async () => {
      const res = await adminService.getPendingRestaurants();
      return res.responseObject;
    },
  });

  const approved = allRestaurants?.filter(r => r.status === 'APPROVED').length ?? 0;
  const rejected = allRestaurants?.filter(r => r.status === 'REJECTED').length ?? 0;
  const pendingCount = pending?.length ?? 0;

  const pieData = [
    { name: 'Approved', value: approved },
    { name: 'Pending', value: pendingCount },
    { name: 'Rejected', value: rejected },
  ].filter(d => d.value > 0);

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 bg-purple-100 rounded-xl flex items-center justify-center">
            <ShieldCheck size={22} className="text-purple-600" />
          </div>
          <div>
            <h1 className="text-3xl font-black text-gray-900">Admin Dashboard</h1>
            <p className="text-sm text-gray-500">Platform overview and management</p>
          </div>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          {loadingRest ? (
            Array.from({ length: 4 }).map((_, i) => <SkeletonStat key={i} />)
          ) : (
            <>
              <StatCard title="Total Restaurants" value={allRestaurants?.length ?? 0} icon={<Store size={18} />} color="orange" />
              <StatCard title="Approved" value={approved} icon={<Store size={18} />} color="green" />
              <StatCard title="Pending Approval" value={pendingCount} icon={<Clock size={18} />} color="blue" />
              <StatCard title="Rejected" value={rejected} icon={<Store size={18} />} color="purple" />
            </>
          )}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          {/* Pie Chart */}
          {pieData.length > 0 && (
            <div className="card p-6">
              <h2 className="text-lg font-bold text-gray-900 mb-4">Restaurant Status Distribution</h2>
              <ResponsiveContainer width="100%" height={220}>
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" outerRadius={80} dataKey="value" label={({ name, value }) => `${name}: ${value}`}>
                    {pieData.map((_, index) => (
                      <Cell key={index} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
          )}

          {/* Pending List Preview */}
          <div className="card p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-gray-900">Pending Approvals</h2>
              <Link to="/admin/approvals" className="text-sm text-orange-600 font-medium hover:text-orange-700">View all</Link>
            </div>
            {!pending?.length ? (
              <p className="text-gray-400 text-sm py-6 text-center">No pending requests</p>
            ) : (
              <div className="space-y-3">
                {pending.slice(0, 4).map(r => (
                  <div key={r.id} className="flex items-center gap-3 p-3 bg-amber-50 rounded-xl">
                    <div className="w-9 h-9 bg-amber-100 rounded-lg flex items-center justify-center">
                      <Store size={16} className="text-amber-600" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-semibold text-gray-900 text-sm truncate">{r.name}</p>
                      <p className="text-xs text-gray-500">by {r.ownerName}</p>
                    </div>
                    <span className="badge-pending">Pending</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {[
            { to: '/admin/approvals', label: 'Restaurant Approvals', desc: 'Approve or reject registrations', icon: <Clock size={22} />, color: 'bg-amber-100 text-amber-600' },
            { to: '/admin/restaurants', label: 'All Restaurants', desc: 'View and manage all restaurants', icon: <Store size={22} />, color: 'bg-orange-100 text-orange-600' },
            { to: '/admin/customers', label: 'Customers', desc: 'View and manage customers', icon: <Users size={22} />, color: 'bg-blue-100 text-blue-600' },
          ].map(item => (
            <Link key={item.to} to={item.to} className="card p-5 flex items-center justify-between hover:shadow-md transition-all group">
              <div className="flex items-center gap-3">
                <div className={`w-11 h-11 rounded-xl flex items-center justify-center ${item.color} group-hover:scale-110 transition-transform`}>
                  {item.icon}
                </div>
                <div>
                  <h3 className="font-bold text-gray-900 text-sm">{item.label}</h3>
                  <p className="text-xs text-gray-500">{item.desc}</p>
                </div>
              </div>
              <ArrowRight size={16} className="text-gray-400 group-hover:text-orange-500 group-hover:translate-x-1 transition-all" />
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
