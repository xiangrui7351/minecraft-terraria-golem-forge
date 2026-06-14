Add-Type -AssemblyName System.Drawing

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$entityDir = Join-Path $root 'src/main/resources/assets/terrariagolem/textures/entity'
$itemDir = Join-Path $root 'src/main/resources/assets/terrariagolem/textures/item'
$blockDir = Join-Path $root 'src/main/resources/assets/terrariagolem/textures/block'
New-Item -ItemType Directory -Force -Path $entityDir, $itemDir, $blockDir | Out-Null

$script:random = [System.Random]::new(20260613)
$script:drawScale = 1

function Clamp-Byte([int]$value) {
    [Math]::Max(0, [Math]::Min(255, $value))
}

function New-Color([int]$r, [int]$g, [int]$b, [int]$a = 255) {
    [System.Drawing.Color]::FromArgb((Clamp-Byte $a), (Clamp-Byte $r), (Clamp-Byte $g), (Clamp-Byte $b))
}

function Scale-Value([int]$value) {
    [int]($value * $script:drawScale)
}

function Fill-Rect($g, [int]$x, [int]$y, [int]$w, [int]$h, $color) {
    if ($w -le 0 -or $h -le 0) {
        return
    }

    $brush = [System.Drawing.SolidBrush]::new($color)
    $g.FillRectangle($brush, (Scale-Value $x), (Scale-Value $y), (Scale-Value $w), (Scale-Value $h))
    $brush.Dispose()
}

function Draw-Rect($g, [int]$x, [int]$y, [int]$w, [int]$h, $color) {
    if ($w -le 0 -or $h -le 0) {
        return
    }

    $pen = [System.Drawing.Pen]::new($color)
    $g.DrawRectangle($pen, (Scale-Value $x), (Scale-Value $y), [Math]::Max(0, (Scale-Value $w) - 1), [Math]::Max(0, (Scale-Value $h) - 1))
    $pen.Dispose()
}

function Draw-Line($g, [int]$x1, [int]$y1, [int]$x2, [int]$y2, $color, [int]$width = 1) {
    $pen = [System.Drawing.Pen]::new($color, [Math]::Max(1, (Scale-Value $width)))
    $g.DrawLine($pen, (Scale-Value $x1), (Scale-Value $y1), (Scale-Value $x2), (Scale-Value $y2))
    $pen.Dispose()
}

function Fill-Ellipse($g, [int]$x, [int]$y, [int]$w, [int]$h, $color) {
    if ($w -le 0 -or $h -le 0) {
        return
    }

    $brush = [System.Drawing.SolidBrush]::new($color)
    $g.FillEllipse($brush, (Scale-Value $x), (Scale-Value $y), (Scale-Value $w), (Scale-Value $h))
    $brush.Dispose()
}

function Shade([System.Drawing.Color]$color, [int]$delta) {
    New-Color ($color.R + $delta) ($color.G + $delta) ($color.B + $delta) $color.A
}

function Save-Png($bitmap, [string]$path) {
    $tempPath = "$path.tmp"
    if (Test-Path -LiteralPath $tempPath) {
        Remove-Item -LiteralPath $tempPath -Force
    }

    $bitmap.Save($tempPath, [System.Drawing.Imaging.ImageFormat]::Png)
    Move-Item -LiteralPath $tempPath -Destination $path -Force
}

