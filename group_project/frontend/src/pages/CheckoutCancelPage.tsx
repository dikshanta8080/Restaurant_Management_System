import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { XCircle } from 'lucide-react';

const CheckoutCancelPage: React.FC = () => {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const orderId = params.get('order_id');

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="card max-w-md w-full p-8 text-center">
        <XCircle size={56} className="text-red-500 mx-auto mb-4" />
        <h1 className="text-2xl font-black text-gray-900 mb-2">Payment Cancelled</h1>
        <p className="text-gray-500 mb-6">
          Dummy payment was not completed. You can retry payment from your order details later.
        </p>
        <button
          onClick={() => navigate(orderId ? `/orders/${orderId}` : '/orders')}
          className="btn-primary w-full"
        >
          Back to Orders
        </button>
      </div>
    </div>
  );
};

export default CheckoutCancelPage;
