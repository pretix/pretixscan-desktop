$jre_version = '10.0.1'
$build = '10'
$hash = 'fb4372174a714e6b8c52526dc134031e'
$script_path = $(Split-Path -parent $MyInvocation.MyCommand.Definition)

function has_file($filename) {
    return Test-Path $filename
}

function get-programfilesdir() {
    $programFiles = (Get-Item "Env:ProgramFiles").Value

    return $programFiles
}

function download-from-oracle($url, $output_filename) {
    if (-not (has_file($output_fileName))) {
        Write-Host  "Downloading JRE from $url"

        try {
            [System.Net.ServicePointManager]::ServerCertificateValidationCallback = { $true }

            $client = New-Object Net.WebClient
            $dummy = $client.Headers.Add('Cookie', 'gpw_e24=http://www.oracle.com; oraclelicense=accept-securebackup-cookie')

            $defaultCreds = [System.Net.CredentialCache]::DefaultCredentials
            if ($defaultCreds -ne $null) {
                $client.Credentials = $defaultCreds
            }

            # Copy from https://github.com/chocolatey/choco/blob/master/src/chocolatey.resources/helpers/functions/Get-WebFile.ps1
            # check if a proxy is required
            $explicitProxy = $env:chocolateyProxyLocation
            $explicitProxyUser = $env:chocolateyProxyUser
            $explicitProxyPassword = $env:chocolateyProxyPassword
            if ($explicitProxy -ne $null) {
                # explicit proxy
              $proxy = New-Object System.Net.WebProxy($explicitProxy, $true)
              if ($explicitProxyPassword -ne $null) {
                  $passwd = ConvertTo-SecureString $explicitProxyPassword -AsPlainText -Force
                  $proxy.Credentials = New-Object System.Management.Automation.PSCredential ($explicitProxyUser, $passwd)
              }

              Write-Host "Using explicit proxy server '$explicitProxy'."
                $client.Proxy = $proxy

            } elseif (!$client.Proxy.IsBypassed($url)) {
              # system proxy (pass through)
                $creds = [net.CredentialCache]::DefaultCredentials
                if ($creds -eq $null) {
                    Write-Debug "Default credentials were null. Attempting backup method"
                    $cred = get-credential
                    $creds = $cred.GetNetworkCredential();
                }
                $proxyaddress = $client.Proxy.GetProxy($url).Authority
                Write-Host "Using system proxy server '$proxyaddress'."
                $proxy = New-Object System.Net.WebProxy($proxyaddress)
                $proxy.Credentials = $creds
                $client.Proxy = $proxy
           }

           $dummy = $client.DownloadFile($url, $output_filename)
           Write-Host  "Written to $output_filename"
        } finally {
            [System.Net.ServicePointManager]::ServerCertificateValidationCallback = $null
        }
    }
}

function download-jre-file($url, $output_filename) {
    $dummy = download-from-oracle $url $output_filename
}

function download-jre() {
    $filename = "jre-" + $jre_version + "_windows-x64_bin.tar.gz"
    Write-Host "Checking for $filename"
    $url = "http://download.oracle.com/otn-pub/java/jdk/$jre_version+$build/$hash/$filename"
    $output_filename = Join-Path $script_path $filename

    $dummy = download-jre-file $url $output_filename

    return $output_filename
}

download-jre
