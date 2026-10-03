param([Parameter(ValueFromRemainingArguments=$true)][string[]]$GradleArguments)
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskTools = Get-Item -LiteralPath (Join-Path $taskRoot '.tools')
# Worktrees may share tools through a junction. Use the canonical cache path so
# Gradle does not operate on the same Windows cache under two different names.
$taskToolRoot = if ($taskTools.LinkType -eq 'Junction') { $taskTools.Target } else { $taskTools.FullName }
$taskJdk = Get-ChildItem -LiteralPath $taskToolRoot -Directory -Filter 'jdk-17*' | Select-Object -First 1
if (!$taskJdk) { throw 'Run scripts/bootstrap-tools.ps1 or set JAVA_HOME and use gradlew directly.' }
$env:JAVA_HOME = $taskJdk.FullName
$env:GRADLE_USER_HOME = Join-Path $taskToolRoot 'gradle-home'
$env:ANDROID_HOME = Join-Path $taskToolRoot 'android-sdk'
Push-Location $taskRoot
try {
    & (Join-Path $taskRoot 'gradlew.bat') @GradleArguments
    exit $LASTEXITCODE
} finally { Pop-Location }
