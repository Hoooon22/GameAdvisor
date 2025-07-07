#!/usr/bin/env pwsh
# BTD Vector DB Web Learning Script
# Collect BTD-related materials from the internet and learn them into vector DB.

param(
    [string]$Mode = "help",
    [string]$Topic = "",
    [string]$Keyword = "",
    [string]$Url = "",
    [string]$Category = "",
    [string[]]$Urls = @(),
    [int]$MaxDepth = 2,
    [int]$MaxPages = 10
)

# Color settings
$SuccessColor = "Green"
$ErrorColor = "Red"
$InfoColor = "Cyan"
$WarningColor = "Yellow"

# API Base URL
$BaseUrl = "http://localhost:8080/api/web-learning"

# Show title
function Show-Title {
    Write-Host "============================================" -ForegroundColor $InfoColor
    Write-Host "     BTD Vector DB Web Learning System" -ForegroundColor $InfoColor
    Write-Host "============================================" -ForegroundColor $InfoColor
    Write-Host ""
}

# Show help
function Show-Help {
    Write-Host "Usage:" -ForegroundColor $InfoColor
    Write-Host "  .\start-web-learning.ps1 [Options]" -ForegroundColor White
    Write-Host ""
    Write-Host "Options:" -ForegroundColor $InfoColor
    Write-Host "  -Mode all          : Collect all BTD related web materials" -ForegroundColor White
    Write-Host "  -Mode topic        : Collect specific topic materials (-Topic required)" -ForegroundColor White
    Write-Host "  -Mode keyword      : Collect keyword materials (-Keyword required)" -ForegroundColor White
    Write-Host "  -Mode url          : Collect from specific URL (-Url, -Category optional)" -ForegroundColor White
    Write-Host "  -Mode urls         : Collect from multiple URLs (-Urls, -Category optional)" -ForegroundColor White
    Write-Host "  -Mode deep         : Site depth crawling (collect sub URLs automatically)" -ForegroundColor White
    Write-Host "  -Mode strategy-guides : Collect all BTD6 strategy guides from Namu Wiki" -ForegroundColor White
    Write-Host "  -Mode status       : Check web learning service status" -ForegroundColor White
    Write-Host "  -Mode help         : Show this help" -ForegroundColor White
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor $InfoColor
    Write-Host "  .\start-web-learning.ps1 -Mode all" -ForegroundColor White
    Write-Host "  .\start-web-learning.ps1 -Mode topic -Topic 'ceramic defense'" -ForegroundColor White
    Write-Host "  .\start-web-learning.ps1 -Mode keyword -Keyword 'MOAB'" -ForegroundColor White
    Write-Host "  .\start-web-learning.ps1 -Mode url -Url 'https://bloons.fandom.com/wiki/Strategies' -Category 'BTD Strategy'" -ForegroundColor White
    Write-Host "  .\start-web-learning.ps1 -Mode deep -Url 'https://bloons.fandom.com' -Category 'BTD Wiki' -MaxDepth 3 -MaxPages 20" -ForegroundColor White
    Write-Host "  .\start-web-learning.ps1 -Mode strategy-guides" -ForegroundColor White
    Write-Host "  .\start-web-learning.ps1 -Mode status" -ForegroundColor White
    Write-Host ""
    Write-Host "Deep crawling options (for deep mode):" -ForegroundColor $InfoColor
    Write-Host "  -MaxDepth         : Maximum crawling depth (default: 2, max: 5)" -ForegroundColor White
    Write-Host "  -MaxPages         : Maximum pages to collect (default: 10, max: 50)" -ForegroundColor White
    Write-Host ""
    Write-Host "Recommended topics:" -ForegroundColor $InfoColor
    Write-Host "  - ceramic defense" -ForegroundColor White
    Write-Host "  - MOAB strategy" -ForegroundColor White
    Write-Host "  - camo detection" -ForegroundColor White
    Write-Host "  - boss battle" -ForegroundColor White
    Write-Host "  - tower combinations" -ForegroundColor White
    Write-Host "  - economy build" -ForegroundColor White
    Write-Host "  - late game strategy" -ForegroundColor White
    Write-Host "  - hero abilities" -ForegroundColor White
    Write-Host ""
    Write-Host "Recommended URLs:" -ForegroundColor $InfoColor
    Write-Host "  - https://bloons.fandom.com/wiki/Strategies" -ForegroundColor White
    Write-Host "  - https://www.reddit.com/r/btd6" -ForegroundColor White
    Write-Host "  - https://steamcommunity.com/app/960090/guides" -ForegroundColor White
    Write-Host "  - https://gamepress.gg/bloons-td-6" -ForegroundColor White
    Write-Host ""
    Write-Host "Notice:" -ForegroundColor $WarningColor
    Write-Host "  - Deep mode may take a long time as it collects many pages" -ForegroundColor $WarningColor
    Write-Host "  - Collect at appropriate intervals to avoid burdening the target site" -ForegroundColor $WarningColor
    Write-Host "  - Only collect pages within the same domain" -ForegroundColor $WarningColor
}

