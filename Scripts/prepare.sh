#!/bin/bash
echo "Health Management System"
echo "Initializing HMS tests..."

echo "Populating DB"
mongo populateDB.js

echo "Connecting to Hyperledger server (SSH Tunnel)"
ssh -L 7051:localhost:7051 -L 9051:localhost:9051 -L 7054:localhost:7054 -L 8054:localhost:8054 -L 9054:localhost:9054 -L 7050:localhost:7050 -L 8080:localhost:8080 -L 27017:localhost:27017 -L 5672:localhost:5672 ghostrider

#echo "Populating profiles"
#source /root/fabric-samples/test-network/setEnv.sh
#peer chaincode invoke -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --tls --cafile "/root/fabric-samples/test-network/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" -C profiles -n profiler --peerAddresses localhost:7051 --tlsRootCertFiles "/root/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" --peerAddresses localhost:9051 --tlsRootCertFiles "/root/fabric-samples/test-network/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" -c '{ "Args": ["CreateProfile", "60587a9b-3e16-4a38-89d1-47ed4d883e99", "{\"id\": \"60587a9b-3e16-4a38-89d1-47ed4d883e99\", \"ageRange\": [30, 50], \"gender\": \"MALE\", \"shiftHoursRage\": [6, 12], \"shiftTypes\": [\"SHIFT_MORNING\"], \"routeIds\": [778, 750]}"]}'
