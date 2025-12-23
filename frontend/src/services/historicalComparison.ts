// US#42 - Service para Comparação Histórica
import api from './api';
import type {
  DecisionHistoricalComparisonRequest,
  DecisionHistoricalComparisonResponse
} from '../types/historicalComparison';

export const compareWithHistorical = async (
  request: DecisionHistoricalComparisonRequest
): Promise<DecisionHistoricalComparisonResponse> => {
  const response = await api.post<DecisionHistoricalComparisonResponse>(
    '/risk/decision/historical-comparison',
    request
  );
  return response.data;
};
