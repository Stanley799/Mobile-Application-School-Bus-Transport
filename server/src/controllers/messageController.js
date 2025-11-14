/**
 * Gets all available recipients for the authenticated user (role-based).
 */
exports.getAvailableRecipients = async (req, res) => {
    const prisma = req.prisma;
    const user = req.user;
    try {
        let recipients = [];
        if (user.role === 'ADMIN') {
            // Admins can message anyone except themselves
            recipients = await prisma.users.findMany({
                where: { id: { not: user.userId } },
                select: { id: true, name: true, role: true }
            });
        } else if (user.role === 'DRIVER') {
            // Drivers: admins and parents of students on their trips
            const admins = await prisma.users.findMany({ where: { role: 'ADMIN' }, select: { id: true, name: true, role: true } });
            // Find parent IDs from trips
            const trips = await prisma.trip.findMany({ where: { driver: { user_id: user.userId } }, include: { trip_attendance_list: { include: { student: true } } } });
            const parentIds = [...new Set(trips.flatMap(trip => trip.trip_attendance_list.map(a => a.student.parent_id).filter(Boolean)))];
            const parents = await prisma.users.findMany({ where: { role: 'PARENT', parents: { id: { in: parentIds } } }, select: { id: true, name: true, role: true } });
            recipients = [...admins, ...parents].filter(r => r.id !== user.userId);
        } else if (user.role === 'PARENT') {
            // Parents: admins and drivers of trips their children are on
            const admins = await prisma.users.findMany({ where: { role: 'ADMIN' }, select: { id: true, name: true, role: true } });
            const parentRecord = await prisma.parents.findUnique({ where: { user_id: user.userId } });
            let driverIds = [];
            if (parentRecord) {
                const trips = await prisma.trip.findMany({ where: { trip_attendance_list: { some: { student: { parent_id: parentRecord.id } } } }, include: { driver: true } });
                driverIds = [...new Set(trips.map(trip => trip.driver.user_id).filter(Boolean))];
            }
            const drivers = await prisma.users.findMany({ where: { role: 'DRIVER', drivers: { id: { in: driverIds } } }, select: { id: true, name: true, role: true } });
            recipients = [...admins, ...drivers].filter(r => r.id !== user.userId);
        }
        res.status(200).json(recipients);
    } catch (error) {
        console.error('Error fetching recipients:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

// src/controllers/messageController.js

/**
 * ===============================================
 *   School Bus Transport - Message Controller
 * ===============================================
 * 
 * Handles messaging between users.
 * Role-based messaging rules:
 * - ADMIN: Can message anyone
 * - DRIVER: Can message admins and parents of students on their current trips
 * - PARENT: Can message admins and drivers of trips their children are on
 */

/**
 * Sends a message from the authenticated user to another user.
 */
exports.sendMessage = async (req, res) => {
    const { receiverId, content, type } = req.body;
    const prisma = req.prisma;
    const user = req.user;

    if (!receiverId || !content) {
        return res.status(400).json({ error: 'receiverId and content are required' });
    }

    try {
        const receiverIdInt = parseInt(receiverId);
        if (isNaN(receiverIdInt)) {
            return res.status(400).json({ error: 'Invalid receiver id' });
        }

        // Check if receiver exists
        const receiver = await prisma.users.findUnique({
            where: { id: receiverIdInt },
            include: {
                administrators: true,
                drivers: true,
                parents: true
            }
        });
        if (!receiver) {
            return res.status(404).json({ error: 'Receiver not found' });
        }

        // Determine message type and enforce role logic
        let messageType = type;
        if (!messageType) {
            // Default: notification for admin/driver, feedback for parent after trip, chat otherwise
            if (user.role === 'ADMIN' || user.role === 'DRIVER') {
                messageType = 'notification';
            } else if (user.role === 'PARENT') {
                messageType = 'feedback';
            } else {
                messageType = 'chat';
            }
        }

        // Only admin/driver can send notification messages
        if (messageType === 'notification') {
            if (!(user.role === 'ADMIN' || user.role === 'DRIVER')) {
                return res.status(403).json({ error: 'Only admin and driver can send notification messages' });
            }
        }
        // Only parent can send feedback, and only after trip (enforce as needed)
        // (Add trip status check here if feedback is tied to trip)

        // Role-based authorization check (existing logic)
        if (user.role === 'ADMIN') {
            // Admins can message anyone
        } else if (user.role === 'DRIVER') {
            if (receiver.role === 'ADMIN') {
                // Can message admin
            } else if (receiver.role === 'PARENT') {
                const parentRecord = await prisma.parents.findUnique({ where: { user_id: receiverIdInt } });
                if (!parentRecord) {
                    return res.status(404).json({ error: 'Parent record not found' });
                }
                const activeTrip = await prisma.trip.findFirst({
                    where: {
                        driver: { user_id: user.userId },
                        status: 'IN_PROGRESS',
                        trip_attendance_list: { some: { student: { parent_id: parentRecord.id } } }
                    }
                });
                if (!activeTrip) {
                    const recentTrip = await prisma.trip.findFirst({
                        where: {
                            driver: { user_id: user.userId },
                            trip_date: { gte: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000) },
                            trip_attendance_list: { some: { student: { parent_id: parentRecord.id } } }
                        }
                    });
                    if (!recentTrip) {
                        return res.status(403).json({ error: 'Forbidden: You can only message parents of students on your trips' });
                    }
                }
            } else {
                return res.status(403).json({ error: 'Forbidden: Drivers can only message admins and parents' });
            }
        } else if (user.role === 'PARENT') {
            if (receiver.role === 'ADMIN') {
                // Can message admin
            } else if (receiver.role === 'DRIVER') {
                const parentRecord = await prisma.parents.findUnique({ where: { user_id: user.userId } });
                if (!parentRecord) {
                    return res.status(403).json({ error: 'Forbidden: Parent record not found' });
                }
                const activeTrip = await prisma.trip.findFirst({
                    where: {
                        driver: { user_id: receiverIdInt },
                        status: { in: ['IN_PROGRESS', 'SCHEDULED'] },
                        trip_attendance_list: { some: { student: { parent_id: parentRecord.id } } }
                    }
                });
                if (!activeTrip) {
                    const recentTrip = await prisma.trip.findFirst({
                        where: {
                            driver: { user_id: receiverIdInt },
                            trip_date: { gte: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000) },
                            trip_attendance_list: { some: { student: { parent_id: parentRecord.id } } }
                        }
                    });
                    if (!recentTrip) {
                        return res.status(403).json({ error: 'Forbidden: You can only message drivers of trips your children are on' });
                    }
                }
            } else {
                return res.status(403).json({ error: 'Forbidden: Parents can only message admins and drivers' });
            }
        }

        // Create message with type
        const message = await prisma.message.create({
            data: {
                sender_id: user.userId,
                receiver_id: receiverIdInt,
                content: content,
                type: messageType
            },
            include: {
                sender: { select: { id: true, name: true, role: true } },
                receiver: { select: { id: true, name: true, role: true } }
            }
        });

        // Emit real-time message to both sender and receiver if connected
        try {
            if (req.io) {
                req.io.to(`user-${user.userId}`).emit('message-broadcast', message);
                req.io.to(`user-${receiverIdInt}`).emit('message-broadcast', message);
            }
        } catch (e) {
            console.error('Socket emit error:', e);
        }
        res.status(201).json(message);
    } catch (error) {
        console.error('Error sending message:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Gets all conversations for the authenticated user.
 */
exports.getConversations = async (req, res) => {
    const prisma = req.prisma;
    const user = req.user;

    try {
        // Get all messages where user is sender or receiver
        const messages = await prisma.message.findMany({
            where: {
                OR: [
                    { sender_id: user.userId },
                    { receiver_id: user.userId }
                ]
            },
            include: {
                sender: {
                    select: {
                        id: true,
                        name: true,
                        role: true
                    }
                },
                receiver: {
                    select: {
                        id: true,
                        name: true,
                        role: true
                    }
                }
            },
            orderBy: {
                timestamp: 'desc'
            }
        });

        // Group by conversation (other user)
        const conversations = {};
        messages.forEach(msg => {
            const otherUserId = msg.sender_id === user.userId ? msg.receiver_id : msg.sender_id;
            const otherUser = msg.sender_id === user.userId ? msg.receiver : msg.sender;

            if (!conversations[otherUserId]) {
                conversations[otherUserId] = {
                    userId: otherUserId,
                    userName: otherUser.name,
                    userRole: otherUser.role,
                    lastMessage: msg.content,
                    lastMessageTime: msg.timestamp,
                    unreadCount: 0 // Could be enhanced with read receipts
                };
            } else {
                // Update if this is a newer message
                if (msg.timestamp > conversations[otherUserId].lastMessageTime) {
                    conversations[otherUserId].lastMessage = msg.content;
                    conversations[otherUserId].lastMessageTime = msg.timestamp;
                }
            }
        });

        res.status(200).json(Object.values(conversations));
    } catch (error) {
        console.error('Error fetching conversations:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Gets all messages in a conversation with another user.
 */
exports.getMessages = async (req, res) => {
    const { userId } = req.params;
    const prisma = req.prisma;
    const user = req.user;

    try {
        const otherUserIdInt = parseInt(userId);
        if (isNaN(otherUserIdInt)) {
            return res.status(400).json({ error: 'Invalid user id' });
        }

        // Verify authorization (user must be part of this conversation)
        const messages = await prisma.message.findMany({
            where: {
                OR: [
                    { sender_id: user.userId, receiver_id: otherUserIdInt },
                    { sender_id: otherUserIdInt, receiver_id: user.userId }
                ]
            },
            include: {
                sender: {
                    select: {
                        id: true,
                        name: true,
                        role: true
                    }
                },
                receiver: {
                    select: {
                        id: true,
                        name: true,
                        role: true
                    }
                }
            },
            orderBy: {
                timestamp: 'asc'
            }
        });

        res.status(200).json(messages);
    } catch (error) {
        console.error('Error fetching messages:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

