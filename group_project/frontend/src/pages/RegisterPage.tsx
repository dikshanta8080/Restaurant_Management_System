import React from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { ChefHat, Eye, EyeOff, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import { authService } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import {
  DEFAULT_STREETS,
  NEPAL_DISTRICT_TO_CITIES,
  NEPAL_DISTRICTS_ALL,
  NEPAL_DISTRICTS_BY_PROVINCE,
  NEPAL_PROVINCES,
  type NepalProvince,
} from '../utils/nepalLocations';

const schema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  email: z.string().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string(),
  province: z.string().min(2, 'Province is required'),
  district: z.string().min(2, 'District is required'),
  city: z.string().min(2, 'City is required'),
  street: z.string().min(2, 'Street is required'),
}).refine(d => d.password === d.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

type FormData = z.infer<typeof schema>;

const RegisterPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = React.useState(false);

  const { register, handleSubmit, setError, watch, formState: { errors, isSubmitting, isValid } } = useForm<FormData>({
    resolver: zodResolver(schema),
    mode: 'onChange',
  });

  const selectedProvince = watch('province');
  const selectedDistrict = watch('district');

  const districtOptions =
    selectedProvince && NEPAL_DISTRICTS_BY_PROVINCE[selectedProvince as NepalProvince]
      ? NEPAL_DISTRICTS_BY_PROVINCE[selectedProvince as NepalProvince]
      : NEPAL_DISTRICTS_ALL;

  const cityOptions = selectedDistrict ? NEPAL_DISTRICT_TO_CITIES[selectedDistrict] ?? [] : [];

  const onSubmit = async (data: FormData) => {
    try {
      const res = await authService.register({
        name: data.name,
        email: data.email,
        password: data.password,
        province: data.province,
        district: data.district,
        city: data.city,
        street: data.street,
      });
      // Auto login after register
      const loginRes = await authService.login({ email: data.email, password: data.password });
      const { token, user } = loginRes.responseObject;
      login(token, user);
      toast.success(`Welcome to FoodHub, ${user.name}!`);
      navigate('/');
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

      toast.error(responseObject?.exceptionMessage || err?.response?.data?.message || 'Registration failed. Please try again.');
    }
  };

  const perks = [
    'Order from top local restaurants',
    'Real-time order tracking',
    'Exclusive deals & discounts',
    'Register your own restaurant',
  ];

  return (
    <div className="min-h-screen flex">
      <div className="hidden lg:flex flex-col justify-center w-1/2 bg-gradient-to-br from-orange-500 via-orange-600 to-red-600 p-12 relative overflow-hidden">
        <div className="absolute -top-20 -right-20 w-80 h-80 bg-white/10 rounded-full blur-3xl" />
        <div className="absolute -bottom-20 -left-20 w-80 h-80 bg-red-700/20 rounded-full blur-3xl" />
        <div className="relative text-white">
          <div className="w-16 h-16 bg-white/20 backdrop-blur rounded-2xl flex items-center justify-center mb-8">
            <ChefHat size={32} className="text-white" />
          </div>
          <h1 className="text-4xl font-black mb-4">Join FoodHub</h1>
          <p className="text-white/80 text-lg mb-10">Create your free account and start ordering in minutes.</p>
          <div className="space-y-4">
            {perks.map(p => (
              <div key={p} className="flex items-center gap-3">
                <CheckCircle size={20} className="text-yellow-300 flex-shrink-0" />
                <span className="text-white/90">{p}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="flex-1 flex items-center justify-center p-6 bg-gray-50">
        <div className="w-full max-w-md">
          <div className="text-center mb-8">
            <Link to="/" className="inline-flex items-center gap-2 mb-6">
              <div className="w-10 h-10 bg-gradient-to-br from-orange-500 to-red-500 rounded-xl flex items-center justify-center">
                <ChefHat size={22} className="text-white" />
              </div>
              <span className="text-2xl font-black bg-gradient-to-r from-orange-500 to-red-500 bg-clip-text text-transparent">FoodHub</span>
            </Link>
            <h2 className="text-3xl font-black text-gray-900">Create Account</h2>
            <p className="text-gray-500 mt-2">Free forever. No credit card required.</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 space-y-5">
            <div>
              <label className="label">Full Name</label>
              <input {...register('name')} type="text" placeholder="Your full name" className="input" />
              {errors.name && <p className="text-red-500 text-xs mt-1">{errors.name.message}</p>}
            </div>

            <div>
              <label className="label">Email Address</label>
              <input {...register('email')} type="email" placeholder="you@example.com" className="input" />
              {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email.message}</p>}
            </div>

            <div>
              <label className="label">Password</label>
              <div className="relative">
                <input
                  {...register('password')}
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Min. 6 characters"
                  className="input pr-10"
                />
                <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password.message}</p>}
            </div>

            <div>
              <label className="label">Confirm Password</label>
              <input
                {...register('confirmPassword')}
                type={showPassword ? 'text' : 'password'}
                placeholder="Repeat your password"
                className="input"
              />
              {errors.confirmPassword && <p className="text-red-500 text-xs mt-1">{errors.confirmPassword.message}</p>}
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="label">Province</label>
                <select {...register('province')} className="input">
                  <option value="">Select Province</option>
                  {NEPAL_PROVINCES.map(p => (
                    <option key={p} value={p}>
                      {p}
                    </option>
                  ))}
                </select>
                {errors.province && <p className="text-red-500 text-xs mt-1">{errors.province.message}</p>}
              </div>
              <div>
                <label className="label">District</label>
                <select {...register('district')} className="input">
                  <option value="">Select District</option>
                  {districtOptions.map(d => (
                    <option key={d} value={d}>
                      {d}
                    </option>
                  ))}
                </select>
                {errors.district && <p className="text-red-500 text-xs mt-1">{errors.district.message}</p>}
              </div>
              <div>
                <label className="label">City</label>
                <input
                  {...register('city')}
                  type="text"
                  placeholder="e.g. Kathmandu"
                  className="input"
                  list="city-suggestions"
                />
                <datalist id="city-suggestions">
                  {cityOptions.map(c => (
                    <option key={c} value={c} />
                  ))}
                </datalist>
                {errors.city && <p className="text-red-500 text-xs mt-1">{errors.city.message}</p>}
              </div>
              <div>
                <label className="label">Street</label>
                <input
                  {...register('street')}
                  type="text"
                  placeholder="e.g. Thamel"
                  className="input"
                  list="street-suggestions"
                />
                <datalist id="street-suggestions">
                  {DEFAULT_STREETS.map(s => (
                    <option key={s} value={s} />
                  ))}
                </datalist>
                {errors.street && <p className="text-red-500 text-xs mt-1">{errors.street.message}</p>}
              </div>
            </div>

            <button
              type="submit"
              disabled={isSubmitting || !isValid}
              className="btn-primary w-full py-3 text-base"
            >
              {isSubmitting ? 'Creating account…' : 'Create Free Account'}
            </button>

            <div className="text-center pt-2">
              <p className="text-sm text-gray-500">
                Already have an account?{' '}
                <Link to="/login" className="text-orange-600 font-semibold hover:text-orange-700">Sign in</Link>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
