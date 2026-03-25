import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';

// Auth Provider
import { AuthProvider, useAuth } from './context/AuthContext';

// Pages
import HomePage from './pages/HomePage';
import AdminLogin from './components/Auth/AdminLogin';
import AdminRegister from './components/Auth/AdminRegister';
import UserLogin from './components/Auth/UserLogin';
import UserRegister from './components/Auth/UserRegister';
import AdminDashboard from './components/Admin/AdminDashboard';
import UserDashboard from './components/User/UserDashboard';
import Unauthorized from './pages/Unauthorized';
import NotFound from './pages/NotFound';

function AppRoutes() {
  const { isAuthenticated, userRole, loading } = useAuth();

  if (loading) {
    return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
  }

  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<HomePage />} />
        <Route path="/admin/login" element={<AdminLogin />} />
        <Route path="/admin/register" element={<AdminRegister />} />
        <Route path="/user/login" element={<UserLogin />} />
        <Route path="/user/register" element={<UserRegister />} />

        {/* Protected Admin Routes */}
        <Route
          path="/admin/dashboard"
          element={
            isAuthenticated && userRole === 'ADMIN' ? (
              <AdminDashboard />
            ) : (
              <Navigate to="/unauthorized" />
            )
          }
        />

        {/* Protected User Routes */}
        <Route
          path="/user/dashboard"
          element={
            isAuthenticated && userRole === 'USER' ? (
              <UserDashboard />
            ) : (
              <Navigate to="/unauthorized" />
            )
          }
        />

        {/* Error Routes */}
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Router>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}

export default App;
