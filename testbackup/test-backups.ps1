
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot

$dump = Join-Path $root "backups\oracle\gamestore.dmp"
$images = Join-Path $root "backups\minio\gamestore-images"

$scripts = @(
    (Join-Path $root "scripts\restore-oracle.ps1"),
    (Join-Path $root "scripts\restore-minio.ps1")
)

Write-Host "Checking GameStore backups..."

if (-not (Test-Path $dump -PathType Leaf)) {
    throw "Oracle dump not found."
}

if ((Get-Item $dump).Length -eq 0) {
    throw "Oracle dump is empty."
}

Write-Host "[OK] Oracle dump exists."

if (-not (Test-Path $images -PathType Container)) {
    throw "MinIO backup directory not found."
}

$count = @(Get-ChildItem $images -File -Recurse).Count

if ($count -ne 38) {
    throw "Expected 38 MinIO images, found $count."
}

Write-Host "[OK] MinIO backup contains $count files."

foreach ($script in $scripts) {
    if (-not (Test-Path $script)) {
        throw "Script not found: $script"
    }

    $tokens = $null
    $errors = $null

    [System.Management.Automation.Language.Parser]::ParseFile(
        $script,
        [ref]$tokens,
        [ref]$errors
    ) | Out-Null

    if ($errors.Count -gt 0) {
        throw "PowerShell syntax error in: $script"
    }

    Write-Host "[OK] $([IO.Path]::GetFileName($script))"
}

Write-Host ""
Write-Host "Backup checks passed."