function Draw-StonePanel($bitmap, $g, [int]$x, [int]$y, [int]$w, [int]$h, [int]$shade = 0) {
    if ($w -le 0 -or $h -le 0) {
        return
    }

    $scaledX = Scale-Value $x
    $scaledY = Scale-Value $y
    $scaledW = [Math]::Max(1, (Scale-Value $w))
    $scaledH = [Math]::Max(1, (Scale-Value $h))

    for ($yy = $scaledY; $yy -lt $scaledY + $scaledH; $yy++) {
        for ($xx = $scaledX; $xx -lt $scaledX + $scaledW; $xx++) {
            if ($xx -lt 0 -or $xx -ge $bitmap.Width -or $yy -lt 0 -or $yy -ge $bitmap.Height) {
                continue
            }

            $grain = $script:random.Next(-18, 19)
            $moss = if ($script:random.NextDouble() -lt 0.045) { $script:random.Next(12, 45) } else { 0 }
            $r = 116 + $grain + $shade - [int]($moss / 5)
            $gg = 58 + [int]($grain / 2) + [int]($shade / 3) + $moss
            $b = 27 + [int]($grain / 3) + [int]($shade / 5)
            $bitmap.SetPixel($xx, $yy, (New-Color $r $gg $b))
        }
    }

    $deep = New-Color 40 22 12
    $shadow = New-Color 55 28 14
    $light = New-Color 178 86 28
    $mossColor = New-Color 38 112 57

    Draw-Rect $g $x $y $w $h $deep
    Fill-Rect $g ($x + 1) ($y + 1) ([Math]::Max(1, $w - 2)) 1 $light
    Fill-Rect $g ($x + 1) ($y + $h - 2) ([Math]::Max(1, $w - 2)) 1 $shadow

    $area = $w * $h
    if ($area -gt 10000) {
        $crackCount = 120
    } else {
        $crackCount = [Math]::Max(1, [int]($area / 120))
    }
    for ($i = 0; $i -lt $crackCount; $i++) {
        $sx = $x + $script:random.Next(1, [Math]::Max(2, $w - 1))
        $sy = $y + $script:random.Next(1, [Math]::Max(2, $h - 1))
        $ex = [Math]::Max($x, [Math]::Min($x + $w - 1, $sx + $script:random.Next(-7, 8)))
        $ey = [Math]::Max($y, [Math]::Min($y + $h - 1, $sy + $script:random.Next(-4, 5)))
        Draw-Line $g $sx $sy $ex $ey (New-Color 35 20 13 190)
    }

    if ($w -gt 8 -and $h -gt 8) {
        $grooveY = $y + [int]($h * 0.45)
        Draw-Line $g ($x + 1) $grooveY ($x + $w - 2) ($grooveY + $script:random.Next(-1, 2)) $shadow
        $grooveX = $x + [int]($w * 0.52)
        Draw-Line $g $grooveX ($y + 2) ($grooveX + $script:random.Next(-1, 2)) ($y + $h - 3) $shadow
    }

    if ($script:random.NextDouble() -lt 0.45) {
        Fill-Rect $g ($x + $script:random.Next(1, [Math]::Max(2, $w - 3))) ($y + $script:random.Next(1, [Math]::Max(2, $h - 2))) ([Math]::Min(4, [Math]::Max(1, $w - 2))) 1 $mossColor
    }
}

function Draw-CubeTexture($bitmap, $g, [int]$u, [int]$v, [int]$w, [int]$h, [int]$d) {
    Draw-StonePanel $bitmap $g $u ($v + $d) $d $h -18
    Draw-StonePanel $bitmap $g ($u + $d) ($v + $d) $w $h 0
    Draw-StonePanel $bitmap $g ($u + $d + $w) ($v + $d) $d $h -12
    Draw-StonePanel $bitmap $g ($u + $d + $w + $d) ($v + $d) $w $h -28
    Draw-StonePanel $bitmap $g ($u + $d) $v $w $d 14
    Draw-StonePanel $bitmap $g ($u + $d + $w) $v $w $d -22
}

function Front-Rect([int]$u, [int]$v, [int]$w, [int]$h, [int]$d) {
    @(($u + $d), ($v + $d), $w, $h)
}

