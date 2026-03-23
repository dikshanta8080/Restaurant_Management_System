import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { CreditCard } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { PaymentResponse } from '../../types/payment';
import { SkeletonRow } from '../../components/Skeleton';

const AllPaymentsAdmin: React.FC = () => {
  const { data: payments, isLoading } = useQuery({
    queryKey: ['admin-payments'],
    queryFn: () => adminService.getAllPayments(),
  });

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 bg-emerald-100 rounded-xl flex items-center justify-center">
            <CreditCard size={22} className="text-emerald-600" />
          </div>
          <div>
            <h1 className="text-3xl font-black text-gray-900">Payments</h1>
            <p className="text-sm text-gray-500">{payments?.length ?? 0} total payments</p>
          </div>
        </div>

        {isLoading ? (
          <div className="card divide-y">{Array.from({ length: 6 }).map((_, i) => <SkeletonRow key={i} />)}</div>
        ) : !(payments?.length) ? (
          <div className="card p-12 text-center text-gray-400">No payments found</div>
        ) : (
          <div className="card overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-500">
                <tr>
                  <th className="text-left px-4 py-3">ID</th>
                  <th className="text-left px-4 py-3">Order</th>
                  <th className="text-left px-4 py-3">User</th>
                  <th className="text-left px-4 py-3">Amount</th>
                  <th className="text-left px-4 py-3">Status</th>
                  <th className="text-left px-4 py-3">Paid At</th>
                </tr>
              </thead>
              <tbody>
                {payments.map((p: PaymentResponse) => (
                  <tr key={p.id} className="border-t border-gray-100">
                    <td className="px-4 py-3 font-medium text-gray-800">#{p.id}</td>
                    <td className="px-4 py-3">#{p.orderId}</td>
                    <td className="px-4 py-3">{p.userId}</td>
                    <td className="px-4 py-3">Rs. {Number(p.amount).toFixed(2)}</td>
                    <td className="px-4 py-3">
                      <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                        p.status === 'PAID' ? 'bg-green-100 text-green-700' : p.status === 'FAILED' ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700'
                      }`}>
                        {p.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-gray-500">
                      {p.paymentTimestamp ? new Date(p.paymentTimestamp).toLocaleString() : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default AllPaymentsAdmin;
