FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
# Template traité au démarrage par envsubst (nginx image officielle)
# BACKEND_URL doit être défini dans l'environnement (Docker Compose ou Railway)
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
EXPOSE 80
