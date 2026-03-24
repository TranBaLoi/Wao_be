$ErrorActionPreference = 'Stop'
$root = (Get-Location).Path
$output = Join-Path $root 'PROJECT_CONTEXT.md'

$files = Get-ChildItem -Recurse -File -Include *.java,*.xml,*.properties,*.md |
    Where-Object {
        $_.FullName -notmatch '\\target\\' -and
        $_.FullName -notmatch '\\.git\\' -and
        $_.Name -ne 'PROJECT_CONTEXT.md' -and
        $_.Name -ne 'fix_context_encoding.ps1'
    } |
    Sort-Object FullName

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$writer = New-Object System.IO.StreamWriter($output, $false, $utf8NoBom)

try {
    $writer.WriteLine('# Project Context')

    foreach ($file in $files) {
        $relativePath = $file.FullName.Substring($root.Length + 1)
        $writer.WriteLine('')
        $writer.WriteLine('================================================================================')
        $writer.WriteLine('File: ' + $relativePath)
        $writer.WriteLine('================================================================================')

        # Force UTF-8 read to keep Vietnamese text and emoji intact.
        $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
        $writer.WriteLine($content)
    }
}
finally {
    $writer.Close()
}

Write-Output 'PROJECT_CONTEXT.md regenerated in UTF-8'

