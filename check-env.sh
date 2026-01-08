#!/bin/bash

echo "=== Environment Variables Debug ==="
echo ""

echo "Current environment variables:"
echo "SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL}"
echo "SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME}"
echo "SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}"
echo "JWT_SECRET: ${JWT_SECRET}"
echo ""

echo "Docker Compose environment substitution test:"
docker-compose config | grep -A 10 -B 5 SPRING_DATASOURCE

echo ""
echo "=== Recommendations ==="
echo "1. Use docker-compose.simple.yml for testing:"
echo "   docker-compose -f docker-compose.simple.yml up -d"
echo ""
echo "2. Check if .env file is being loaded:"
echo "   docker-compose --env-file .env config"
echo ""
echo "3. Verify environment variables in container:"
echo "   docker exec jobboard-app env | grep SPRING"