
$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$backupPath = Join-Path $projectRoot "backups\minio\gamestore-images"
$envPath = Join-Path $projectRoot ".env"

$bucket = "gamestore-images"
$endpoint = "http://localhost:9000"

if (-not (Test-Path $backupPath -PathType Container)) {
    throw "MinIO backup directory not found: $backupPath"
}

$files = @(Get-ChildItem $backupPath -File -Recurse)

if ($files.Count -eq 0) {
    throw "MinIO backup directory is empty."
}

$aws = "C:\Program Files\Amazon\AWSCLIV2\aws.exe"

if (-not (Test-Path $aws)) {
    $command = Get-Command aws -ErrorAction SilentlyContinue

    if (-not $command) {
        throw "AWS CLI not found. Install AWS CLI v2."
    }

    $aws = $command.Source
}

if (-not (Test-Path $envPath)) {
    throw ".env file not found."
}

$settings = @{}

Get-Content $envPath | ForEach-Object {
    if ($_ -match '^\s*(MINIO_ROOT_USER|MINIO_ROOT_PASSWORD)=(.*)$') {
        $settings[$matches[1]] = $matches[2].Trim()
    }
}

if (-not $settings["MINIO_ROOT_USER"] -or
    -not $settings["MINIO_ROOT_PASSWORD"]) {
    throw "MinIO credentials are missing from .env."
}

$env:AWS_ACCESS_KEY_ID = $settings["MINIO_ROOT_USER"]
$env:AWS_SECRET_ACCESS_KEY = $settings["MINIO_ROOT_PASSWORD"]
$env:AWS_DEFAULT_REGION = "us-east-1"

Write-Host "Checking MinIO connection..."

& $aws --endpoint-url $endpoint s3 ls

if ($LASTEXITCODE -ne 0) {
    throw "Cannot connect to MinIO."
}

& $aws --endpoint-url $endpoint s3api head-bucket --bucket $bucket 2>$null
$bucketExists = ($LASTEXITCODE -eq 0)

if (-not $bucketExists) {
    Write-Host "Creating bucket: $bucket"

    & $aws --endpoint-url $endpoint s3 mb "s3://$bucket"

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to create MinIO bucket."
    }
}

Write-Host "Restoring MinIO images..."

& $aws --endpoint-url $endpoint s3 sync $backupPath "s3://$bucket"

if ($LASTEXITCODE -ne 0) {
    throw "MinIO restore failed."
}

Write-Host "MinIO restore completed successfully."
