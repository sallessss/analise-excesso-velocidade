FROM node:24-alpine AS compilacao

WORKDIR /aplicacao

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:1.29-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=compilacao /aplicacao/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80
