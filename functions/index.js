const functions = require('firebase-functions');
const admin = require('firebase-admin');
const PDFDocument = require('pdfkit');
const { Storage } = require('@google-cloud/storage');

admin.initializeApp();
const db = admin.firestore();
const storage = new Storage();

/**
 * Generate PDF report when a trip is completed
 * Triggered by Firestore trigger on trips collection
 */
exports.generateTripReport = functions.firestore
  .document('trips/{tripId}')
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();
    const tripId = context.params.tripId;

    // Only generate report when trip status changes to COMPLETED
    if (before.status !== 'COMPLETED' && after.status === 'COMPLETED') {
      try {
        // Fetch trip details
        const trip = after;
        
        // Fetch route details
        const routeDoc = await db.collection('routes').doc(trip.routeId).get();
        const route = routeDoc.exists ? routeDoc.data() : { name: 'Unknown Route' };
        
        // Fetch bus details
        const busDoc = await db.collection('buses').doc(trip.busId).get();
        const bus = busDoc.exists ? busDoc.data() : { name: 'Unknown Bus', licensePlate: 'N/A' };
        
        // Fetch driver details
        const driverDoc = await db.collection('users').doc(trip.driverId).get();
        const driver = driverDoc.exists ? driverDoc.data() : { name: 'Unknown Driver' };
        
        // Fetch all students for this trip
        const studentsSnapshot = await db.collection('students')
          .where(admin.firestore.FieldPath.documentId(), 'in', trip.studentIds || [])
          .get();
        const students = studentsSnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        
        // Fetch attendance records
        const attendanceSnapshot = await db.collection('attendance')
          .where('tripId', '==', tripId)
          .get();
        const attendanceMap = {};
        attendanceSnapshot.forEach(doc => {
          const att = doc.data();
          attendanceMap[att.studentId] = att.isPresent;
        });
        
        // Generate PDF
        const doc = new PDFDocument({ margin: 50 });
        const chunks = [];
        
        doc.on('data', chunk => chunks.push(chunk));
        doc.on('end', async () => {
          const pdfBuffer = Buffer.concat(chunks);
          
          // Upload to Firebase Storage
          const bucket = storage.bucket();
          const fileName = `trip-reports/${tripId}.pdf`;
          const file = bucket.file(fileName);
          
          await file.save(pdfBuffer, {
            metadata: {
              contentType: 'application/pdf',
            },
          });
          
          // Make file publicly readable
          await file.makePublic();
          
          // Get public URL
          const downloadUrl = `https://storage.googleapis.com/${bucket.name}/${fileName}`;
          
          // Save report metadata to Firestore
          await db.collection('tripReports').add({
            tripId: tripId,
            pdfUrl: `gs://${bucket.name}/${fileName}`,
            downloadUrl: downloadUrl,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            generatedAt: admin.firestore.FieldValue.serverTimestamp()
          });
          
          // Send notifications to relevant users
          await sendTripReportNotifications(tripId, trip, students, downloadUrl);
        });
        
        // Write PDF content
        doc.fontSize(20).text('Trip Report', { align: 'center' });
        doc.moveDown();
        
        doc.fontSize(14).text(`Trip Name: ${trip.tripName || 'N/A'}`);
        doc.text(`Route: ${route.name}`);
        doc.text(`Bus: ${bus.name} (${bus.licensePlate})`);
        doc.text(`Driver: ${driver.name}`);
        doc.text(`Grade: ${trip.grade}`);
        doc.moveDown();
        
        if (trip.startTime) {
          doc.text(`Start Time: ${trip.startTime.toDate().toLocaleString()}`);
        }
        if (trip.endTime) {
          doc.text(`End Time: ${trip.endTime.toDate().toLocaleString()}`);
        }
        doc.moveDown();
        
        doc.fontSize(16).text('Attendance', { underline: true });
        doc.moveDown();
        
        // Attendance table
        let y = doc.y;
        doc.fontSize(12);
        doc.text('Student Name', 50, y);
        doc.text('Status', 300, y);
        doc.moveDown(0.5);
        doc.moveTo(50, doc.y).lineTo(500, doc.y).stroke();
        doc.moveDown(0.3);
        
        students.forEach(student => {
          const isPresent = attendanceMap[student.id] || false;
          doc.text(student.name, 50);
          doc.text(isPresent ? 'Present' : 'Absent', 300);
          doc.moveDown(0.4);
        });
        
        doc.end();
        
        return null;
      } catch (error) {
        console.error('Error generating trip report:', error);
        throw error;
      }
    }
    
    return null;
  });

