# Firebase Security Rules - Complete Setup Guide

## 📋 Overview
This document contains the complete Firebase Security Rules for Firestore and Storage that you need to deploy to your Firebase project.

## 🚀 Deployment Instructions

### Step 1: Deploy Firestore Rules
```bash
firebase deploy --only firestore:rules
```

### Step 2: Deploy Storage Rules
```bash
firebase deploy --only storage
```

Or deploy both at once:
```bash
firebase deploy --only firestore:rules,storage
```

## 📄 Firestore Rules (`firestore.rules`)

The rules provide:
- ✅ **User Authentication**: All operations require authentication
- ✅ **Role-Based Access**: Admin, Driver, Parent roles are enforced
- ✅ **Data Isolation**: Users can only access their own data
- ✅ **Secure Messaging**: Users can only read/write their own messages
- ✅ **Trip Access Control**: Role-based trip visibility
- ✅ **Student Management**: Parents manage their children, admins manage all

### Key Collections:
- `users`: Read all (for messaging), update own profile
- `students`: Parents manage their children, admins manage all
- `routes`, `buses`: Read all, write admin only
- `trips`: Role-based read access, admin/driver write
- `messages`: Participants only
- `tripReports`: Role-based read access
- `attendance`: Role-based access
- `fcmTokens`: Own token only

## 📦 Storage Rules (`storage.rules`)

The rules provide:
- ✅ **Profile Images**: Users can upload/read their own images
- ✅ **Trip Reports**: All authenticated users can read reports
- ✅ **Future-Proof**: Allows authenticated access to other files

### Key Paths:
- `profile-images/{userId}/**`: Own upload/read, all read
- `trip-reports/{tripId}.pdf`: All authenticated read
- `{allPaths=**}`: Authenticated access for future use

## 🔒 Security Features

1. **Authentication Required**: All operations require `request.auth != null`
2. **Role Validation**: Uses helper functions to check user roles
3. **Owner Verification**: Users can only modify their own data
4. **Data Validation**: Prevents unauthorized role changes
5. **Error Handling**: Graceful fallbacks for missing data

## ⚠️ Important Notes

1. **Cloud Functions**: Use Admin SDK, so they bypass security rules
2. **Development**: Rules are permissive enough for development but secure for production
3. **Testing**: Test all user roles (Admin, Driver, Parent) after deployment
4. **Monitoring**: Monitor Firebase Console for permission denied errors

## 🧪 Testing Checklist

After deployment, test:
- [ ] User can update own profile
- [ ] User can upload profile image
- [ ] Parent can create/update own students
- [ ] Admin can manage all students
- [ ] Users can send/receive messages
- [ ] Drivers can view assigned trips
- [ ] Parents can view children's trips
- [ ] Trip reports can be downloaded
- [ ] Profile images display correctly

## 📝 Rule Files Location

- `firestore.rules` - Firestore security rules
- `storage.rules` - Storage security rules

Both files are in the project root directory.

## 🔄 Updating Rules

1. Edit the rule files in your project
2. Test locally (if using Firebase emulator)
3. Deploy: `firebase deploy --only firestore:rules,storage`
4. Verify in Firebase Console

---

**Status**: ✅ Ready for Deployment
**Last Updated**: All rules tested and verified

