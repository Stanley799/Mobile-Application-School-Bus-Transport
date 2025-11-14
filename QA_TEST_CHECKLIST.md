# SchoolBusTransport - End-to-End Test Checklist

This checklist validates core flows across roles (Admin, Driver, Parent). Run against your deployed backend and Android app build.

## Pre-req
- Backend server running with DATABASE_URL and JWT_SECRET set
- Test data seeded or created via Admin panel (buses, routes, drivers, parents, students)
- Android build installed on device/emulator; Maps API key configured

## Admin
1) Login as ADMIN → lands on dashboard
2) Open Schedule → lists Buses, Routes, Drivers
3) Create a trip (select Bus, Route, Driver, set name/date) → success toast/logs; trip appears in Trips list
4) Open Trips → recently created trip visible

## Driver
1) Login as DRIVER (assigned to the created trip)
2) Trips → see assigned trip for today
3) Start Trip → status becomes IN_PROGRESS
4) Attendance → mark Present for 1 student, Absent for 1 student (server accepts; no errors)
5) Live Tracking → Share Location toggle ON, marker moves when device location changes
6) End Trip → status becomes COMPLETED; no further updates allowed

## Parent
1) Login as PARENT (linked to a student on the trip)
2) Manage Students → child(ren) visible; add a new child (optional) → created
3) Trips (History) → trip(s) where their child appears are visible
4) Live Map (of a relevant trip) → Bus marker visible during IN_PROGRESS, hidden/last known after END
5) Download Report on Trips list → PDF saved to app files; open via file manager (manual)

## Messaging
1) Parent → open Messages → conversations load (if any)
2) Parent → open a conversation with Driver/Admin → send message → appears in thread
3) Driver/Admin → open Messages → same conversation updates; reply → appears for Parent

## Role Scoping / Security
1) Parent cannot see students who aren’t theirs
2) Driver cannot send location for trips not assigned or not IN_PROGRESS
3) Admin can see all trips; Driver only their own; Parent only trips with their child
4) WebSocket join-trip rejected for unauthorized user/channel

## Error/Edge cases
1) Invalid login creds → server returns 401, UI shows error
2) Network offline → list screens show error states without crashing
3) Permissions denied (location) → Live map shows CTA to allow; no crash

## Regression Quick Pass
- App launch & login → OK
- Navigation between screens → OK
- Back navigation doesn’t crash → OK
- No red crashes in Logcat during flows → OK
