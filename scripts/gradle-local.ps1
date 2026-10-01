param([Parameter(ValueFromRemainingArguments=$true)][string[]]$GradleArguments)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskJdk = Get-ChildItem -LiteralPath (Join-Path $taskRoot '.tools') -Directory -Filter 'jdk-17*' | Select-Object -First 1
if (!$taskJdk) { throw 'Run scripts/bootstrap-tools.ps1 or set JAVA_HOME and use gradlew directly.' }
$env:JAVA_HOME = $taskJdk.FullName
$env:GRADLE_USER_HOME = Join-Path $taskRoot '.tools/gradle-home'
$env:ANDROID_HOME = Join-Path $taskRoot '.tools/android-sdk'
Push-Location $taskRoot
try {
    & (Join-Path $taskRoot 'gradlew.bat') @GradleArguments
    exit $LASTEXITCODE
} finally { Pop-Location }
