# God-set procedural pixel art; 32px item sprites and standard 64x32 armor UVs.
# Mirrors tools/make-textures.py (same palette and shapes) for Windows users.
Add-Type -AssemblyName System.Drawing
$ErrorActionPreference='Stop'
$dest=Join-Path $PSScriptRoot '../src/main/resources/assets/realityimmortal/textures'
New-Item -ItemType Directory -Force -Path "$dest/item","$dest/models/armor" | Out-Null
$pal=@{O='#2E2000';D='#8A5A00';M='#C9920E';G='#FFC93C';L='#FFE9A8';W='#FFFFFF';S='#E9EEF5';C='#8FD8FF';V='#FF9D2E'}
function Col($c) { [Drawing.ColorTranslator]::FromHtml($pal[$c]) }
function Rect($b,$x,$y,$w,$h,$c) { for($yy=$y;$yy -lt $y+$h;$yy++){for($xx=$x;$xx -lt $x+$w;$xx++){if($xx -ge 0 -and $yy -ge 0 -and $xx -lt $b.Width -and $yy -lt $b.Height){$b.SetPixel($xx,$yy,(Col $c))}}} }
function Poly($b,$points,$c) { $g=[Drawing.Graphics]::FromImage($b);$brush=[Drawing.SolidBrush]::new((Col $c));$pts=[Drawing.Point[]]@($points | ForEach-Object {[Drawing.Point]::new($_[0],$_[1])});$g.FillPolygon($brush,$pts);$brush.Dispose();$g.Dispose() }
function Save($b,$p) {$b.Save([IO.Path]::GetFullPath($p),[Drawing.Imaging.ImageFormat]::Png)}
$sword=[Drawing.Bitmap]::new(32,128)
for($f=0;$f -lt 4;$f++) {
 $b=[Drawing.Bitmap]::new(32,32)
 Poly $b @(@(4,29),@(2,27),@(10,19),@(12,21)) 'D'
 Rect $b 4 26 2 2 'G';Rect $b 7 23 2 2 'G'
 Rect $b 2 27 3 3 'G';Rect $b 3 28 1 1 'W'
 Poly $b @(@(7,19),@(22,4),@(30,0),@(29,9),@(14,24)) 'O'
 Poly $b @(@(8,18),@(23,4),@(29,2),@(28,8),@(14,22)) 'G'
 Poly $b @(@(10,17),@(24,5),@(28,3),@(26,8),@(15,21)) 'L'
 Poly $b @(@(12,16),@(25,5),@(26,6),@(24,10),@(16,19)) 'W'
 Poly $b @(@(14,16),@(24,6),@(25,7),@(15,17)) 'C'
 for($k=0;$k -lt 10;$k++) { $x=14+$k;$y=17-$k;if(($k+$f)%4 -eq 0){Rect $b $x $y 2 1 'W'} }
 if($f%2 -eq 0){Rect $b 28 1 1 3 'W';Rect $b 27 2 3 1 'W'} else {Rect $b 28 2 1 1 'W'}
 Poly $b @(@(4,16),@(7,13),@(18,24),@(17,28),@(13,26),@(12,22),@(9,19),@(5,19)) 'O'
 Poly $b @(@(5,16),@(8,15),@(16,23),@(17,26),@(14,24),@(11,21),@(7,18)) 'G'
 Poly $b @(@(6,16),@(8,16),@(14,22),@(15,24),@(13,23),@(10,20),@(7,17)) 'L'
 Rect $b 6 15 2 1 'W'
 Rect $b 10 19 3 3 'C';Rect $b 11 20 1 1 'W'
 $g=[Drawing.Graphics]::FromImage($sword);$g.DrawImageUnscaled($b,0,32*$f);$g.Dispose();$b.Dispose()
}
Save $sword "$dest/item/reality_sword.png";$sword.Dispose()
foreach($part in @('helmet','chestplate','leggings','boots')) {
 $b=[Drawing.Bitmap]::new(32,32)
 switch($part) {
  helmet {
   Poly $b @(@(5,24),@(5,10),@(9,5),@(22,5),@(26,10),@(26,24),@(21,27),@(20,18),@(11,18),@(10,27)) 'O'
   Poly $b @(@(7,22),@(7,11),@(10,7),@(21,7),@(24,11),@(24,23),@(22,24),@(21,16),@(10,16),@(9,24)) 'G'
   Rect $b 10 3 2 3 'G';Rect $b 15 2 2 4 'G';Rect $b 20 3 2 3 'G'
   Rect $b 10 3 2 1 'W';Rect $b 15 2 2 1 'W';Rect $b 20 3 2 1 'W'
   Rect $b 9 8 13 2 'L';Rect $b 9 10 14 2 'M'
   Rect $b 9 14 14 2 'C';Rect $b 11 14 10 1 'W'
   Rect $b 7 17 3 6 'S';Rect $b 22 17 3 6 'S'
   Rect $b 6 9 1 12 'L';Rect $b 25 9 1 12 'L'
  }
  chestplate {
   Poly $b @(@(3,8),@(10,5),@(13,8),@(18,8),@(21,5),@(28,8),@(30,16),@(25,18),@(24,27),@(7,27),@(6,18),@(1,16)) 'O'
   Poly $b @(@(4,9),@(9,7),@(12,10),@(19,10),@(22,7),@(27,9),@(28,14),@(23,16),@(22,25),@(9,25),@(8,16),@(3,14)) 'G'
   Rect $b 10 12 12 12 'M'
   Poly $b @(@(9,12),@(15,16),@(15,18),@(9,15)) 'S';Poly $b @(@(22,12),@(16,16),@(16,18),@(22,15)) 'S'
   Rect $b 15 11 2 3 'V';Rect $b 15 18 2 3 'V';Rect $b 11 15 3 2 'V';Rect $b 18 15 3 2 'V'
   Rect $b 12 12 2 2 'V';Rect $b 18 12 2 2 'V';Rect $b 12 18 2 2 'V';Rect $b 18 18 2 2 'V'
   Rect $b 14 14 4 4 'W'
   Rect $b 10 23 12 2 'D';Rect $b 14 23 4 2 'G';Rect $b 15 23 2 2 'W'
   Rect $b 3 10 5 2 'L';Rect $b 24 10 5 2 'L'
  }
  leggings {
   Rect $b 7 4 18 24 'O';Rect $b 9 6 14 6 'G';Rect $b 9 12 6 14 'M';Rect $b 17 12 6 14 'M'
   for($y=14;$y -lt 29;$y++){for($x=15;$x -lt 17;$x++){$b.SetPixel($x,$y,[Drawing.Color]::Transparent)}}
   Rect $b 8 6 16 2 'D';Rect $b 14 5 4 4 'G';Rect $b 15 6 2 2 'W'
   Rect $b 9 13 1 12 'L';Rect $b 22 13 1 12 'L'
   Rect $b 10 13 4 1 'G';Rect $b 18 13 4 1 'G'
   Rect $b 10 20 4 3 'C';Rect $b 18 20 4 3 'C'
   Rect $b 11 21 2 1 'W';Rect $b 19 21 2 1 'W'
  }
  boots {
   Rect $b 5 8 9 19 'O';Rect $b 18 8 9 19 'O';Rect $b 3 21 11 6 'O';Rect $b 18 21 11 6 'O'
   Rect $b 7 10 5 12 'G';Rect $b 20 10 5 12 'G';Rect $b 5 22 7 3 'M';Rect $b 20 22 7 3 'M'
   Rect $b 6 10 7 2 'L';Rect $b 19 10 7 2 'L'
   Rect $b 4 13 2 4 'S';Rect $b 26 13 2 4 'S'
   Rect $b 4 12 1 2 'W';Rect $b 27 12 1 2 'W'
   Rect $b 9 14 2 2 'C';Rect $b 21 14 2 2 'C'
   Rect $b 4 24 9 1 'D';Rect $b 19 24 9 1 'D'
  }
 }
 Save $b "$dest/item/immortal_$part.png";$b.Dispose()
}
# Armor UVs use the vanilla humanoid cuboid layout at 64x32.
for($layer=1;$layer -le 2;$layer++) {
 $b=[Drawing.Bitmap]::new(64,32)
 # Bright divine gold across all occupied UV regions.
 foreach($r in @(@(0,0,32,16),@(0,16,16,16),@(16,16,24,16),@(40,16,16,16))) {
  Rect $b $r[0] $r[1] $r[2] $r[3] 'G'
  for($y=$r[1];$y -lt $r[1]+$r[3];$y++){for($x=$r[0];$x -lt $r[0]+$r[2];$x++){
    if(($x+2*$y)%7 -eq 0){Rect $b $x $y 1 1 'M'}
  }}
 }
 # Helmet front (8,8) 8x8: crown band, visor, cheek plates.
 Rect $b 8 8 8 1 'L';Rect $b 8 10 8 1 'M';Rect $b 8 11 8 1 'C'
 Rect $b 10 11 4 1 'W'
 Rect $b 10 12 4 3 'D';Rect $b 8 12 1 4 'S';Rect $b 15 12 1 4 'S'
 Rect $b 11 8 2 2 'W';Rect $b 0 11 8 1 'C';Rect $b 16 11 16 1 'C'
 # Chest front (20,20) 8x12, back (32,20) 8x12: sun emblem.
 Rect $b 20 20 8 2 'M';Rect $b 20 22 2 2 'C';Rect $b 26 22 2 2 'C'
 Rect $b 23 21 2 2 'V';Rect $b 23 27 2 2 'V';Rect $b 20 24 2 2 'V';Rect $b 26 24 2 2 'V'
 Rect $b 21 22 2 1 'V';Rect $b 25 22 2 1 'V';Rect $b 21 26 2 1 'V';Rect $b 25 26 2 1 'V'
 Rect $b 22 23 4 4 'W'
 Rect $b 20 29 8 1 'D';Rect $b 20 31 8 1 'L'
 Rect $b 35 21 2 8 'M';Rect $b 35 23 2 4 'C';Rect $b 32 29 8 1 'D'
 # Arms and boots/leggings use shared 4x12 front UVs.
 Rect $b 44 20 4 3 'M';Rect $b 44 20 4 1 'L';Rect $b 44 27 4 1 'D';Rect $b 45 23 1 4 'V'
 Rect $b 4 20 4 1 'L';Rect $b 4 21 1 9 'C';Rect $b 5 25 3 2 'M';Rect $b 4 30 4 1 'L'
 Rect $b 12 30 4 1 'L';Rect $b 48 27 8 1 'D'
 if($layer -eq 2){Rect $b 20 20 8 3 'M';Rect $b 20 20 8 1 'L';Rect $b 23 20 2 2 'C'}
 Save $b "$dest/models/armor/immortal_layer_$layer.png";$b.Dispose()
}