function Draw-TorsoMarks($g) {
    $dark = New-Color 38 20 12
    $line = New-Color 206 105 32
    $gold = New-Color 238 156 39
    $moss = New-Color 42 122 59

    $front = Front-Rect 0 0 20 24 13
    $x = $front[0]
    $y = $front[1]
    Fill-Rect $g ($x + 2) ($y + 2) 16 2 (New-Color 76 37 17 210)
    Draw-Line $g ($x + 10) ($y + 3) ($x + 10) ($y + 22) $dark
    Draw-Line $g ($x + 5) ($y + 6) ($x + 15) ($y + 6) $line
    Draw-Line $g ($x + 4) ($y + 8) ($x + 8) ($y + 12) $dark
    Draw-Line $g ($x + 16) ($y + 8) ($x + 12) ($y + 12) $dark
    Fill-Rect $g ($x + 8) ($y + 15) 4 4 $gold
    Fill-Rect $g ($x + 9) ($y + 16) 2 2 (New-Color 255 221 80)
    Fill-Rect $g ($x + 2) ($y + 20) 5 1 $moss
    Fill-Rect $g ($x + 13) ($y + 2) 4 1 $moss

    $band = Front-Rect 0 38 26 6 11
    Fill-Rect $g ($band[0] + 4) ($band[1] + 2) 18 2 (New-Color 58 27 13)
    Draw-Line $g ($band[0] + 7) ($band[1] + 1) ($band[0] + 13) ($band[1] + 4) $line
    Draw-Line $g ($band[0] + 19) ($band[1] + 1) ($band[0] + 13) ($band[1] + 4) $line

    $hip = Front-Rect 0 56 16 5 15
    Fill-Rect $g ($hip[0] + 3) ($hip[1] + 1) 10 2 (New-Color 55 28 14)
    Fill-Rect $g ($hip[0] + 7) ($hip[1] + 1) 2 3 $gold
}

function Draw-Core($g) {
    $front = Front-Rect 144 32 9 11 1
    $x = $front[0]
    $y = $front[1]
    Fill-Ellipse $g ($x - 2) ($y - 2) 13 15 (New-Color 255 84 10 90)
    Fill-Rect $g $x $y $front[2] $front[3] (New-Color 95 31 11)
    Fill-Rect $g ($x + 1) ($y + 1) 7 9 (New-Color 255 85 12)
    Fill-Ellipse $g ($x + 2) ($y + 3) 5 5 (New-Color 255 183 38)
    Fill-Rect $g ($x + 4) ($y + 1) 1 9 (New-Color 255 236 102)
    Fill-Rect $g ($x + 1) ($y + 5) 7 1 (New-Color 255 236 102)
    Fill-Ellipse $g ($x + 4) ($y + 5) 1 1 (New-Color 255 255 210)
    Draw-Rect $g $x $y $front[2] $front[3] (New-Color 45 22 12)

    Fill-Ellipse $g 176 32 17 17 (New-Color 255 92 12 85)
    Fill-Rect $g 178 34 13 13 (New-Color 255 102 14)
    Fill-Ellipse $g 181 37 7 7 (New-Color 255 224 78)
    Fill-Ellipse $g 183 39 3 3 (New-Color 255 255 190)
    Draw-Rect $g 178 34 13 13 (New-Color 54 26 12)
}

