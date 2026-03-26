import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig';

const FileUpload = ({ adminId, onUploadSuccess }) => {
  const [categories, setCategories] = useState([]);
  const [formData, setFormData] = useState({
    category: '',
    customCategory: '',
    useCustom: false,
    accessType: 'PUBLIC',
    description: '',
    price: ''
  });
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [categoryLoading, setCategoryLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [creatingCategory, setCreatingCategory] = useState(false);

  // Fetch categories on component mount
  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      setCategoryLoading(true);
      const response = await api.get('/categories');
      setCategories(response.data || []);
    } catch (err) {
      console.error('Failed to fetch categories:', err);
      setError('Failed to load categories');
    } finally {
      setCategoryLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleCreateCategory = async () => {
    if (!formData.customCategory.trim()) {
      setError('Please enter a category name');
      return;
    }

    try {
      setCreatingCategory(true);
      const response = await api.post('/categories', {
        name: formData.customCategory.trim()
      });
      setCategories([...categories, response.data]);
      setFormData(prev => ({
        ...prev,
        category: response.data.id,
        customCategory: '',
        useCustom: false
      }));
      setSuccess('Category created successfully!');
      setTimeout(() => setSuccess(null), 2000);
    } catch (err) {
      console.error('Failed to create category:', err);
      setError(err.response?.data?.message || 'Failed to create category');
    } finally {
      setCreatingCategory(false);
    }
  };

  const handleToggleCustom = () => {
    setFormData(prev => ({
      ...prev,
      useCustom: !prev.useCustom,
      category: '',
      customCategory: ''
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!file) {
      setError('Please select a file');
      return;
    }

    if (!formData.category) {
      setError('Please select or create a category');
      return;
    }

    const uploadFormData = new FormData();
    uploadFormData.append('file', file);
    uploadFormData.append('categoryId', formData.category);
    uploadFormData.append('accessType', formData.accessType);
    uploadFormData.append('description', formData.description);
    
    if (formData.accessType === 'RESTRICTED' && formData.price) {
      uploadFormData.append('price', formData.price);
    }

    try {
      setLoading(true);
      setError(null);
      
      const response = await api.post('/files/upload', uploadFormData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      setSuccess(`File "${response.data.fileName}" uploaded successfully!`);
      setFile(null);
      setFormData({
        category: '',
        customCategory: '',
        useCustom: false,
        accessType: 'PUBLIC',
        description: '',
        price: ''
      });
      
      if (onUploadSuccess) {
        onUploadSuccess();
      }

      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Upload error:', err);
      setError(err.response?.data?.message || 'File upload failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6">
      <h3 className="text-2xl font-bold mb-4">Upload New File</h3>
      
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {success && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
          {success}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* File Input */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Select File *
          </label>
          <input
            type="file"
            onChange={handleFileChange}
            className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
            required
          />
          {file && <p className="text-sm text-green-600 mt-1">Selected: {file.name}</p>}
        </div>

        {/* Category Selection */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <label className="block text-sm font-medium text-gray-700">
              Category *
            </label>
            <button
              type="button"
              onClick={handleToggleCustom}
              className="text-xs bg-gray-200 hover:bg-gray-300 px-2 py-1 rounded"
            >
              {formData.useCustom ? 'Select Category' : 'Create New'}
            </button>
          </div>

          {!formData.useCustom ? (
            // Select existing category
            <select
              name="category"
              value={formData.category}
              onChange={handleInputChange}
              className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
              disabled={categoryLoading}
              required
            >
              <option value="">
                {categoryLoading ? 'Loading categories...' : 'Select a category'}
              </option>
              {categories.map(cat => (
                <option key={cat.id} value={cat.id}>
                  {cat.name}
                </option>
              ))}
            </select>
          ) : (
            // Create new category
            <div className="flex gap-2">
              <input
                type="text"
                name="customCategory"
                value={formData.customCategory}
                onChange={handleInputChange}
                placeholder="Enter new category name"
                className="flex-1 px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                required
              />
              <button
                type="button"
                onClick={handleCreateCategory}
                disabled={creatingCategory || !formData.customCategory.trim()}
                className="bg-green-600 hover:bg-green-700 disabled:opacity-50 text-white px-4 py-2 rounded font-semibold"
              >
                {creatingCategory ? 'Creating...' : 'Create'}
              </button>
            </div>
          )}
        </div>

        {/* Access Type */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Access Type *
          </label>
          <select
            name="accessType"
            value={formData.accessType}
            onChange={handleInputChange}
            className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
          >
            <option value="PUBLIC">Public (Everyone can view)</option>
            <option value="RESTRICTED">Restricted (Request access required)</option>
          </select>
        </div>

        {/* Price (for restricted files) */}
        {formData.accessType === 'RESTRICTED' && (
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Price (Optional)
            </label>
            <input
              type="number"
              name="price"
              value={formData.price}
              onChange={handleInputChange}
              placeholder="Enter price or leave empty"
              step="0.01"
              className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
            />
          </div>
        )}

        {/* Description */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Description
          </label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            placeholder="Enter file description"
            rows="3"
            className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
          />
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={loading}
          className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 disabled:opacity-50 font-semibold"
        >
          {loading ? 'Uploading...' : 'Upload File'}
        </button>
      </form>
    </div>
  );
};

export default FileUpload;
