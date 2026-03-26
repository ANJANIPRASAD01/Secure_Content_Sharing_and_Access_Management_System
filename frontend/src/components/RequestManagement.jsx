import React, { useState, useEffect, useCallback } from 'react';
import api from '../api/axiosConfig';

const RequestManagement = ({ adminId }) => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('pending');
  
  // Direct grant states
  const [users, setUsers] = useState([]);
  const [files, setFiles] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);
  const [timeLimitMonths, setTimeLimitMonths] = useState(6);
  const [grantError, setGrantError] = useState(null);
  const [grantSuccess, setGrantSuccess] = useState(null);

  const fetchPendingRequests = useCallback(async () => {
    try {
      setLoading(true);
      const response = await api.get(
        `/access-requests/admin/${adminId}/pending`
      );
      setRequests(response.data);
      setError(null);
    } catch (err) {
      console.error('Error fetching requests:', err);
      setError('Failed to load requests');
    } finally {
      setLoading(false);
    }
  }, [adminId]);

  const fetchFiles = useCallback(async () => {
    try {
      const response = await api.get(
        `/admin/${adminId}/files`
      );
      setFiles(response.data);
    } catch (err) {
      console.error('Error fetching files:', err);
    }
  }, [adminId]);

  useEffect(() => {
    fetchPendingRequests();
    fetchFiles();
  }, [fetchPendingRequests, fetchFiles]);

  const handleSearchUsers = async (query) => {
    setSearchQuery(query);
    if (query.trim().length < 2) {
      setUsers([]);
      return;
    }

    try {
      const response = await api.get(
        `/user/search?q=${encodeURIComponent(query)}`
      );
      setUsers(response.data);
    } catch (err) {
      console.error('Error searching users:', err);
      setUsers([]);
    }
  };

  const handleApprove = async (requestId) => {
    try {
      await api.post(
        `/access-requests/approve`,
        { requestId, action: 'APPROVED' },
        {
          params: { adminId }
        }
      );

      setRequests(requests.filter(req => req.id !== requestId));
      alert('Access request approved!');
    } catch (err) {
      console.error('Error approving request:', err);
      alert('Failed to approve request');
    }
  };

  const handleReject = async (requestId) => {
    const rejectReason = prompt('Enter reason for rejection (optional):');

    try {
      await api.post(
        `/access-requests/reject`,
        { 
          requestId, 
          action: 'REJECTED',
          reason: rejectReason || 'No reason provided'
        },
        {
          params: { adminId }
        }
      );

      setRequests(requests.filter(req => req.id !== requestId));
      alert('Access request rejected!');
    } catch (err) {
      console.error('Error rejecting request:', err);
      alert('Failed to reject request');
    }
  };

  const handleGrantAccess = async () => {
    if (!selectedUser || !selectedFile) {
      setGrantError('Please select both a user and a file');
      return;
    }

    try {
      setGrantError(null);
      setGrantSuccess(null);

      // Calculate expiry date based on months
      const expiryDate = new Date();
      expiryDate.setMonth(expiryDate.getMonth() + parseInt(timeLimitMonths));

      await api.post(
        `/access-control/grant`,
        {
          fileId: selectedFile,
          userId: selectedUser,
          timeLimitMonths: parseInt(timeLimitMonths)
        }
      );

      setGrantSuccess(`Access granted successfully! Expires on: ${expiryDate.toLocaleDateString()}`);
      
      // Reset form
      setSelectedUser(null);
      setSelectedFile(null);
      setTimeLimitMonths(6);
      setSearchQuery('');
      setUsers([]);

      setTimeout(() => setGrantSuccess(null), 5000);
    } catch (err) {
      console.error('Error granting access:', err);
      setGrantError(err.response?.data?.message || 'Failed to grant access');
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <div className="mb-6">
        <div className="flex gap-4 mb-6">
          <button
            onClick={() => setActiveTab('pending')}
            className={`px-4 py-2 rounded font-medium ${
              activeTab === 'pending'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            Pending Requests
          </button>
          <button
            onClick={() => setActiveTab('grant')}
            className={`px-4 py-2 rounded font-medium ${
              activeTab === 'grant'
                ? 'bg-green-600 text-white'
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
          >
            Grant Access
          </button>
        </div>
      </div>

      {/* Pending Requests Tab */}
      {activeTab === 'pending' && (
        <div>
          <h3 className="text-2xl font-bold mb-4">File Access Requests</h3>
          
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {error}
            </div>
          )}

          {loading ? (
            <p className="text-gray-600">Loading requests...</p>
          ) : requests.length === 0 ? (
            <p className="text-gray-600">No pending access requests</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full border-collapse">
                <thead className="bg-gray-100">
                  <tr>
                    <th className="border p-3 text-left">File Name</th>
                    <th className="border p-3 text-left">User</th>
                    <th className="border p-3 text-left">Email</th>
                    <th className="border p-3 text-left">Status</th>
                    <th className="border p-3 text-left">Requested</th>
                    <th className="border p-3 text-center">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {requests.map((request) => (
                    <tr key={request.id} className="hover:bg-gray-50">
                      <td className="border p-3">{request.fileName}</td>
                      <td className="border p-3">{request.userName}</td>
                      <td className="border p-3">{request.userEmail}</td>
                      <td className="border p-3">
                        <span className="px-3 py-1 rounded-full bg-yellow-100 text-yellow-800 text-sm font-semibold">
                          {request.status}
                        </span>
                      </td>
                      <td className="border p-3 text-sm text-gray-600">
                        {new Date(request.requestedAt).toLocaleDateString()}
                      </td>
                      <td className="border p-3 text-center space-x-2">
                        <button
                          onClick={() => handleApprove(request.id)}
                          className="bg-green-600 text-white px-3 py-1 rounded hover:bg-green-700 text-sm"
                        >
                          Approve
                        </button>
                        <button
                          onClick={() => handleReject(request.id, request.fileName)}
                          className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700 text-sm"
                        >
                          Reject
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Grant Access Tab */}
      {activeTab === 'grant' && (
        <div className="max-w-lg">
          <h3 className="text-2xl font-bold mb-4">Grant Access to User</h3>

          {grantError && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
              {grantError}
            </div>
          )}

          {grantSuccess && (
            <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
              {grantSuccess}
            </div>
          )}

          {/* User Search */}
          <div className="mb-6">
            <label className="block text-gray-700 font-medium mb-2">Search User</label>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => handleSearchUsers(e.target.value)}
              placeholder="Search by email or name..."
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />

            {searchQuery && users.length > 0 && (
              <div className="mt-2 border border-gray-300 rounded-lg max-h-48 overflow-y-auto">
                {users.map((user) => (
                  <button
                    key={user.id}
                    onClick={() => {
                      setSelectedUser(user.id);
                      setSearchQuery('');
                      setUsers([]);
                    }}
                    className="w-full text-left px-4 py-2 hover:bg-blue-50 border-b last:border-b-0"
                  >
                    <div className="font-medium">{user.firstName} {user.lastName}</div>
                    <div className="text-sm text-gray-600">{user.email}</div>
                  </button>
                ))}
              </div>
            )}

            {selectedUser && (
              <div className="mt-2 p-3 bg-blue-50 border border-blue-200 rounded">
                <p className="text-sm text-gray-600">Selected User ID: {selectedUser}</p>
                <button
                  onClick={() => setSelectedUser(null)}
                  className="text-sm text-blue-600 hover:text-blue-800 mt-1"
                >
                  Change Selection
                </button>
              </div>
            )}
          </div>

          {/* File Selection */}
          <div className="mb-6">
            <label className="block text-gray-700 font-medium mb-2">Select File</label>
            <select
              value={selectedFile || ''}
              onChange={(e) => setSelectedFile(e.target.value ? parseInt(e.target.value) : null)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Choose a file...</option>
              {files.map((file) => (
                <option key={file.id} value={file.id}>
                  {file.originalFileName} ({file.accessType})
                </option>
              ))}
            </select>
          </div>

          {/* Time Limit Selection */}
          <div className="mb-6">
            <label className="block text-gray-700 font-medium mb-2">Access Duration</label>
            <div className="grid grid-cols-3 gap-3">
              {[1, 6, 12].map((months) => (
                <button
                  key={months}
                  onClick={() => setTimeLimitMonths(months)}
                  className={`px-4 py-2 rounded font-medium transition ${
                    timeLimitMonths === months
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  }`}
                >
                  {months} Month{months > 1 ? 's' : ''}
                </button>
              ))}
            </div>
          </div>

          {/* Grant Button */}
          <button
            onClick={handleGrantAccess}
            disabled={!selectedUser || !selectedFile}
            className="w-full bg-green-600 text-white py-3 rounded-lg hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed font-semibold"
          >
            Grant Access
          </button>
        </div>
      )}
    </div>
  );
};

export default RequestManagement;
