function Get-VerifiedDownload {
    param(
        [Parameter(Mandatory)][string]$Url,
        [Parameter(Mandatory)][string]$Output,
        [string]$Checksum = '',
        [ValidateSet('SHA256', 'SHA1')][string]$Algorithm = 'SHA256'
    )
    $taskPartial = $Output + '.partial'
    try {
        & curl.exe --fail --location --retry 2 --silent --show-error --output $taskPartial $Url
        if ($LASTEXITCODE -ne 0) { throw 'Download failed' }
        if ($Checksum -and (Get-FileHash -LiteralPath $taskPartial -Algorithm $Algorithm).Hash.ToLowerInvariant() -ne $Checksum.ToLowerInvariant()) {
            throw 'Download checksum mismatch'
        }
        Move-Item -LiteralPath $taskPartial -Destination $Output -Force
    } finally {
        if (Test-Path -LiteralPath $taskPartial) { Remove-Item -LiteralPath $taskPartial }
    }
}
