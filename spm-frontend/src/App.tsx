import { lazy, Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import { Login } from './components/auth/Login';
import { NotFound } from './components/auth/NotFound';
import { AccessDenied } from './components/auth/AccessDenied';
import { DashboardRedirect } from './components/auth/DashboardRedirect';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { Layout } from './components/shared/Layout';
import { LoadingSpinner } from './components/shared/LoadingSpinner';
import { ErrorBoundary } from './components/shared/ErrorBoundary';

const TeacherRoutes = lazy(() => import('./components/teacher/TeacherRoutes'));
const ParentRoutes = lazy(() => import('./components/parent/ParentRoutes'));
const StudentRoutes = lazy(() => import('./components/student/StudentRoutes'));
const AdminRoutes = lazy(() => import('./components/admin/AdminRoutes'));

export default function App() {
  return (
    <ErrorBoundary>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/403" element={<AccessDenied />} />

        <Route
          path="/"
          element={
            <ProtectedRoute>
              <DashboardRedirect />
            </ProtectedRoute>
          }
        />

        <Route
          path="/teacher/*"
          element={
            <ProtectedRoute requiredRoles={['TEACHER']}>
              <Layout>
                <Suspense fallback={<LoadingSpinner />}>
                  <TeacherRoutes />
                </Suspense>
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/parent/*"
          element={
            <ProtectedRoute requiredRoles={['PARENT']}>
              <Layout>
                <Suspense fallback={<LoadingSpinner />}>
                  <ParentRoutes />
                </Suspense>
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/student/*"
          element={
            <ProtectedRoute requiredRoles={['STUDENT']}>
              <Layout>
                <Suspense fallback={<LoadingSpinner />}>
                  <StudentRoutes />
                </Suspense>
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/*"
          element={
            <ProtectedRoute requiredRoles={['ADMIN']}>
              <Layout>
                <Suspense fallback={<LoadingSpinner />}>
                  <AdminRoutes />
                </Suspense>
              </Layout>
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<NotFound />} />
      </Routes>
    </ErrorBoundary>
  );
}
