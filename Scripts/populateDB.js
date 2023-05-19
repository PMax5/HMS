const dbName = "hms"
const conn = new Mongo()

if (!conn.getDBNames().includes(dbName)) {
    const hmsDB = conn.getDB(dbName)
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
        },
        {
            serviceId: 'service_gateways',
            serviceConfig: '{ \"serviceChannels\": { \"service_data\": \"service_data\", \"service_profiler\": \"service_profiler\", \"service_registry\": \"service_registry\" }, \"serverPort\": 8081 }'
        }
    ]);

    hmsDB.routes.insertMany([
        {
            id: 778,
            vehicles: [
                {
                    id: 2488,
                    type: "SHORT_DISTANCE"
                },
                {
                    id: 2482,
                    type: "SHORT_DISTANCE"
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
            slots: [
                2,
                5,
                4
            ]
        },
        {
            id: 750,
            vehicles: [
                {
                    id: 4245,
                    type: "SHORT_DISTANCE"
                },
                {
                    id: 4245,
                    type: "SHORT_DISTANCE"
                }
            ],
            characteristics: [
                "HIGH_TRAFFIC",
                "LARGE_ROADS",
                "CRIMINAL_AREA"
            ],
            shiftTypes: [
                "SHIFT_MORNING",
                "SHIFT_AFTERNOON",
                "SHIFT_NIGHT"
            ],
            slots: [
                10,
                15,
                8
            ]
        },
        {
            id: 720,
            vehicles: [
                {
                    id: 4245,
                    type: "SHORT_DISTANCE"
                },
                {
                    id: 4245,
                    type: "SHORT_DISTANCE"
                }
            ],
            characteristics: [
                "LOW_TRAFFIC",
                "THIN_ROADS",
                "REGULAR_AREA"
            ],
            shiftTypes: [
                "SHIFT_AFTERNOON",
                "SHIFT_NIGHT"
            ],
            slots: [
                1,
                4,
                3
            ]
        }
    ]);
}