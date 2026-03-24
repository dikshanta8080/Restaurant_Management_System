import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation } from '@tanstack/react-query';
import { Store, MapPin, Upload, CheckCircle, RefreshCw } from 'lucide-react';
import toast from 'react-hot-toast';
import { customerService } from '../services/customerService';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Navigate } from 'react-router-dom';
import {
  DEFAULT_STREETS,
  NEPAL_DISTRICT_TO_CITIES,
  NEPAL_DISTRICTS_ALL,
  NEPAL_DISTRICTS_BY_PROVINCE,
  NEPAL_PROVINCES,
  type NepalProvince,
} from '../utils/nepalLocations';

const schema = z.object({
  name: z.string().min(2, 'Restaurant name required'),
  description: z.string().min(10, 'Description must be at least 10 characters'),
  province: z.string().min(1, 'Province required'),
  district: z.string().min(1, 'District required'),
  city: z.string().min(1, 'City required'),
  street: z.string().min(1, 'Street required'),
});
type FormData = z.infer<typeof schema>;

const RegisterRestaurantPage: React.FC = () => {
  const { isAuthenticated, refreshRole } = useAuth();
  const navigate = useNavigate();
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [syncing, setSyncing] = useState(false);

  const handleSyncAccount = async () => {
    setSyncing(true);
    try {
      await refreshRole();
      toast.success('Account synced! Redirecting to your dashboard…');
      navigate('/restaurant/dashboard');
    } catch {
      toast.error('Sync failed. Please log out and log back in.');
    } finally {
      setSyncing(false);
    }
  };

  const { register, handleSubmit, watch, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const selectedProvince = watch('province');
  const selectedDistrict = watch('district');

  const districtOptions =
    selectedProvince && NEPAL_DISTRICTS_BY_PROVINCE[selectedProvince as NepalProvince]
      ? NEPAL_DISTRICTS_BY_PROVINCE[selectedProvince as NepalProvince]
      : NEPAL_DISTRICTS_ALL;

  const cityOptions = selectedDistrict ? NEPAL_DISTRICT_TO_CITIES[selectedDistrict] ?? [] : [];

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      customerService.createRestaurant({ ...data, multipartFile: imageFile ?? undefined }),
    onSuccess: () => {
      setSuccess(true);
      toast.success('Restaurant registered! Awaiting admin approval.');
    },
    onError: (err: any) => toast.error(err?.response?.data?.message || 'Registration failed'),
  });

  if (!isAuthenticated) return <Navigate to="/login" replace />;

  if (success) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center page-enter">
        <div className="card p-12 text-center max-w-md">
          <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
            <CheckCircle size={40} className="text-green-500" />
          </div>
          <h2 className="text-2xl font-black text-gray-900 mb-3">Application Submitted!</h2>
          <p className="text-gray-500 mb-6">Your restaurant registration is pending admin approval. Once approved, click the button below to access your restaurant dashboard immediately.</p>
          <div className="space-y-3">
            <button
              onClick={handleSyncAccount}
              disabled={syncing}
              className="btn-primary w-full flex items-center justify-center gap-2"
            >
              <RefreshCw size={16} className={syncing ? 'animate-spin' : ''} />
              {syncing ? 'Syncing…' : 'Sync Account & Go to Dashboard'}
            </button>
            <button onClick={() => navigate('/')} className="btn-secondary w-full">Back to Home</button>
          </div>
          <p className="text-xs text-gray-400 mt-4">If not yet approved, sync will keep you as a customer until the admin approves.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 py-10">
        <div className="flex items-center gap-3 mb-8">
          <div className="w-10 h-10 bg-orange-100 rounded-xl flex items-center justify-center">
            <Store size={22} className="text-orange-600" />
          </div>
          <div>
            <h1 className="text-3xl font-black text-gray-900">Register Your Restaurant</h1>
            <p className="text-gray-500 text-sm">Fill the form and wait for admin approval</p>
          </div>
        </div>

        <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 mb-6 flex gap-3">
          <CheckCircle size={20} className="text-amber-600 flex-shrink-0 mt-0.5" />
          <div className="text-sm text-amber-800">
            <p className="font-semibold">Approval Required</p>
            <p>After submitting, an admin will review your application before your restaurant goes live.</p>
          </div>
        </div>

        <form onSubmit={handleSubmit(d => mutation.mutate(d))} className="card p-8 space-y-5">
          <div>
            <label className="label">Restaurant Image</label>
            <label className={`flex flex-col items-center justify-center w-full h-40 border-2 border-dashed rounded-xl cursor-pointer transition-colors ${
              imagePreview ? 'border-orange-300 bg-orange-50' : 'border-gray-200 hover:border-orange-300 hover:bg-orange-50'
            }`}>
              {imagePreview ? (
                <img src={imagePreview} alt="preview" className="h-full w-full object-cover rounded-xl" />
              ) : (
                <div className="flex flex-col items-center gap-2 text-gray-400">
                  <Upload size={28} />
                  <p className="text-sm">Click to upload restaurant image</p>
                  <p className="text-xs">PNG, JPG up to 5MB</p>
                </div>
              )}
              <input
                type="file"
                accept="image/*"
                className="hidden"
                onChange={e => {
                  const f = e.target.files?.[0];
                  if (f) { setImageFile(f); setImagePreview(URL.createObjectURL(f)); }
                }}
              />
            </label>
          </div>

          <div>
            <label className="label">Restaurant Name *</label>
            <input {...register('name')} className="input" placeholder="e.g. The Golden Spoon" />
            {errors.name && <p className="text-red-500 text-xs mt-1">{errors.name.message}</p>}
          </div>

          <div>
            <label className="label">Description *</label>
            <textarea {...register('description')} rows={3} className="input resize-none" placeholder="Tell customers about your restaurant…" />
            {errors.description && <p className="text-red-500 text-xs mt-1">{errors.description.message}</p>}
          </div>

          <div className="border-t border-gray-100 pt-5">
            <p className="flex items-center gap-2 font-semibold text-gray-700 mb-4">
              <MapPin size={16} className="text-orange-500" /> Location
            </p>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="label">Province *</label>
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
                <label className="label">District *</label>
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
                <label className="label">City *</label>
                <input
                  {...register('city')}
                  className="input"
                  placeholder="e.g. Kathmandu"
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
                <label className="label">Street *</label>
                <input
                  {...register('street')}
                  className="input"
                  placeholder="e.g. Thamel"
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
          </div>

          <button type="submit" disabled={mutation.isPending} className="btn-primary w-full py-3">
            {mutation.isPending ? 'Submitting…' : 'Submit for Approval'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default RegisterRestaurantPage;
