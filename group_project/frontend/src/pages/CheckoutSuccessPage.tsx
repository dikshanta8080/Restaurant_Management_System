import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { CheckCircle } from 'lucide-react';
import { paymentService } from '../services/paymentService';
import { orderService } from '../services/orderService';

const CheckoutSuccessPage: React.FC = () => {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const orderId = params.get('order_id');

  const { data: paymentRes } = useQuery({
    queryKey: ['payment', orderId],
    enabled: !!orderId,
    queryFn: () => paymentService.getPaymentByOrder(Number(orderId!)),
    retry: 1,
  });

  const { data: orderRes } = useQuery({
    queryKey: ['order-for-success', orderId],
    enabled: !!orderId,
    queryFn: () => orderService.getOrderById(Number(orderId!)),
    retry: 1,
  });

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="card max-w-md w-full p-8 text-center">
        <CheckCircle size={56} className="text-green-500 mx-auto mb-4" />
        <h1 className="text-2xl font-black text-gray-900 mb-2">Payment Successful</h1>
        <p className="text-gray-500 mb-6">Dummy payment has been recorded. You can now track your order status.</p>

        {paymentRes?.responseObject && (
          <div className="text-left bg-gray-50 border border-gray-100 rounded-xl p-4 mb-6">
            <div className="text-xs text-gray-500">Recorded payment</div>
            <div className="mt-1 text-sm text-gray-800">
              <span className="font-semibold">Status:</span> {paymentRes.responseObject.status}
            </div>
            <div className="mt-1 text-sm text-gray-800 truncate">
              <span className="font-semibold">Transaction:</span> {paymentRes.responseObject.transactionId}
            </div>
          </div>
        )}

        {orderRes && (
          <div className="text-left bg-gray-50 border border-gray-100 rounded-xl p-4 mb-6">
            <div className="text-xs text-gray-500">Delivery address</div>
            <div className="mt-1 text-sm text-gray-800">
              {[orderRes.deliveryStreet, orderRes.deliveryCity, orderRes.deliveryDistrict, orderRes.deliveryProvince]
                .filter(Boolean)
                .join(', ') || '—'}
            </div>
          </div>
        )}

        <button
          onClick={() => navigate(orderId ? `/orders/${orderId}` : '/orders')}
          className="btn-primary w-full"
        >
          View Order
        </button>
      </div>
    </div>
  );
};

export default CheckoutSuccessPage;
