import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Users, Trash2, Search } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { SkeletonRow } from '../../components/Skeleton';
import Modal from '../../components/Modal';
import toast from 'react-hot-toast';
import BackButton from '../../components/BackButton';

const AllCustomersAdmin: React.FC = () => {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const { data: customers, isLoading, error } = useQuery({
    queryKey: ['admin-customers'],
    queryFn: () => adminService.getAllCustomers(),
    retry: 1,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminService.deleteUser(id),
    onSuccess: () => {
      toast.success('User deleted!');
      queryClient.invalidateQueries({ queryKey: ['admin-customers'] });
      setDeleteId(null);
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Failed to delete user'),
  });

  const filtered = customers?.filter((c: any) =>
    c.name?.toLowerCase().includes(search.toLowerCase()) ||
    c.email?.toLowerCase().includes(search.toLowerCase())
  ) ?? [];

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <BackButton className="mb-6" redirectTo="/admin" />
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center">
            <Users size={22} className="text-blue-600" />
          </div>
          <div>
            <h1 className="text-3xl font-black text-gray-900">Customers</h1>
            <p className="text-sm text-gray-500">{customers?.length ?? 0} registered users</p>
          </div>
        </div>

        <div className="relative max-w-sm mb-6">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input value={search} onChange={e => setSearch(e.target.value)} className="input pl-9 text-sm" placeholder="Search customers…" />
        </div>

        {isLoading ? (
          <div className="card divide-y">{Array.from({ length: 6 }).map((_, i) => <SkeletonRow key={i} />)}</div>
        ) : filtered.length === 0 ? (
          <div className="card flex flex-col items-center py-16 text-gray-400 text-center">
            <Users size={48} className="mb-3 opacity-30" />
            <p className="font-medium">{error ? 'Failed to fetch customers' : 'No customers found'}</p>
          </div>
        ) : (
          <div className="card divide-y">
            {filtered.map((c: any) => (
              <div key={c.id} className="flex items-center gap-4 p-4 hover:bg-gray-50/50 transition-colors">
                <div className="w-10 h-10 bg-gradient-to-br from-blue-400 to-blue-600 rounded-full flex items-center justify-center flex-shrink-0">
                  <span className="text-white font-bold text-sm">{c.name?.charAt(0).toUpperCase()}</span>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-gray-900 truncate">{c.name}</p>
                  <p className="text-sm text-gray-500 truncate">{c.email}</p>
                </div>
                <span className="text-xs px-2 py-1 bg-blue-50 text-blue-700 rounded-full font-medium">{c.role}</span>
                <button
                  onClick={() => setDeleteId(c.id)}
                  className="p-2 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Delete confirm modal */}
      <Modal isOpen={deleteId !== null} onClose={() => setDeleteId(null)} title="Delete User">
        <div className="text-center">
          <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Trash2 size={28} className="text-red-500" />
          </div>
          <p className="text-gray-600 mb-6">Are you sure you want to delete this user? This action cannot be undone.</p>
          <div className="flex gap-3">
            <button onClick={() => setDeleteId(null)} className="btn-secondary flex-1">Cancel</button>
            <button
              onClick={() => deleteId && deleteMutation.mutate(deleteId)}
              disabled={deleteMutation.isPending}
              className="btn-danger flex-1"
            >
              {deleteMutation.isPending ? 'Deleting…' : 'Delete User'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default AllCustomersAdmin;
