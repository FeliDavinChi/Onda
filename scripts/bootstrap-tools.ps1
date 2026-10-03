param([switch]$AcceptAndroidLicense)
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskTools = Join-Path $taskRoot '.tools'
New-Item -ItemType Directory -Force -Path $taskTools | Out-Null
. (Join-Path $PSScriptRoot 'download-file.ps1')
function Download([string]$Url, [string]$Output, [string]$Checksum = '', [string]$Algorithm = 'SHA256') {
    Get-VerifiedDownload -Url $Url -Output $Output -Checksum $Checksum -Algorithm $Algorithm
}
$taskMetadata = Join-Path $taskTools 'jdk-metadata.json'
if (!(Test-Path $taskMetadata)) {
    Download 'https://api.adoptium.net/v3/assets/latest/17/hotspot?architecture=x64&image_type=jdk&os=windows' $taskMetadata
}
$taskJdk = (Get-Content $taskMetadata -Raw | ConvertFrom-Json)[0]
$taskJdkZip = Join-Path $taskTools 'jdk.zip'
if (!(Test-Path $taskJdkZip)) { Download $taskJdk.binary.package.link $taskJdkZip $taskJdk.binary.package.checksum }
if ((Get-FileHash $taskJdkZip -Algorithm SHA256).Hash.ToLowerInvariant() -ne $taskJdk.binary.package.checksum) {
    throw 'JDK checksum mismatch'
}
$taskJdkHome = Join-Path $taskTools $taskJdk.release_name
if (!(Test-Path $taskJdkHome)) { Expand-Archive -LiteralPath $taskJdkZip -DestinationPath $taskTools }
$env:JAVA_HOME = $taskJdkHome
$env:PATH = "$taskJdkHome\bin;$env:PATH"
Write-Output "JDK: $taskJdkHome"
$taskXmlPath = Join-Path $taskTools 'android-repository.xml'
if (!(Test-Path $taskXmlPath)) { Download 'https://dl.google.com/android/repository/repository2-1.xml' $taskXmlPath }
[xml]$taskXml = Get-Content $taskXmlPath -Raw
$taskPackage = $taskXml.SelectSingleNode("//*[local-name()='remotePackage' and @path='cmdline-tools;12.0']")
$taskArchive = $taskPackage.SelectSingleNode("*[local-name()='archives']/*[local-name()='archive'][*[local-name()='host-os']='windows']/*[local-name()='complete']")
$taskSdkZip = Join-Path $taskTools 'sdk-tools.zip'
$taskArchiveUrl = 'https://dl.google.com/android/repository/' + $taskArchive.url
if (!(Test-Path $taskSdkZip)) { Download $taskArchiveUrl $taskSdkZip ([string]$taskArchive.checksum) 'SHA1' }
if ((Get-FileHash $taskSdkZip -Algorithm SHA1).Hash.ToLowerInvariant() -ne [string]$taskArchive.checksum) {
    throw 'Android tools checksum mismatch'
}
$taskSdkHome = Join-Path $taskTools 'android-sdk'
$taskCmdlineParent = Join-Path $taskSdkHome 'cmdline-tools'
$taskCmdlineHome = Join-Path $taskCmdlineParent '12.0'
if (!(Test-Path $taskCmdlineHome)) {
    Expand-Archive -LiteralPath $taskSdkZip -DestinationPath (Join-Path $taskTools 'sdk-unpacked')
    New-Item -ItemType Directory -Force -Path $taskCmdlineParent | Out-Null
    Move-Item -LiteralPath (Join-Path $taskTools 'sdk-unpacked/cmdline-tools') -Destination $taskCmdlineHome
}
$env:ANDROID_HOME = $taskSdkHome
$taskSdkManager = Join-Path $taskCmdlineHome 'bin/sdkmanager.bat'
if ($AcceptAndroidLicense) {
    1..20 | ForEach-Object { 'y' } | & $taskSdkManager "--sdk_root=$taskSdkHome" --licenses
    if ($LASTEXITCODE -ne 0) { throw 'License acceptance failed' }
}
& $taskSdkManager "--sdk_root=$taskSdkHome" 'platforms;android-37.0' 'platforms;android-36' 'build-tools;36.0.0' 'platform-tools' 'ndk;28.2.13676358' 'cmake;3.22.1'
if ($LASTEXITCODE -ne 0) { throw 'SDK installation failed' }
$taskLocalProperties = 'sdk.dir=' + $taskSdkHome.Replace('\','/')
Set-Content -LiteralPath (Join-Path $taskRoot 'local.properties') -Value $taskLocalProperties -Encoding Ascii
$taskWrapperDir = Join-Path $taskRoot 'gradle/wrapper'
New-Item -ItemType Directory -Force -Path $taskWrapperDir | Out-Null
Download 'https://services.gradle.org/distributions/gradle-9.3.1-wrapper.jar.sha256' (Join-Path $taskTools 'wrapper.sha256')
$taskWrapperHash = (Get-Content (Join-Path $taskTools 'wrapper.sha256') -Raw).Trim()
Download 'https://raw.githubusercontent.com/gradle/gradle/v9.3.1/gradle/wrapper/gradle-wrapper.jar' (Join-Path $taskWrapperDir 'gradle-wrapper.jar') $taskWrapperHash
if ((Get-FileHash (Join-Path $taskWrapperDir 'gradle-wrapper.jar') -Algorithm SHA256).Hash.ToLowerInvariant() -ne $taskWrapperHash) {
    throw 'Gradle wrapper checksum mismatch'
}
Download 'https://services.gradle.org/distributions/gradle-9.3.1-bin.zip.sha256' (Join-Path $taskTools 'gradle.sha256')
$taskGradleHash = (Get-Content (Join-Path $taskTools 'gradle.sha256') -Raw).Trim()
$taskWrapperProperties = Join-Path $taskWrapperDir 'gradle-wrapper.properties'
$taskProperties = Get-Content $taskWrapperProperties -Raw
if ($taskProperties -notmatch 'distributionSha256Sum=') {
    Add-Content -LiteralPath $taskWrapperProperties -Value "distributionSha256Sum=$taskGradleHash" -Encoding Ascii
}
Write-Output 'Build tools installed; wrapper and distributions use verified checksums.'
