#!/bin/bash

FRONT50=http://spin-front50:8080
FIAT=http://spin-fiat:7003

# Create a service account using Front50
curl -X POST -H "Content-type: application/json" -d '{ "name": "spinnakertestsuite", "memberOf": ["EXECGRP"] }'  $FRONT50/serviceAccounts

# Retrieve service accounts from Front50
curl $FRONT50/serviceAccounts

# Trigger role synchronization using Fiat
curl -X POST $FIAT/roles/sync
