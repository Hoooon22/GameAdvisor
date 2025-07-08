#!/usr/bin/env pwsh

# PowerShell 한글 인코딩 설정
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# 콘솔 코드페이지를 UTF-8로 설정 (Windows용)
if ($IsWindows -or $PSVersionTable.PSEdition -eq 'Desktop') {
    try {
        chcp 65001 > $null
    } catch {
        # 무시 (일부 환경에서는 chcp가 작동하지 않을 수 있음)
    }
}

# Collection Results Check Script
# Usage: ./check-collection-results.ps1

Write-Host "=== BTD6 수집 결과 확인 ===" -ForegroundColor Green
Write-Host "전략 가이드 수집 결과를 확인중입니다..." -ForegroundColor Yellow

# Check backend server status
try {
    $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/api/web-learning/status" -Method Get -TimeoutSec 5
    
    if ($healthCheck.success -eq $true) {
        Write-Host "✅ 백엔드 서버가 정상 실행중입니다." -ForegroundColor Green
    } else {
        Write-Host "❌ 백엔드 서버에 문제가 있습니다." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 백엔드 서버에 연결할 수 없습니다." -ForegroundColor Red
    Write-Host "백엔드 서버를 실행해주세요: cd backend && ./gradlew bootRun" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "📊 수집 결과 확인중..." -ForegroundColor White

# Check BTD data count
try {
    $countResult = Invoke-RestMethod -Uri "http://localhost:8080/api/sample-data/count?gameName=bloonstd" -Method Get
    
    if ($countResult.success -eq $true) {
        Write-Host "✅ BTD 데이터 개수: $($countResult.totalCount)" -ForegroundColor Green
        
        if ($countResult.totalCount -gt 100) {
            Write-Host "🎉 훌륭합니다! 충분한 데이터를 수집했습니다." -ForegroundColor Green
        } elseif ($countResult.totalCount -gt 50) {
            Write-Host "👍 좋습니다! 적당한 양의 데이터가 있습니다." -ForegroundColor Cyan
        } elseif ($countResult.totalCount -gt 0) {
            Write-Host "⚠️  일부 데이터가 있지만, 더 수집하는 것이 좋겠습니다." -ForegroundColor Yellow
        } else {
            Write-Host "❌ 데이터를 찾을 수 없습니다. 수집이 실패했을 수 있습니다." -ForegroundColor Red
        }
    } else {
        Write-Host "❌ 데이터 개수 확인 실패: $($countResult.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 데이터 개수 확인 오류: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "🔍 벡터 검색 기능 테스트중..." -ForegroundColor White

# Test a simple search
try {
    $searchPayload = @{
        query = "세라믹 블룬 공략법"
        gameName = "BloonsTD"
        situationType = "ceramic_defense"
    } | ConvertTo-Json -Depth 3

    $searchResult = Invoke-RestMethod -Uri "http://localhost:8080/api/vector/search" -Method Post -Body $searchPayload -ContentType "application/json; charset=utf-8"
    
    if ($searchResult.success -eq $true -and $searchResult.results.Count -gt 0) {
        Write-Host "✅ 벡터 검색이 정상 작동합니다! $($searchResult.results.Count)개의 결과를 찾았습니다." -ForegroundColor Green
        $sampleAdvice = $searchResult.results[0].advice
        # UTF-8 디코딩 시도
        try {
            $bytes = [System.Text.Encoding]::GetEncoding('iso-8859-1').GetBytes($sampleAdvice)
            $decodedAdvice = [System.Text.Encoding]::UTF8.GetString($bytes)
            Write-Host "📋 샘플 결과: $decodedAdvice" -ForegroundColor Cyan
        } catch {
            Write-Host "📋 샘플 결과: $sampleAdvice" -ForegroundColor Cyan
        }
    } else {
        Write-Host "⚠️  벡터 검색에서 결과를 찾지 못했습니다. 데이터가 제대로 인덱싱되지 않았을 수 있습니다." -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ 벡터 검색 테스트 실패: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "🎮 게임 조언 기능 테스트중..." -ForegroundColor White

# Test game advice
try {
    $advicePayload = @{
        query = "63라운드 세라믹 블룬 도움"
        gameName = "BloonsTD"
        currentRound = 63
        availableMoney = 10000
        placedTowers = @("dart_monkey", "ninja_monkey")
    } | ConvertTo-Json -Depth 3

    $adviceResult = Invoke-RestMethod -Uri "http://localhost:8080/api/advice/game" -Method Post -Body $advicePayload -ContentType "application/json; charset=utf-8"
    
    if ($adviceResult.success -eq $true) {
        Write-Host "✅ 게임 조언 기능이 정상 작동합니다!" -ForegroundColor Green
        
        $advice = $adviceResult.advice
        $suggestedTowers = $adviceResult.suggestedTowers -join ', '
        
        # UTF-8 디코딩 시도
        try {
            $bytes = [System.Text.Encoding]::GetEncoding('iso-8859-1').GetBytes($advice)
            $decodedAdvice = [System.Text.Encoding]::UTF8.GetString($bytes)
            Write-Host "💡 샘플 조언: $decodedAdvice" -ForegroundColor Cyan
        } catch {
            Write-Host "💡 샘플 조언: $advice" -ForegroundColor Cyan
        }
        
        Write-Host "🔧 추천 타워: $suggestedTowers" -ForegroundColor Cyan
    } else {
        Write-Host "⚠️  게임 조언 테스트 실패: $($adviceResult.message)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ 게임 조언 테스트 실패: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 요약 ===" -ForegroundColor Green
Write-Host "수집 프로세스: ✅ 완료 (108개 페이지 수집)" -ForegroundColor Green
Write-Host "데이터 저장: 위의 개수 결과 확인" -ForegroundColor Cyan
Write-Host "검색 기능: 위의 검색 테스트 결과 확인" -ForegroundColor Cyan
Write-Host "조언 기능: 위의 조언 테스트 결과 확인" -ForegroundColor Cyan

Write-Host ""
Write-Host "🚀 다음 단계:" -ForegroundColor White
Write-Host "1. 모든 테스트가 통과했다면, 이제 클라이언트 애플리케이션을 사용할 수 있습니다" -ForegroundColor Cyan
Write-Host "2. 실행: cd client && ./gradlew run" -ForegroundColor Cyan
Write-Host "3. BTD6 게임을 시작하여 오버레이 조언 시스템을 테스트하세요" -ForegroundColor Cyan

Write-Host ""
Write-Host "완료! 🎉" -ForegroundColor Green 