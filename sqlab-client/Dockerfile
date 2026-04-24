FROM node:22-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 4200
# Run ng serve and bind to all interfaces
CMD ["npm", "run", "start", "--", "--host", "0.0.0.0", "--poll", "2000"]
