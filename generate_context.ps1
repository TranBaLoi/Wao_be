$files = Get-ChildItem -Recurse -File -Include *.java, *.xml, *.properties, *.md | Where-Object { $_.FullName -notmatch "target" -and $_.FullName -notmatch ".mvn" -and $_.FullName -notmatch ".git" -and $_.Name -ne "PROJECT_CONTEXT.md" -and $_.Name -ne "generate_context.ps1" };
$output = "PROJECT_CONTEXT.md";
Set-Content -Path $output -Value "# Project Context" -Encoding UTF8;
foreach ($file in $files) {
    Add-Content -Path $output -Value "`n================================================================================";
    Add-Content -Path $output -Value "File: $($file.FullName.Replace((Get-Location).Path + '\', ''))";
    Add-Content -Path $output -Value "================================================================================";
    try {
        $content = Get-Content -Path $file.FullName -Raw -ErrorAction Stop
        Add-Content -Path $output -Value $content
    } catch {
        Add-Content -Path $output -Value "Error reading file: $($_.Exception.Message)"
    }
}
Write-Host "Context file generated at $output"
