// US#31 - Badges coloridos para status
import React from 'react';
import { Chip } from '@mui/material';
import type { FinalDecision, RiskLevel, SlaStatus, NotificationSeverity, ConfidenceStatus } from '../types';

export const DecisionBadge: React.FC<{ decision: FinalDecision }> = ({ decision }) => {
  const colors = {
    APPROVED: 'success',
    REJECTED: 'error',
    REQUIRES_REVIEW: 'warning',
    CONDITIONAL_APPROVAL: 'info'
  } as const;

  return <Chip label={decision} color={colors[decision]} size="small" />;
};

export const RiskLevelBadge: React.FC<{ level: RiskLevel }> = ({ level }) => {
  const colors = {
    CRITICAL: 'error',
    HIGH: 'warning',
    MEDIUM: 'info',
    LOW: 'success'
  } as const;

  return <Chip label={level} color={colors[level]} size="small" />;
};

export const SlaStatusBadge: React.FC<{ status: SlaStatus }> = ({ status }) => {
  const colors = {
    ACTIVE: 'info',
    ACKNOWLEDGED: 'primary',
    ESCALATED: 'warning',
    BREACHED: 'error',
    RESOLVED: 'success'
  } as const;

  return <Chip label={status} color={colors[status]} size="small" />;
};

export const SeverityBadge: React.FC<{ severity: NotificationSeverity }> = ({ severity }) => {
  const colors = {
    CRITICAL: 'error',
    HIGH: 'warning',
    MEDIUM: 'info',
    LOW: 'success',
    INFO: 'default'
  } as const;

  return <Chip label={severity} color={colors[severity]} size="small" />;
};

export const ConfidenceBadge: React.FC<{ status: ConfidenceStatus }> = ({ status }) => {
  const colors = {
    EXCELLENT: 'success',
    HEALTHY: 'info',
    ATTENTION: 'warning',
    CRITICAL: 'error'
  } as const;

  return <Chip label={status} color={colors[status]} />;
};
