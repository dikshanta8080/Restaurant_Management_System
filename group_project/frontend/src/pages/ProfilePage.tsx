import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { User, Camera, Save } from 'lucide-react';
import { customerService } from '../services/customerService';
import toast from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';
import { SkeletonText } from '../components/Skeleton';
import { getImageUrl } from '../utils/imageUtils';

const ProfilePage: React.FC = () => {
  const { user: authUser } = useAuth();
  const queryClient = useQueryClient();
  const [name, setName] = useState('');
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);

  const { data: profile, isLoading } = useQuery({
    queryKey: ['profile'],
    queryFn: async () => {
      const res = await customerService.getProfile();
      setName(res.responseObject.name);
      return res.responseObject;
    },
  });

  const updateMutation = useMutation({
    mutationFn: () => customerService.updateProfile({ name, multipartFile: avatarFile ?? undefined }),
    onSuccess: () => {
      toast.success('Profile updated!');
      queryClient.invalidateQueries({ queryKey: ['profile'] });
    },
    onError: (err: any) => toast.error(err?.response?.data?.message || 'Failed to update profile'),
  });

  const handleAvatarChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setAvatarFile(file);
      setAvatarPreview(URL.createObjectURL(file));
    }
  };

  const imageSrc = avatarPreview || getImageUrl(profile?.profileImageUrl);
  const [avatarBroken, setAvatarBroken] = React.useState(false);

  React.useEffect(() => {
    setAvatarBroken(false);
  }, [imageSrc]);

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 py-10">
        <h1 className="text-3xl font-black text-gray-900 mb-8">My Profile</h1>
        <div className="card p-8">
          {isLoading ? (
            <SkeletonText lines={5} />
          ) : (
            <>
              {/* Avatar */}
              <div className="flex justify-center mb-8">
                <div className="relative">
                  <div className="w-28 h-28 rounded-full bg-gradient-to-br from-orange-400 to-red-400 flex items-center justify-center overflow-hidden shadow-lg">
                    {imageSrc && !avatarBroken ? (
                      <img
                        src={imageSrc}
                        alt="Avatar"
                        className="w-full h-full object-cover"
                        onError={() => setAvatarBroken(true)}
                      />
                    ) : (
                      <span className="text-white text-4xl font-black">{profile?.name?.charAt(0).toUpperCase()}</span>
                    )}
                  </div>
                  <label className="absolute bottom-0 right-0 w-9 h-9 bg-orange-500 rounded-full flex items-center justify-center cursor-pointer hover:bg-orange-600 transition-colors shadow-sm">
                    <Camera size={16} className="text-white" />
                    <input type="file" accept="image/*" className="hidden" onChange={handleAvatarChange} />
                  </label>
                </div>
              </div>

              <div className="space-y-5">
                <div>
                  <label className="label">Full Name</label>
                  <input
                    value={name}
                    onChange={e => setName(e.target.value)}
                    className="input"
                    placeholder="Your full name"
                  />
                </div>
                <div>
                  <label className="label">Email</label>
                  <input value={profile?.email ?? ''} disabled className="input bg-gray-50 text-gray-500 cursor-not-allowed" />
                </div>
                <div>
                  <label className="label">Role</label>
                  <div className="px-4 py-2.5 rounded-xl border border-gray-200 bg-gray-50">
                    <span className="text-sm font-semibold text-orange-600">{profile?.role}</span>
                  </div>
                </div>
                <button
                  onClick={() => updateMutation.mutate()}
                  disabled={updateMutation.isPending}
                  className="btn-primary flex items-center gap-2 w-full justify-center py-3"
                >
                  <Save size={16} />
                  {updateMutation.isPending ? 'Saving…' : 'Save Changes'}
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
