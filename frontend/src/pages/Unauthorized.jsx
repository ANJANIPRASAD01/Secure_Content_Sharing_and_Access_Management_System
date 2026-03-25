import React from 'react';
import { Link } from 'react-router-dom';

const Unauthorized = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-red-500 to-pink-600 flex items-center justify-center">
      <div className="bg-white rounded-lg shadow-2xl p-8 max-w-md text-center">
        <h1 className="text-5xl font-bold text-red-600 mb-4">401</h1>
        <h2 className="text-2xl font-bold text-gray-800 mb-4">Unauthorized</h2>
        <p className="text-gray-600 mb-8">
          You don't have permission to access this page. Please log in with the correct account.
        </p>
        <div className="space-x-4">
          <Link
            to="/admin/login"
            className="inline-block bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700"
          >
            Admin Login
          </Link>
          <Link
            to="/user/login"
            className="inline-block bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700"
          >
            User Login
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Unauthorized;