/**
 * Send FCM notifications when trip report is ready
 */
async function sendTripReportNotifications(tripId, trip, students, downloadUrl) {
  const messaging = admin.messaging();
  
  // Get parent IDs from students
  const parentIds = [...new Set(students.map(s => s.parentId).filter(Boolean))];
  
  // Send to parents
  for (const parentId of parentIds) {
    const parentDoc = await db.collection('users').doc(parentId).get();
    if (parentDoc.exists) {
      const parent = parentDoc.data();
      // Get FCM token from user document or a separate tokens collection
      const tokenDoc = await db.collection('fcmTokens').doc(parentId).get();
      if (tokenDoc.exists) {
        const token = tokenDoc.data().token;
        try {
          await messaging.send({
            token: token,
            notification: {
              title: 'Trip Report Ready',
              body: `Trip report for ${trip.tripName || 'your child\'s trip'} is ready for download.`
            },
            data: {
              type: 'TRIP_REPORT_READY',
              tripId: tripId,
              downloadUrl: downloadUrl
            }
          });
          
          // Also create in-app notification
          await db.collection('notifications').add({
            userId: parentId,
            title: 'Trip Report Ready',
            body: `Trip report for ${trip.tripName || 'your child\'s trip'} is ready for download.`,
            type: 'TRIP_REPORT_READY',
            tripId: tripId,
            read: false,
            createdAt: admin.firestore.FieldValue.serverTimestamp()
          });
        } catch (error) {
          console.error(`Error sending notification to parent ${parentId}:`, error);
        }
      }
    }
  }
  
  // Send to driver
  const driverTokenDoc = await db.collection('fcmTokens').doc(trip.driverId).get();
  if (driverTokenDoc.exists) {
    const driverToken = driverTokenDoc.data().token;
    try {
      await messaging.send({
        token: driverToken,
        notification: {
          title: 'Trip Completed',
          body: `Trip report for ${trip.tripName} is ready.`
        },
        data: {
          type: 'TRIP_REPORT_READY',
          tripId: tripId,
          downloadUrl: downloadUrl
        }
      });
      
      await db.collection('notifications').add({
        userId: trip.driverId,
        title: 'Trip Completed',
        body: `Trip report for ${trip.tripName} is ready.`,
        type: 'TRIP_REPORT_READY',
        tripId: tripId,
        read: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      });
    } catch (error) {
      console.error(`Error sending notification to driver:`, error);
    }
  }
  
  // Send to all admins
  const adminsSnapshot = await db.collection('users')
    .where('role', '==', 'ADMIN')
    .get();
  
  for (const adminDoc of adminsSnapshot.docs) {
    const adminId = adminDoc.id;
    const adminTokenDoc = await db.collection('fcmTokens').doc(adminId).get();
    if (adminTokenDoc.exists) {
      const adminToken = adminTokenDoc.data().token;
      try {
        await messaging.send({
          token: adminToken,
          notification: {
            title: 'New Trip Report',
            body: `Trip report for ${trip.tripName} is available.`
          },
          data: {
            type: 'TRIP_REPORT_READY',
            tripId: tripId,
            downloadUrl: downloadUrl
          }
        });
        
        await db.collection('notifications').add({
          userId: adminId,
          title: 'New Trip Report',
          body: `Trip report for ${trip.tripName} is available.`,
          type: 'TRIP_REPORT_READY',
          tripId: tripId,
          read: false,
          createdAt: admin.firestore.FieldValue.serverTimestamp()
        });
      } catch (error) {
        console.error(`Error sending notification to admin ${adminId}:`, error);
      }
    }
  }
}

/**
 * Callable function to mark attendance
 */
