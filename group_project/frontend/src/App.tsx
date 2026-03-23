import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';

// Pages
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import RestaurantsPage from './pages/RestaurantsPage';
import RestaurantDetailPage from './pages/RestaurantDetailPage';
import CartPage from './pages/CartPage';
import OrdersPage from './pages/OrdersPage';
import OrderDetailPage from './pages/OrderDetailPage';
import ProfilePage from './pages/ProfilePage';
import RegisterRestaurantPage from './pages/RegisterRestaurantPage';

// Restaurant pages
import RestaurantDashboard from './pages/restaurant/RestaurantDashboard';
import FoodItemsManagement from './pages/restaurant/FoodItemsManagement';
import RestaurantOrders from './pages/restaurant/RestaurantOrders';

// Admin pages
import AdminDashboard from './pages/admin/AdminDashboard';
import PendingApprovals from './pages/admin/PendingApprovals';
import AllRestaurantsAdmin from './pages/admin/AllRestaurantsAdmin';
import AllCustomersAdmin from './pages/admin/AllCustomersAdmin';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30000,
      retry: 1,
    },
  },
});

// Layout with Navbar (exclude auth pages)
const WithNavbar: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <>
    <Navbar />
    {children}
  </>
);

// Auth page layout (no navbar)
const AuthLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <>{children}</>
);

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <CartProvider>
          <BrowserRouter>
            <Routes>
              {/* Auth routes (no navbar) */}
              <Route path="/login" element={<AuthLayout><LoginPage /></AuthLayout>} />
              <Route path="/register" element={<AuthLayout><RegisterPage /></AuthLayout>} />

              {/* Public routes with navbar */}
              <Route path="/" element={<WithNavbar><LandingPage /></WithNavbar>} />
              <Route path="/restaurants" element={<WithNavbar><RestaurantsPage /></WithNavbar>} />
              <Route path="/restaurants/:id" element={<WithNavbar><RestaurantDetailPage /></WithNavbar>} />
              <Route path="/register-restaurant" element={<WithNavbar><RegisterRestaurantPage /></WithNavbar>} />

              {/* Customer protected routes */}
              <Route path="/cart" element={
                <ProtectedRoute roles={['CUSTOMER']}>
                  <WithNavbar><CartPage /></WithNavbar>
                </ProtectedRoute>
              } />
              <Route path="/orders" element={
                <ProtectedRoute roles={['CUSTOMER']}>
                  <WithNavbar><OrdersPage /></WithNavbar>
                </ProtectedRoute>
              } />
              <Route path="/orders/:id" element={
                <ProtectedRoute roles={['CUSTOMER']}>
                  <WithNavbar><OrderDetailPage /></WithNavbar>
                </ProtectedRoute>
              } />
              <Route path="/profile" element={
                <ProtectedRoute>
                  <WithNavbar><ProfilePage /></WithNavbar>
                </ProtectedRoute>
              } />

              {/* Restaurant protected routes */}
              <Route path="/restaurant/dashboard" element={
                <ProtectedRoute roles={['RESTAURANT']}>
                  <WithNavbar><RestaurantDashboard /></WithNavbar>
                </ProtectedRoute>
              } />
              <Route path="/restaurant/food-items" element={
                <ProtectedRoute roles={['RESTAURANT']}>
                  <WithNavbar><FoodItemsManagement /></WithNavbar>
                </ProtectedRoute>
              } />
              <Route path="/restaurant/orders" element={
                <ProtectedRoute roles={['RESTAURANT']}>
                  <WithNavbar><RestaurantOrders /></WithNavbar>
                </ProtectedRoute>
              } />

              {/* Admin protected routes */}
              <Route path="/admin" element={
                <ProtectedRoute roles={['ADMIN']}>
                  <WithNavbar><AdminDashboard /></WithNavbar>
                </ProtectedRoute>
              } />
              <Route path="/admin/approvals" element={
                <ProtectedRoute roles={['ADMIN']}>
                  <WithNavbar><PendingApprovals /></WithNavbar>
                </ProtectedRoute>
              } />
              <Route path="/admin/restaurants" element={
                <ProtectedRoute roles={['ADMIN']}>
                  <WithNavbar><AllRestaurantsAdmin /></WithNavbar>
                </ProtectedRoute>
              } />
              <Route path="/admin/customers" element={
                <ProtectedRoute roles={['ADMIN']}>
                  <WithNavbar><AllCustomersAdmin /></WithNavbar>
                </ProtectedRoute>
              } />

              {/* Fallback */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </BrowserRouter>

          {/* Global toast notifications */}
          <Toaster
            position="top-right"
            toastOptions={{
              duration: 3500,
              style: {
                background: '#fff',
                color: '#1f2937',
                borderRadius: '12px',
                boxShadow: '0 10px 40px rgba(0,0,0,0.12)',
                padding: '12px 16px',
                fontSize: '14px',
                fontWeight: '500',
              },
              success: {
                iconTheme: { primary: '#22c55e', secondary: '#fff' },
              },
              error: {
                iconTheme: { primary: '#ef4444', secondary: '#fff' },
              },
            }}
          />
        </CartProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
