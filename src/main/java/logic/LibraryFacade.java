package logic;

import database.DatabaseConnection;
import model.Book;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

//FACADE tasarım deseni (sisttemdeki karışıklığı tek bir yerde toplamak)

public class LibraryFacade {
    private Connection conn;
    private NotificationSystem notifier;

    public LibraryFacade() {
        conn = DatabaseConnection.getInstance().getConnection();
        notifier = new NotificationSystem();
        notifier.addObserver(msg -> System.out.println("SİSTEM BİLDİRİMİ: " + msg));
    }

    public void initSystem() {
        try {
            Statement s = conn.createStatement();

            // 1. AYARLAR TABLOSU
            s.execute("CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT)");
            s.execute("INSERT OR IGNORE INTO settings VALUES ('gunluk_ceza', '5')");
            s.execute("INSERT OR IGNORE INTO settings VALUES ('max_odunc', '3')");

            // 2. KULLANICILAR TABLOSU
            s.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, role TEXT, ad_soyad TEXT, telefon TEXT)");

            // 3. KİTAPLAR TABLOSU (YENİ SÜTUNLAR EKLENDİ)
            s.execute("CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY AUTOINCREMENT, isbn TEXT, ad TEXT, yazar TEXT, kategori TEXT, stok INTEGER, yayinevi TEXT, baski_yili INTEGER)");

            // 4. ÖDÜNÇ TABLOSU
            s.execute("CREATE TABLE IF NOT EXISTS loans (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, book_id INTEGER, alis_tarihi TEXT, planlanan_iade TEXT, durum TEXT)");

            // --- DEMO VERİLER (Sistem ilk açıldığında boş kalmasın diye) ---
            s.execute("INSERT OR IGNORE INTO users (username, password, role, ad_soyad) VALUES ('admin', 'admin', 'ceo', 'Sistem Yöneticisi')");
            s.execute("INSERT OR IGNORE INTO users (username, password, role, ad_soyad, telefon) VALUES ('ahmet', '123', 'personel', 'Ahmet Personel', '555-1111')");
            s.execute("INSERT OR IGNORE INTO users (username, password, role, ad_soyad, telefon) VALUES ('ali', '123', 'uye', 'Ali Yılmaz', '555-2222')");

            // Kitap tablosu boşsa 5 tane örnek kitap ekle
            ResultSet rs = s.executeQuery("SELECT count(*) FROM books");
            if(rs.next() && rs.getInt(1) == 0) {
                String[] books = {
                        "('9789750719387', 'Kürk Mantolu Madonna', 'Sabahattin Ali', 'Roman', 5, 'Yapı Kredi Yayınları', 2021)",
                        "('9786053606116', 'Suç ve Ceza', 'Fyodor Dostoyevski', 'Klasik', 3, 'İş Bankası Kültür', 2020)",
                        "('9789753638029', 'Harry Potter', 'J.K. Rowling', 'Fantastik', 7, 'YKY', 2019)",
                        "('9789750726439', '1984', 'George Orwell', 'Bilim Kurgu', 6, 'Can Yayınları', 2022)",
                        "('9789751412638', 'Nutuk', 'Mustafa Kemal Atatürk', 'Tarih', 10, 'Remzi Kitabevi', 2023)"
                };
                for(String b : books) {
                    s.execute("INSERT INTO books (isbn, ad, yazar, kategori, stok, yayinevi, baski_yili) VALUES " + b);
                }
            }

        } catch(Exception e){ e.printStackTrace(); }
    }

    public List<Book> searchBooks(String col, String val) {
        List<Book> list = new ArrayList<>();
        try {
            // Veriyi çekerken yeni sütunları unutmuyoruz
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM books WHERE " + col + " LIKE '%" + val + "%'");
            while(rs.next()) {
                Book b = new Book.BookBuilder()
                        .setAd(rs.getString("ad"))
                        .setYazar(rs.getString("yazar"))
                        .setKategori(rs.getString("kategori"))
                        .setStok(rs.getInt("stok"))
                        .setIsbn(rs.getString("isbn"))          // Yeni
                        .setYayinevi(rs.getString("yayinevi"))  // Yeni
                        .setBaskiYili(rs.getInt("baski_yili"))  // Yeni
                        .build();
                b.setId(rs.getInt("id"));
                list.add(b);
            }
        } catch(Exception e){ e.printStackTrace(); }
        return list;
    }

    public void addBook(Book b) {
        try {
            // String birleştirirken tek tırnak (') hatası yapmamak çok önemli
            // Metinler tek tırnak içinde, sayılar tırnaksız yazılır.
            String sql = "INSERT INTO books (ad, yazar, isbn, stok, kategori, yayinevi, baski_yili) VALUES ('"+
                    b.getAd()+"','"+b.getYazar()+"','"+b.getIsbn()+"',"+b.getStok()+",'"+b.getKategori()+"','"+b.getYayinevi()+"',"+b.getBaskiYili()+")";

            conn.createStatement().execute(sql);
            System.out.println("Kitap başarıyla veritabanına eklendi: " + b.getAd()); // Kontrol için konsola yaz
        } catch(Exception e){
            e.printStackTrace(); // Hata varsa konsolda görelim
        }
    }

    public void deleteBook(int bid) {
        try { conn.createStatement().execute("DELETE FROM books WHERE id="+bid); } catch(Exception e){}
    }

    public String borrowBook(int bid, int uid) {
        try {
            int max = Integer.parseInt(getSetting("max_odunc"));
            ResultSet rs = conn.createStatement().executeQuery("SELECT count(*) FROM loans WHERE user_id="+uid+" AND durum='aktif'");
            if(rs.next() && rs.getInt(1) >= max) return "Limit Dolu!";

            ResultSet rs2 = conn.createStatement().executeQuery("SELECT stok FROM books WHERE id="+bid);
            if(rs2.next() && rs2.getInt(1) > 0) {
                conn.createStatement().execute("INSERT INTO loans (user_id, book_id, alis_tarihi, planlanan_iade, durum) VALUES ("+uid+","+bid+",'"+LocalDate.now()+"','"+LocalDate.now().plusDays(15)+"','aktif')");
                conn.createStatement().execute("UPDATE books SET stok=stok-1 WHERE id="+bid);
                return "Başarılı!";
            }
            return "Stok Yok!";
        } catch(Exception e){ return "Hata: " + e.getMessage(); }
    }

    public void returnBook(int bid, int uid) {
        try {
            conn.createStatement().execute("UPDATE loans SET durum='iade' WHERE book_id="+bid+" AND user_id="+uid+" AND durum='aktif'");
            conn.createStatement().execute("UPDATE books SET stok=stok+1 WHERE id="+bid);
            notifier.notifyAll("Kitap (ID: "+bid+") iade edildi.");
        } catch(Exception e){ e.printStackTrace(); }
    }

    public void getUserLoans(int uid, DefaultTableModel m) {
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT b.id, b.ad, l.planlanan_iade FROM loans l JOIN books b ON l.book_id=b.id WHERE l.user_id="+uid+" AND l.durum='aktif'");
            double ceza = Double.parseDouble(getSetting("gunluk_ceza"));
            while(rs.next()){
                long gun = ChronoUnit.DAYS.between(LocalDate.parse(rs.getString("planlanan_iade")), LocalDate.now());
                m.addRow(new Object[]{rs.getInt("id"), rs.getString("ad"), rs.getString("planlanan_iade"), (gun>0 ? gun*ceza : 0)});
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    public void getMembers(DefaultTableModel m) {
        try { ResultSet rs=conn.createStatement().executeQuery("SELECT * FROM users WHERE role='uye'");
            while(rs.next()) m.addRow(new Object[]{rs.getInt("id"), rs.getString("ad_soyad"), rs.getString("telefon")}); } catch(Exception e){}
    }
    public void getAllUsers(DefaultTableModel m) {
        try { ResultSet rs=conn.createStatement().executeQuery("SELECT * FROM users");
            while(rs.next()) m.addRow(new Object[]{rs.getInt("id"), rs.getString("ad_soyad"), rs.getString("username"), rs.getString("role")}); } catch(Exception e){}
    }
    public void addOrUpdateUser(String u, String p, String r, String a, String t) {
        try { conn.createStatement().execute("INSERT OR REPLACE INTO users (username, password, role, ad_soyad, telefon) VALUES ('"+u+"','"+p+"','"+r+"','"+a+"','"+t+"')"); } catch(Exception e){}
    }
    public void updateProfile(int uid, String ad, String tel, String pass) {
        try {
            conn.createStatement().execute("UPDATE users SET ad_soyad='"+ad+"', telefon='"+tel+"' WHERE id="+uid);
            if(!pass.isEmpty()) conn.createStatement().execute("UPDATE users SET password='"+pass+"' WHERE id="+uid);
        } catch(Exception e){}
    }
    public String getSetting(String k) {
        try { ResultSet rs = conn.createStatement().executeQuery("SELECT value FROM settings WHERE key='"+k+"'"); if(rs.next()) return rs.getString("value"); } catch(Exception e){} return "0";
    }
    public void updateSetting(String k, String v) { try{conn.createStatement().execute("UPDATE settings SET value='"+v+"' WHERE key='"+k+"'");}catch(Exception e){} }
}