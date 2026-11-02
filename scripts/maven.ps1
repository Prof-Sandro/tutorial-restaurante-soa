# Sem parâmetros avançados: opções como -o e -pl devem chegar intactas ao Maven.
$argumentosMaven = @($args)
if ($argumentosMaven.Count -eq 0) { $argumentosMaven = @('clean','verify') }
$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $raiz
try {
    & mvn "-Dmaven.repo.local=$raiz\.m2\repository" @argumentosMaven
    if ($LASTEXITCODE -ne 0) { throw "Maven terminou com código $LASTEXITCODE" }
} finally { Pop-Location }