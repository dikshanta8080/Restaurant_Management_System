import React from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowRight, ChefHat, Star, Zap, Shield, Utensils, TrendingUp } from 'lucide-react';
import { customerService } from '../services/customerService';
import RestaurantCard from '../components/RestaurantCard';
import { SkeletonCard } from '../components/Skeleton';

const features = [
  { icon: <Utensils size={24} />, title: 'Diverse Cuisine', desc: 'Choose from hundreds of restaurants across cuisine types' },
  { icon: <Zap size={24} />, title: 'Fast Delivery', desc: 'Real-time order tracking from prep to your door' },
  { icon: <Shield size={24} />, title: 'Secure Payments', desc: 'Your orders and payments are always secure' },
  { icon: <Star size={24} />, title: 'Verified Reviews', desc: 'Authentic ratings from real customers' },
];

const LandingPage: React.FC = () => {
  const { data, isLoading } = useQuery({
    queryKey: ['restaurants', 'approved'],
    queryFn: async () => {
      const res = await customerService.getAllRestaurants();
      return res.responseObject.filter(r => r.status === 'APPROVED').slice(0, 6);
    },
  });

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative bg-gradient-to-br from-orange-500 via-orange-600 to-red-600 text-white overflow-hidden">
        {/* Background decoration */}
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute -top-32 -right-32 w-96 h-96 bg-white/10 rounded-full blur-3xl" />
          <div className="absolute -bottom-32 -left-32 w-96 h-96 bg-red-700/30 rounded-full blur-3xl" />
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-orange-400/20 rounded-full blur-3xl" />
        </div>

        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-36">
          <div className="max-w-3xl">
            <div className="inline-flex items-center gap-2 bg-white/20 backdrop-blur-sm rounded-full px-4 py-2 mb-8">
              <TrendingUp size={16} />
              <span className="text-sm font-medium">Nepal's #1 Multi-Restaurant Platform</span>
            </div>
            <h1 className="text-5xl md:text-7xl font-black leading-tight mb-6">
              Discover
              <span className="block text-yellow-300">Delicious</span>
              Food Near You
            </h1>
            <p className="text-lg md:text-xl text-white/80 mb-10 max-w-xl leading-relaxed">
              Order from the best local restaurants. Fresh food delivered fast, with real-time tracking and verified reviews.
            </p>
            <div className="flex flex-wrap gap-4">
              <Link to="/restaurants" className="flex items-center gap-2 bg-white text-orange-600 font-bold px-8 py-4 rounded-2xl hover:bg-orange-50 transition-all shadow-lg hover:shadow-xl active:scale-95">
                Order Now <ArrowRight size={18} />
              </Link>
              <Link to="/register" className="flex items-center gap-2 bg-white/20 backdrop-blur-sm text-white font-bold px-8 py-4 rounded-2xl hover:bg-white/30 transition-all border border-white/30">
                Join FoodHub
              </Link>
            </div>

            {/* Stats */}
            <div className="flex flex-wrap gap-8 mt-14">
              {[
                { val: '50+', label: 'Restaurants' },
                { val: '1,200+', label: 'Happy Customers' },
                { val: '4.8★', label: 'Average Rating' },
              ].map(s => (
                <div key={s.label}>
                  <p className="text-3xl font-black">{s.val}</p>
                  <p className="text-white/70 text-sm">{s.label}</p>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Wave */}
        <div className="absolute bottom-0 left-0 right-0">
          <svg viewBox="0 0 1440 80" className="fill-gray-50" xmlns="http://www.w3.org/2000/svg">
            <path d="M0,80L1440,0L1440,80L0,80Z" />
          </svg>
        </div>
      </section>

      {/* Features */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-14">
            <h2 className="text-4xl font-black text-gray-900 mb-4">Why Choose FoodHub?</h2>
            <p className="text-gray-500 text-lg max-w-xl mx-auto">We connect hungry people with the best local restaurants, making every meal an experience.</p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {features.map(f => (
              <div key={f.title} className="card p-6 hover:shadow-md transition-all duration-300 hover:-translate-y-1 group text-center">
                <div className="w-14 h-14 bg-gradient-to-br from-orange-100 to-red-100 rounded-2xl flex items-center justify-center mx-auto mb-4 group-hover:scale-110 transition-transform text-orange-600">
                  {f.icon}
                </div>
                <h3 className="font-bold text-gray-900 mb-2">{f.title}</h3>
                <p className="text-sm text-gray-500 leading-relaxed">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Featured Restaurants */}
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-10">
            <div>
              <h2 className="text-4xl font-black text-gray-900">Featured Restaurants</h2>
              <p className="text-gray-500 mt-1">Handpicked top restaurants near you</p>
            </div>
            <Link to="/restaurants" className="flex items-center gap-2 text-orange-600 font-semibold hover:text-orange-700 transition-colors">
              View All <ArrowRight size={18} />
            </Link>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {isLoading
              ? Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)
              : data?.map(r => <RestaurantCard key={r.id} restaurant={r} />)
            }
          </div>
          {!isLoading && (!data || data.length === 0) && (
            <div className="text-center py-16 text-gray-400">
              <ChefHat size={64} className="mx-auto mb-4 opacity-40" />
              <p className="text-lg font-medium">No restaurants yet. Be the first to register!</p>
              <Link to="/register-restaurant" className="mt-4 inline-block btn-primary">Register Your Restaurant</Link>
            </div>
          )}
        </div>
      </section>

      {/* CTA Banner */}
      <section className="py-20 bg-gradient-to-r from-gray-900 to-gray-800">
        <div className="max-w-4xl mx-auto px-4 text-center">
          <div className="w-16 h-16 bg-orange-500 rounded-2xl flex items-center justify-center mx-auto mb-6">
            <ChefHat size={32} className="text-white" />
          </div>
          <h2 className="text-4xl font-black text-white mb-4">Own a Restaurant?</h2>
          <p className="text-gray-300 text-lg mb-8 max-w-xl mx-auto">
            Join FoodHub and reach thousands of hungry customers. Easy to set up, powerful tools to grow your business.
          </p>
          <Link to="/register-restaurant" className="inline-flex items-center gap-2 bg-orange-500 hover:bg-orange-600 text-white font-bold px-8 py-4 rounded-2xl transition-all shadow-lg hover:shadow-orange-500/30 active:scale-95">
            List Your Restaurant <ArrowRight size={18} />
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-400 py-10 text-center">
        <div className="flex items-center justify-center gap-2 mb-3">
          <div className="w-8 h-8 bg-gradient-to-br from-orange-500 to-red-500 rounded-lg flex items-center justify-center">
            <ChefHat size={16} className="text-white" />
          </div>
          <span className="font-bold text-white text-lg">FoodHub</span>
        </div>
        <p className="text-sm">© 2026 FoodHub. Made with ❤️ for food lovers.</p>
      </footer>
    </div>
  );
};

export default LandingPage;
