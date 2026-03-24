import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useMutation, useQuery } from '@tanstack/react-query';
import { CheckCircle } from 'lucide-react';
import { paymentService } from '../services/paymentService';
import { orderService } from '../services/orderService';
import toast from 'react-hot-toast';

const CheckoutSuccessPage: React.FC = () => {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const orderIdsParam = params.get('order_ids');
  const orderId = params.get('order_id');
  const sessionId = params.get('session_id');
  const orderIds = React.useMemo(() => {
    if (orderIdsParam) {
      return orderIdsParam.split(',').map(Number).filter(n => Number.isFinite(n));
    }
    return orderId ? [Number(orderId)] : [];
  }, [orderIdsParam, orderId]);

  const confirmMutation = useMutation({
    mutationFn: ({ oid, txn }: { oid: number; txn: string }) => paymentService.confirmOrderPaid(oid, txn),
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Payment verification failed. Please refresh this page.');
    },
  });

  React.useEffect(() => {
    if (!orderId || !sessionId || confirmMutation.isSuccess || confirmMutation.isPending) return;
    const oid = Number(orderId);
    if (!Number.isFinite(oid)) return;
    confirmMutation.mutate({ oid, txn: sessionId });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orderId, sessionId]);

  const { data: firstPayment } = useQuery({
    queryKey: ['payment', orderIds[0]],
    enabled: !!orderIds.length,
    queryFn: () => paymentService.getPaymentByOrder(Number(orderIds[0])),
    retry: 1,
  });

  const { data: orderList } = useQuery({
    queryKey: ['orders-for-success', orderIds.join(',')],
    enabled: !!orderIds.length,
    queryFn: async () => Promise.all(orderIds.map(id => orderService.getOrderById(id))),
    retry: 1,
  });

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="card max-w-md w-full p-8 text-center">
        <CheckCircle size={56} className="text-green-500 mx-auto mb-4" />
        <h1 className="text-2xl font-black text-gray-900 mb-2">Payment Successful</h1>
        <p className="text-gray-500 mb-6">Payment has been recorded. You can now track your order status.</p>

        {firstPayment?.responseObject && (
          <div className="text-left bg-gray-50 border border-gray-100 rounded-xl p-4 mb-6">
            <div className="text-xs text-gray-500">Recorded payment</div>
            <div className="mt-1 text-sm text-gray-800">
              <span className="font-semibold">Status:</span> {firstPayment.responseObject.status}
            </div>
            <div className="mt-1 text-sm text-gray-800 truncate">
              <span className="font-semibold">Transaction:</span> {firstPayment.responseObject.transactionId}
            </div>
          </div>
        )}

        {orderList && orderList.length > 0 && (
          <div className="text-left bg-gray-50 border border-gray-100 rounded-xl p-4 mb-6">
            <div className="text-xs text-gray-500">Created orders ({orderList.length})</div>
            <div className="mt-2 space-y-2">
              {orderList.map(order => (
                <div key={order.id} className="text-sm text-gray-800 flex items-center justify-between">
                  <span>Order #{order.id}</span>
                  <span className="font-semibold">Rs. {Number(order.totalPrice).toFixed(2)}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        <button
          onClick={() => navigate('/orders')}
          className="btn-primary w-full"
        >
          View Orders
        </button>
      </div>
    </div>
  );
};

export default CheckoutSuccessPage;