function Draw-HeadMarks($g) {
    $dark = New-Color 36 19 11
    $amber = New-Color 255 128 19
    $gold = New-Color 255 206 60
    $white = New-Color 255 246 162
    $moss = New-Color 42 118 58

    $face = Front-Rect 0 128 16 13 14
    $x = $face[0]
    $y = $face[1]
    Fill-Rect $g ($x + 1) ($y + 1) 14 3 (New-Color 74 35 16)
    Fill-Ellipse $g ($x + 2) ($y + 4) 4 3 $dark
    Fill-Ellipse $g ($x + 10) ($y + 4) 4 3 $dark
    Fill-Ellipse $g ($x + 3) ($y + 5) 2 1 $gold
    Fill-Ellipse $g ($x + 11) ($y + 5) 2 1 $gold
    Fill-Ellipse $g ($x + 4) ($y + 6) 1 1 $white
    Fill-Ellipse $g ($x + 12) ($y + 6) 1 1 $white
    Draw-Line $g ($x + 3) ($y + 9) ($x + 12) ($y + 9) $dark
    Draw-Line $g ($x + 5) ($y + 11) ($x + 10) ($y + 11) (New-Color 82 39 18)
    Fill-Rect $g ($x + 1) ($y + 12) 4 1 $moss

    $brow = Front-Rect 64 128 20 4 3
    Fill-Rect $g ($brow[0] + 2) ($brow[1] + 1) 16 2 (New-Color 45 23 12)
    Fill-Rect $g ($brow[0] + 5) ($brow[1] + 1) 3 1 $amber
    Fill-Rect $g ($brow[0] + 12) ($brow[1] + 1) 3 1 $amber

    $snout = Front-Rect 112 128 10 5 2
    Fill-Rect $g ($snout[0] + 1) ($snout[1] + 1) 8 3 (New-Color 54 26 13)
    Fill-Rect $g ($snout[0] + 3) ($snout[1] + 2) 4 1 (New-Color 181 83 24)

    $crest = Front-Rect 0 160 12 4 11
    Fill-Rect $g ($crest[0] + 1) ($crest[1] + 1) 10 2 (New-Color 76 37 18)
    Fill-Rect $g ($crest[0] + 5) $crest[1] 2 4 (New-Color 202 95 27)

    $crown = Front-Rect 52 160 6 3 8
    Fill-Rect $g ($crown[0] + 1) ($crown[1] + 1) 4 1 $gold

    $eye = Front-Rect 84 160 3 3 1
    Fill-Ellipse $g ($eye[0] - 1) ($eye[1] - 1) 5 5 (New-Color 255 103 12 90)
    Fill-Ellipse $g $eye[0] $eye[1] 3 3 $amber
    Fill-Ellipse $g ($eye[0] + 1) ($eye[1] + 1) 1 1 $white
    Draw-Rect $g $eye[0] $eye[1] 3 3 $dark

    $pupil = Front-Rect 96 160 1 1 1
    Fill-Ellipse $g $pupil[0] $pupil[1] 1 1 $white

    $mouth = Front-Rect 104 160 7 1 1
    Fill-Rect $g $mouth[0] $mouth[1] 7 1 (New-Color 22 12 8)

    $jaw = Front-Rect 112 160 12 4 3
    Fill-Rect $g ($jaw[0] + 1) ($jaw[1] + 1) 10 2 (New-Color 63 30 14)
    Fill-Rect $g ($jaw[0] + 3) ($jaw[1] + 2) 6 1 (New-Color 199 91 25)

    $teeth = Front-Rect 144 160 8 2 2
    Fill-Rect $g $teeth[0] $teeth[1] 8 2 (New-Color 236 168 57)
    Fill-Rect $g ($teeth[0] + 1) $teeth[1] 1 2 $white
    Fill-Rect $g ($teeth[0] + 6) $teeth[1] 1 2 $white
}

