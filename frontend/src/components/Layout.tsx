// US#31 - Layout principal com navegação
import React from 'react';
import { 
  AppBar, 
  Box, 
  Toolbar, 
  Typography, 
  Button, 
  Container,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText
} from '@mui/material';
import { 
  Dashboard as DashboardIcon,
  Assignment as AuditIcon,
  Timer as SlaIcon,
  Notifications as NotificationIcon,
  BarChart as MetricsIcon,
  Science as SimulationIcon,
  HubOutlined as ImpactGraphIcon,
  InsightsOutlined as InsightsIcon,
  Timeline as TimelineIcon,
  HistoryOutlined as HistoryIcon,
  ChatOutlined as ChatIcon,
  NotificationsActiveOutlined as AlertPreferencesIcon,
  DashboardOutlined as DashboardExecutivoIcon,
  Logout as LogoutIcon
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const drawerWidth = 240;

interface LayoutProps {
  children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const menuItems = [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/', roles: [] },
    { text: 'Dashboard Executivo', icon: <DashboardExecutivoIcon />, path: '/dashboard-executivo', roles: ['ADMIN', 'RISK_MANAGER'] },
    { text: 'Auditorias', icon: <AuditIcon />, path: '/audits', roles: [] },
    { text: 'SLAs', icon: <SlaIcon />, path: '/slas', roles: [] },
    { text: 'Notificações', icon: <NotificationIcon />, path: '/notifications', roles: [] },
    { text: 'Métricas', icon: <MetricsIcon />, path: '/metrics', roles: [] },
    { text: 'Simulação', icon: <SimulationIcon />, path: '/simulation', roles: [] },
    { text: 'Impacto Sistêmico', icon: <ImpactGraphIcon />, path: '/impact-graph', roles: ['ADMIN', 'RISK_MANAGER', 'ENGINEER'] },
    { text: 'Impacto Executivo', icon: <InsightsIcon />, path: '/executive-impact', roles: ['ADMIN', 'RISK_MANAGER'] },
    { text: 'Timeline de Decisão', icon: <TimelineIcon />, path: '/timeline', roles: ['ADMIN', 'RISK_MANAGER'] },
    { text: 'Comparação Histórica', icon: <HistoryIcon />, path: '/historical-comparison', roles: ['ADMIN', 'RISK_MANAGER'] },
    { text: 'Chat de Risco', icon: <ChatIcon />, path: '/risk-chat', roles: ['ADMIN', 'RISK_MANAGER', 'ENGINEER'] },
    { text: 'Preferências de Alertas', icon: <AlertPreferencesIcon />, path: '/alert-preferences', roles: ['ADMIN', 'RISK_MANAGER'] }
  ];

  return (
    <Box sx={{ display: 'flex' }}>
      {/* AppBar */}
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Backoffice Alerta - Dashboard Executivo
          </Typography>
          <Typography variant="body2" sx={{ mr: 2 }}>
            {user?.username} ({user?.roles.join(', ')})
          </Typography>
          <Button color="inherit" startIcon={<LogoutIcon />} onClick={handleLogout}>
            Sair
          </Button>
        </Toolbar>
      </AppBar>

      {/* Drawer */}
      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
          },
        }}
      >
        <Toolbar />
        <Box sx={{ overflow: 'auto' }}>
          <List>
            {menuItems
              .filter(item => 
                item.roles.length === 0 || 
                item.roles.some(role => user?.roles.includes(role as any))
              )
              .map((item) => (
              <ListItem key={item.text} disablePadding>
                <ListItemButton
                  selected={location.pathname === item.path}
                  onClick={() => navigate(item.path)}
                >
                  <ListItemIcon>{item.icon}</ListItemIcon>
                  <ListItemText primary={item.text} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        </Box>
      </Drawer>

      {/* Main content */}
      <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
        <Toolbar />
        <Container maxWidth="xl">
          {children}
        </Container>
      </Box>
    </Box>
  );
};
