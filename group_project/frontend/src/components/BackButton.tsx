import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

type BackButtonProps = {
  redirectTo?: string;
  label?: string;
  className?: string;
};

function getRoleFallback(role: string | undefined): string {
  if (role === 'ADMIN') return '/admin';
  if (role === 'RESTAURANT') return '/restaurant/dashboard';
  return '/';
}

export default function BackButton({ redirectTo, label = 'Back', className }: BackButtonProps) {
  const navigate = useNavigate();
  const { user } = useAuth();

  const fallback = redirectTo ?? getRoleFallback(user?.role);

  // React Router history index is available in the browser as `window.history.state.idx` (v6+).
  const canGoBack = typeof window !== 'undefined' && typeof window.history?.state?.idx === 'number'
    ? window.history.state.idx > 0
    : window.history.length > 1;

  const onBack = () => {
    if (canGoBack) navigate(-1);
    else navigate(fallback, { replace: true });
  };

  return (
    <button
      type="button"
      onClick={onBack}
      title="Go Back"
      className={`inline-flex items-center gap-2 text-gray-600 hover:text-orange-600 font-medium transition-colors ${className ?? ''}`}
    >
      <ArrowLeft size={16} />
      <span className="text-sm">{label}</span>
    </button>
  );
}

