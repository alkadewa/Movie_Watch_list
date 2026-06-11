#  CineTrack - Cinematic Movie Watchlist

CineTrack adalah aplikasi Android modern yang dirancang khusus untuk para pencinta film (cinephiles). Aplikasi ini memungkinkan pengguna untuk mengelola daftar tontonan mereka dengan alur kerja yang intuitif dan desain antarmuka "Cinematic Dark Mode" yang premium.

##  Fitur Utama

- **Premium Cinematic UI**: Desain mode gelap dengan aksen warna merah khas bioskop untuk pengalaman visual yang mendalam.
- **Smart Library Workflow**: 
  - Film baru masuk ke daftar **General**.
  - Pindahkan film ke kategori **Want to Watch**, **Watching**, atau **Watched** hanya dengan satu klik.
- **Real-Time Search**: Cari film di seluruh kategori secara instan saat Anda mengetik.
- **Rating & Reviews**: Berikan bintang dan tulis ulasan pribadi untuk film yang telah Anda tonton.
- **Release Year Tracking**: Informasi tahun rilis yang rapi untuk setiap film.
- **Multi-Genre Selection**: Pilih berbagai genre sekaligus untuk setiap film.
- **Firebase Integration**: 
  - **Authentication**: Keamanan akun dengan fitur Ganti Password dan Hapus Akun Permanen.
  - **Realtime Database**: Sinkronisasi data film Anda secara instan di semua perangkat.

##  Teknologi yang Digunakan

- **Language**: Java / Kotlin (Android)
- **Database & Auth**: Firebase (Realtime Database & Firebase Auth)
- **Network**: Retrofit (untuk integrasi API masa depan)
- **UI Components**: Material Components for Android (Material 3)
- **Architecture**: Model-View-Adapter (MVA)

##  Tampilan Aplikasi

- **Home**: Daftar horizontal film baru dan sistem Tab untuk kategori.
- **Search**: Pencarian global dengan label status (WATCHING, WATCHED, dll).
- **Profile**: Manajemen akun, ganti password, dan logo aplikasi yang elegan.

##  Cara Menjalankan
1. Clone repositori ini: `git clone https://github.com/alkadewa/Movie_Watch_list.git`
2. Buka project di **Android Studio**.
3. Pastikan Anda sudah menghubungkan file `google-services.json` milik Anda sendiri ke folder `app/`.
4. *Build* dan jalankan di emulator atau perangkat fisik.

---
*Dibuat dengan ❤️ untuk para penikmat film.*
