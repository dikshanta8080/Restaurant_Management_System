import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Store, Trash2, Search, CheckCircle, XCircle, Clock } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { SkeletonRow } from '../../components/Skeleton';
import toast from 'react-hot-toast';

const AllRestaurantsAdmin: React.FC = () => {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');

  const { data: restaurants, isLoading } = useQuery({
    queryKey: ['admin-restaurants'],
    queryFn: async () => {
      const res = await adminService.getAllRestaurants();
      return res.responseObject;
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: 'APPROVED' | 'REJECTED' | 'PENDING' }) =>
      adminService.updateRestaurantStatus({ restaurantId: id, restaurantStatus: status }),
    onSuccess: () => {
      toast.success('Status updated!');
      queryClient.invalidateQueries({ queryKey: ['admin-restaurants'] });
      queryClient.invalidateQueries({ queryKey: ['admin-pending'] });
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Update failed'),
  });

  const filtered = restaurants?.filter(r =>
    (r.name ?? '').toLowerCase().includes(search.toLowerCase()) ||
    r.ownerName?.toLowerCase().includes(search.toLowerCase())
  ) ?? [];

  const statusIcon = (s: string) => {
    if (s === 'APPROVED') return <CheckCircle size={14} className="text-green-500" />;
    if (s === 'REJECTED') return <XCircle size={14} className="text-red-400" />;
    return <Clock size={14} className="text-yellow-500" />;
  };

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 bg-orange-100 rounded-xl flex items-center justify-center">
            <Store size={22} className="text-orange-600" />
          </div>
          <div>
            <h1 className="text-3xl font-black text-gray-900">All Restaurants</h1>
            <p className="text-sm text-gray-500">{restaurants?.length ?? 0} total</p>
          </div>
        </div>

        <div className="relative max-w-sm mb-6">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input value={search} onChange={e => setSearch(e.target.value)} className="input pl-9 text-sm" placeholder="Search restaurants…" />
        </div>

        {isLoading ? (
          <div className="card divide-y">
            {Array.from({ length: 6 }).map((_, i) => <SkeletonRow key={i} />)}
          </div>
        ) : filtered.length === 0 ? (
          <div className="card flex flex-col items-center py-16 text-gray-400 text-center">
            <Store size={48} className="mb-3 opacity-30" />
            <p className="font-medium">No restaurants found</p>
          </div>
        ) : (
          <div className="card divide-y">
            {filtered.map(r => (
              <div key={r.id} className="flex items-center gap-4 p-4 hover:bg-gray-50/50 transition-colors">
                <div className="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center flex-shrink-0 overflow-hidden">
                  {r.imageUrl ? (
                    <img src={`/${r.imageUrl}`} alt={r.name} className="w-full h-full object-cover" onError={e => { (e.target as HTMLImageElement).style.display = 'none'; }} />
                  ) : <Store size={20} className="text-orange-400" />}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="font-semibold text-gray-900 truncate">{r.name}</p>
                    <span className={`flex items-center gap-1 text-xs px-2 py-0.5 rounded-full ${
                      r.status === 'APPROVED' ? 'bg-green-100 text-green-700' :
                      r.status === 'REJECTED' ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700'
                    }`}>
                      {statusIcon(r.status)} {r.status}
                    </span>
                  </div>
                  <p className="text-xs text-gray-400">by {r.ownerName}</p>
                </div>
                <div className="flex gap-2">
                  {r.status !== 'APPROVED' && (
                    <button
                      onClick={() => updateMutation.mutate({ id: r.id, status: 'APPROVED' })}
                      className="text-xs font-medium px-3 py-1.5 bg-green-50 text-green-700 hover:bg-green-100 rounded-lg transition-colors border border-green-100"
                    >
                      Approve
                    </button>
                  )}
                  {r.status !== 'REJECTED' && (
                    <button
                      onClick={() => updateMutation.mutate({ id: r.id, status: 'REJECTED' })}
                      className="text-xs font-medium px-3 py-1.5 bg-red-50 text-red-600 hover:bg-red-100 rounded-lg transition-colors border border-red-100"
                    >
                      Reject
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default AllRestaurantsAdmin;
