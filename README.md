# Rapor TK Alfalah — Android source v0.2.0

Tema biru-putih, responsif, aplikasi offline untuk satu guru/admin.

## Fitur prototipe
- Tema biru-putih dan tata letak responsif untuk ponsel/tablet.
- Catatan asesmen berdasarkan elemen CP PAUD.
- Penyusunan narasi capaian otomatis dari catatan guru; narasi dapat diedit sebelum disimpan.
- Data siswa, pertumbuhan, rapor, serta ekspor/impor JSON.
- Cetak melalui dialog cetak Android.

## Build APK
Gunakan Android Studio, JDK 17, Android SDK Platform 35, lalu jalankan `gradle assembleDebug`. Workflow GitHub Actions tersedia di `.github/workflows/build-apk.yml`.

## Batasan penting
Ini masih prototipe WebView v0.2.0. Penyimpanan memakai `localStorage`, belum Room/SQLite native. Fitur cadangan dan cetak perlu diuji pada perangkat Android. Jangan gunakan untuk data resmi siswa sebelum migrasi database, pengamanan akses, validasi backup/restore, dan pengujian menyeluruh.

## Rencana sebelum rilis
Migrasi Room/SQLite, PIN, backup/restore teruji, format rapor final, portofolio foto, kehadiran, tanda tangan, dan pengujian pada beberapa ukuran layar.