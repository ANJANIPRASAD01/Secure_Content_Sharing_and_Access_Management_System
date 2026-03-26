import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';

const FileEditor = ({ file, categories, onEditSuccess, onClose }) => {
  const [formData, setFormData] = useState({
    description: file?.description || '',
    price: file?.price || '',
    categoryId: file?.categoryId || '',
    accessType: file?.accessType || 'PUBLIC',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (file) {
      setFormData({
        description: file.description || '',
        price: file.price || '',
        categoryId: file.categoryId || '',
        accessType: file.accessType || 'PUBLIC',
      });
    }
  }, [file]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const updatePayload = {
        description: formData.description,
        accessType: formData.accessType,
      };

      // Only include price if access type is RESTRICTED
      if (formData.accessType === 'RESTRICTED') {
        updatePayload.price = parseFloat(formData.price) || 0;
      }

      // Only include categoryId if it's provided
      if (formData.categoryId) {
        updatePayload.categoryId = parseInt(formData.categoryId);
      }

      await api.put(`/files/${file.id}`, updatePayload);
      setSuccess('File details updated successfully!');
      setTimeout(() => {
        onEditSuccess();
        onClose();
      }, 1500);
    } catch (err) {
      console.error('Edit file error:', err);
      setError(err.response?.data?.message || 'Failed to update file details');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {error && (
        <div className="bg-red-100 text-red-700 p-3 rounded">
          {error}
        </div>
      )}
      {success && (
        <div className="bg-green-100 text-green-700 p-3 rounded">
          {success}
        </div>
      )}

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          File Name
        </label>
        <input
          type="text"
          disabled
          value={file?.originalFileName || ''}
          className="w-full px-4 py-2 border rounded bg-gray-100 text-gray-500"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Description
        </label>
        <textarea
          name="description"
          value={formData.description}
          onChange={handleChange}
          placeholder="Enter file description"
          rows="4"
          className="w-full px-4 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Access Type
        </label>
        <select
          name="accessType"
          value={formData.accessType}
          onChange={handleChange}
          className="w-full px-4 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="PUBLIC">Public</option>
          <option value="RESTRICTED">Restricted</option>
          <option value="PRIVATE">Private</option>
        </select>
      </div>

      {formData.accessType === 'RESTRICTED' && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Price ($)
          </label>
          <input
            type="number"
            name="price"
            value={formData.price}
            onChange={handleChange}
            placeholder="Enter price for restricted file"
            step="0.01"
            min="0"
            className="w-full px-4 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      )}

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Category
        </label>
        <select
          name="categoryId"
          value={formData.categoryId}
          onChange={handleChange}
          className="w-full px-4 py-2 border rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="">Select a category</option>
          {categories?.map(cat => (
            <option key={cat.id} value={cat.id}>
              {cat.name}
            </option>
          ))}
        </select>
      </div>

      <div className="flex gap-3 pt-4">
        <button
          type="submit"
          disabled={loading}
          className="flex-1 bg-blue-600 text-white py-2 rounded hover:bg-blue-700 disabled:bg-gray-500"
        >
          {loading ? 'Updating...' : 'Update File'}
        </button>
        <button
          type="button"
          onClick={onClose}
          className="flex-1 bg-gray-300 text-gray-800 py-2 rounded hover:bg-gray-400"
        >
          Cancel
        </button>
      </div>
    </form>
  );
};

export default FileEditor;