# Test server status
function Test-ServerStatus {
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/status" -Method Get -TimeoutSec 5
        return $true
    }
    catch {
        return $false
    }
}

# Collect all web materials
function Start-AllCollection {
    Write-Host "Starting to collect all BTD related web materials..." -ForegroundColor $InfoColor
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/collect-all" -Method Post
        
        if ($response.success) {
            Write-Host "Success: " -ForegroundColor $SuccessColor -NoNewline
            Write-Host $response.message -ForegroundColor $SuccessColor
            Write-Host "Status: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.status -ForegroundColor $WarningColor
            Write-Host ""
            Write-Host "This task runs in the background and may take a few minutes." -ForegroundColor $WarningColor
        }
        else {
            Write-Host "Error: " -ForegroundColor $ErrorColor -NoNewline
            Write-Host $response.message -ForegroundColor $ErrorColor
        }
    }
    catch {
        Write-Host "Failed to request web material collection: $($_.Exception.Message)" -ForegroundColor $ErrorColor
    }
}

# Collect topic-specific web materials
function Start-TopicCollection {
    param([string]$TopicName)
    
    if ([string]::IsNullOrWhiteSpace($TopicName)) {
        Write-Host "Error: Topic not specified. Please use -Topic parameter." -ForegroundColor $ErrorColor
        return
    }
    
    Write-Host "Starting to collect web materials for topic '$TopicName'..." -ForegroundColor $InfoColor
    
    try {
        $uri = "$BaseUrl/collect-topic?topic=" + [Uri]::EscapeDataString($TopicName)
        $response = Invoke-RestMethod -Uri $uri -Method Post
        
        if ($response.success) {
            Write-Host "Success: " -ForegroundColor $SuccessColor -NoNewline
            Write-Host $response.message -ForegroundColor $SuccessColor
            Write-Host "Topic: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.topic -ForegroundColor White
            Write-Host "Status: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.status -ForegroundColor $WarningColor
        }
        else {
            Write-Host "Error: " -ForegroundColor $ErrorColor -NoNewline
            Write-Host $response.message -ForegroundColor $ErrorColor
        }
    }
    catch {
        Write-Host "Failed to request topic-specific web material collection: $($_.Exception.Message)" -ForegroundColor $ErrorColor
    }
}

# Collect keyword-specific web materials
function Start-KeywordCollection {
    param([string]$KeywordText)
    
    if ([string]::IsNullOrWhiteSpace($KeywordText)) {
        Write-Host "Error: Keyword not specified. Please use -Keyword parameter." -ForegroundColor $ErrorColor
        return
    }
    
    Write-Host "Starting to collect web materials for keyword '$KeywordText'..." -ForegroundColor $InfoColor
    
    try {
        $uri = "$BaseUrl/collect-keyword?keyword=" + [Uri]::EscapeDataString($KeywordText)
        $response = Invoke-RestMethod -Uri $uri -Method Post
        
        if ($response.success) {
            Write-Host "Success: " -ForegroundColor $SuccessColor -NoNewline
            Write-Host $response.message -ForegroundColor $SuccessColor
            Write-Host "Keyword: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.keyword -ForegroundColor White
            Write-Host "Enhanced keyword: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.enhanced_keyword -ForegroundColor White
            Write-Host "Status: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.status -ForegroundColor $WarningColor
        }
        else {
            Write-Host "Error: " -ForegroundColor $ErrorColor -NoNewline
            Write-Host $response.message -ForegroundColor $ErrorColor
        }
    }
    catch {
        Write-Host "Failed to request keyword-specific web material collection: $($_.Exception.Message)" -ForegroundColor $ErrorColor
    }
}

