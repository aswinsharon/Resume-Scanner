#!/bin/bash

# Debug Environment Variables Script
echo "=== Job Board Platform - Environment Debug ==="
echo ""

# Check if .env file exists
if [ -f ".env" ]; then
    echo "✅ .env file found"
    echo "📄 .env file contents:"
    echo "----------------------------------------"
    cat .env
    echo "----------------------------------------"
else
    echo "❌ .env file not found!"
    echo "💡 Copy .env.sample to .env:"
    echo "   cp .env.sample .env"
    exit 1
fi

echo ""
echo "=== Environment Variable Check ==="

# Check key environment variables
check_var() {
    local var_name=$1
    local var_value=$(grep "^$var_name=" .env | cut -d'=' -f2)
    
    if [ -n "$var_value" ]; then
        echo "✅ $var_name = $var_value"
    else
        echo "❌ $var_name is not set"
    fi
}

echo "Database Configuration:"
check_var "POSTGRES_DB"
check_var "POSTGRES_USER" 
check_var "POSTGRES_PASSWORD"

echo ""
echo "Spring Database Configuration:"
check_var "SPRING_DATASOURCE_URL"
check_var "SPRING_DATASOURCE_USERNAME"
check_var "SPRING_DATASOURCE_PASSWORD"

echo ""
echo "JWT Configuration:"
check_var "JWT_SECRET"
check_var "JWT_EXPIRATION"

echo ""
echo "Application Configuration:"
check_var "SPRING_PROFILES_ACTIVE"
check_var "APP_PORT"

echo ""
echo "=== Docker Compose Validation ==="

# Test docker-compose config
if command -v docker-compose &> /dev/null; then
    echo "Testing docker-compose configuration..."
    docker-compose config > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "✅ docker-compose.yml is valid"
    else
        echo "❌ docker-compose.yml has errors:"
        docker-compose config
    fi
else
    echo "⚠️  docker-compose not found, skipping validation"
fi

echo ""
echo "=== Recommendations ==="
echo "1. Ensure all required environment variables are set in .env"
echo "2. Check that SPRING_DATASOURCE_* variables match POSTGRES_* variables"
echo "3. Verify JWT_SECRET is at least 32 characters long"
echo "4. Run: docker-compose up -d to start the application"
echo "5. Check logs: docker-compose logs -f app"

echo ""
echo "=== Quick Start Commands ==="
echo "# Start the application:"
echo "docker-compose up -d"
echo ""
echo "# Check application logs:"
echo "docker-compose logs -f app"
echo ""
echo "# Check database logs:"
echo "docker-compose logs -f postgres"
echo ""
echo "# Stop the application:"
echo "docker-compose down"