// US#31 - Configuração de rotas
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from '../context/AuthContext';
import { Layout } from '../components/Layout';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { Login } from '../pages/Login';
import { Dashboard } from '../pages/Dashboard';
import { Audits } from '../pages/Audits';
import { Slas } from '../pages/Slas';
import { Notifications } from '../pages/Notifications';
import { Metrics } from '../pages/Metrics';
import { Simulation } from '../pages/Simulation';
import ImpactGraph from '../pages/ImpactGraph';
import { ExecutiveImpact } from '../pages/ExecutiveImpact';
import { Timeline } from '../pages/Timeline';
import { HistoricalComparison } from '../pages/HistoricalComparison';
import { RiskChat } from '../pages/RiskChat';
import { AlertPreferences } from '../pages/AlertPreferences';
import { DashboardExecutivo } from '../pages/DashboardExecutivo';

const AppRoutes: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginRoute />} />
          <Route path="/" element={
            <ProtectedRoute>
              <Layout>
                <Dashboard />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/dashboard-executivo" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'RISK_MANAGER']}>
              <Layout>
                <DashboardExecutivo />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/audits" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'RISK_MANAGER', 'VIEWER']}>
              <Layout>
                <Audits />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/slas" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'RISK_MANAGER']}>
              <Layout>
                <Slas />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/notifications" element={
            <ProtectedRoute>
              <Layout>
                <Notifications />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/metrics" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'RISK_MANAGER', 'VIEWER']}>
              <Layout>
                <Metrics />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/simulation" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'RISK_MANAGER']}>
              <Layout>
                <Simulation />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/impact-graph" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'RISK_MANAGER', 'ENGINEER']}>
              <Layout>
                <ImpactGraph />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/executive-impact" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'RISK_MANAGER']}>
              <Layout>
                <ExecutiveImpact />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/timeline/:pullRequestId?" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'RISK_MANAGER']}>
              <Layout>
                <Timeline />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/historical-comparison" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'RISK_MANAGER']}>
              <Layout>
                <HistoricalComparison />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/risk-chat" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'RISK_MANAGER', 'ENGINEER']}>
              <Layout>
                <RiskChat />
              </Layout>
            </ProtectedRoute>
          } />
          <Route path="/alert-preferences" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'RISK_MANAGER']}>
              <Layout>
                <AlertPreferences />
              </Layout>
            </ProtectedRoute>
          } />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

// Componente para redirecionar usuário autenticado da página de login
const LoginRoute: React.FC = () => {
  const { isAuthenticated } = useAuth();
  
  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }
  
  return <Login />;
};

export default AppRoutes;