# Collect web materials from single URL
function Start-UrlCollection {
    param(
        [string]$Url,
        [string]$Category
    )
    
    if ([string]::IsNullOrWhiteSpace($Url)) {
        Write-Host "Error: URL not specified. Please use -Url parameter." -ForegroundColor $ErrorColor
        return
    }
    
    # Set default category if not provided
    if ([string]::IsNullOrWhiteSpace($Category)) {
        $Category = "User Defined"
    }
    
    Write-Host "Starting to collect web materials from URL '$Url'..." -ForegroundColor $InfoColor
    Write-Host "Category: $Category" -ForegroundColor $InfoColor
    
    try {
        $uri = "$BaseUrl/collect-url?url=" + [Uri]::EscapeDataString($Url) + "&category=" + [Uri]::EscapeDataString($Category)
        $response = Invoke-RestMethod -Uri $uri -Method Post
        
        if ($response.success) {
            Write-Host "Success: " -ForegroundColor $SuccessColor -NoNewline
            Write-Host $response.message -ForegroundColor $SuccessColor
            Write-Host "URL: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.url -ForegroundColor White
            Write-Host "Category: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.category -ForegroundColor White
            Write-Host "Status: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.status -ForegroundColor $WarningColor
            Write-Host ""
            Write-Host "This task runs in the background and may take a few seconds." -ForegroundColor $WarningColor
        }
        else {
            Write-Host "Error: " -ForegroundColor $ErrorColor -NoNewline
            Write-Host $response.message -ForegroundColor $ErrorColor
        }
    }
    catch {
        Write-Host "Failed to request URL web material collection: $($_.Exception.Message)" -ForegroundColor $ErrorColor
    }
}

# Collect web materials from multiple URLs
function Start-UrlsCollection {
    param(
        [string[]]$Urls,
        [string]$Category
    )
    
    if ($Urls.Count -eq 0) {
        Write-Host "Error: URL list not specified. Please use -Urls parameter." -ForegroundColor $ErrorColor
        Write-Host "Example: -Urls 'https://example1.com','https://example2.com'" -ForegroundColor $WarningColor
        return
    }
    
    # Set default category if not provided
    if ([string]::IsNullOrWhiteSpace($Category)) {
        $Category = "User Defined Multiple"
    }
    
    Write-Host "Starting to collect web materials from multiple URLs ($($Urls.Count) URLs)..." -ForegroundColor $InfoColor
    Write-Host "Category: $Category" -ForegroundColor $InfoColor
    Write-Host ""
    Write-Host "URL list to collect:" -ForegroundColor $InfoColor
    for ($i = 0; $i -lt $Urls.Count; $i++) {
        Write-Host "  $($i+1). $($Urls[$i])" -ForegroundColor White
    }
    Write-Host ""
    
    try {
        # Construct URL list in JSON format
        $urlsJson = $Urls | ConvertTo-Json
        $uri = "$BaseUrl/collect-urls?category=" + [Uri]::EscapeDataString($Category)
        
        $response = Invoke-RestMethod -Uri $uri -Method Post -Body $urlsJson -ContentType "application/json"
        
        if ($response.success) {
            Write-Host "Success: " -ForegroundColor $SuccessColor -NoNewline
            Write-Host $response.message -ForegroundColor $SuccessColor
            Write-Host "URL count: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.url_count -ForegroundColor White
            Write-Host "Category: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.category -ForegroundColor White
            Write-Host "Status: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.status -ForegroundColor $WarningColor
            Write-Host ""
            Write-Host "This task runs in the background and may take a few minutes." -ForegroundColor $WarningColor
        }
        else {
            Write-Host "Error: " -ForegroundColor $ErrorColor -NoNewline
            Write-Host $response.message -ForegroundColor $ErrorColor
        }
    }
    catch {
        Write-Host "Failed to request multiple URL web material collection: $($_.Exception.Message)" -ForegroundColor $ErrorColor
    }
}

