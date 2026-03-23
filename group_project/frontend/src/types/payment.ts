export interface PaymentResponse {
  id: number;
  orderId: number;
  userId: number;
  amount: number;
  status: 'PENDING' | 'PAID' | 'FAILED';
  transactionId: string;
  paymentTimestamp: string;
}
