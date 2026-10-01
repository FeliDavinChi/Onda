$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'download-file.ps1')
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskDirectory = Join-Path $taskRoot '.tools/download-test'
New-Item -ItemType Directory -Force -Path $taskDirectory | Out-Null
$taskInput = Join-Path $taskDirectory 'input.txt'
$taskOutput = Join-Path $taskDirectory 'output.txt'
Set-Content -LiteralPath $taskInput -Value 'verified-download-fixture'
$taskUrl = ([Uri]$taskInput).AbsoluteUri
$taskChecksum = (Get-FileHash -LiteralPath $taskInput -Algorithm SHA256).Hash
Get-VerifiedDownload -Url $taskUrl -Output $taskOutput -Checksum $taskChecksum
if ((Get-FileHash -LiteralPath $taskOutput -Algorithm SHA256).Hash -ne $taskChecksum) { throw 'Valid download was not promoted' }
# A failed replacement must retain the known-good file and leave no partial artifact.
$taskRejected = $false
try { Get-VerifiedDownload -Url $taskUrl -Output $taskOutput -Checksum ('0' * 64) }
catch { $taskRejected = $true }
if (!$taskRejected) { throw 'Corrupt download was accepted' }
if (Test-Path -LiteralPath ($taskOutput + '.partial')) { throw 'Partial file was retained' }
if ((Get-FileHash -LiteralPath $taskOutput -Algorithm SHA256).Hash -ne $taskChecksum) { throw 'Known-good download was overwritten' }
Get-VerifiedDownload -Url $taskUrl -Output $taskOutput -Checksum $taskChecksum
Write-Output 'Verified download tests passed: promotion, checksum rejection, cleanup, recovery.'
