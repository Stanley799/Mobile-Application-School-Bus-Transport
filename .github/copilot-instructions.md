# Copilot Instructions for SchoolBusTransport

## Project Overview
- **Monorepo** with two main components:
  - `app/`: Android app (Kotlin, Jetpack Compose, Hilt, Google Maps, Firebase)
  - `server/`: Node.js backend (Express, Prisma, PostgreSQL, Socket.io)

## Architecture & Data Flow
- **Mobile app** communicates with backend via REST API and real-time updates via WebSocket (Socket.io).
- **Backend** enforces role-based access (ADMIN, DRIVER, PARENT) for all endpoints and WebSocket events.
- **Database schema** is defined in `server/prisma/schema.prisma` and managed with Prisma migrations.
- **Live bus tracking**: Drivers send location updates via WebSocket; parents and admins receive real-time updates.
- **Authentication**: JWT-based, required for both REST and WebSocket (token sent in handshake `auth`).

## Developer Workflows
### Backend
- Install: `npm install` in `server/`
- DB migration: `npx prisma migrate dev --name <desc>`
- Seed DB: `npm run db:seed` (runs `prisma/seed.js`)
- Start dev server: `npm run dev` (nodemon, hot reload)
- Main entry: `src/index.js` (see also `src/sockets.js` for WebSocket logic)

### Android App
- Build/Run: Use Android Studio or `./gradlew assembleDebug` in root
- Google Maps API key: Provided via Gradle property `GOOGLE_MAPS_API_KEY` (see `app/build.gradle.kts`)
- Firebase: Configured via `app/google-services.json` and `SchoolBusApplication.kt`
- Dependency injection: Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`)
- Real-time: Uses `SocketManager` (see `data/realtime/SocketManager.kt`) for WebSocket

## Project-Specific Patterns & Conventions
- **Role-based filtering**: All backend controllers filter data by user role (see `tripController.js` for example).
- **WebSocket rooms**: Clients join trip-specific rooms for location updates (`join-trip` event).
- **API URLs**: Android `BASE_URL` is set per build type in `app/build.gradle.kts` (debug vs release).
- **Permissions**: AndroidManifest declares all required permissions for location, network, notifications.
- **Testing**: No explicit test scripts found; follow standard Jest/Mocha for Node, Android Studio for app.

## Integration Points
- **Prisma**: All DB access via Prisma Client (`@prisma/client`)
- **Socket.io**: Real-time comms for live tracking (see both backend and app SocketManager)
- **Firebase**: Analytics, Crashlytics, FCM (optional, see `SchoolBusApplication.kt`)

## Key Files & Directories
- `server/prisma/schema.prisma`: DB schema
- `server/src/controllers/`: Business logic (role-based)
- `server/src/sockets.js`: WebSocket event handling
- `app/src/main/java/com/example/schoolbustransport/data/realtime/SocketManager.kt`: WebSocket client
- `app/src/main/java/com/example/schoolbustransport/SchoolBusApplication.kt`: App entry, DI, Firebase
- `app/build.gradle.kts`: Android build config, API URLs

## Example: Real-Time Location Update
- Driver emits: `location-update` (with JWT)
- Server validates, broadcasts to trip room
- Parent/admin receives `location-broadcast` event

---
For more details, see `server/README.md` and code comments in controllers and SocketManager classes.
