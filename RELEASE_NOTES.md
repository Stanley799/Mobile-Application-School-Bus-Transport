# SchoolBusTransport - Release Notes / Build Guide

## 1) Backend

### Environment
- `DATABASE_URL`=postgres://user:pass@host:5432/db
- `JWT_SECRET`=your-strong-secret
- `CORS_ORIGIN`=http://localhost:port or * for testing

### Commands
```bash
cd server
npm install
npx prisma migrate dev
npm run dev # or npm start
```

### Socket.io
- WebSocket served on same origin as REST. Android client strips trailing `/api/` from `BASE_URL` and passes JWT via `auth.token` on handshake.

## 2) Android App

### Maps API Key
- Set in `local.properties`:
```
GOOGLE_MAPS_API_KEY=YOUR_KEY
```
- Ensure the key has Maps SDK for Android enabled and accepts your app’s SHA.

### Build
```bash
# From project root
./gradlew :app:assembleDebug
# APK path: app/build/outputs/apk/debug/app-debug.apk
```

### BASE_URL
- `BuildConfig.BASE_URL` is defined per build type in `app/build.gradle.kts` (ends with `/api/`).
- Example debug value: `https://your-host.ngrok-free.dev/api/`

## 3) Roles & Capabilities (Summary)
- Admin
  - Schedule trips/buses/routes; view all trips; download reports; message any user; view all users
- Driver
  - View assigned trips; start/end trip; take attendance; share live location; message admin/parents
- Parent
  - Register children; view only their children/trips; live map for eligible trips; message admin/driver; download simplified trip report

## 4) Notable Endpoints
- Auth: `POST /api/auth/login`
- Users: `GET /api/users/profile/:id`, `GET /api/users/me`
- Trips: `GET /api/trips`, `POST /api/trips` (admin), `PUT /api/trips/:id/{start|end}`, `POST /api/trips/:id/attendance`, `GET /api/trips/:id/report`
- Locations: `POST /api/locations`, `GET /api/locations/trip/:tripId`, `GET /api/locations/trip/:tripId/latest`
- Messaging: `POST /api/messages`, `GET /api/messages/conversations`, `GET /api/messages/:userId`
- Students: `POST /api/students`, `GET /api/students`, `GET /api/students/:id`, `PUT /api/students/:id`
- Admin Lists: `GET /api/admin/{buses|routes|drivers}`

## 5) Suggested Test Accounts (seeded)
- Admin: `admin@schoolbus.com` / `password123`
- Driver: `driver@schoolbus.com` / `password123`
- Parent: `parent@schoolbus.com` / `password123`

> Create via seed or use `/api/auth/register` (then link to role tables as needed).

## 6) Known Considerations
- PDF saved to app internal files for now; use Android SAF to export to Downloads if required
- UI pickers in Admin panel are basic lists; replace with dropdowns or search if needed
- Sender alignment in chat thread assumes current-user mapping; hook into session userId for exact alignment

## 7) Troubleshooting
- Map is blank: verify Maps API key & network; check Logcat for Google Play Services
- 401/403: confirm JWT stored, `BuildConfig.BASE_URL` correct, role records linked
- Socket not receiving: confirm backend is reachable and token passed in `handshake.auth.token`
