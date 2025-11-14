
// src/controllers/studentController.js

/**
 * ===============================================
 *   School Bus Transport - Student Controller
 * ===============================================
 * 
 * Handles student management.
 * - Parents can register their children
 * - Admins can manage all students
 * - Drivers can view students on their trips
 */

/**
 * Creates a new student. Parents can register their children, admins can create any student.
 */
exports.createStudent = async (req, res) => {
    const { studentFname, studentLname, admission, grade, stream, parentId } = req.body;
    const prisma = req.prisma;
    const user = req.user;

    if (!studentFname || !studentLname || !admission) {
        return res.status(400).json({ error: 'studentFname, studentLname, and admission are required' });
    }

    try {
        let finalParentId = parentId;

        // If user is a parent, they can only register students for themselves
        if (user.role === 'PARENT') {
            const parent = await prisma.parents.findUnique({
                where: { user_id: user.userId }
            });

            if (!parent) {
                return res.status(403).json({ error: 'Forbidden: Parent record not found' });
            }

            finalParentId = parent.id;
        } else if (user.role !== 'ADMIN') {
            return res.status(403).json({ error: 'Forbidden: Only parents and admins can create students' });
        }

        // Check if admission number already exists
        const existingStudent = await prisma.students.findUnique({
            where: { admission: parseInt(admission) }
        });

        if (existingStudent) {
            return res.status(409).json({ error: 'Student with this admission number already exists' });
        }

        // Create student
        const student = await prisma.students.create({
            data: {
                student_fname: studentFname,
                student_lname: studentLname,
                admission: parseInt(admission),
                grade: grade || null,
                stream: stream || null,
                parent_id: finalParentId || null
            },
            include: {
                parent: {
                    include: {
                        user: {
                            select: {
                                id: true,
                                name: true,
                                email: true,
                                phone: true
                            }
                        }
                    }
                }
            }
        });

        res.status(201).json(student);
    } catch (error) {
        console.error('Error creating student:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Gets students. Admins see all, parents see only their children, drivers see students on their trips.
 */
exports.getStudents = async (req, res) => {
    const prisma = req.prisma;
    const user = req.user;

    try {
        let students;

        if (user.role === 'ADMIN') {
            // Admins see all students
            students = await prisma.students.findMany({
                include: {
                    parent: {
                        include: {
                            user: {
                                select: {
                                    id: true,
                                    name: true,
                                    email: true,
                                    phone: true
                                }
                            }
                        }
                    }
                },
                orderBy: {
                    student_fname: 'asc'
                }
            });
        } else if (user.role === 'PARENT') {
            // Parents see only their children
            const parent = await prisma.parents.findUnique({
                where: { user_id: user.userId }
            });

            if (!parent) {
                return res.status(403).json({ error: 'Forbidden: Parent record not found' });
            }

            students = await prisma.students.findMany({
                where: {
                    parent_id: parent.id
                },
                include: {
                    parent: {
                        include: {
                            user: {
                                select: {
                                    id: true,
                                    name: true,
                                    email: true,
                                    phone: true
                                }
                            }
                        }
                    }
                },
                orderBy: {
                    student_fname: 'asc'
                }
            });
        } else if (user.role === 'DRIVER') {
            // Drivers see students on their active trips
            const activeTrips = await prisma.trip.findMany({
                where: {
                    driver: {
                        user_id: user.userId
                    },
                    status: {
                        in: ['IN_PROGRESS', 'SCHEDULED']
                    }
                },
                include: {
                    trip_attendance_list: {
                        include: {
                            student: {
                                include: {
                                    parent: {
                                        include: {
                                            user: {
                                                select: {
                                                    id: true,
                                                    name: true,
                                                    email: true,
                                                    phone: true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });

            // Extract unique students
            const studentMap = new Map();
            activeTrips.forEach(trip => {
                trip.trip_attendance_list.forEach(item => {
                    if (!studentMap.has(item.student.id)) {
                        studentMap.set(item.student.id, item.student);
                    }
                });
            });

            students = Array.from(studentMap.values());
        } else {
            return res.status(403).json({ error: 'Forbidden' });
        }

        res.status(200).json(students);
    } catch (error) {
        console.error('Error fetching students:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Gets a single student by ID.
 */
exports.getStudentById = async (req, res) => {
    const { id } = req.params;
    const prisma = req.prisma;
    const user = req.user;

    try {
        const studentIdInt = parseInt(id);
        if (isNaN(studentIdInt)) {
            return res.status(400).json({ error: 'Invalid student id' });
        }

        const student = await prisma.students.findUnique({
            where: { id: studentIdInt },
            include: {
                parent: {
                    include: {
                        user: {
                            select: {
                                id: true,
                                name: true,
                                email: true,
                                phone: true
                            }
                        }
                    }
                }
            }
        });

        if (!student) {
            return res.status(404).json({ error: 'Student not found' });
        }

        // Authorization check
        if (user.role === 'PARENT') {
            const parent = await prisma.parents.findUnique({
                where: { user_id: user.userId }
            });

            if (!parent || student.parent_id !== parent.id) {
                return res.status(403).json({ error: 'Forbidden: You can only view your own children' });
            }
        } else if (user.role === 'DRIVER') {
            // Check if student is on driver's active trip
            const activeTrip = await prisma.trip.findFirst({
                where: {
                    driver: {
                        user_id: user.userId
                    },
                    status: {
                        in: ['IN_PROGRESS', 'SCHEDULED']
                    },
                    trip_attendance_list: {
                        some: {
                            student_id: studentIdInt
                        }
                    }
                }
            });

            if (!activeTrip) {
                return res.status(403).json({ error: 'Forbidden: Student is not on your active trip' });
            }
        }
        // ADMIN can view any student

        res.status(200).json(student);
    } catch (error) {
        console.error('Error fetching student:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

/**
 * Updates a student. Only admins and the student's parent can update.
 */
exports.updateStudent = async (req, res) => {
    const { id } = req.params;
    const { studentFname, studentLname, grade, stream } = req.body;
    const prisma = req.prisma;
    const user = req.user;

    try {
        const studentIdInt = parseInt(id);
        if (isNaN(studentIdInt)) {
            return res.status(400).json({ error: 'Invalid student id' });
        }

        const student = await prisma.students.findUnique({
            where: { id: studentIdInt }
        });

        if (!student) {
            return res.status(404).json({ error: 'Student not found' });
        }

        // Authorization check
        if (user.role === 'PARENT') {
            const parent = await prisma.parents.findUnique({
                where: { user_id: user.userId }
            });

            if (!parent || student.parent_id !== parent.id) {
                return res.status(403).json({ error: 'Forbidden: You can only update your own children' });
            }
        } else if (user.role !== 'ADMIN') {
            return res.status(403).json({ error: 'Forbidden: Only admins and parents can update students' });
        }

        // Update student
        const updatedStudent = await prisma.students.update({
            where: { id: studentIdInt },
            data: {
                student_fname: studentFname || student.student_fname,
                student_lname: studentLname || student.student_lname,
                grade: grade !== undefined ? grade : student.grade,
                stream: stream !== undefined ? stream : student.stream
            },
            include: {
                parent: {
                    include: {
                        user: {
                            select: {
                                id: true,
                                name: true,
                                email: true,
                                phone: true
                            }
                        }
                    }
                }
            }
        });

        res.status(200).json(updatedStudent);
    } catch (error) {
        console.error('Error updating student:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};

