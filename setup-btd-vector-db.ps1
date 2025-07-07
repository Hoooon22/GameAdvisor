# BTD 벡터 DB 구축 및 테스트 스크립트
Write-Host "=== BTD 벡터 DB 구축 시작 ===" -ForegroundColor Green

$BASE_URL = "http://localhost:8080/api/btd-vector"

# 1단계: BTD 벡터 DB 통계 확인
Write-Host "`n1. 현재 BTD 벡터 DB 상태 확인..." -ForegroundColor Yellow
try {
    $statsResponse = Invoke-RestMethod -Uri "$BASE_URL/stats" -Method GET -ContentType "application/json"
    Write-Host "현재 BTD 지식 개수: $($statsResponse.totalKnowledgeCount)" -ForegroundColor Cyan
    Write-Host "지원 게임: $($statsResponse.gameSupported)" -ForegroundColor Cyan
    Write-Host "검색 기능: $($statsResponse.searchCapabilities -join ', ')" -ForegroundColor Cyan
} catch {
    Write-Host "통계 조회 실패: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "백엔드 서버가 실행 중인지 확인하세요 (http://localhost:8080)" -ForegroundColor Yellow
    exit 1
}

# 2단계: 기본 샘플 데이터 생성
Write-Host "`n2. BTD 기본 샘플 지식 데이터 생성..." -ForegroundColor Yellow
try {
    $sampleDataResponse = Invoke-RestMethod -Uri "$BASE_URL/sample-data" -Method POST -ContentType "application/json"
    Write-Host "기본 샘플 데이터 생성 완료!" -ForegroundColor Green
    Write-Host "이전 데이터 개수: $($sampleDataResponse.beforeCount)" -ForegroundColor Cyan
    Write-Host "현재 데이터 개수: $($sampleDataResponse.afterCount)" -ForegroundColor Cyan
    Write-Host "추가된 데이터: $($sampleDataResponse.addedCount)" -ForegroundColor Cyan
} catch {
    Write-Host "기본 샘플 데이터 생성 실패: $($_.Exception.Message)" -ForegroundColor Red
}

# 2-1단계: 고급 전략 데이터 생성
Write-Host "`n2-1. BTD 고급 전략 데이터 생성..." -ForegroundColor Yellow
try {
    $advancedDataResponse = Invoke-RestMethod -Uri "$BASE_URL/advanced-data" -Method POST -ContentType "application/json"
    Write-Host "고급 전략 데이터 생성 완료!" -ForegroundColor Green
    Write-Host "이전 데이터 개수: $($advancedDataResponse.beforeCount)" -ForegroundColor Cyan
    Write-Host "현재 데이터 개수: $($advancedDataResponse.afterCount)" -ForegroundColor Cyan
    Write-Host "추가된 데이터: $($advancedDataResponse.addedCount)" -ForegroundColor Cyan
} catch {
    Write-Host "고급 전략 데이터 생성 실패: $($_.Exception.Message)" -ForegroundColor Red
}

# 2-2단계: 상황별 맞춤 데이터 생성
Write-Host "`n2-2. BTD 상황별 맞춤 데이터 생성..." -ForegroundColor Yellow
try {
    $situationDataResponse = Invoke-RestMethod -Uri "$BASE_URL/situation-data" -Method POST -ContentType "application/json"
    Write-Host "상황별 맞춤 데이터 생성 완료!" -ForegroundColor Green
    Write-Host "이전 데이터 개수: $($situationDataResponse.beforeCount)" -ForegroundColor Cyan
    Write-Host "현재 데이터 개수: $($situationDataResponse.afterCount)" -ForegroundColor Cyan
    Write-Host "추가된 데이터: $($situationDataResponse.addedCount)" -ForegroundColor Cyan
} catch {
    Write-Host "상황별 맞춤 데이터 생성 실패: $($_.Exception.Message)" -ForegroundColor Red
}

# 3단계: 벡터 검색 테스트
Write-Host "`n3. BTD 지식 검색 테스트..." -ForegroundColor Yellow

$testScenarios = @(
    @{
        situation = "Round 65 ceramic bloons are too hard to defend"
        expected = "Ceramic Defense Strategy"
    },
    @{
        situation = "Round 80 MOAB is too strong to pop"
        expected = "MOAB Attack Strategy" 
    },
    @{
        situation = "Camo bloons are invisible and passing through"
        expected = "Camo Detection Strategy"
    },
    @{
        situation = "Lead bloons cannot be popped"
        expected = "Lead Popping Strategy"
    },
    @{
        situation = "Not enough money in early game to build towers"
        expected = "Economy Building Strategy"
    },
    @{
        situation = "Round 63 ceramic rush is too difficult"
        expected = "Round 63 Specialized Strategy"
    },
    @{
        situation = "Cannot defeat the first BAD in round 80"
        expected = "BAD Attack Strategy"
    },
    @{
        situation = "Which towers to use on water maps"
        expected = "Water Map Strategy"
    },
    @{
        situation = "How to use ninja and alchemist combo"
        expected = "Ninja-Alchemist Combo"
    },
    @{
        situation = "Boss bloons are too strong to win"
        expected = "Boss Strategy"
    }
)

foreach ($scenario in $testScenarios) {
    Write-Host "`n--- 테스트 시나리오: $($scenario.expected) ---" -ForegroundColor Magenta
    Write-Host "상황: $($scenario.situation)" -ForegroundColor White
    
    try {
        $searchRequest = @{
            situation = $scenario.situation
            limit = "3"
        } | ConvertTo-Json -Depth 10
        
        $searchResponse = Invoke-RestMethod -Uri "$BASE_URL/search" -Method POST -Body $searchRequest -ContentType "application/json"
        
        Write-Host "검색 결과: $($searchResponse.resultCount)개" -ForegroundColor Green
        
        if ($searchResponse.resultCount -gt 0) {
            for ($i = 0; $i -lt [Math]::Min($searchResponse.resultCount, 2); $i++) {
                $result = $searchResponse.results[$i]
                $knowledge = $result.knowledge
                Write-Host "  [$($i+1)] $($knowledge.title)" -ForegroundColor Cyan
                Write-Host "      유사도: $([Math]::Round($result.similarity, 3))" -ForegroundColor Gray
                Write-Host "      조언: $($knowledge.advice.Substring(0, [Math]::Min($knowledge.advice.Length, 80)))..." -ForegroundColor Gray
            }
        } else {
            Write-Host "  검색 결과가 없습니다." -ForegroundColor Red
        }
        
        Start-Sleep -Seconds 1
        
    } catch {
        Write-Host "검색 실패: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 4단계: 최종 통계 확인
Write-Host "`n4. 최종 BTD 벡터 DB 상태 확인..." -ForegroundColor Yellow
try {
    $finalStatsResponse = Invoke-RestMethod -Uri "$BASE_URL/stats" -Method GET -ContentType "application/json"
    Write-Host "최종 BTD 지식 개수: $($finalStatsResponse.totalKnowledgeCount)" -ForegroundColor Green
} catch {
    Write-Host "최종 통계 조회 실패: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== BTD 벡터 DB 구축 완료 ===" -ForegroundColor Green
Write-Host "`n사용 가능한 API 엔드포인트:" -ForegroundColor Yellow
Write-Host "- GET  $BASE_URL/stats              : 통계 조회" -ForegroundColor Cyan
Write-Host "- POST $BASE_URL/sample-data        : 기본 샘플 데이터 생성" -ForegroundColor Cyan
Write-Host "- POST $BASE_URL/advanced-data      : 고급 전략 데이터 생성" -ForegroundColor Cyan
Write-Host "- POST $BASE_URL/situation-data     : 상황별 맞춤 데이터 생성" -ForegroundColor Cyan
Write-Host "- POST $BASE_URL/search             : 지식 검색" -ForegroundColor Cyan
Write-Host "- POST $BASE_URL/knowledge          : 새 지식 추가" -ForegroundColor Cyan
Write-Host "- POST $BASE_URL/knowledge/{id}/use : 사용량 증가" -ForegroundColor Cyan
Write-Host "- DELETE $BASE_URL/all              : 모든 데이터 삭제" -ForegroundColor Cyan

Write-Host "`n검색 예시:" -ForegroundColor Yellow
@"
{
  "situation": "라운드 100에서 BAD를 못 잡겠어요",
  "limit": "5"
}
"@ | Write-Host -ForegroundColor Gray 