const { PrismaClient, grade_enum } = require('@prisma/client');
const bcrypt = require('bcrypt');

const prisma = new PrismaClient();

async function main() {
	// Wipe existing data in the correct order to avoid foreign key constraint errors
	await prisma.trip_attendance_list.deleteMany({});
	await prisma.attendance.deleteMany({});
	await prisma.instructions.deleteMany({});
	await prisma.report.deleteMany({});
	await prisma.trip.deleteMany({});
	await prisma.bus.deleteMany({});
	await prisma.route.deleteMany({});
	await prisma.students.deleteMany({});
	await prisma.parents.deleteMany({});
	await prisma.drivers.deleteMany({});
	await prisma.administrators.deleteMany({});
	await prisma.message.deleteMany({});
	await prisma.users.deleteMany({});

	const saltRounds = 10;
	const testPassword = await bcrypt.hash('password123', saltRounds);

	// Admin user + admin record
	const adminUser = await prisma.users.create({
		data: {
			email: 'admin@schoolbus.com',
			password: testPassword,
			name: 'Admin User',
			role: 'ADMIN',
			phone: '111-222-3333'
		},
	});
	await prisma.administrators.create({
		data: {
			user_id: adminUser.id,
			admin_fname: 'Admin',
			admin_lname: 'User'
		}
	});

	// Driver user + drivers record
	const driverUser = await prisma.users.create({
		data: {
			email: 'driver@schoolbus.com',
			password: testPassword,
			name: 'Driver User',
			role: 'DRIVER',
			phone: '444-555-6666'
		}
	});
	const driver = await prisma.drivers.create({
		data: {
			user_id: driverUser.id,
			driver_fname: 'John',
			driver_lname: 'Doe'
		}
	});

	// Parent user + parents record
	const parentUser = await prisma.users.create({
		data: {
			email: 'parent@schoolbus.com',
			password: testPassword,
			name: 'Parent User',
			role: 'PARENT',
			phone: '777-888-9999'
		},
	});
	const parent = await prisma.parents.create({
		data: {
			user_id: parentUser.id,
			parent_fname: 'Parent',
			parent_lname: 'User',
			address: '123 Main St'
		}
	});

	// Students for the parent
	const student1 = await prisma.students.create({
		data: {
			student_fname: 'Alice',
			student_lname: 'Smith',
			grade: grade_enum["Grade 5"],
			stream: null,
			admission: 1001,
			parent_id: parent.id,
			pickup_latitude: -1.2921, // Example: Nairobi
			pickup_longitude: 36.8219
		}
	});
	const student2 = await prisma.students.create({
		data: {
			student_fname: 'Bob',
			student_lname: 'Johnson',
			grade: grade_enum["Grade 3"],
			stream: null,
			admission: 1002,
			parent_id: parent.id,
			pickup_latitude: -1.3000, // Example: Nearby location
			pickup_longitude: 36.8000
		}
	});

	// Route + Bus
	const route = await prisma.route.create({
		data: {
			route_name: 'Morning Route',
			estimated_time: '45 mins',
			start_location: 'School',
			end_location: 'Neighborhood'
		}
	});
	const bus = await prisma.bus.create({
		data: {
			number_plate: 'KDE-123A',
			status: 'ACTIVE',
			capacity: 40,
			driver_id: driver.id,
			bus_name: 'Blue Bird'
		}
	});

	// Trip scheduled for today, attached to the driver/bus/route
	const today = new Date();
	const trip = await prisma.trip.create({
		data: {
			trip_id: `TRIP-${Date.now()}`,
			trip_date: today,
			bus: { connect: { id: bus.id } },
			route: { connect: { id: route.id } },
			driver: { connect: { id: driver.id } },
			trip_name: 'Morning Pickup',
			status: 'SCHEDULED',
			bus_name: bus.bus_name,
			route_name: route.route_name,
			driver_name: `${driver.driver_fname} ${driver.driver_lname}`
		}
	});

	// Attach students to the trip attendance list
	await prisma.trip_attendance_list.createMany({
		data: [
			{ trip_id: trip.id, student_id: student1.id },
			{ trip_id: trip.id, student_id: student2.id }
		],
		skipDuplicates: true
	});

	console.log('Seed complete:');
	console.log(' Admin  -> admin@schoolbus.com / password123');
	console.log(' Driver -> driver@schoolbus.com / password123');
	console.log(' Parent -> parent@schoolbus.com / password123');
}

main()
	.catch(e => {
		console.error(e);
		process.exit(1);
	})
	.finally(async () => {
		await prisma.$disconnect();
	});