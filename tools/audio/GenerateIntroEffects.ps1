$ErrorActionPreference = 'Stop'
$repository = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$resources = Join-Path $repository 'shared/src/commonMain/resources'
$output = Join-Path $resources 'audio/cinematic'
$manifest = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'intro-effects.json') -Raw | ConvertFrom-Json
Add-Type -Path (Join-Path $PSScriptRoot 'IntroEffectsSynth.cs')
New-Item -ItemType Directory -Path $output -Force | Out-Null
$clips = @{}
foreach ($effect in $manifest.effects) {
    $samples = [IntroEffectsSynth]::Create($effect.kind, $effect.seconds, $effect.seed)
    $clips[$effect.name] = $samples
    [IntroEffectsSynth]::Write((Join-Path $output ($effect.name + '.wav')), $samples)
}
foreach ($source in $manifest.sources) {
    $clips[$source.name] = [IntroEffectsSynth]::ReadMono((Join-Path $resources $source.path))
}
$mix = New-Object 'float[]' ([int]($manifest.durationSeconds * 44100))
foreach ($cue in $manifest.cues) {
    [IntroEffectsSynth]::Mix($mix, $clips[$cue.sound], $cue.at, $cue.gain)
}
[IntroEffectsSynth]::Write((Join-Path $output 'intro_effects.wav'), $mix)
Write-Output ('Generated {0} original clips and a {1}-second synchronized effects track.' -f $manifest.effects.Count, $manifest.durationSeconds)
