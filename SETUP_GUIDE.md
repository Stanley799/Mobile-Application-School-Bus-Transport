# Firebase Setup Guide for School Bus Transport

## Prerequisites

1. **Firebase Project Setup**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or use existing
   - Enable **Blaze Plan** (required for Cloud Functions and Storage)

2. **Enable Firebase Services**
   - ✅ Firestore Database
   - ✅ Firebase Authentication
   - ✅ Cloud Functions
   - ✅ Cloud Storage
   - ✅ Cloud Messaging (FCM)

## Step 1: Firestore Database Setup

1. **Create Firestore Database**
   - Go to Firestore Database in Firebase Console
   - Create database in **Production mode** (we'll add security rules)
   - Choose your preferred location

2. **Deploy Security Rules**
   ```bash
   firebase deploy --only firestore:rules
   ```
   The rules file is in `firestore.rules`

3. **Create Indexes**
   Firestore will prompt you to create indexes when needed, or create them manually:
   - `trips`: `status` (Ascending)
   - `trips`: `driverId` (Ascending), `status` (Ascending)
   - `trips`: `scheduledDate` (Ascending), `status` (Ascending)
   - `attendance`: `tripId` (Ascending), `studentId` (Ascending)
   - `messages`: `senderId` (Ascending), `receiverId` (Ascending), `timestamp` (Descending)
   - `students`: `parentId` (Ascending)
   - `students`: `grade` (Ascending), `parentId` (Ascending)
   - `notifications`: `userId` (Ascending), `read` (Ascending), `createdAt` (Descending)

## Step 2: Cloud Storage Setup

1. **Create Storage Bucket**
   - Go to Storage in Firebase Console
   - Create bucket (if not exists)
   - Choose location (preferably same as Firestore)

2. **Deploy Storage Rules**
   ```bash
   firebase deploy --only storage
   ```
   The rules file is in `storage.rules`

## Step 3: Cloud Functions Setup

1. **Install Firebase CLI** (if not installed)
   ```bash
   npm install -g firebase-tools
   ```

2. **Login to Firebase**
   ```bash
   firebase login
   ```

3. **Initialize Functions** (if not done)
   ```bash
   cd functions
   npm install
   ```

4. **Deploy Functions**
   ```bash
   firebase deploy --only functions
   ```

   This will deploy:
   - `generateTripReport` - Triggered when trip status changes to COMPLETED
   - `markAttendance` - Callable function for marking attendance
   - `startTrip` - Callable function to start a trip
   - `endTrip` - Callable function to end a trip

## Step 4: Android App Setup

1. **Add google-services.json**
   - Download from Firebase Console → Project Settings → Your apps
   - Place in `app/` directory

2. **Update Firebase Configuration**
   - Ensure `google-services.json` is in `app/` directory
   - Build should automatically include it

3. **FCM Setup**
   - The app already includes FCM service
   - Ensure `google-services.json` has correct package name
   - FCM tokens are automatically saved to `fcmTokens` collection

## Step 5: Initial Data Setup

### Create Admin User

1. **Via Firebase Console:**
   - Go to Authentication → Users
   - Add user manually or use email/password
   - Go to Firestore → `users` collection
   - Create document with user's UID
   - Set `role: "ADMIN"`, `name`, `email`, `phone`

2. **Via App:**
   - Register first user
   - Manually update role in Firestore to "ADMIN"

### Create Test Data (Optional)

You can create test routes, buses, and drivers via the app's Admin interface once logged in as admin.

## Step 6: Testing

1. **Test Authentication**
   - Register/Login as different roles (Admin, Driver, Parent)
   - Verify roles are saved correctly

2. **Test Trip Creation** (Admin)
   - Create routes
   - Create buses
   - Create trips with students

3. **Test Attendance** (Driver)
   - Mark attendance for students
   - Start trip
   - End trip

4. **Test PDF Generation**
   - Complete a trip
   - Verify PDF is generated in Storage
   - Verify notification is sent
   - Download PDF from app

5. **Test Notifications**
   - Complete a trip
   - Verify FCM notifications are received
   - Verify in-app notifications appear

## Troubleshooting

### Cloud Functions Not Working
- Check Firebase Console → Functions → Logs
- Ensure billing is enabled (Blaze plan)
- Verify functions are deployed: `firebase functions:list`

### PDF Not Generating
- Check Cloud Functions logs
- Verify Storage bucket exists and rules allow writes
- Check trip status is actually COMPLETED

### Notifications Not Received
- Verify FCM token is saved in `fcmTokens` collection
- Check device notification permissions
- Verify `google-services.json` is correct
- Check Cloud Functions logs for notification errors

### Firestore Permission Denied
- Verify security rules are deployed
- Check user is authenticated
- Verify user role matches required permissions

## Security Checklist

- ✅ Firestore security rules deployed
- ✅ Storage security rules deployed
- ✅ Cloud Functions validate user authentication
- ✅ Role-based access control implemented
- ✅ FCM tokens stored securely
- ✅ User data protected by ownership rules

## Production Checklist

- [ ] Enable Firestore persistence (optional, for offline support)
- [ ] Set up Firebase App Check (recommended)
- [ ] Configure custom domain for Storage (optional)
- [ ] Set up monitoring and alerts
- [ ] Review and optimize Firestore indexes
- [ ] Set up backup strategy
- [ ] Test all features end-to-end
- [ ] Load test Cloud Functions
- [ ] Review security rules with team
- [ ] Document API contracts

## Support

For issues:
1. Check Firebase Console logs
2. Check Android Logcat for app errors
3. Review Cloud Functions logs
4. Verify all services are enabled
5. Check billing status

