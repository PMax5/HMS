const conn = new Mongo()
const hmsDB = conn.getDB('hms')

hmsDB.configs.insertMany([
    {
        serviceId: 'service_data',
        serviceConfig: '{ \"hyperledgerUserId\": \"dataUser\" }'
    },
    {
        serviceId: 'service_profiler',
        serviceConfig: '{ \"hyperledgerUserId\": \"profilerUser\", \"minHealthyBPM\": 80, \"maxHealthyBPM\": 120, \"maxDrowsiness\": 60, \"maxProblematicShifts\": 3 }'
    },
    {
        serviceId: 'service_registry',
        serviceConfig: '{ \"userAffiliation\": \"org1.department1\", \"mspId\": \"Org1MSP\", \"userIds\": [\"dataUser\", \"profilerUser\"] }'
    }
]);

hmsDB.routes.insertMany([
    {
        id: 778,
        vehicles: [
            {

            }
        ],
        characteristics: [
            "LOW_TRAFFIC",
            "LARGE_ROADS",
            "REGULAR_AREA"
        ],
        shiftTypes: [
            "SHIFT_MORNING",
            "SHIFT_AFTERNOON",
            "SHIFT_NIGHT"
        ],
        slots: 4
    }
]);