# Ki?m Th?c T?i & Gi�p B?t T?

Ma ngu?n mod Minecraft Java Edition **1.21.1**, **NeoForge 21.1.262**,
Java **21**. Kh�ng d�ng v?i Forge, Fabric ho?c Bedrock.

## T�nh nang

- Ki?m Th?c T?i: ch? s? s�t thuong co b?n 1.000 (500 tim) khi d�n d�nh h?i d?y.
  S�t thuong th?c t? v?n ch?u co ch? gi�p, mi?n nhi?m v� c�c mod kh�c.
- Chu?t ph?i: g�y 1.000 s�t thuong l�n qu�i th� d?ch trong b�n k�nh 8 kh?i,
  ph?i nh�n th?y m?c ti�u; h?i chi�u 5 gi�y. Kh�ng d�nh ngu?i choi ho?c th� nu�i.
  D�nh thu?ng v?n tu�n theo lu?t PvP c?a m�y ch?.
- 4 m�n Gi�p B?t T?: mu, �o, qu?n, gi�y. Ch? khi m?c **d? 4 m�n** m?i b?t b?o v?.
- D? b?: h?y s�t thuong qua s? ki?n NeoForge, d?y m�u m?i tick, d?p l?a,
  h?i oxy, ch?ng d�ng bang, h?i th?c an v� x�a hi?u ?ng x?u m?i gi�y.
- Roi du?i d�y th? gi?i: dua l�n tr�n b? m?t c�ng c?t t?a d?.
  ? v�ng ho�n to�n tr?ng nhu r�a End, c� th? ti?p t?c roi; hay di chuy?n v? d?t.
- Ki?m v� gi�p kh�ng m?t d? b?n, kh�ng ch�y khi roi th�nh v?t ph?m.
- D�ng h�nh ?nh netherite c� �nh ph� ph�p; chua c� texture ri�ng.
- C�c v?t ph?m n?m trong tab Creative Combat v� c� t�n ti?ng Vi?t/ti?ng Anh.

Kh�ng cam k?t th?ng m?i mod: /kill c?a qu?n tr? vi�n v?n c� t�c d?ng.
Mod kh�c x�a th?c th?, d?t m�u tr?c ti?p ho?c can thi?p s? ki?n c� th? vu?t b?o v?.
Kh�ng c?p quy?n bay ho?c thay d?i ch? d? choi. Th�o b?t k? m�n n�o l� m?t b?o v?.

## Build tr�n Windows

1. C�i **JDK 21** t? https://adoptium.net/temurin/releases/?version=21 .
2. D?t `JAVA_HOME` v? thu m?c JDK 21 (kh�ng ph?i thu m?c `bin`), m? PowerShell m?i.
3. Gi?i n�n g�i ngu?n, m? PowerShell trong thu m?c ch?a `build-mod.ps1`.
4. Ch?y:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-mod.ps1
```

Script t?i Gradle 8.14.3 t? nh� ph�t h�nh, ki?m tra SHA-256 r?i ch?y build.
C?n Internet d? t?i Gradle, NeoForge v� thu vi?n. Kh�ng d?i ch�nh s�ch h? th?ng.
N?u da c� Gradle 8.14.3 + JDK 21, c� th? d�ng `gradle build` tr?c ti?p.

K?t qu? d? ki?n: `build/libs/reality-immortal-1.0.0.jar`.
Ch�p JAR v�o thu m?c `mods` c?a d�ng profile Minecraft 1.21.1 NeoForge.
N?u choi m�y ch?, c�i c�ng phi�n b?n mod ? c? m�y ch? v� c�c m�y ngu?i choi.
Kh�ng ch�p ZIP ma ngu?n v�o `mods`.

Ch?y m�i tru?ng th? nghi?m: `powershell -ExecutionPolicy Bypass -File .\build-mod.ps1 -Task runClient`.

## L?y v?t ph?m b?ng l?nh

B?t cheats ho?c c� quy?n OP:

```mcfunction
/give @s realityimmortal:reality_sword
/give @s realityimmortal:immortal_helmet
/give @s realityimmortal:immortal_chestplate
/give @s realityimmortal:immortal_leggings
/give @s realityimmortal:immortal_boots
```

## C�ng th?c (b�n ch? t?o)

Ki?m: S = sao d?a ng?c; N = th?i netherite; B = g?y blaze.

| | | |
|---|---|---|
| Tr?ng | S | Tr?ng |
| Tr?ng | N | Tr?ng |
| Tr?ng | B | Tr?ng |

M?i m�n gi�p: gh�p kh�ng d?nh h�nh **m�n gi�p netherite tuong ?ng + 1 sao d?a ng?c
+ 1 t�o v�ng ph� ph�p**. K?t qu? l� m�n m?i, kh�ng gi? t�n/ph� ph�p c?a nguy�n li?u.

## Ki?m tra v� gi?i h?n b?n b�n giao

Da ki?m tra JSON, t�n t�i nguy�n, ma v?t ph?m v� c� ph�p PowerShell b?ng c�ng c?.
**Chua bi�n d?ch ho?c ch?y Minecraft**: m�i tru?ng t?o ma ch? c� Java 8 v� kh�ng
truy c?p du?c m�y ch? t?i c�ng c?. D�y l� ma ngu?n, chua ph?i JAR du?c ki?m th?.

Tru?c khi d�ng v?i th? gi?i ch�nh, build v� ki?m tra tr�n m?t th? gi?i m?i:

1. M? game; ki?m tra 5 v?t ph?m, t�n, h�nh ?nh v� t?t c? c�ng th?c.
2. D�nh qu�i b?ng ki?m; chu?t ph?i g?n qu�i, th� nu�i v� ngu?i choi; ki?m tra h?i chi�u.
3. M?c d? b?, th? dung nham, roi cao, ng?t nu?c, d?c v� void.
4. Th�o m?t m�n, ki?m tra s�t thuong ho?t d?ng l?i; ki?m tra /kill v?n ho?t d?ng.
5. Th? server NeoForge ri�ng d? ki?m tra d?ng b? v� tuong th�ch c�c mod dang d�ng.

## Ch?nh code

- `RealitySword.java`: s�t thuong, b�n k�nh, h?i chi�u.
- `RealityImmortal.java`: dang ky v?t ph?m v� co ch? gi�p.
- `src/main/resources/data/realityimmortal/recipe`: c�ng th?c 1.21.1.
- `src/main/resources/assets/realityimmortal`: t�n v� m� h�nh v?t ph?m.

Tham chi?u API v� c?u h�nh:
- https://docs.neoforged.net/docs/1.21.1/items/tools/
- https://docs.neoforged.net/docs/1.21.1/concepts/events/
- https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle
