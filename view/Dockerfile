# Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files
COPY package*.json ./
COPY pnpm-lock.yaml ./

# Install pnpm and dependencies
RUN npm install -g pnpm && pnpm install --no-frozen-lockfile

# Copy source code
COPY . .

# Build arguments for environment variables
ARG VITE_API_BASE_URL
ARG VITE_GHN_TOKEN
ARG VITE_GHN_DEV_TOKEN
ARG VITE_GHN_STORE_ID

# Set environment variables for build
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_GHN_TOKEN=$VITE_GHN_TOKEN
ENV VITE_GHN_DEV_TOKEN=$VITE_GHN_DEV_TOKEN
ENV VITE_GHN_STORE_ID=$VITE_GHN_STORE_ID

# Build application (skip TypeScript type checking for Docker build)
RUN pnpm exec vite build --config ./config/vite.config.prod.ts

# Runtime stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx static assets
RUN rm -rf ./*

# Copy built files from build stage
COPY --from=build /app/dist .

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]

