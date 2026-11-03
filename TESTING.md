# Checklist Pengujian Rapor TK Alfalah

Status: daftar pengujian yang harus dijalankan sebelum APK dinyatakan siap dipakai sekolah.

## Build dan instalasi
- [ ] GitHub Actions menyelesaikan job build tanpa error.
- [ ] Artifact APK tersedia dan ukuran file lebih dari 0 byte.
- [ ] APK dapat dipasang pada Android 7.0 (API 24) atau lebih baru.
- [ ] Aplikasi dapat dibuka tanpa koneksi internet.

## Database dan data
- [ ] Tambahkan siswa, tutup aplikasi, buka kembali, dan pastikan data tetap ada.
- [ ] Tambahkan asesmen dan pastikan catatan terhubung ke siswa yang benar.
- [ ] Tambahkan pengukuran pertumbuhan dan periksa riwayatnya.
- [ ] Simpan rapor semester ganjil, ubah periode ke semester genap, dan pastikan rapor ganjil tetap ada.
- [ ] Hapus siswa dan pastikan asesmen, pertumbuhan, serta rapor turunannya ikut terhapus.
- [ ] Uji migrasi data dari versi localStorage lama; data lama harus tetap tersedia sampai transaksi migrasi berhasil.

## Backup dan pemulihan
- [ ] Ekspor cadangan JSON melalui pemilih lokasi Android.
- [ ] Periksa file JSON dapat dibuka dan berisi data yang diharapkan.
- [ ] Impor kembali file cadangan dan bandingkan jumlah data.
- [ ] Uji file JSON yang rusak/tidak lengkap; aplikasi harus menolak tanpa merusak data yang sudah tersimpan.

## Tampilan dan cetak
- [ ] Uji layar ponsel kecil, ponsel besar, dan tablet.
- [ ] Uji papan ketik saat mengisi form.
- [ ] Uji cetak rapor ke PDF melalui layanan cetak Android.
- [ ] Pastikan teks tidak terpotong dan format rapor terbaca.

## Catatan keamanan
- Database berada di penyimpanan privat aplikasi, tetapi belum dienkripsi dengan SQLCipher.
- File backup JSON tidak dienkripsi. Simpan di lokasi tepercaya dan jangan membagikannya sembarangan.
- Jangan gunakan data siswa asli untuk operasional sebelum seluruh checklist kritis selesai.