function Draw-FistMarks($g) {
    $dark = New-Color 38 20 12
    $line = New-Color 195 88 25
    $gold = New-Color 235 146 36
    $moss = New-Color 43 119 58

    $palm = Front-Rect 0 184 16 13 13
    $x = $palm[0]
    $y = $palm[1]
    Fill-Rect $g ($x + 2) ($y + 2) 12 3 (New-Color 72 35 16)
    Draw-Line $g ($x + 3) ($y + 7) ($x + 13) ($y + 7) $dark
    Draw-Line $g ($x + 5) ($y + 2) ($x + 3) ($y + 10) $line
    Draw-Line $g ($x + 11) ($y + 2) ($x + 13) ($y + 10) $line
    Fill-Ellipse $g ($x + 7) ($y + 5) 2 2 $gold
    Fill-Rect $g ($x + 1) ($y + 11) 5 1 $moss

    $finger = Front-Rect 64 184 6 8 5
    Fill-Rect $g ($finger[0] + 1) ($finger[1] + 1) 4 2 (New-Color 66 32 15)
    Draw-Line $g ($finger[0] + 2) ($finger[1] + 3) ($finger[0] + 4) ($finger[1] + 6) $dark
    Fill-Rect $g ($finger[0] + 2) ($finger[1] + 1) 2 1 $line

    $thumb = Front-Rect 96 184 4 6 7
    Fill-Rect $g ($thumb[0] + 1) ($thumb[1] + 1) 2 4 (New-Color 70 33 15)
    Fill-Rect $g ($thumb[0] + 1) ($thumb[1] + 3) 2 1 $line

    $wrist = Front-Rect 124 184 10 8 4
    Fill-Rect $g ($wrist[0] + 1) ($wrist[1] + 1) 8 6 (New-Color 71 34 16)
    Draw-Line $g ($wrist[0] + 2) ($wrist[1] + 2) ($wrist[0] + 8) ($wrist[1] + 5) $line
    Fill-Rect $g ($wrist[0] + 4) ($wrist[1] + 3) 2 2 $gold
}

function Draw-LegMarks($g) {
    $dark = New-Color 43 23 13
    $line = New-Color 179 79 23

    $leg = Front-Rect 0 216 7 11 8
    Fill-Rect $g ($leg[0] + 2) ($leg[1] + 1) 3 8 (New-Color 71 34 16)
    Draw-Line $g ($leg[0] + 1) ($leg[1] + 4) ($leg[0] + 5) ($leg[1] + 8) $dark
    Fill-Rect $g ($leg[0] + 3) ($leg[1] + 2) 1 5 $line

    $foot = Front-Rect 36 216 11 5 11
    Fill-Rect $g ($foot[0] + 1) ($foot[1] + 1) 9 2 (New-Color 62 30 15)
    Fill-Rect $g ($foot[0] + 4) ($foot[1] + 1) 3 1 $line

    $base = Front-Rect 88 216 13 3 13
    Fill-Rect $g ($base[0] + 1) ($base[1] + 1) 11 1 (New-Color 49 25 13)
}

function Draw-ChainSwatches($g) {
    $chainDark = New-Color 28 30 28
    $chainMid = New-Color 82 86 80
    $chainLight = New-Color 148 154 139
    Fill-Rect $g 104 0 24 18 $chainDark
    for ($y = 1; $y -lt 18; $y += 4) {
        Fill-Rect $g 105 $y 10 2 $chainMid
        Fill-Rect $g 116 ($y + 2) 10 2 $chainMid
        Fill-Rect $g 107 $y 3 1 $chainLight
        Fill-Rect $g 118 ($y + 2) 3 1 $chainLight
    }
    Draw-Rect $g 104 0 24 18 (New-Color 17 17 17)
}

