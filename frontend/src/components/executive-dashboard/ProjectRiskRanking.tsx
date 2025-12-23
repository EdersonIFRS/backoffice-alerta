import React from 'react';
import { Card, CardContent, Typography, Box, LinearProgress, List, ListItem, ListItemText, Chip } from '@mui/material';
import { TrendingDown as RiskIcon } from '@mui/icons-material';
import { ProjectRiskSummary } from '../../services/executiveDashboard';

interface ProjectRiskRankingProps {
  projects: ProjectRiskSummary[];
}

/**
 * Ranking de projetos por risco
 * 
 * US#60 - Dashboard Executivo de Alertas & Risco
 */
export const ProjectRiskRanking: React.FC<ProjectRiskRankingProps> = ({ projects }) => {
  const getRiskColor = (blockRate: number): string => {
    if (blockRate >= 30) return 'error';
    if (blockRate >= 15) return 'warning';
    return 'success';
  };

  return (
    <Card>
      <CardContent>
        <Box display="flex" alignItems="center" mb={2}>
          <RiskIcon sx={{ mr: 1, color: 'error.main' }} />
          <Typography variant="h6" fontWeight="bold">
            Top 5 Projetos em Risco
          </Typography>
        </Box>

        {projects.length === 0 ? (
          <Typography variant="body2" color="text.secondary" align="center" py={4}>
            Nenhum projeto com risco detectado
          </Typography>
        ) : (
          <List>
            {projects.map((project, index) => (
              <ListItem 
                key={project.projectId}
                sx={{ 
                  flexDirection: 'column', 
                  alignItems: 'stretch',
                  borderLeft: index === 0 ? 3 : 0,
                  borderColor: 'error.main',
                  mb: 2,
                  bgcolor: index === 0 ? 'error.lighter' : 'transparent',
                  borderRadius: 1
                }}
              >
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                  <ListItemText 
                    primary={
                      <Typography variant="subtitle1" fontWeight="bold">
                        #{index + 1} {project.projectName}
                      </Typography>
                    }
                    secondary={`${project.alertsLast30Days} alertas (30 dias)`}
                  />
                  <Chip 
                    label={`${project.blockRate.toFixed(1)}%`}
                    color={getRiskColor(project.blockRate) as any}
                    size="small"
                  />
                </Box>
                <LinearProgress 
                  variant="determinate" 
                  value={Math.min(project.blockRate, 100)} 
                  color={getRiskColor(project.blockRate) as any}
                  sx={{ height: 8, borderRadius: 1 }}
                />
              </ListItem>
            ))}
          </List>
        )}
      </CardContent>
    </Card>
  );
};
