$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$dumpPath = Join-Path $projectRoot "backups\oracle\gamestore.dmp"

$container = "gamestore-oracle"
$directoryName = "GAMESTORE_RESTORE_DIR"
$containerDumpPath = "/tmp/gamestore.dmp"

if (-not (Test-Path $dumpPath)) {
    throw "Не найден файл резервной копии: $dumpPath"
}

$running = docker inspect -f '{{.State.Running}}' $container 2>$null

if ($LASTEXITCODE -ne 0 -or $running -ne "true") {
    throw "Контейнер $container не запущен. Выполните docker compose up -d oracle"
}

Write-Host "Проверяем базу Oracle..."

$sql = @"
ALTER SESSION SET CONTAINER = FREEPDB1;
SET HEADING OFF FEEDBACK OFF PAGESIZE 0
SELECT COUNT(*) FROM dba_tables
WHERE owner = 'GAMESTORE'
AND table_name IN ('GAMES', 'CATEGORIES', 'APP_USERS');
EXIT;
"@

$result = $sql | docker exec -i $container sqlplus -s "/ as sysdba"

if ($LASTEXITCODE -ne 0 -or ($result -match 'ORA-\d+')) {
    throw "Не удалось проверить состояние Oracle: $result"
}

$count = @($result | Where-Object { $_ -match '^\s*\d+\s*$' } |
    ForEach-Object { [int]$_.Trim() })

if ($count.Count -ne 1) {
    throw "Не удалось определить количество существующих таблиц: $result"
}

if ($count[0] -gt 0) {
    throw "В схеме GAMESTORE уже есть таблицы. Импорт остановлен во избежание конфликтов."
}

Write-Host "Копируем резервную копию в контейнер..."

docker cp $dumpPath "${container}:${containerDumpPath}"

if ($LASTEXITCODE -ne 0) {
    throw "Не удалось скопировать дамп Oracle."
}

$sql = @"
ALTER SESSION SET CONTAINER = FREEPDB1;
CREATE OR REPLACE DIRECTORY GAMESTORE_RESTORE_DIR AS '/tmp';
GRANT READ, WRITE ON DIRECTORY GAMESTORE_RESTORE_DIR TO GAMESTORE;
EXIT;
"@

$result = $sql | docker exec -i $container sqlplus -s "/ as sysdba"

if ($LASTEXITCODE -ne 0 -or ($result -match 'ORA-\d+')) {
    throw "Не удалось подготовить DIRECTORY: $result"
}

Write-Host "Запускаем импорт Oracle Data Pump..."

$envPath = Join-Path $projectRoot ".env"

if (-not (Test-Path $envPath)) {
    throw "Не найден файл .env"
}

$line = Get-Content $envPath |
    Where-Object { $_ -match '^APP_USER_PASSWORD=' } |
    Select-Object -First 1

if (-not $line) {
    throw "В .env отсутствует APP_USER_PASSWORD"
}

$password = $line.Substring("APP_USER_PASSWORD=".Length)

$connectString = "GAMESTORE/$password@//localhost:1521/FREEPDB1"

docker exec $container impdp $connectString `
    "DIRECTORY=$directoryName" `
    "DUMPFILE=gamestore.dmp" `
    "LOGFILE=gamestore_import.log" `
    "SCHEMAS=GAMESTORE"

if ($LASTEXITCODE -ne 0) {
    throw "Импорт Oracle завершился с ошибкой."
}

Write-Host "Импорт Oracle завершён. Проверьте журнал gamestore_import.log."