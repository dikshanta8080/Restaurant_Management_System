import React from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { ChefHat, Eye, EyeOff } from 'lucide-react';
import toast from 'react-hot-toast';
import { authService } from '../services/authService';
import { useAuth } from '../context/AuthContext';

const schema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(1, 'Password is required'),
});

type FormData = z.infer<typeof schema>;

const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = React.useState(false);

  const { register, handleSubmit, setError, formState: { errors, isSubmitting, isValid } } = useForm<FormData>({
    resolver: zodResolver(schema),
    mode: 'onChange',
  });

  const onSubmit = async (data: FormData) => {
    try {
      const res = await authService.login(data);
      const { token, user } = res.responseObject;
      login(token, user);
      toast.success(`Welcome back, ${user.name}!`);
      // Role-based redirect
      if (user.role === 'ADMIN') navigate('/admin');
      else if (user.role === 'RESTAURANT') navigate('/restaurant/dashboard');
      else navigate('/');
    } catch (err: any) {
      const responseObject = err?.response?.data?.responseObject;
      const errorMap = responseObject?.errorMap;

      if (errorMap && typeof errorMap === 'object') {
        Object.entries(errorMap).forEach(([field, message]) => {
          setError(field as keyof FormData, { type: 'server', message: String(message) });
        });
        toast.error(responseObject?.exceptionMessage || 'Please fix the highlighted fields.');
        return;
      }

      toast.error(responseObject?.exceptionMessage || err?.response?.data?.message || 'Login failed. Check your credentials.');
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left visual panel */}
      <div className="hidden lg:flex flex-col justify-center items-center w-1/2 bg-gradient-to-br from-orange-500 via-orange-600 to-red-600 p-12 relative overflow-hidden">
        <div className="absolute -top-20 -left-20 w-80 h-80 bg-white/10 rounded-full blur-3xl" />
        <div className="absolute -bottom-20 -right-20 w-80 h-80 bg-red-700/30 rounded-full blur-3xl" />
        <div className="relative text-center text-white">
          <div className="w-20 h-20 bg-white/20 backdrop-blur rounded-3xl flex items-center justify-center mx-auto mb-8 shadow-2xl">
            <ChefHat size={40} className="text-white" />
          </div>
          <h1 className="text-4xl font-black mb-4">Welcome Back!</h1>
          <p className="text-white/80 text-lg max-w-sm leading-relaxed">
            Sign in to explore amazing restaurants, track your orders, and enjoy the best food.
          </p>
          <div className="mt-14 grid grid-cols-3 gap-6">
            {['🍕', '🍜', '🍔', '🍣', '🥗', '🍰'].map((emoji, i) => (
              <div
                key={i}
                className="text-4xl bg-white/10 backdrop-blur rounded-2xl p-4 hover:bg-white/20 transition-colors cursor-default"
              >
                {emoji}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right form panel */}
      <div className="flex-1 flex items-center justify-center p-6 bg-gray-50">
        <div className="w-full max-w-md">
          <div className="text-center mb-8">
            <Link to="/" className="inline-flex items-center gap-2 mb-6">
              <div className="w-10 h-10 bg-gradient-to-br from-orange-500 to-red-500 rounded-xl flex items-center justify-center">
                <ChefHat size={22} className="text-white" />
              </div>
              <span className="text-2xl font-black bg-gradient-to-r from-orange-500 to-red-500 bg-clip-text text-transparent">FoodHub</span>
            </Link>
            <h2 className="text-3xl font-black text-gray-900">Sign In</h2>
            <p className="text-gray-500 mt-2">Enter your credentials to continue</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 space-y-5">
            <div>
              <label className="label">Email address</label>
              <input
                {...register('email')}
                type="email"
                placeholder="you@example.com"
                className="input"
                autoComplete="email"
              />
              {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>}
            </div>

            <div>
              <label className="label">Password</label>
              <div className="relative">
                <input
                  {...register('password')}
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  className="input pr-10"
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password.message}</p>}
            </div>

            <button
              type="submit"
              disabled={isSubmitting || !isValid}
              className="btn-primary w-full py-3 text-base"
            >
              {isSubmitting ? 'Signing in…' : 'Sign In'}
            </button>

            <div className="text-center pt-2">
              <p className="text-sm text-gray-500">
                Don't have an account?{' '}
                <Link to="/register" className="text-orange-600 font-semibold hover:text-orange-700">
                  Create one free
                </Link>
              </p>
            </div>
          </form>

          {/* Quick credentials hint */}
          <div className="mt-4 p-4 bg-amber-50 border border-amber-200 rounded-xl text-sm text-amber-800">
            <p className="font-semibold mb-1">Admin credentials:</p>
            <p>Email: dikshantaacharya04@gmail.com</p>
            <p>Password: @Dikshyant9898</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
