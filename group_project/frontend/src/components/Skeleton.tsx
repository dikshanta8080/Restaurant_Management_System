import React from 'react';

export const SkeletonCard: React.FC = () => (
  <div className="card p-0 overflow-hidden animate-pulse">
    <div className="bg-gray-200 h-48 w-full" />
    <div className="p-4 space-y-3">
      <div className="h-4 bg-gray-200 rounded-lg w-3/4" />
      <div className="h-3 bg-gray-100 rounded-lg w-full" />
      <div className="h-3 bg-gray-100 rounded-lg w-2/3" />
    </div>
  </div>
);

export const SkeletonRow: React.FC = () => (
  <div className="flex items-center gap-4 p-4 animate-pulse">
    <div className="w-12 h-12 bg-gray-200 rounded-xl flex-shrink-0" />
    <div className="flex-1 space-y-2">
      <div className="h-4 bg-gray-200 rounded-lg w-1/2" />
      <div className="h-3 bg-gray-100 rounded-lg w-1/3" />
    </div>
  </div>
);

export const SkeletonText: React.FC<{ lines?: number }> = ({ lines = 3 }) => (
  <div className="space-y-2 animate-pulse">
    {Array.from({ length: lines }).map((_, i) => (
      <div key={i} className={`h-3 bg-gray-200 rounded-lg ${i === lines - 1 ? 'w-2/3' : 'w-full'}`} />
    ))}
  </div>
);

export const SkeletonStat: React.FC = () => (
  <div className="card p-6 animate-pulse space-y-3">
    <div className="h-3 bg-gray-200 rounded w-1/2" />
    <div className="h-8 bg-gray-200 rounded w-1/3" />
  </div>
);
