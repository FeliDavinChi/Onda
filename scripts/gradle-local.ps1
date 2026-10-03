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
    $taskGradle = Join-Path $taskRoot '.tools/gradle-9.3.1/bin/gradle.bat'
    if (!(Test-Path -LiteralPath $taskGradle)) { $taskGradle = Join-Path $taskRoot 'gradlew.bat' }
    & $taskGradle '-Djava.net.preferIPv4Stack=true' @GradleArguments
    exit $LASTEXITCODE
} finally { Pop-Location }
