// src/controllers/adminController.js

/**
 * Admin-only read APIs to hydrate scheduling UIs.
 * These are intentionally simple lists with the minimal fields needed for selection.
 */
exports.listBuses = async (req, res) => {
	if (req.user.role !== 'ADMIN') return res.status(403).json({ error: 'Forbidden' })
	try {
		const items = await req.prisma.bus.findMany({
			select: { id: true, number_plate: true, bus_name: true, capacity: true, status: true }
		})
		res.json(items)
	} catch (e) {
		console.error('listBuses error', e)
		res.status(500).json({ error: 'Internal server error' })
	}
}

exports.listRoutes = async (req, res) => {
	if (req.user.role !== 'ADMIN') return res.status(403).json({ error: 'Forbidden' })
	try {
		const items = await req.prisma.route.findMany({
			select: { id: true, route_name: true, estimated_time: true }
		})
		res.json(items)
	} catch (e) {
		console.error('listRoutes error', e)
		res.status(500).json({ error: 'Internal server error' })
	}
}

exports.listDrivers = async (req, res) => {
	if (req.user.role !== 'ADMIN') return res.status(403).json({ error: 'Forbidden' })
	try {
		const items = await req.prisma.drivers.findMany({
			select: { id: true, driver_fname: true, driver_lname: true, user: { select: { id: true, name: true, phone: true } } }
		})
		res.json(items)
	} catch (e) {
		console.error('listDrivers error', e)
		res.status(500).json({ error: 'Internal server error' })
	}
}
