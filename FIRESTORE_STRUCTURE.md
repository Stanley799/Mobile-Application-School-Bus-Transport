# Firestore Data Structure

## Collections

### `users`
```javascript
{
  id: "user123",
  name: "John Doe",
  email: "john@example.com",
  phone: "+1234567890",
  role: "PARENT" | "DRIVER" | "ADMIN",
  createdAt: Timestamp,
  profileImageUrl: string | null
}
```

### `students`
```javascript
{
  id: "student123",
  name: "Jane Doe",
  age: 10,
  gender: "Female",
  grade: "Grade5" | "Grade6" | "Grade7" | "Grade8" | "Grade9",
  school: "School Name",
  parentId: "user123", // Reference to users collection
  homeLocation: "123 Main St",
  pickupLat: 40.7128,
  pickupLng: -74.0060,
  createdAt: Timestamp
}
```

### `routes`
```javascript
{
  id: "route123",
  name: "Route A",
  from: "School",
  to: "Downtown",
  distance: "15 km",
  waypoints: [
    { lat: 40.7128, lng: -74.0060, name: "Stop 1" }
  ],
  createdAt: Timestamp
}
```

### `buses`
```javascript
{
  id: "bus123",
  name: "Bus 1",
  licensePlate: "ABC-123",
  numberOfSeats: 50,
  status: "ACTIVE" | "MAINTENANCE" | "INACTIVE",
  createdAt: Timestamp
}
```

### `trips`
```javascript
{
  id: "trip123",
  tripName: "Morning Route A",
  routeId: "route123", // Reference to routes
  busId: "bus123", // Reference to buses
  driverId: "user456", // Reference to users (role: DRIVER)
  grade: "Grade5", // Student class for this trip
  status: "SCHEDULED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED",
  scheduledDate: Timestamp,
  departureTime: "08:00",
  startTime: Timestamp | null, // When trip actually started
  endTime: Timestamp | null, // When trip actually ended
  studentIds: ["student1", "student2"], // Array of student IDs
  createdAt: Timestamp,
  updatedAt: Timestamp
}
```

### `attendance`
```javascript
{
  id: "attendance123",
  tripId: "trip123", // Reference to trips
  studentId: "student123", // Reference to students
  isPresent: true | false,
  markedAt: Timestamp,
  markedBy: "user456" // Driver who marked attendance
}
```

### `messages`
```javascript
{
  id: "message123",
  senderId: "user123", // Reference to users
  receiverId: "user456", // Reference to users
  content: "Message text",
  type: "chat" | "notification",
  timestamp: Timestamp,
  sender: {
    id: "user123",
    name: "John Doe",
    role: "PARENT"
  },
  receiver: {
    id: "user456",
    name: "Jane Driver",
    role: "DRIVER"
  }
}
```

### `tripReports`
```javascript
{
  id: "report123",
  tripId: "trip123", // Reference to trips
  pdfUrl: "gs://bucket/reports/trip123.pdf", // Firebase Storage path
  downloadUrl: "https://...", // Public download URL
  createdAt: Timestamp,
  generatedAt: Timestamp
}
```

### `notifications`
```javascript
{
  id: "notif123",
  userId: "user123", // Who should receive this
  title: "Trip Report Ready",
  body: "Trip report for Morning Route A is ready for download",
  type: "TRIP_REPORT_READY" | "TRIP_STARTED" | "TRIP_COMPLETED",
  tripId: "trip123", // Reference to trips
  read: false,
  createdAt: Timestamp
}
```

## Indexes Required

1. `trips` collection:
   - `status` (ascending)
   - `driverId` (ascending), `status` (ascending)
   - `scheduledDate` (ascending), `status` (ascending)

2. `attendance` collection:
   - `tripId` (ascending), `studentId` (ascending)

3. `messages` collection:
   - `senderId` (ascending), `receiverId` (ascending), `timestamp` (descending)
   - `receiverId` (ascending), `timestamp` (descending)

4. `students` collection:
   - `parentId` (ascending)
   - `grade` (ascending), `parentId` (ascending)

5. `notifications` collection:
   - `userId` (ascending), `read` (ascending), `createdAt` (descending)

