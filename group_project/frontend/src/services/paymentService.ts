import api from './axios';
import { ApiResponse } from '../types/common';
import { PaymentResponse } from '../types/payment';

export const paymentService = {
  payNowDummy: (orderId: number): Promise<ApiResponse<PaymentResponse>> =>
    api.post('/api/v1/customer/payments/pay-now', null, { params: { orderId } }).then(r => r.data),

  getPaymentByOrder: (orderId: number): Promise<ApiResponse<PaymentResponse>> =>
    api.get(`/api/v1/customer/payments/order/${orderId}`).then(r => r.data),
};
