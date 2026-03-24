import api from './axios';
import { ApiResponse } from '../types/common';
import { PaymentResponse } from '../types/payment';

export interface CheckoutSessionResponse {
  checkoutUrl: string;
  sessionId: string;
}

export const paymentService = {
  createCheckoutSession: (orderId: number): Promise<ApiResponse<CheckoutSessionResponse>> =>
    api.post('/api/v1/customer/payments/create-checkout-session', { orderId }).then(r => r.data),

  confirmOrderPaid: (orderId: number, transactionId: string): Promise<ApiResponse<PaymentResponse>> =>
    api.post(`/api/v1/customer/payments/order/${orderId}/confirm`, null, { params: { transactionId } }).then(r => r.data),

  getPaymentByOrder: (orderId: number): Promise<ApiResponse<PaymentResponse>> =>
    api.get(`/api/v1/customer/payments/order/${orderId}`).then(r => r.data),
};
