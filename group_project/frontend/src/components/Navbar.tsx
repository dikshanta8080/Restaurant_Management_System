import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { ShoppingCart, Menu, X, ChefHat, User, LogOut, LayoutDashboard, ShieldCheck, Store } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

const Navbar: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);

  useEffect(() => {
    const handler = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handler);
    return () => window.removeEventListener('scroll', handler);
  }, []);

  useEffect(() => {
    setMobileOpen(false);
    setProfileOpen(false);
  }, [location.pathname]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getDashboardLink = () => {
    if (user?.role === 'ADMIN') return '/admin';
    if (user?.role === 'RESTAURANT') return '/restaurant/dashboard';
    return '/';
  };

  const getDashboardIcon = () => {
    if (user?.role === 'ADMIN') return <ShieldCheck size={16} />;
    if (user?.role === 'RESTAURANT') return <Store size={16} />;
    return <LayoutDashboard size={16} />;
  };

  return (
    <>
      <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${
        scrolled ? 'bg-white shadow-md' : 'bg-white/95 backdrop-blur-sm'
      }`}>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <Link to="/" className="flex items-center gap-2 group">
              <div className="w-9 h-9 bg-gradient-to-br from-orange-500 to-red-500 rounded-xl flex items-center justify-center group-hover:scale-105 transition-transform">
                <ChefHat size={20} className="text-white" />
              </div>
              <span className="text-xl font-extrabold bg-gradient-to-r from-orange-500 to-red-500 bg-clip-text text-transparent">
                FoodHub
              </span>
            </Link>

            {/* Desktop Nav */}
            <div className="hidden md:flex items-center gap-6">
              <Link to="/restaurants" className="text-gray-600 hover:text-orange-500 font-medium transition-colors text-sm">
                Restaurants
              </Link>
              {!isAuthenticated && (
                <Link to="/register-restaurant" className="text-gray-600 hover:text-orange-500 font-medium transition-colors text-sm">
                  List Restaurant
                </Link>
              )}
              {isAuthenticated && user?.role === 'CUSTOMER' && (
                <>
                  <Link to="/orders" className="text-gray-600 hover:text-orange-500 font-medium transition-colors text-sm">
                    My Orders
                  </Link>
                  <Link to="/cart" className="relative p-2 text-gray-600 hover:text-orange-500 transition-colors">
                    <ShoppingCart size={22} />
                    {cartCount > 0 && (
                      <span className="absolute -top-1 -right-1 bg-orange-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold animate-pulse">
                        {cartCount > 9 ? '9+' : cartCount}
                      </span>
                    )}
                  </Link>
                </>
              )}
            </div>

            {/* Right side */}
            <div className="hidden md:flex items-center gap-3">
              {isAuthenticated ? (
                <div className="relative">
                  <button
                    onClick={() => setProfileOpen(!profileOpen)}
                    className="flex items-center gap-2 bg-gray-50 hover:bg-orange-50 border border-gray-200 hover:border-orange-200 rounded-xl px-4 py-2 transition-all"
                  >
                    <div className="w-7 h-7 bg-gradient-to-br from-orange-400 to-red-400 rounded-full flex items-center justify-center">
                      <span className="text-white text-xs font-bold">{user?.name.charAt(0).toUpperCase()}</span>
                    </div>
                    <span className="text-sm font-medium text-gray-700">{user?.name.split(' ')[0]}</span>
                    <span className="text-xs bg-orange-100 text-orange-700 px-1.5 py-0.5 rounded-lg font-medium">{user?.role}</span>
                  </button>

                  {profileOpen && (
                    <div className="absolute right-0 mt-2 w-52 bg-white rounded-2xl shadow-xl border border-gray-100 py-2 animate-fade-in z-50">
                      <Link to="/profile" className="flex items-center gap-3 px-4 py-2.5 hover:bg-orange-50 text-gray-700 text-sm transition-colors">
                        <User size={15} /> My Profile
                      </Link>
                      <Link to={getDashboardLink()} className="flex items-center gap-3 px-4 py-2.5 hover:bg-orange-50 text-gray-700 text-sm transition-colors">
                        {getDashboardIcon()} Dashboard
                      </Link>
                      {user?.role === 'CUSTOMER' && (
                        <Link to="/register-restaurant" className="flex items-center gap-3 px-4 py-2.5 hover:bg-orange-50 text-gray-700 text-sm transition-colors">
                          <Store size={15} /> List Your Restaurant
                        </Link>
                      )}
                      <hr className="my-1 border-gray-100" />
                      <button
                        onClick={handleLogout}
                        className="flex items-center gap-3 px-4 py-2.5 hover:bg-red-50 text-red-600 text-sm w-full text-left transition-colors"
                      >
                        <LogOut size={15} /> Logout
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                <div className="flex items-center gap-2">
                  <Link to="/login" className="text-sm font-medium text-gray-700 hover:text-orange-500 px-4 py-2 rounded-xl transition-colors">
                    Login
                  </Link>
                  <Link to="/register" className="btn-primary text-sm">
                    Get Started
                  </Link>
                </div>
              )}
            </div>

            {/* Mobile menu button */}
            <div className="md:hidden flex items-center gap-2">
              {isAuthenticated && user?.role === 'CUSTOMER' && (
                <Link to="/cart" className="relative p-2 text-gray-600">
                  <ShoppingCart size={22} />
                  {cartCount > 0 && (
                    <span className="absolute -top-1 -right-1 bg-orange-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center font-bold">
                      {cartCount > 9 ? '9+' : cartCount}
                    </span>
                  )}
                </Link>
              )}
              <button
                onClick={() => setMobileOpen(!mobileOpen)}
                className="p-2 text-gray-600 hover:text-orange-500 transition-colors"
              >
                {mobileOpen ? <X size={24} /> : <Menu size={24} />}
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Mobile Drawer */}
      {mobileOpen && (
        <div className="fixed inset-0 z-40 md:hidden">
          <div className="fixed inset-0 bg-black/40 backdrop-blur-sm" onClick={() => setMobileOpen(false)} />
          <div className="fixed right-0 top-0 h-full w-72 bg-white shadow-2xl animate-slide-in-right">
            <div className="flex items-center justify-between p-5 border-b">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 bg-gradient-to-br from-orange-500 to-red-500 rounded-lg flex items-center justify-center">
                  <ChefHat size={16} className="text-white" />
                </div>
                <span className="font-extrabold text-lg bg-gradient-to-r from-orange-500 to-red-500 bg-clip-text text-transparent">FoodHub</span>
              </div>
              <button onClick={() => setMobileOpen(false)} className="p-1.5 rounded-lg hover:bg-gray-100">
                <X size={20} />
              </button>
            </div>

            <div className="p-5 space-y-1">
              {isAuthenticated && (
                <div className="bg-orange-50 rounded-xl p-4 mb-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-gradient-to-br from-orange-400 to-red-400 rounded-full flex items-center justify-center">
                      <span className="text-white font-bold">{user?.name.charAt(0).toUpperCase()}</span>
                    </div>
                    <div>
                      <p className="font-semibold text-gray-800">{user?.name}</p>
                      <p className="text-xs text-gray-500">{user?.email}</p>
                    </div>
                  </div>
                </div>
              )}

              {[
                { to: '/restaurants', label: 'Browse Restaurants' },
                ...(isAuthenticated && user?.role === 'CUSTOMER' ? [
                  { to: '/orders', label: 'My Orders' },
                  { to: '/cart', label: 'Cart' },
                  { to: '/register-restaurant', label: 'List Your Restaurant' },
                  { to: '/profile', label: 'My Profile' },
                ] : []),
                ...(isAuthenticated ? [{ to: getDashboardLink(), label: 'Dashboard' }] : []),
                ...(!isAuthenticated ? [
                  { to: '/register-restaurant', label: 'List Restaurant' },
                ] : []),
              ].map(link => (
                <Link
                  key={link.to}
                  to={link.to}
                  className="flex items-center px-4 py-3 rounded-xl text-gray-700 hover:bg-orange-50 hover:text-orange-600 font-medium transition-colors"
                >
                  {link.label}
                </Link>
              ))}

              <div className="pt-4 mt-4 border-t">
                {isAuthenticated ? (
                  <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 w-full px-4 py-3 rounded-xl text-red-600 hover:bg-red-50 font-medium transition-colors"
                  >
                    <LogOut size={16} /> Logout
                  </button>
                ) : (
                  <div className="space-y-2">
                    <Link to="/login" className="block w-full btn-secondary text-center text-sm">Login</Link>
                    <Link to="/register" className="block w-full btn-primary text-center text-sm">Get Started</Link>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Spacer */}
      <div className="h-16" />

      {/* Overlay to close profile dropdown */}
      {profileOpen && (
        <div className="fixed inset-0 z-30" onClick={() => setProfileOpen(false)} />
      )}
    </>
  );
};

export default Navbar;
