# PowerShell 한글 환경 설정 스크립트
# 이 스크립트를 실행하면 PowerShell에서 한글이 제대로 표시됩니다.

Write-Host "PowerShell 한글 환경 설정을 시작합니다..." -ForegroundColor Green

# 1. 콘솔 인코딩 설정
Write-Host "1. 콘솔 인코딩을 UTF-8로 설정중..." -ForegroundColor Yellow

try {
    [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
    [Console]::InputEncoding = [System.Text.Encoding]::UTF8
    $OutputEncoding = [System.Text.Encoding]::UTF8
    Write-Host "   ✅ 콘솔 인코딩 설정 완료" -ForegroundColor Green
} catch {
    Write-Host "   ❌ 콘솔 인코딩 설정 실패: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. 코드페이지 설정 (Windows)
Write-Host "2. 코드페이지를 UTF-8(65001)로 설정중..." -ForegroundColor Yellow

if ($IsWindows -or $PSVersionTable.PSEdition -eq 'Desktop') {
    try {
        $currentCP = chcp
        Write-Host "   현재 코드페이지: $currentCP" -ForegroundColor Cyan
        
        chcp 65001 > $null
        
        $newCP = chcp
        Write-Host "   새 코드페이지: $newCP" -ForegroundColor Cyan
        Write-Host "   ✅ 코드페이지 설정 완료" -ForegroundColor Green
    } catch {
        Write-Host "   ❌ 코드페이지 설정 실패: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "   ℹ️  Windows가 아닌 환경에서는 코드페이지 설정을 건너뜁니다." -ForegroundColor Cyan
}

# 3. PowerShell 프로파일에 설정 추가 여부 확인
Write-Host "3. PowerShell 프로파일 설정 확인중..." -ForegroundColor Yellow

$profilePath = $PROFILE.CurrentUserCurrentHost
Write-Host "   프로파일 경로: $profilePath" -ForegroundColor Cyan

if (Test-Path $profilePath) {
    $profileContent = Get-Content $profilePath -Raw -ErrorAction SilentlyContinue
    if ($profileContent -like "*UTF8*" -or $profileContent -like "*65001*") {
        Write-Host "   ✅ 프로파일에 이미 UTF-8 설정이 있습니다." -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  프로파일에 UTF-8 설정이 없습니다." -ForegroundColor Yellow
        Write-Host "   프로파일에 UTF-8 설정을 추가하시겠습니까? (Y/N): " -ForegroundColor White -NoNewline
        $response = Read-Host
        
        if ($response -eq 'Y' -or $response -eq 'y') {
            try {
                $utf8Settings = @"

# UTF-8 인코딩 설정 (한글 지원)
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
`$OutputEncoding = [System.Text.Encoding]::UTF8

# Windows에서 UTF-8 코드페이지 설정
if (`$IsWindows -or `$PSVersionTable.PSEdition -eq 'Desktop') {
    try {
        chcp 65001 > `$null
    } catch {
        # 무시
    }
}
"@
                Add-Content -Path $profilePath -Value $utf8Settings
                Write-Host "   ✅ 프로파일에 UTF-8 설정을 추가했습니다." -ForegroundColor Green
                Write-Host "   ℹ️  새로운 PowerShell 세션에서 자동으로 적용됩니다." -ForegroundColor Cyan
            } catch {
                Write-Host "   ❌ 프로파일 수정 실패: $($_.Exception.Message)" -ForegroundColor Red
            }
        } else {
            Write-Host "   ℹ️  프로파일 수정을 건너뜁니다." -ForegroundColor Cyan
        }
    }
} else {
    Write-Host "   ⚠️  PowerShell 프로파일이 존재하지 않습니다." -ForegroundColor Yellow
    Write-Host "   프로파일을 생성하고 UTF-8 설정을 추가하시겠습니까? (Y/N): " -ForegroundColor White -NoNewline
    $response = Read-Host
    
    if ($response -eq 'Y' -or $response -eq 'y') {
        try {
            $profileDir = Split-Path $profilePath -Parent
            if (-not (Test-Path $profileDir)) {
                New-Item -ItemType Directory -Path $profileDir -Force | Out-Null
            }
            
            $utf8Settings = @"
# UTF-8 인코딩 설정 (한글 지원)
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
`$OutputEncoding = [System.Text.Encoding]::UTF8

# Windows에서 UTF-8 코드페이지 설정
if (`$IsWindows -or `$PSVersionTable.PSEdition -eq 'Desktop') {
    try {
        chcp 65001 > `$null
    } catch {
        # 무시
    }
}
"@
            Set-Content -Path $profilePath -Value $utf8Settings
            Write-Host "   ✅ 새 프로파일을 생성하고 UTF-8 설정을 추가했습니다." -ForegroundColor Green
            Write-Host "   ℹ️  새로운 PowerShell 세션에서 자동으로 적용됩니다." -ForegroundColor Cyan
        } catch {
            Write-Host "   ❌ 프로파일 생성 실패: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "   ℹ️  프로파일 생성을 건너뜁니다." -ForegroundColor Cyan
    }
}

# 4. 테스트
Write-Host ""
Write-Host "4. 한글 표시 테스트중..." -ForegroundColor Yellow

$testStrings = @(
    "안녕하세요! PowerShell 한글 테스트입니다.",
    "게임 어드바이저 프로젝트에 오신 것을 환영합니다! 🎮",
    "BloonsTD 6 전략 가이드 수집이 완료되었습니다.",
    "벡터 검색과 AI 조언 기능이 정상 작동합니다! ✅"
)

foreach ($testString in $testStrings) {
    Write-Host "   $testString" -ForegroundColor Cyan
    Start-Sleep -Milliseconds 500
}

Write-Host ""
Write-Host "=== 설정 완료 ===" -ForegroundColor Green
Write-Host "한글 환경 설정이 완료되었습니다!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 적용된 설정:" -ForegroundColor White
Write-Host "  • 콘솔 인코딩: UTF-8" -ForegroundColor Cyan
Write-Host "  • 출력 인코딩: UTF-8" -ForegroundColor Cyan
Write-Host "  • 코드페이지: 65001 (UTF-8)" -ForegroundColor Cyan
Write-Host ""
Write-Host "🚀 다음 단계:" -ForegroundColor White
Write-Host "1. 새로운 PowerShell 창을 열어서 테스트하세요" -ForegroundColor Cyan
Write-Host "2. ./check-collection-results.ps1 스크립트를 실행하세요" -ForegroundColor Cyan
Write-Host "3. 한글이 제대로 표시되는지 확인하세요" -ForegroundColor Cyan
Write-Host ""
Write-Host "문제가 계속 발생하면:" -ForegroundColor White
Write-Host "• PowerShell 폰트를 'Consolas', 'Cascadia Code', 또는 'Malgun Gothic'으로 변경하세요" -ForegroundColor Yellow
Write-Host "• Windows Terminal을 사용하는 것을 권장합니다" -ForegroundColor Yellow 