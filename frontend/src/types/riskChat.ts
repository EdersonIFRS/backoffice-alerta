// US#47 - Tipos TypeScript para Chat Unificado de Análise de Impacto (Risk Chat)

export type ExplainFocus = 'BUSINESS' | 'TECHNICAL' | 'EXECUTIVE';

export type ChatMessageType = 'INFO' | 'WARNING' | 'ACTION';

export type ConfidenceLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface ChatQueryRequest {
  question: string;
  projectId?: string;
  focus?: ExplainFocus;
  pullRequestId?: string;
  environment?: string;
  changeType?: string;
}

export interface ChatMessage {
  type: ChatMessageType;
  title: string;
  content: string;
  sources?: string[];
  confidence?: ConfidenceLevel;
}

export interface ChatResponse {
  answer: string;
  messages: ChatMessage[];
  confidence: ConfidenceLevel;
  usedFallback: boolean;
  disclaimer?: string;
}

export interface ChatHistoryItem {
  question: string;
  response: ChatResponse;
  timestamp: string;
}
