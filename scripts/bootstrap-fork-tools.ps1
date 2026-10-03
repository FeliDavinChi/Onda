param()
$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path -Parent $PSScriptRoot
$taskTools = Join-Path $taskRoot '.tools'
$taskSdk = Join-Path $taskTools 'android-sdk'
. (Join-Path $PSScriptRoot 'download-file.ps1')
$taskMetadataPath = Join-Path $taskTools 'android-repository-v3.xml'
Get-VerifiedDownload -Url 'https://dl.google.com/android/repository/repository2-3.xml' -Output $taskMetadataPath
[xml]$taskMetadata = Get-Content -LiteralPath $taskMetadataPath -Raw
foreach ($taskPackageName in @('platforms;android-37.0', 'platforms;android-36', 'build-tools;36.0.0', 'cmake;3.22.1', 'ndk;28.2.13676358')) {
    $taskDestination = [IO.Path]::GetFullPath((Join-Path $taskSdk ($taskPackageName.Replace(';','/'))))
    if (!$taskDestination.StartsWith([IO.Path]::GetFullPath($taskSdk) + '\')) { throw 'SDK path outside local tools' }
    if (Test-Path -LiteralPath (Join-Path $taskDestination 'source.properties')) { Write-Output "Already installed: $taskPackageName"; continue }
    $taskPackage = $taskMetadata.SelectSingleNode("//*[local-name()='remotePackage' and @path='$taskPackageName']")
    if (!$taskPackage) { throw "SDK package unavailable: $taskPackageName" }
    # Existing SDK licenses must already cover these packages. This script does not accept terms.
    $taskLicenseRef = $taskPackage.SelectSingleNode("*[local-name()='uses-license']").ref
    $taskLicenseFile = Join-Path $taskSdk "licenses/$taskLicenseRef"
    if (!(Test-Path -LiteralPath $taskLicenseFile)) { throw "Review/accept SDK license $taskLicenseRef before installation" }
    $taskArchive = $taskPackage.SelectSingleNode("*[local-name()='archives']/*[local-name()='archive'][not(*[local-name()='host-os']) or *[local-name()='host-os']='windows']/*[local-name()='complete']")
    $taskSafeName = $taskPackageName.Replace(';','-')
    $taskZip = Join-Path $taskTools "$taskSafeName.zip"
    $taskHash = $taskArchive.checksum.InnerText
    if ($taskHash -notmatch '^[0-9a-fA-F]{40}$') { throw "Invalid SHA1 metadata for $taskPackageName" }
    if (!(Test-Path -LiteralPath $taskZip) -or (Get-FileHash -LiteralPath $taskZip -Algorithm SHA1).Hash.ToLowerInvariant() -ne $taskHash.ToLowerInvariant()) {
        Write-Output "Downloading and verifying: $taskPackageName"
        Get-VerifiedDownload -Url ('https://dl.google.com/android/repository/' + [string]$taskArchive.url) -Output $taskZip -Checksum $taskHash -Algorithm SHA1
    }
    $taskUnpack = Join-Path $taskTools "fork-sdk-unpacked/$taskSafeName"
    New-Item -ItemType Directory -Force -Path $taskUnpack | Out-Null
    Expand-Archive -LiteralPath $taskZip -DestinationPath $taskUnpack -Force
    $taskChildren = @(Get-ChildItem -LiteralPath $taskUnpack -Directory)
    if (Test-Path -LiteralPath (Join-Path $taskUnpack 'source.properties')) {
        $taskPackageSource = $taskUnpack
    } elseif ($taskChildren.Count -eq 1) {
        $taskPackageSource = $taskChildren[0].FullName
    } else { throw "Unexpected package layout for $taskPackageName" }
    if (!(Test-Path -LiteralPath (Join-Path $taskPackageSource 'source.properties'))) { throw "Package metadata missing: $taskPackageName" }
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $taskDestination) | Out-Null
    # Both move targets are resolved workspace-local paths; never overwrite an existing SDK installation.
    if (Test-Path -LiteralPath $taskDestination) { throw "Partial installation needs inspection: $taskDestination" }
    if (!$taskPackageSource.StartsWith([IO.Path]::GetFullPath($taskTools) + '\')) { throw 'Unpack path outside tools' }
    Move-Item -LiteralPath $taskPackageSource -Destination $taskDestination
    Write-Output "Installed: $taskPackageName"
}