# Site depth crawling
function Start-DeepCollection {
    param(
        [string]$BaseUrl,
        [string]$Category,
        [int]$MaxDepth,
        [int]$MaxPages
    )
    
    if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
        Write-Host "Error: Base URL not specified. Please use -Url parameter." -ForegroundColor $ErrorColor
        return
    }
    
    # Set default category if not provided
    if ([string]::IsNullOrWhiteSpace($Category)) {
        $Category = "Site Depth Collection"
    }
    
    # Parameter validation
    if ($MaxDepth -lt 1 -or $MaxDepth -gt 5) {
        Write-Host "Adjusting MaxDepth to 1-5 range. (input value: $MaxDepth -> 2)" -ForegroundColor $WarningColor
        $MaxDepth = 2
    }
    
    if ($MaxPages -lt 1 -or $MaxPages -gt 50) {
        Write-Host "Adjusting MaxPages to 1-50 range. (input value: $MaxPages -> 10)" -ForegroundColor $WarningColor
        $MaxPages = 10
    }
    
    Write-Host "Starting site depth crawling..." -ForegroundColor $InfoColor
    Write-Host "Base URL: $BaseUrl" -ForegroundColor $InfoColor
    Write-Host "Category: $Category" -ForegroundColor $InfoColor
    Write-Host "Max Depth: $MaxDepth" -ForegroundColor $InfoColor
    Write-Host "Max Pages: $MaxPages" -ForegroundColor $InfoColor
    Write-Host ""
    Write-Host "This task may take a long time. Please wait..." -ForegroundColor $WarningColor
    
    try {
        $apiBaseUrl = "http://localhost:8080/api/web-learning"
        $uri = "$apiBaseUrl/collect-site-deep?baseUrl=" + [Uri]::EscapeDataString($BaseUrl) + 
               "&category=" + [Uri]::EscapeDataString($Category) +
               "&maxDepth=$MaxDepth&maxPages=$MaxPages"
        
        $response = Invoke-RestMethod -Uri $uri -Method Post
        
        if ($response.success) {
            Write-Host "Success: " -ForegroundColor $SuccessColor -NoNewline
            Write-Host $response.message -ForegroundColor $SuccessColor
            Write-Host "Base URL: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.base_url -ForegroundColor White
            Write-Host "Category: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.category -ForegroundColor White
            Write-Host "Max Depth: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.max_depth -ForegroundColor White
            Write-Host "Max Pages: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.max_pages -ForegroundColor White
            Write-Host "Status: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.status -ForegroundColor $WarningColor
            Write-Host ""
            Write-Host "Estimated time: " -ForegroundColor $InfoColor -NoNewline
            Write-Host $response.estimated_time -ForegroundColor $WarningColor
            Write-Host ""
            Write-Host "Crawling in progress..." -ForegroundColor $WarningColor
            Write-Host "   - Only collect links within the same domain" -ForegroundColor White
            Write-Host "   - Exclude files like images and videos" -ForegroundColor White
            Write-Host "   - Exclude login and admin pages" -ForegroundColor White
        }
        else {
            Write-Host "Error: " -ForegroundColor $ErrorColor -NoNewline
            Write-Host $response.message -ForegroundColor $ErrorColor
        }
    }
    catch {
        Write-Host "Failed to request site depth crawling: $($_.Exception.Message)" -ForegroundColor $ErrorColor
    }
}

