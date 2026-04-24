FROM node:20-alpine

RUN corepack enable && corepack prepare pnpm@latest --activate

WORKDIR /app

# Solo copiar dependencias primero (cache)
COPY package.json pnpm-lock.yaml ./

RUN pnpm install

# Luego copiar código
COPY . .

EXPOSE 5173

CMD ["pnpm", "dev", "--host", "0.0.0.0"]