function New-EntityTexture {
    $script:drawScale = 4
    $texture = [System.Drawing.Bitmap]::new((256 * $script:drawScale), (256 * $script:drawScale), [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $gfx = [System.Drawing.Graphics]::FromImage($texture)
    $gfx.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $gfx.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $gfx.Clear((New-Color 92 44 18))

    Draw-StonePanel $texture $gfx 0 0 256 256 -8

    Draw-CubeTexture $texture $gfx 0 0 20 24 13
    Draw-CubeTexture $texture $gfx 0 38 26 6 11
    Draw-CubeTexture $texture $gfx 0 56 16 5 15
    Draw-CubeTexture $texture $gfx 64 36 10 8 9
    Draw-CubeTexture $texture $gfx 96 36 4 8 8
    Draw-CubeTexture $texture $gfx 98 58 5 4 6
    Draw-CubeTexture $texture $gfx 64 55 14 4 2
    Draw-CubeTexture $texture $gfx 64 62 18 5 2
    Draw-CubeTexture $texture $gfx 64 71 8 5 2
    Draw-CubeTexture $texture $gfx 144 32 9 11 1

    Draw-CubeTexture $texture $gfx 0 128 16 13 14
    Draw-CubeTexture $texture $gfx 64 128 20 4 3
    Draw-CubeTexture $texture $gfx 112 128 10 5 2
    Draw-CubeTexture $texture $gfx 144 128 3 6 8
    Draw-CubeTexture $texture $gfx 0 160 12 4 11
    Draw-CubeTexture $texture $gfx 52 160 6 3 8
    Draw-CubeTexture $texture $gfx 84 160 3 3 1
    Draw-CubeTexture $texture $gfx 96 160 1 1 1
    Draw-CubeTexture $texture $gfx 104 160 7 1 1
    Draw-CubeTexture $texture $gfx 112 160 12 4 3
    Draw-CubeTexture $texture $gfx 144 160 8 2 2

    Draw-CubeTexture $texture $gfx 0 184 16 13 13
    Draw-CubeTexture $texture $gfx 64 184 6 8 5
    Draw-CubeTexture $texture $gfx 96 184 4 6 7
    Draw-CubeTexture $texture $gfx 124 184 10 8 4

    Draw-CubeTexture $texture $gfx 0 216 7 11 8
    Draw-CubeTexture $texture $gfx 36 216 11 5 11
    Draw-CubeTexture $texture $gfx 88 216 13 3 13

    Draw-ChainSwatches $gfx
    Draw-TorsoMarks $gfx
    Draw-Core $gfx
    Draw-HeadMarks $gfx
    Draw-FistMarks $gfx
    Draw-LegMarks $gfx

    $entityPath = Join-Path $entityDir 'terraria_golem.png'
    Save-Png $texture $entityPath
    $gfx.Dispose()
    $texture.Dispose()
    $script:drawScale = 1
}

function New-ItemTexture([string]$name, [scriptblock]$draw) {
    $bitmap = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.Clear([System.Drawing.Color]::Transparent)
    & $draw $bitmap $g
    $path = Join-Path $itemDir "$name.png"
    Save-Png $bitmap $path
    $g.Dispose()
    $bitmap.Dispose()
}

function New-BlockTexture([string]$name, [scriptblock]$draw) {
    $bitmap = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bitmap)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $g.Clear([System.Drawing.Color]::Transparent)
    & $draw $bitmap $g
    $path = Join-Path $blockDir "$name.png"
    Save-Png $bitmap $path
    $g.Dispose()
    $bitmap.Dispose()
}

New-EntityTexture

New-ItemTexture 'golem_power_cell' {
    param($bitmap, $g)
    Fill-Rect $g 4 1 8 14 (New-Color 58 31 17)
    Fill-Rect $g 5 2 6 12 (New-Color 137 62 22)
    Fill-Rect $g 6 4 4 7 (New-Color 255 96 18)
    Fill-Rect $g 7 5 2 5 (New-Color 255 219 73)
    Fill-Rect $g 7 7 2 1 (New-Color 255 255 190)
    Draw-Rect $g 4 1 8 14 (New-Color 31 20 13)
}

New-ItemTexture 'golem_treasure_bag' {
    param($bitmap, $g)
    Fill-Rect $g 4 3 8 10 (New-Color 75 36 84)
    Fill-Rect $g 3 5 10 7 (New-Color 104 52 117)
    Fill-Rect $g 5 2 6 2 (New-Color 191 118 37)
    Fill-Rect $g 5 5 6 5 (New-Color 136 71 143)
    Fill-Rect $g 7 6 2 3 (New-Color 255 174 38)
    Fill-Rect $g 6 11 4 2 (New-Color 54 27 64)
    Draw-Rect $g 3 4 10 9 (New-Color 30 18 35)
}

New-ItemTexture 'beetle_husk' {
    param($bitmap, $g)
    Fill-Rect $g 3 4 10 8 (New-Color 80 42 24)
    Fill-Rect $g 4 5 8 6 (New-Color 150 71 26)
    Fill-Rect $g 6 3 4 10 (New-Color 42 112 58 220)
    Fill-Rect $g 7 5 2 5 (New-Color 72 154 72 220)
    Draw-Rect $g 3 4 10 8 (New-Color 31 22 14)
}

New-ItemTexture 'sun_core' {
    param($bitmap, $g)
    Fill-Rect $g 5 1 6 14 (New-Color 118 50 22)
    Fill-Rect $g 1 5 14 6 (New-Color 118 50 22)
    Fill-Rect $g 4 4 8 8 (New-Color 255 91 20)
    Fill-Rect $g 6 6 4 4 (New-Color 255 220 78)
    Fill-Rect $g 7 7 2 2 (New-Color 255 255 190)
    Draw-Rect $g 4 4 8 8 (New-Color 63 33 17)
}

New-BlockTexture 'golem_altar_side' {
    param($bitmap, $g)
    Draw-StonePanel $bitmap $g 0 0 16 16 -2
    Draw-Line $g 1 4 14 4 (New-Color 65 33 17)
    Draw-Line $g 1 11 14 11 (New-Color 65 33 17)
    Draw-Line $g 5 1 5 14 (New-Color 61 31 16)
    Draw-Line $g 10 1 10 14 (New-Color 61 31 16)
    Fill-Rect $g 6 6 4 4 (New-Color 58 29 15)
    Fill-Rect $g 7 7 2 2 (New-Color 228 145 29)
    Fill-Rect $g 1 1 3 1 (New-Color 44 120 58)
    Fill-Rect $g 12 13 3 1 (New-Color 37 96 50)
}

New-BlockTexture 'golem_altar_base_top' {
    param($bitmap, $g)
    Draw-StonePanel $bitmap $g 0 0 16 16 -6
    Draw-Rect $g 2 2 12 12 (New-Color 62 32 17)
    Fill-Rect $g 4 6 8 4 (New-Color 78 36 18)
    Fill-Rect $g 5 7 6 2 (New-Color 222 131 24)
    Fill-Rect $g 7 6 2 4 (New-Color 255 219 70)
    Draw-Rect $g 4 5 8 5 (New-Color 45 25 14)
}

New-BlockTexture 'golem_altar_gold' {
    param($bitmap, $g)
    $g.Clear((New-Color 221 130 22))
    Fill-Rect $g 1 1 14 14 (New-Color 252 177 35)
    Fill-Rect $g 4 4 8 8 (New-Color 255 221 78)
    Fill-Rect $g 6 6 4 4 (New-Color 255 246 148)
    Draw-Rect $g 0 0 16 16 (New-Color 118 67 15)
}

New-BlockTexture 'golem_altar_star' {
    param($bitmap, $g)
    $g.Clear((New-Color 255 177 22))
    Fill-Rect $g 2 2 12 12 (New-Color 255 216 63)
    Fill-Rect $g 5 5 6 6 (New-Color 255 244 138)
    Fill-Rect $g 7 7 2 2 (New-Color 255 255 217)
    Draw-Line $g 0 8 15 8 (New-Color 255 231 92) 2
    Draw-Line $g 8 0 8 15 (New-Color 255 231 92) 2
    Draw-Line $g 3 3 12 12 (New-Color 255 202 42) 2
    Draw-Line $g 12 3 3 12 (New-Color 255 202 42) 2
}

New-BlockTexture 'golem_altar_top' {
    param($bitmap, $g)
    $top = [System.Drawing.Bitmap]::FromFile((Join-Path $blockDir 'golem_altar_base_top.png'))
    $g.DrawImage($top, 0, 0)
    $top.Dispose()
}

New-BlockTexture 'golem_altar' {
    param($bitmap, $g)
    $top = [System.Drawing.Bitmap]::FromFile((Join-Path $blockDir 'golem_altar_star.png'))
    $g.DrawImage($top, 0, 0)
    $top.Dispose()
}
