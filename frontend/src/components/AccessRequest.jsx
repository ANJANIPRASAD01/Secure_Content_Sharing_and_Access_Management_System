import React, { useState } from 'react';
import axios from 'axios';

const AccessRequest = ({ fileId, fileName, userId, onRequestSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  const token = localStorage.getItem('token');

  const handleRequestAccess = async () => {
    try {
      setLoading(true);
      setError(null);
      setMessage(null);

      const response = await axios.post(
        `http://localhost:8080/api/access-requests/request/${fileId}/${userId}`,
        {},
        {
          headers: { Authorization: `Bearer ${token}` }
        }
      );

      setMessage(`Access request sent! The admin will review your request soon.`);
      
      if (onRequestSuccess) {
        onRequestSuccess();
      }

      setTimeout(() => setMessage(null), 3000);
    } catch (err) {
      console.error('Request error:', err);
      setError(err.response?.data?.message || 'Failed to send request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3 className="text-xl font-bold mb-4">Request Access</h3>
      
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {message && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
          {message}
        </div>
      )}

      <p className="text-gray-600 mb-4">
        Request access to <strong>{fileName}</strong> from the admin. They will review your request and grant you access if approved.
      </p>

      <button
        onClick={handleRequestAccess}
        disabled={loading}
        className="w-full bg-blue-600 text-white py-3 rounded hover:bg-blue-700 disabled:opacity-50 font-semibold"
      >
        {loading ? 'Sending Request...' : 'Request Access'}
      </button>
    </div>
  );
};

export default AccessRequest;
