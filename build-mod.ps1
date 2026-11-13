param([ValidateSet('build', 'runClient', 'runServer')][string]$Task = 'build')
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath "$env:JAVA_HOME\bin\javac.exe")) {
    throw 'Install JDK 21 and set JAVA_HOME to its directory, then reopen PowerShell.'
}
$javaVersion = & "$env:JAVA_HOME\bin\javac.exe" -version 2>&1
if ("$javaVersion" -notmatch '^javac 21\.') { throw "JDK 21 required; found $javaVersion" }
$gradleVersion = '8.14.3'
$cache = Join-Path $PSScriptRoot '.build-tools'
$gradle = Join-Path $cache "gradle-$gradleVersion\bin\gradle.bat"
if (-not (Test-Path -LiteralPath $gradle)) {
    New-Item -ItemType Directory -Force -Path $cache | Out-Null
    $zip = Join-Path $cache "gradle-$gradleVersion-bin.zip"
    $url = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
    Invoke-WebRequest -Uri $url -OutFile $zip -UseBasicParsing
    $expected = (Invoke-WebRequest -Uri "$url.sha256" -UseBasicParsing).Content.Trim()
    if ((Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash -ine $expected) {
        throw 'Gradle checksum mismatch. Build stopped.'
    }
    Expand-Archive -LiteralPath $zip -DestinationPath $cache -Force
}
& $gradle --no-daemon $Task
if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE" }
if ($Task -eq 'build') { Write-Host 'Output: build\libs\reality-immortal-1.1.1.jar' }
