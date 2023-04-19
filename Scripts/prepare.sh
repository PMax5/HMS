#!/bin/bash
echo "Health Management System"
echo "Initializing HMS tests..."

echo "Populating DB"
mongo populateDB.js

echo "Connecting to Hyperledger server (SSH Tunnel)"
ssh -L 7051:localhost:7051 -L 9051:localhost:9051 -L 7054:localhost:7054 -L 8054:localhost:8054 -L 9054:localhost:9054 -L 7050:localhost:7050 ghostrider