# Collect BTD6 strategy guides from Namu Wiki
function Start-StrategyGuidesCollection {
    Write-Host "Starting to collect BTD6 strategy guides from Namu Wiki..." -ForegroundColor $InfoColor
    
    # Define all strategy guide URLs from WebDataCollectionService.java
    $StrategyGuideUrls = @(
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EC%A0%84%EB%9E%B5",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EC%98%81%EC%9B%85",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%ED%83%80%EC%9B%8C",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%ED%83%80%EC%9B%8C/1%EC%B0%A8%20%EA%B3%B5%EA%B2%A9",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%ED%83%80%EC%9B%8C/%EA%B5%B0%EC%82%AC",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%ED%83%80%EC%9B%8C/%EB%A7%88%EB%B2%95",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%ED%83%80%EC%9B%8C/%EC%A7%80%EC%9B%90",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%ED%8C%8C%EB%9D%BC%EA%B3%A4",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%ED%92%8D%EC%84%A0",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EB%9D%BC%EC%9A%B4%EB%93%9C",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EB%B3%B4%EC%8A%A4",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%ED%99%A9%EA%B8%88%20%ED%92%8D%EC%84%A0",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EB%A7%B5/%EC%B4%88%EB%B3%B4",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EB%A7%B5/%EC%A4%91%EA%B8%89",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EB%A7%B5/%EA%B3%A0%EA%B8%89",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EB%A7%B5/%EC%A0%84%EB%AC%B8",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EB%A7%B5/%EA%B8%B0%ED%83%80",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EA%B2%8C%EC%9E%84%20%EB%AA%A8%EB%93%9C",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EC%9B%90%EC%88%AD%EC%9D%B4%20%EC%A7%80%EC%8B%9D",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%ED%80%98%EC%8A%A4%ED%8A%B8",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%ED%8A%B8%EB%A1%9C%ED%94%BC%20%EC%83%81%EC%A0%90",
        "https://namu.wiki/w/%EB%B8%94%EB%A3%AC%EC%8A%A4%20TD%206/%EC%97%85%EC%A0%81"
    )
    
    $Category = "BTD6 NamuWiki Strategy Guide"
    
    Write-Host "Category: $Category" -ForegroundColor $InfoColor
    Write-Host "Total URLs: $($StrategyGuideUrls.Count)" -ForegroundColor $InfoColor
    Write-Host ""
    Write-Host "URL list to collect:" -ForegroundColor $InfoColor
    for ($i = 0; $i -lt $StrategyGuideUrls.Count; $i++) {
        Write-Host "  $($i+1). $($StrategyGuideUrls[$i])" -ForegroundColor White
    }
    Write-Host ""
    
    try {
        # Use the existing urls collection function
        Start-UrlsCollection -Urls $StrategyGuideUrls -Category $Category
    }
    catch {
        Write-Host "Failed to collect strategy guides: $($_.Exception.Message)" -ForegroundColor $ErrorColor
    }
}

# Check service status
function Show-ServiceStatus {
    Write-Host "Checking web learning service status..." -ForegroundColor $InfoColor
    
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/status" -Method Get
        
        if ($response.success) {
            Write-Host "Success: " -ForegroundColor $SuccessColor -NoNewline
            Write-Host $response.message -ForegroundColor $SuccessColor
            Write-Host ""
            Write-Host "Available topics:" -ForegroundColor $InfoColor
            foreach ($topic in $response.available_topics) {
                Write-Host "  - $topic" -ForegroundColor White
            }
        }
        else {
            Write-Host "Error: " -ForegroundColor $ErrorColor -NoNewline
            Write-Host $response.message -ForegroundColor $ErrorColor
        }
    }
    catch {
        Write-Host "Failed to check service status: $($_.Exception.Message)" -ForegroundColor $ErrorColor
    }
}

# Main execution logic
Show-Title

# Check server status first
if (-not (Test-ServerStatus)) {
    Write-Host "Error: Backend server is not running!" -ForegroundColor $ErrorColor
    Write-Host "Please start the backend server first with the following command:" -ForegroundColor $WarningColor
    Write-Host "   cd backend" -ForegroundColor White
    Write-Host "   .\gradlew bootRun" -ForegroundColor White
    Write-Host ""
    exit 1
}

# Execute according to mode
switch ($Mode.ToLower()) {
    "all" {
        Start-AllCollection
    }
    "topic" {
        Start-TopicCollection -TopicName $Topic
    }
    "keyword" {
        Start-KeywordCollection -KeywordText $Keyword
    }
    "url" {
        Start-UrlCollection -Url $Url -Category $Category
    }
    "urls" {
        Start-UrlsCollection -Urls $Urls -Category $Category
    }
    "status" {
        Show-ServiceStatus
    }
    "help" {
        Show-Help
    }
    "deep" {
        Start-DeepCollection -BaseUrl $Url -Category $Category -MaxDepth $MaxDepth -MaxPages $MaxPages
    }
    "strategy-guides" {
        Start-StrategyGuidesCollection
    }
    default {
        Write-Host "Error: Unknown mode: $Mode" -ForegroundColor $ErrorColor
        Write-Host ""
        Show-Help
    }
}

Write-Host ""
Write-Host "Complete!" -ForegroundColor $SuccessColor 