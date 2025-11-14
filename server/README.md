# School Bus Transport Backend - Setup

## Prerequisites
- Node.js 18+
- PostgreSQL database

## Environment Variables (.env)
Create `server/.env` with:

```
DATABASE_URL="postgresql://USER:PASSWORD@HOST:PORT/DBNAME?schema=public"
JWT_SECRET="replace-with-strong-secret"
PORT=3000
# Optional: Firebase Cloud Messaging (for server-side notifications)
FCM_SERVER_KEY=""
```

## Install & Database
```
npm install
npx prisma migrate dev --name init
npm run db:seed   # optional if seed.js is set up
```

## Development
```
npm run dev
```
Server: http://localhost:3000

## Production Notes
- Restrict CORS origins in `src/index.js`.
- Configure process manager (PM2/systemd) and HTTPS (reverse proxy).
- Rotate `JWT_SECRET` and secure database credentials.
