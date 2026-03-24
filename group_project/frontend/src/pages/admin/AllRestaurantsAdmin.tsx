import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Store, Trash2, Search, CheckCircle, XCircle, Clock } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { SkeletonRow } from '../../components/Skeleton';
import toast from 'react-hot-toast';
import { getImageUrl } from '../../utils/imageUtils';
import BackButton from '../../components/BackButton';
import Modal from '../../components/Modal';

const AllRestaurantsAdmin: React.FC = () => {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [brokenImageIds, setBrokenImageIds] = useState<Record<number, boolean>>({});
  const [deleteId, setDeleteId] = useState<number | null>(null);

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

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminService.deleteRestaurant(id),
    onMutate: async (id: number) => {
      await queryClient.cancelQueries({ queryKey: ['admin-restaurants'] });
      const previous = queryClient.getQueryData<any[]>(['admin-restaurants']);
      queryClient.setQueryData<any[]>(['admin-restaurants'], old =>
        (old ?? []).filter(r => r.id !== id)
      );
      return { previous };
    },
    onSuccess: () => {
      toast.success('Restaurant deleted successfully');
      setDeleteId(null);
      queryClient.invalidateQueries({ queryKey: ['admin-restaurants'] });
      queryClient.invalidateQueries({ queryKey: ['admin-pending'] });
      queryClient.invalidateQueries({ queryKey: ['restaurants'] });
      queryClient.invalidateQueries({ queryKey: ['home-feed'] });
    },
    onError: (e: any) => {
      queryClient.invalidateQueries({ queryKey: ['admin-restaurants'] });
      toast.error(e?.response?.data?.message || 'Failed to delete restaurant');
    },
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
        <BackButton className="mb-6" redirectTo="/admin" />
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
                  {(() => {
                    const imgUrl = getImageUrl(r.imageUrl);
                    const failed = !!brokenImageIds[r.id];
                    if (imgUrl && !failed) {
                      return (
                        <img
                          src={imgUrl}
                          alt={r.name}
                          className="w-full h-full object-cover"
                          onError={() => setBrokenImageIds(prev => ({ ...prev, [r.id]: true }))}
                        />
                      );
                    }
                    return <Store size={20} className="text-orange-400" />;
                  })()}
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
                  <button
                    onClick={() => setDeleteId(r.id)}
                    className="text-xs font-medium px-3 py-1.5 bg-red-50 text-red-600 hover:bg-red-100 rounded-lg transition-colors border border-red-100 inline-flex items-center gap-1.5"
                  >
                    <Trash2 size={13} />
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <Modal
        isOpen={deleteId !== null}
        onClose={() => setDeleteId(null)}
        title="Delete Restaurant"
      >
        <div className="text-center">
          <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Trash2 size={28} className="text-red-500" />
          </div>
          <p className="text-gray-600 mb-6">
            Are you sure you want to delete this restaurant? This will also remove related food items.
          </p>
          <div className="flex gap-3">
            <button onClick={() => setDeleteId(null)} className="btn-secondary flex-1">
              Cancel
            </button>
            <button
              onClick={() => deleteId && deleteMutation.mutate(deleteId)}
              disabled={deleteMutation.isPending}
              className="btn-danger flex-1"
            >
              {deleteMutation.isPending ? 'Deleting…' : 'Delete'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default AllRestaurantsAdmin;
