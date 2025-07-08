#!/usr/bin/env pwsh

# BTD6 Strategy Guide Collection Script
# Usage: ./collect-strategy-guides-en.ps1

Write-Host "=== BTD6 Strategy Guide Collection Started ===" -ForegroundColor Green
Write-Host "Checking backend server status..." -ForegroundColor Yellow

# Backend server health check
try {
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/api/web-learning/status" -Method Get -TimeoutSec 5
    
    if ($healthCheck.success -eq $true) {
        Write-Host "✅ Backend server is running normally." -ForegroundColor Green
    } else {
        Write-Host "❌ Backend server response has issues." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Cannot connect to backend server." -ForegroundColor Red
    Write-Host "Please ensure backend server is running:" -ForegroundColor Yellow
    Write-Host "cd backend ; ./gradlew bootRun" -ForegroundColor Cyan
    exit 1
}

Write-Host ""
Write-Host "Starting strategy guide collection..." -ForegroundColor Yellow
Write-Host "- Collecting 100+ detailed Bloons TD 6 wiki pages" -ForegroundColor Cyan
Write-Host "- Running in background, takes about 3-5 minutes to complete" -ForegroundColor Cyan
Write-Host ""

# Strategy guide collection API call
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/web-learning/collect-strategy-guides" -Method Post -ContentType "application/json"
    
    if ($response.success -eq $true) {
        Write-Host "✅ Strategy guide collection started successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "📋 Collection Info:" -ForegroundColor White
        Write-Host "- Message: $($response.message)" -ForegroundColor Cyan
        Write-Host "- Description: $($response.description)" -ForegroundColor Cyan
        Write-Host "- Status: $($response.status)" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "📂 Collection Categories:" -ForegroundColor White
        foreach ($category in $response.categories) {
            Write-Host "  • $category" -ForegroundColor Cyan
        }
        Write-Host ""
        Write-Host "🔄 Collection is running in background." -ForegroundColor Yellow
        Write-Host "You can check progress in the backend server console." -ForegroundColor Yellow
        
    } else {
        Write-Host "❌ Failed to start strategy guide collection." -ForegroundColor Red
        Write-Host "Error: $($response.message)" -ForegroundColor Red
        exit 1
    }
    
} catch {
    Write-Host "❌ API call failed." -ForegroundColor Red
    Write-Host "Error details: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== Additional Options ===" -ForegroundColor Green
Write-Host "1. Collect specific topic: POST /api/web-learning/collect-topic?topic=<topic>" -ForegroundColor Cyan
Write-Host "2. Keyword search: POST /api/web-learning/collect-keyword?keyword=<keyword>" -ForegroundColor Cyan
Write-Host "3. Collect all web data: POST /api/web-learning/collect-all" -ForegroundColor Cyan
Write-Host "4. Check status: GET /api/web-learning/status" -ForegroundColor Cyan
Write-Host ""
Write-Host "Complete! 🎉" -ForegroundColor Green 