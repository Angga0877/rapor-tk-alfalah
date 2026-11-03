# Backup Otomatis Google Drive

## Cara mengaktifkan di APK
1. Pastikan aplikasi Google Drive terpasang dan akun Google sudah aktif di Android.
2. Buka Rapor TK Alfalah > Pengaturan > Backup otomatis Google Drive.
3. Tekan **Pilih folder Google Drive** lalu pilih folder khusus untuk cadangan Rapor TK Alfalah.
4. Buat kata sandi backup minimal 10 karakter dan simpan di tempat aman.
5. Tekan **Simpan kata sandi backup**. Aplikasi akan menjadwalkan backup awal.

## Cara kerja
- Database utama tetap SQLite lokal; koneksi internet tidak diperlukan untuk mengisi rapor.
- Setiap penyimpanan data yang berhasil menjadwalkan satu pekerjaan WorkManager.
- Pekerjaan hanya berjalan saat koneksi internet tersedia. Jika offline, pekerjaan menunggu dan dicoba kembali.
- Setiap file backup memakai enkripsi AES-256-GCM. Kunci diturunkan dari kata sandi dengan PBKDF2-HMAC-SHA256 dan salt acak.
- Password yang disimpan di perangkat dilindungi dengan Android Keystore.
- Folder menyimpan sampai 10 snapshot backup terbaru dengan nama bertanggal.
- File berekstensi .rta; file tidak dapat dibaca sebagai JSON biasa.

## Pemulihan saat HP hilang/rusak
1. Instal APK Rapor TK Alfalah pada HP baru.
2. Dari Pengaturan, gunakan pemilih file untuk memilih file .rta dari folder Google Drive.
3. Masukkan kata sandi backup yang sama ketika diminta.
4. Setelah data pulih, pilih kembali folder Google Drive dan simpan kata sandi yang sama untuk mengaktifkan backup otomatis pada HP baru.

## Batasan dan hal yang harus diuji
- Pemilihan folder memakai Android Storage Access Framework. Google Drive harus tersedia sebagai penyedia dokumen di perangkat dan pengguna harus memberikan izin folder.
- Keberhasilan menulis melalui penyedia Google Drive bergantung pada aplikasi Drive, status akun, koneksi, dan kebijakan Android pada perangkat.
- Backup terbaru baru muncul di Drive setelah pekerjaan selesai; saat offline, data terbaru masih berada di antrean lokal/WorkManager.
- Kata sandi tidak dapat dipulihkan oleh pengembang. Jika terlupa, file terenkripsi tidak dapat dibuka.
- Uji dengan data contoh terlebih dahulu: backup online, perubahan berulang, mode pesawat, koneksi pulih, restore ke HP/perangkat uji lain, kata sandi salah, dan file rusak.
- Fitur backup otomatis belum boleh dianggap tervalidasi di produksi sebelum APK dibangun dan skenario uji di atas berhasil pada perangkat target.

## Catatan keamanan
Backup otomatis terenkripsi. Tombol **Ekspor JSON (tanpa enkripsi)** adalah jalur manual yang menghasilkan data terbuka; simpan file tersebut di lokasi privat dan hapus jika tidak lagi diperlukan.
