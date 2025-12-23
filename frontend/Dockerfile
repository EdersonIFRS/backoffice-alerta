# US#73 - Dockerfile Frontend (React + Nginx)
# Multi-stage build

# Stage 1: Build
FROM node:18-alpine AS build
WORKDIR /app

# Copiar package files
COPY package*.json ./

# Install dependencies
RUN npm ci --legacy-peer-deps

# Copiar código fonte
COPY . .

# Build de produção (skip type check, apenas build)
RUN npx vite build

# Stage 2: Runtime com Nginx
FROM nginx:1.25-alpine

# Copiar build do React
COPY --from=build /app/dist /usr/share/nginx/html

# Configuração Nginx para SPA
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expor porta
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/health || exit 1

# Nginx roda como daemon off
CMD ["nginx", "-g", "daemon off;"]
