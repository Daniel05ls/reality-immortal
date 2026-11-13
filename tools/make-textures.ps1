# Original procedural pixel art; 32px item sprites and standard 64x32 armor UVs.
Add-Type -AssemblyName System.Drawing
$ErrorActionPreference='Stop'
$dest=Join-Path $PSScriptRoot '../src/main/resources/assets/realityimmortal/textures'
New-Item -ItemType Directory -Force -Path "$dest/item","$dest/models/armor" | Out-Null
$pal=@{O='#0B0C20';D='#191933';M='#302652';P='#6843A8';V='#B66CFF';C='#40E9EB';W='#D9FFFF';G='#F5C96D'}
function Col($c) { [Drawing.ColorTranslator]::FromHtml($pal[$c]) }
function Rect($b,$x,$y,$w,$h,$c) { for($yy=$y;$yy -lt $y+$h;$yy++){for($xx=$x;$xx -lt $x+$w;$xx++){if($xx -ge 0 -and $yy -ge 0 -and $xx -lt $b.Width -and $yy -lt $b.Height){$b.SetPixel($xx,$yy,(Col $c))}}} }
function Poly($b,$points,$c) { $g=[Drawing.Graphics]::FromImage($b);$brush=[Drawing.SolidBrush]::new((Col $c));$pts=[Drawing.Point[]]@($points | ForEach-Object {[Drawing.Point]::new($_[0],$_[1])});$g.FillPolygon($brush,$pts);$brush.Dispose();$g.Dispose() }
function Save($b,$p) {$b.Save([IO.Path]::GetFullPath($p),[Drawing.Imaging.ImageFormat]::Png)}
$sword=[Drawing.Bitmap]::new(32,128)
for($f=0;$f -lt 4;$f++) {
 $b=[Drawing.Bitmap]::new(32,32)
 Poly $b @(@(4,29),@(2,27),@(10,19),@(12,21)) 'O'
 Poly $b @(@(4,27),@(10,21),@(11,22),@(5,28)) 'P'
 Rect $b 3 27 2 2 'G'
 Poly $b @(@(8,18),@(23,3),@(30,1),@(28,8),@(14,23)) 'O'
 Poly $b @(@(9,18),@(24,4),@(29,2),@(27,8),@(14,21)) 'C'
 Poly $b @(@(11,18),@(25,5),@(28,3),@(25,9),@(14,20)) 'P'
 Poly $b @(@(13,17),@(26,4),@(24,10),@(16,19)) 'D'
 Poly $b @(@(14,17),@(26,5),@(23,11),@(16,18)) 'V'
 for($k=0;$k -lt 10;$k++) { $x=14+$k;$y=17-$k;if(($k+$f)%4 -eq 0){Rect $b $x $y 2 1 'W'} }
 Poly $b @(@(5,15),@(7,14),@(17,24),@(16,27),@(13,25),@(13,22),@(9,18),@(6,18)) 'O'
 Poly $b @(@(6,15),@(8,16),@(15,23),@(16,25),@(14,24),@(11,20),@(7,17)) 'G'
 Rect $b 10 19 3 3 'C';Rect $b 11 20 1 1 'W'
 Rect $b 22 11 2 1 'C';Rect $b 25 12 1 2 'V'
 $g=[Drawing.Graphics]::FromImage($sword);$g.DrawImageUnscaled($b,0,32*$f);$g.Dispose();$b.Dispose()
}
Save $sword "$dest/item/reality_sword.png";$sword.Dispose()
foreach($part in @('helmet','chestplate','leggings','boots')) {
 $b=[Drawing.Bitmap]::new(32,32)
 switch($part) {
  helmet {
   Poly $b @(@(5,24),@(5,10),@(9,5),@(22,5),@(26,10),@(26,24),@(21,27),@(20,18),@(11,18),@(10,27)) 'O'
   Poly $b @(@(7,22),@(7,11),@(10,7),@(21,7),@(24,11),@(24,23),@(22,24),@(21,16),@(10,16),@(9,24)) 'P'
   Rect $b 9 10 14 5 'M';Rect $b 8 15 16 2 'C';Rect $b 11 16 10 2 'O'
   Poly $b @(@(14,7),@(16,4),@(18,7),@(16,11)) 'G';Rect $b 15 7 2 2 'W'
   Rect $b 6 9 1 12 'C';Rect $b 25 9 1 12 'V'
  }
  chestplate {
   Poly $b @(@(3,8),@(10,5),@(13,8),@(18,8),@(21,5),@(28,8),@(30,16),@(25,18),@(24,27),@(7,27),@(6,18),@(1,16)) 'O'
   Poly $b @(@(4,9),@(9,7),@(12,10),@(19,10),@(22,7),@(27,9),@(28,14),@(23,16),@(22,25),@(9,25),@(8,16),@(3,14)) 'P'
   Rect $b 10 12 12 12 'M';Rect $b 10 23 12 2 'G'
   Poly $b @(@(9,12),@(15,16),@(15,18),@(9,15)) 'C';Poly $b @(@(22,12),@(16,16),@(16,18),@(22,15)) 'C'
   Poly $b @(@(16,12),@(19,16),@(16,20),@(13,16)) 'V';Rect $b 15 15 2 3 'W'
   Rect $b 3 10 5 2 'C';Rect $b 24 10 5 2 'C';Rect $b 12 20 8 1 'P'
  }
  leggings {
   Rect $b 7 4 18 24 'O';Rect $b 9 6 14 6 'P';Rect $b 9 12 6 14 'M';Rect $b 17 12 6 14 'M'
   for($y=14;$y -lt 29;$y++){for($x=15;$x -lt 17;$x++){$b.SetPixel($x,$y,[Drawing.Color]::Transparent)}}
   Rect $b 8 6 16 2 'G';Rect $b 14 5 4 4 'C';Rect $b 15 6 2 2 'W'
   Rect $b 9 13 1 12 'C';Rect $b 22 13 1 12 'C';Rect $b 10 20 4 3 'P';Rect $b 18 20 4 3 'P'
   Rect $b 10 21 4 1 'V';Rect $b 18 21 4 1 'V'
  }
  boots {
   Rect $b 5 8 9 19 'O';Rect $b 18 8 9 19 'O';Rect $b 3 21 11 6 'O';Rect $b 18 21 11 6 'O'
   Rect $b 7 10 5 12 'M';Rect $b 20 10 5 12 'M';Rect $b 5 22 7 3 'P';Rect $b 20 22 7 3 'P'
   Rect $b 6 10 7 2 'G';Rect $b 19 10 7 2 'G';Rect $b 7 13 1 9 'C';Rect $b 24 13 1 9 'C'
   Rect $b 4 24 9 1 'C';Rect $b 19 24 9 1 'C';Rect $b 9 14 2 2 'V';Rect $b 21 14 2 2 'V'
  }
 }
 Save $b "$dest/item/immortal_$part.png";$b.Dispose()
}
# Armor UVs use the vanilla humanoid cuboid layout at 64x32.
for($layer=1;$layer -le 2;$layer++) {
 $b=[Drawing.Bitmap]::new(64,32)
 # Dark iridescent metal across all occupied UV regions.
 foreach($r in @(@(0,0,32,16),@(0,16,16,16),@(16,16,24,16),@(40,16,16,16))) {
  Rect $b $r[0] $r[1] $r[2] $r[3] 'D'
  for($y=$r[1];$y -lt $r[1]+$r[3];$y++){for($x=$r[0];$x -lt $r[0]+$r[2];$x++){
    if(($x+2*$y)%7 -eq 0){Rect $b $x $y 1 1 'M'}
  }}
 }
 # Helmet front (8,8) 8x8: crown, visor, cheek plates.
 Rect $b 8 8 8 1 'G';Rect $b 8 10 8 1 'P';Rect $b 8 11 8 1 'C'
 Rect $b 10 12 4 3 'O';Rect $b 8 12 1 4 'V';Rect $b 15 12 1 4 'V'
 Rect $b 11 8 2 2 'W';Rect $b 0 11 8 1 'C';Rect $b 16 11 16 1 'C'
 # Chest front (20,20) 8x12, back (32,20) 8x12.
 Rect $b 20 20 8 2 'P';Rect $b 20 22 2 2 'C';Rect $b 26 22 2 2 'C'
 Rect $b 22 24 4 1 'C';Rect $b 23 22 2 5 'V';Rect $b 23 24 2 2 'W'
 Rect $b 20 29 8 1 'G';Rect $b 20 31 8 1 'P'
 Rect $b 35 21 2 8 'P';Rect $b 35 23 2 4 'C';Rect $b 32 29 8 1 'G'
 # Arms and boots/leggings use shared 4x12 front UVs.
 Rect $b 44 20 4 3 'P';Rect $b 44 20 4 1 'C';Rect $b 44 27 4 1 'G';Rect $b 45 23 1 4 'V'
 Rect $b 4 20 4 1 'G';Rect $b 4 21 1 9 'C';Rect $b 5 25 3 2 'P';Rect $b 4 30 4 1 'C'
 Rect $b 12 30 4 1 'C';Rect $b 48 27 8 1 'G'
 if($layer -eq 2){Rect $b 20 20 8 3 'M';Rect $b 20 20 8 1 'G';Rect $b 23 20 2 2 'C'}
 Save $b "$dest/models/armor/immortal_layer_$layer.png";$b.Dispose()
}
