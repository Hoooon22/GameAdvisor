#!/usr/bin/env pwsh

# BTD6 위키 목차별 전략 가이드 수집 스크립트
# 사용법: ./collect-strategy-guides.ps1 -Url "https://bloons.fandom.com/wiki/Bloons_TD_6"

param(
    [string]$Url = "https://bloons.fandom.com/wiki/Bloons_TD_6",
    [string]$Category = "BloonsTD",
    [bool]$ResetFirst = $true
)

Write-Host "=== BTD6 위키 목차별 전략 가이드 수집 시작 ===" -ForegroundColor Green
Write-Host "백엔드 서버 상태를 확인하고 있습니다..." -ForegroundColor Yellow

# 백엔드 서버 상태 확인
try {
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/api/web-learning/status" -Method Get -TimeoutSec 5
    if ($healthCheck.success -eq $true) {
        Write-Host "✅ 백엔드 서버가 정상적으로 실행 중입니다." -ForegroundColor Green
    } else {
        Write-Host "❌ 백엔드 서버 응답에 문제가 있습니다." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 백엔드 서버에 연결할 수 없습니다." -ForegroundColor Red
    Write-Host "백엔드 서버가 실행 중인지 확인하세요:" -ForegroundColor Yellow
    Write-Host "cd backend && ./gradlew bootRun" -ForegroundColor Cyan
    exit 1
}

Write-Host ""
Write-Host "전략 가이드 수집을 시작합니다..." -ForegroundColor Yellow
Write-Host "- 대상 URL: $Url" -ForegroundColor Cyan
Write-Host "- 카테고리: $Category" -ForegroundColor Cyan
Write-Host ""

# 벡터 DB 학습 API 호출 (목차별 세분화 저장)
try {
    $encodedUrl = [System.Web.HttpUtility]::UrlEncode($Url)
    $encodedCategory = [System.Web.HttpUtility]::UrlEncode($Category)
    $uri = "http://localhost:8080/api/web-learning/learn-single-url?url=$encodedUrl&category=$encodedCategory&resetFirst=$ResetFirst"
    $response = Invoke-RestMethod -Uri $uri -Method Post

    if ($response.success -eq $true) {
        Write-Host "✅ 벡터 DB 학습이 성공적으로 시작되었습니다!" -ForegroundColor Green
        Write-Host "- 메시지: $($response.message)" -ForegroundColor Cyan
        Write-Host "- 설명: $($response.description)" -ForegroundColor Cyan
        Write-Host "- 상태: $($response.status)" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "🔄 백엔드 서버 콘솔에서 진행 상황을 확인할 수 있습니다." -ForegroundColor Yellow
    } else {
        Write-Host "❌ 벡터 DB 학습 시작에 실패했습니다." -ForegroundColor Red
        Write-Host "오류: $($response.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ API 호출 중 오류가 발생했습니다." -ForegroundColor Red
    Write-Host "오류 세부사항: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== 추가 작업 옵션 ===" -ForegroundColor Green
Write-Host "1. 다른 위키 URL로 수집: ./collect-strategy-guides.ps1 -Url <위키URL>" -ForegroundColor Cyan
Write-Host "2. 전체 웹 수집: POST /api/web-learning/collect-all" -ForegroundColor Cyan
Write-Host "3. 상태 확인: GET /api/web-learning/status" -ForegroundColor Cyan
Write-Host ""
Write-Host "완료! 🎉" -ForegroundColor Green 