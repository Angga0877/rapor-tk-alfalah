# Rapor TK Alfalah — Android Offline

Aplikasi rapor digital TK Alfalah dengan antarmuka biru-putih yang responsif untuk ponsel Android.

## Status pengembangan

**Versi sumber: 0.3.0 — migrasi SQLite native sedang diuji.**

Aplikasi menggunakan Android WebView untuk antarmuka dan SQLite native melalui `SQLiteOpenHelper` untuk penyimpanan utama. Tidak membutuhkan akun, server, atau koneksi internet untuk operasional harian.

## Fitur prototipe saat ini

- Data peserta didik: nama, nomor induk, kelompok, tanggal lahir.
- Catatan asesmen berdasarkan elemen Capaian Pembelajaran PAUD.
- Penyusunan narasi capaian otomatis dari catatan pengamatan; guru tetap dapat mengedit narasi.
- Data pertumbuhan: berat dan tinggi badan.
- Rapor per siswa dan periode semester.
- Ekspor dan impor cadangan JSON.
- Cetak melalui layanan cetak Android.
- Migrasi awal data lama dari `localStorage` ke SQLite ketika aplikasi pertama kali dibuka setelah pembaruan.

## Struktur database SQLite

| Tabel | Fungsi |
|---|---|
| `students` | Identitas peserta didik |
| `assessments` | Catatan asesmen dan stimulasi |
| `growth` | Riwayat pengukuran pertumbuhan |
| `reports` | Deskripsi rapor per siswa dan semester |
| `settings` | Identitas sekolah dan periode aktif |

Relasi catatan asesmen, pertumbuhan, dan rapor menggunakan ID siswa. Penghapusan siswa menghapus catatan turunannya melalui foreign key cascade. Penyimpanan perubahan dilakukan dalam transaksi SQLite.

## Build APK

Persyaratan:
- JDK 17
- Android SDK Platform 35
- Gradle 8.9

Build lokal:
```bash
gradle assembleDebug
```

APK debug akan dibuat di:
`app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions akan menjalankan build pada push ke branch `main` dan menyimpan APK sebagai workflow artifact jika build berhasil.

## Data dan keamanan

- Database disimpan di direktori privat aplikasi Android.
- Aplikasi tidak meminta izin internet.
- Backup JSON harus disimpan di tempat aman; file backup berisi data siswa dan tidak dienkripsi oleh aplikasi.
- Menghapus aplikasi atau membersihkan data aplikasi dapat menghapus database lokal. Ekspor backup secara berkala.
- Jangan gunakan data anak sesungguhnya sebelum pengujian perangkat, validasi backup/restore, dan pemeriksaan keamanan selesai.

## Batasan yang masih harus diselesaikan sebelum rilis

- Pengujian build APK dan instalasi pada perangkat Android nyata.
- Uji migrasi dari versi localStorage dan uji pemulihan cadangan.
- PIN/otentikasi lokal dan perlindungan data sensitif.
- Form rapor final sesuai format sekolah dan Kurikulum Merdeka PAUD.
- Validasi input, pengelolaan foto portofolio, absensi, dan tanda tangan.
- Pengujian kompatibilitas pada berbagai ukuran layar dan versi Android.

## Catatan teknis

Penyimpanan saat ini memakai SQLite native melalui `SQLiteOpenHelper`, bukan Room. Ini tetap database SQLite lokal yang terstruktur dan mendukung transaksi serta relasi. Migrasi ke Room dapat dilakukan kemudian bila kebutuhan repository dan pengujian sudah disiapkan.
