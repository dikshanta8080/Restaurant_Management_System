import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Edit2, Trash2, X, Upload, Utensils } from 'lucide-react';
import { restaurantService } from '../../services/restaurantService';
import { foodItemService } from '../../services/foodItemService';
import Modal from '../../components/Modal';
import { SkeletonRow } from '../../components/Skeleton';
import toast from 'react-hot-toast';
import { FoodItemResponse } from '../../types/foodItem';
import { getImageUrl } from '../../utils/imageUtils';

interface FoodForm {
  name: string;
  description: string;
  price: string;
  available: boolean;
  categoryId: string;
  file?: File | null;
}

const EMPTY_FORM: FoodForm = { name: '', description: '', price: '', available: true, categoryId: '', file: null };

const FoodItemsManagement: React.FC = () => {
  const queryClient = useQueryClient();
  const [showModal, setShowModal] = useState(false);
  const [editItem, setEditItem] = useState<FoodItemResponse | null>(null);
  const [form, setForm] = useState<FoodForm>(EMPTY_FORM);
  const [preview, setPreview] = useState<string | null>(null);
  const [brokenImageIds, setBrokenImageIds] = useState<Record<number, boolean>>({});

  const { data: restaurant } = useQuery({
    queryKey: ['own-restaurant'],
    queryFn: async () => {
      const res = await restaurantService.getOwnRestaurant();
      return res.responseObject;
    },
    retry: 1,
  });

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: async () => {
      const res = await foodItemService.getAllCategories();
      return res.responseObject;
    },
  });

  const { data: items, isLoading } = useQuery({
    queryKey: ['restaurant-food-items', restaurant?.id],
    queryFn: () => foodItemService.getFoodItemsByRestaurant(restaurant!.id),
    enabled: !!restaurant?.id,
  });

  const addMutation = useMutation({
    mutationFn: () => foodItemService.addFoodItem(restaurant!.id, {
      name: form.name,
      description: form.description,
      price: parseFloat(form.price),
      available: form.available,
      categoryId: parseInt(form.categoryId),
      multipartFile: form.file ?? undefined,
    }),
    onSuccess: () => {
      toast.success('Food item added!');
      queryClient.invalidateQueries({ queryKey: ['restaurant-food-items'] });
      setShowModal(false); setForm(EMPTY_FORM); setPreview(null);
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Failed to add item'),
  });

  const updateMutation = useMutation({
    mutationFn: () => foodItemService.updateFoodItem(editItem!.id, {
      name: form.name,
      description: form.description,
      price: parseFloat(form.price),
      available: form.available,
      categoryId: parseInt(form.categoryId),
    }),
    onSuccess: () => {
      toast.success('Food item updated!');
      queryClient.invalidateQueries({ queryKey: ['restaurant-food-items'] });
      setShowModal(false); setEditItem(null); setForm(EMPTY_FORM);
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Failed to update item'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => foodItemService.deleteFoodItem(id),
    onSuccess: () => {
      toast.success('Food item deleted');
      queryClient.invalidateQueries({ queryKey: ['restaurant-food-items'] });
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Failed to delete'),
  });

  const categoryMap = Object.fromEntries((categories ?? []).map(c => [c.id, c.categoryName]));

  const openAdd = () => {
    setEditItem(null);
    setForm(EMPTY_FORM);
    setPreview(null);
    setShowModal(true);
  };

  const openEdit = (item: FoodItemResponse) => {
    setEditItem(item);
    setForm({
      name: item.name,
      description: item.description,
      price: item.price.toString(),
      available: item.available,
      categoryId: item.categoryId.toString(),
      file: null,
    });
    setPreview(getImageUrl(item.imageUrl));
    setBrokenImageIds({});
    setShowModal(true);
  };

  const handleSubmit = () => {
    if (!form.name || !form.price || !form.categoryId) {
      toast.error('Please fill all required fields'); return;
    }
    if (editItem) updateMutation.mutate();
    else addMutation.mutate();
  };

  const isPending = addMutation.isPending || updateMutation.isPending;

  return (
    <div className="min-h-screen bg-gray-50 page-enter">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-orange-100 rounded-xl flex items-center justify-center">
              <Utensils size={22} className="text-orange-600" />
            </div>
            <div>
              <h1 className="text-3xl font-black text-gray-900">Food Menu</h1>
              <p className="text-sm text-gray-500">{items?.length ?? 0} items</p>
            </div>
          </div>
          <button onClick={openAdd} className="btn-primary flex items-center gap-2">
            <Plus size={18} /> Add Item
          </button>
        </div>

        {isLoading ? (
          <div className="card divide-y">
            {Array.from({ length: 5 }).map((_, i) => <SkeletonRow key={i} />)}
          </div>
        ) : !items?.length ? (
          <div className="card p-16 flex flex-col items-center text-center">
            <Utensils size={48} className="text-orange-200 mb-4" />
            <p className="text-xl font-bold text-gray-700 mb-2">No food items yet</p>
            <p className="text-gray-400 mb-6">Start building your menu by adding food items</p>
            <button onClick={openAdd} className="btn-primary flex items-center gap-2">
              <Plus size={18} /> Add First Item
            </button>
          </div>
        ) : (
          <div className="card divide-y">
            {items.map(item => (
              <div key={item.id} className="flex items-center gap-4 p-4 hover:bg-gray-50/50 transition-colors">
                <div className="w-14 h-14 rounded-xl bg-orange-50 flex items-center justify-center overflow-hidden flex-shrink-0">
                  {(() => {
                    const imgUrl = getImageUrl(item.imageUrl);
                    const failed = !!brokenImageIds[item.id];
                    if (imgUrl && !failed) {
                      return (
                        <img
                          src={imgUrl}
                          alt={item.name}
                          className="w-full h-full object-cover"
                          onError={() => setBrokenImageIds(prev => ({ ...prev, [item.id]: true }))}
                        />
                      );
                    }
                    return <span className="text-2xl">🍛</span>;
                  })()}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="font-semibold text-gray-900 truncate">{item.name}</p>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${item.available ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                      {item.available ? 'Available' : 'Unavailable'}
                    </span>
                  </div>
                  <p className="text-sm text-gray-500 truncate">{item.description}</p>
                  <p className="text-sm font-bold text-orange-600">Rs. {Number(item.price).toFixed(2)} • {categoryMap[item.categoryId] ?? 'Unknown'}</p>
                </div>
                <div className="flex gap-2">
                  <button onClick={() => openEdit(item)} className="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors">
                    <Edit2 size={16} />
                  </button>
                  <button onClick={() => deleteMutation.mutate(item.id)} className="p-2 text-red-400 hover:bg-red-50 rounded-lg transition-colors">
                    <Trash2 size={16} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add/Edit Modal */}
      <Modal isOpen={showModal} onClose={() => { setShowModal(false); setEditItem(null); }} title={editItem ? 'Edit Food Item' : 'Add Food Item'}>
        <div className="space-y-4">
          {/* Image upload (only for new items) */}
          {!editItem && (
            <div>
              <label className="label">Image</label>
              <label className={`flex flex-col items-center justify-center h-32 border-2 border-dashed rounded-xl cursor-pointer transition-colors ${preview ? 'border-orange-300' : 'border-gray-200 hover:border-orange-200'}`}>
                {preview ? (
                  <img src={preview} className="h-full w-full object-cover rounded-xl" alt="preview" />
                ) : (
                  <div className="flex flex-col items-center gap-1 text-gray-400">
                    <Upload size={24} />
                    <span className="text-xs">Upload food image</span>
                  </div>
                )}
                <input type="file" accept="image/*" className="hidden" onChange={e => {
                  const f = e.target.files?.[0];
                  if (f) { setForm(prev => ({ ...prev, file: f })); setPreview(URL.createObjectURL(f)); }
                }} />
              </label>
            </div>
          )}
          <div>
            <label className="label">Name *</label>
            <input value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))} className="input" placeholder="e.g. Chicken Momo" />
          </div>
          <div>
            <label className="label">Description</label>
            <textarea value={form.description} onChange={e => setForm(p => ({ ...p, description: e.target.value }))} className="input resize-none" rows={2} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Price (Rs.) *</label>
              <input type="number" value={form.price} onChange={e => setForm(p => ({ ...p, price: e.target.value }))} className="input" placeholder="0.00" min="0" step="0.01" />
            </div>
            <div>
              <label className="label">Category *</label>
              <select value={form.categoryId} onChange={e => setForm(p => ({ ...p, categoryId: e.target.value }))} className="input">
                <option value="">Select category</option>
                {categories?.map(c => (
                  <option key={c.id} value={c.id}>{c.categoryName}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                className="sr-only peer"
                checked={form.available}
                onChange={e => setForm(p => ({ ...p, available: e.target.checked }))}
              />
              <div className="w-11 h-6 bg-gray-200 peer-checked:bg-orange-500 rounded-full peer peer-focus:ring-4 peer-focus:ring-orange-100 transition-colors" />
              <div className="absolute left-0.5 top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform peer-checked:translate-x-5" />
            </label>
            <span className="text-sm font-medium text-gray-700">Available for order</span>
          </div>
          <div className="flex gap-3 pt-2">
            <button onClick={() => { setShowModal(false); setEditItem(null); }} className="btn-secondary flex-1">Cancel</button>
            <button onClick={handleSubmit} disabled={isPending} className="btn-primary flex-1">
              {isPending ? 'Saving…' : editItem ? 'Update Item' : 'Add Item'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default FoodItemsManagement;
