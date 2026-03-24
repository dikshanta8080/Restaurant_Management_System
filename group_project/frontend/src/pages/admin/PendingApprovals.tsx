import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Clock, CheckCircle, XCircle, Store } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { SkeletonCard } from '../../components/Skeleton';
import toast from 'react-hot-toast';
import { getImageUrl } from '../../utils/imageUtils';
import BackButton from '../../components/BackButton';

const PendingApprovals: React.FC = () => {
  const queryClient = useQueryClient();
  const [brokenImageIds, setBrokenImageIds] = useState<Record<number, boolean>>({});

  const { data: pending, isLoading } = useQuery({
    queryKey: ['admin-pending'],
    queryFn: async () => {
      const res = await adminService.getPendingRestaurants();
      return res.responseObject;
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: 'APPROVED' | 'REJECTED' }) =>
      adminService.updateRestaurantStatus({ restaurantId: id, restaurantStatus: status }),
    onSuccess: (_, { status }) => {
      toast.success(`Restaurant ${status.toLowerCase()}!`);
      queryClient.invalidateQueries({ queryKey: ['admin-pending'] });
      queryClient.invalidateQueries({ queryKey: ['admin-restaurants'] });
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Failed to update status'),
  });

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <BackButton className="mb-6" redirectTo="/admin" />
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 bg-amber-100 rounded-xl flex items-center justify-center">
            <Clock size={22} className="text-amber-600" />
          </div>
          <div>
            <h1 className="text-3xl font-black text-gray-900">Pending Approvals</h1>
            <p className="text-sm text-gray-500">{pending?.length ?? 0} restaurant{(pending?.length ?? 0) !== 1 ? 's' : ''} awaiting review</p>
          </div>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {Array.from({ length: 4 }).map((_, i) => <SkeletonCard key={i} />)}
          </div>
        ) : !pending?.length ? (
          <div className="card flex flex-col items-center justify-center py-20 text-center text-gray-400">
            <CheckCircle size={56} className="mb-4 text-green-300" />
            <p className="text-xl font-bold text-gray-600">All caught up!</p>
            <p className="text-sm mt-1">No pending restaurant requests at the moment.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {pending.map(r => (
              <div key={r.id} className="card p-5">
                {/* Image */}
                <div className="h-36 bg-gradient-to-br from-orange-100 to-amber-100 rounded-xl overflow-hidden mb-4 flex items-center justify-center">
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
                    return <span className="text-4xl">🍽️</span>;
                  })()}
                </div>
                <div className="mb-4">
                  <div className="flex items-start justify-between">
                    <h3 className="font-bold text-gray-900 text-lg">{r.name}</h3>
                    <span className="badge-pending flex-shrink-0 ml-2">Pending</span>
                  </div>
                  {r.description && <p className="text-sm text-gray-500 mt-1 line-clamp-2">{r.description}</p>}
                  <p className="text-xs text-gray-400 mt-2 flex items-center gap-1">
                    <Store size={11} /> by {r.ownerName}
                  </p>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => updateMutation.mutate({ id: r.id, status: 'REJECTED' })}
                    disabled={updateMutation.isPending}
                    className="flex-1 flex items-center justify-center gap-1.5 py-2.5 rounded-xl bg-red-50 text-red-600 hover:bg-red-100 font-semibold text-sm transition-colors border border-red-100"
                  >
                    <XCircle size={15} /> Reject
                  </button>
                  <button
                    onClick={() => updateMutation.mutate({ id: r.id, status: 'APPROVED' })}
                    disabled={updateMutation.isPending}
                    className="flex-1 flex items-center justify-center gap-1.5 py-2.5 rounded-xl bg-green-50 text-green-700 hover:bg-green-100 font-semibold text-sm transition-colors border border-green-100"
                  >
                    <CheckCircle size={15} /> Approve
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default PendingApprovals;