exports.markAttendance = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }
  
  const { tripId, studentId, isPresent } = data;
  
  if (!tripId || !studentId || typeof isPresent !== 'boolean') {
    throw new functions.https.HttpsError('invalid-argument', 'Missing required fields');
  }
  
  try {
    // Verify user is a driver and assigned to this trip
    const userDoc = await db.collection('users').doc(context.auth.uid).get();
    const user = userDoc.data();
    
    if (user.role !== 'DRIVER') {
      throw new functions.https.HttpsError('permission-denied', 'Only drivers can mark attendance');
    }
    
    const tripDoc = await db.collection('trips').doc(tripId).get();
    if (!tripDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Trip not found');
    }
    
    const trip = tripDoc.data();
    if (trip.driverId !== context.auth.uid) {
      throw new functions.https.HttpsError('permission-denied', 'Driver not assigned to this trip');
    }
    
    // Save or update attendance
    const attendanceQuery = await db.collection('attendance')
      .where('tripId', '==', tripId)
      .where('studentId', '==', studentId)
      .limit(1)
      .get();
    
    if (attendanceQuery.empty) {
      await db.collection('attendance').add({
        tripId: tripId,
        studentId: studentId,
        isPresent: isPresent,
        markedAt: admin.firestore.FieldValue.serverTimestamp(),
        markedBy: context.auth.uid
      });
    } else {
      await attendanceQuery.docs[0].ref.update({
        isPresent: isPresent,
        markedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    }
    
    return { success: true };
  } catch (error) {
    console.error('Error marking attendance:', error);
    throw new functions.https.HttpsError('internal', error.message);
  }
});

/**
 * Callable function to start a trip
 */
exports.startTrip = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }
  
  const { tripId } = data;
  
  if (!tripId) {
    throw new functions.https.HttpsError('invalid-argument', 'Trip ID required');
  }
  
  try {
    const userDoc = await db.collection('users').doc(context.auth.uid).get();
    const user = userDoc.data();
    
    if (user.role !== 'DRIVER') {
      throw new functions.https.HttpsError('permission-denied', 'Only drivers can start trips');
    }
    
    const tripRef = db.collection('trips').doc(tripId);
    const tripDoc = await tripRef.get();
    
    if (!tripDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Trip not found');
    }
    
    const trip = tripDoc.data();
    if (trip.driverId !== context.auth.uid) {
      throw new functions.https.HttpsError('permission-denied', 'Driver not assigned to this trip');
    }
    
    if (trip.status !== 'SCHEDULED') {
      throw new functions.https.HttpsError('failed-precondition', 'Trip must be scheduled to start');
    }
    
    await tripRef.update({
      status: 'IN_PROGRESS',
      startTime: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    return { success: true };
  } catch (error) {
    console.error('Error starting trip:', error);
    throw new functions.https.HttpsError('internal', error.message);
  }
});

/**
 * Callable function to end a trip
 */
exports.endTrip = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }
  
  const { tripId } = data;
  
  if (!tripId) {
    throw new functions.https.HttpsError('invalid-argument', 'Trip ID required');
  }
  
  try {
    const userDoc = await db.collection('users').doc(context.auth.uid).get();
    const user = userDoc.data();
    
    if (user.role !== 'DRIVER') {
      throw new functions.https.HttpsError('permission-denied', 'Only drivers can end trips');
    }
    
    const tripRef = db.collection('trips').doc(tripId);
    const tripDoc = await tripRef.get();
    
    if (!tripDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Trip not found');
    }
    
    const trip = tripDoc.data();
    if (trip.driverId !== context.auth.uid) {
      throw new functions.https.HttpsError('permission-denied', 'Driver not assigned to this trip');
    }
    
    if (trip.status !== 'IN_PROGRESS') {
      throw new functions.https.HttpsError('failed-precondition', 'Trip must be in progress to end');
    }
    
    await tripRef.update({
      status: 'COMPLETED',
      endTime: admin.firestore.FieldValue.serverTimestamp(),
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    
    // PDF generation will be triggered by the onUpdate trigger
    
    return { success: true };
  } catch (error) {
    console.error('Error ending trip:', error);
    throw new functions.https.HttpsError('internal', error.message);
  }
});

