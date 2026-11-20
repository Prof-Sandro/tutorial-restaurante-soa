# Finalidade: Criar os descritores web.xml e sun-jaxws.xml e conferir a configuração de execução.
param([ValidateSet('soap','rest')][string]$Protocolo = 'soap')
$ErrorActionPreference = 'Stop'
$env:DB_URL = "jdbc:mysql://localhost:3306/restaurante_${Protocolo}?connectionTimeZone=UTC"
$env:DB_USER = 'restaurante'
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host '12345678' -AsSecureString)).Password
$env:RESTAURANTE_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host '987654321987' -AsSecureString)).Password
$env:RESTAURANTE_GARCOM_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host '123456789123' -AsSecureString)).Password
$env:PUBLIC_BASE_URL = if ($Protocolo -eq 'soap') { 'http://localhost:8080/restaurante' } else { 'http://localhost:8081' }
Write-Host 'Variáveis configuradas somente neste terminal. Para celular, ajuste PUBLIC_BASE_URL para o IP acessível.'
