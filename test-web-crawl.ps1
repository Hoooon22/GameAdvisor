# 웹 크롤링 테스트 스크립트
param(
    [string]$Url = "",
    [string]$Category = "테스트"
)

# 색상 설정
$SuccessColor = "Green"
$ErrorColor = "Red"
$InfoColor = "Cyan"
$WarningColor = "Yellow"

Write-Host "============================================" -ForegroundColor $InfoColor
Write-Host "     웹 크롤링 테스트" -ForegroundColor $InfoColor
Write-Host "============================================" -ForegroundColor $InfoColor
Write-Host ""

if ([string]::IsNullOrWhiteSpace($Url)) {
    Write-Host "사용법: .\test-web-crawl.ps1 -Url 'URL주소' -Category '카테고리'" -ForegroundColor $InfoColor
    Write-Host ""
    Write-Host "예시:" -ForegroundColor $InfoColor
    Write-Host "  .\test-web-crawl.ps1 -Url 'https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EC%98%81%EC%9B%85' -Category '영웅'" -ForegroundColor White
    exit 1
}

Write-Host "📋 테스트 정보:" -ForegroundColor $InfoColor
Write-Host "  URL: $Url" -ForegroundColor White
Write-Host "  카테고리: $Category" -ForegroundColor White
Write-Host ""

# 서버 상태 확인
Write-Host "🔍 백엔드 서버 상태 확인 중..." -ForegroundColor $InfoColor
try {
    $statusResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/web-learning/status" -Method Get -TimeoutSec 5
    Write-Host "✅ 백엔드 서버 연결 성공!" -ForegroundColor $SuccessColor
    Write-Host "  메시지: $($statusResponse.message)" -ForegroundColor White
    Write-Host ""
}
catch {
    Write-Host "❌ 백엔드 서버에 연결할 수 없습니다!" -ForegroundColor $ErrorColor
    Write-Host "  오류: $($_.Exception.Message)" -ForegroundColor $ErrorColor
    Write-Host ""
    Write-Host "💡 해결 방법:" -ForegroundColor $WarningColor
    Write-Host "  1. 새 PowerShell 창에서 백엔드 서버를 실행하세요:" -ForegroundColor White
    Write-Host "     cd backend" -ForegroundColor White
    Write-Host "     .\gradlew bootRun" -ForegroundColor White
    Write-Host "  2. 서버가 완전히 시작될 때까지 기다리세요 (약 30초-1분)" -ForegroundColor White
    Write-Host "  3. 다시 이 스크립트를 실행하세요" -ForegroundColor White
    exit 1
}

# URL 크롤링 시도
Write-Host "🕷️ URL 크롤링 시작..." -ForegroundColor $InfoColor
try {
    $baseUri = "http://localhost:8080/api/web-learning/collect-url"
    $body = @{
        url = $Url
        category = $Category
    }
    
    Write-Host "  요청 URL: $baseUri" -ForegroundColor White
    Write-Host "  대상 페이지: $Url" -ForegroundColor White
    Write-Host "  카테고리: $Category" -ForegroundColor White
    Write-Host ""
    
    $response = Invoke-RestMethod -Uri $baseUri -Method Post -Body $body -TimeoutSec 30
    
    if ($response.success) {
        Write-Host "✅ 크롤링 요청 성공!" -ForegroundColor $SuccessColor
        Write-Host "  메시지: $($response.message)" -ForegroundColor White
        Write-Host "  처리된 URL: $($response.url)" -ForegroundColor White
        Write-Host "  카테고리: $($response.category)" -ForegroundColor White
        Write-Host "  상태: $($response.status)" -ForegroundColor $WarningColor
        Write-Host ""
        Write-Host "💡 백그라운드에서 크롤링이 진행됩니다. 잠시 기다려주세요." -ForegroundColor $WarningColor
        
        # 잠시 후 결과 확인
        Write-Host "⏱️ 5초 후 처리 상태를 다시 확인합니다..." -ForegroundColor $InfoColor
        Start-Sleep 5
        
        try {
            $statusCheck = Invoke-RestMethod -Uri "http://localhost:8080/api/web-learning/status" -Method Get
            Write-Host "📊 현재 서비스 상태: $($statusCheck.message)" -ForegroundColor $InfoColor
        }
        catch {
            Write-Host "⚠️ 상태 확인 실패, 하지만 크롤링은 진행 중일 수 있습니다." -ForegroundColor $WarningColor
        }
        
    }
    else {
        Write-Host "❌ 크롤링 요청 실패!" -ForegroundColor $ErrorColor
        Write-Host "  메시지: $($response.message)" -ForegroundColor $ErrorColor
        if ($response.error) {
            Write-Host "  오류: $($response.error)" -ForegroundColor $ErrorColor
        }
    }
}
catch {
    Write-Host "❌ 크롤링 요청 중 오류 발생!" -ForegroundColor $ErrorColor
    Write-Host "  오류: $($_.Exception.Message)" -ForegroundColor $ErrorColor
    Write-Host ""
    Write-Host "💡 가능한 원인:" -ForegroundColor $WarningColor
    Write-Host "  1. URL이 접근 불가능하거나 차단됨" -ForegroundColor White
    Write-Host "  2. 네트워크 연결 문제" -ForegroundColor White
    Write-Host "  3. 서버 내부 오류" -ForegroundColor White
    Write-Host "  4. 나무위키의 크롤링 방지 정책" -ForegroundColor White
    Write-Host ""
    Write-Host "🔧 시도해볼 수 있는 해결책:" -ForegroundColor $WarningColor
    Write-Host "  - 다른 URL로 테스트해보기 (예: https://bloons.fandom.com/wiki/Strategies)" -ForegroundColor White
    Write-Host "  - 몇 분 후 다시 시도하기" -ForegroundColor White
}

Write-Host ""
Write-Host "테스트 완료!" -ForegroundColor $SuccessColor 