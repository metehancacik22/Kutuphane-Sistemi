/*
package com.example.kutuphanesistemi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

// --- UYGULAMA BAŞLANGICI ---
public class HelloApplication extends JFrame {

    // --- RENK PALETİ (Senin kodunla birebir aynı) ---
    private final Color CLR_BLUE   = new Color(52, 152, 219);
    private final Color CLR_RED    = new Color(231, 76, 60);
    private final Color CLR_GREEN  = new Color(46, 204, 113);
    private final Color CLR_ORANGE = new Color(243, 156, 18);
    private final Color CLR_DARK   = new Color(44, 62, 80);
    private final Font  FONT_BOLD  = new Font("Segoe UI", Font.BOLD, 14);

    // --- (DESEN 7) FACADE PATTERN: Tüm SQL işlerini bu sınıf yönetir ---
    private final LibraryFacade libraryFacade;
    private BaseUser currentUser; // Abstract class kullanımı

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    public static void main(String[] args) {
        // --- (DESEN 1) SINGLETON PATTERN: Veritabanı başlatılıyor ---
        DatabaseConnection.getInstance();
        SwingUtilities.invokeLater(() -> new HelloApplication().setVisible(true));
    }

    public HelloApplication() {
        libraryFacade = new LibraryFacade();
        libraryFacade.initSystem(); // Tabloları ve Demo verileri yükle (Senin verilerin)

        setTitle("Kütüphane Yönetim Sistemi - ULTIMATE (Pro)");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel.add(createLoginPanel(), "LOGIN");
        add(mainPanel);
    }

    // --- 1. GİRİŞ EKRANI (Görünüm AYNI) ---
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CLR_DARK);

        JPanel card = new JPanel(new GridLayout(5, 1, 10, 15));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(400, 400));

        JLabel lblTitle = new JLabel("SİSTEM GİRİŞİ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(CLR_DARK);

        JTextField txtUser = new JTextField(); txtUser.setBorder(BorderFactory.createTitledBorder("Kullanıcı Adı"));
        JPasswordField txtPass = new JPasswordField(); txtPass.setBorder(BorderFactory.createTitledBorder("Şifre"));

        JButton btnLogin = createBtn("GİRİŞ YAP", CLR_BLUE);

        btnLogin.addActionListener(e -> {
            // --- (DESEN 2) FACTORY PATTERN: Kullanıcı üretimi ---
            BaseUser user = UserFactory.login(txtUser.getText(), new String(txtPass.getPassword()));
            if(user != null) {
                currentUser = user;
                mainPanel.add(createDashboardPanel(), "DASHBOARD");
                cardLayout.show(mainPanel, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this, "Hatalı Giriş!");
            }
        });

        JLabel lblInfo = new JLabel("<html><center>Admin: admin/admin <br> Üyeler: ali/123 <br> Personel: ahmet/123</center></html>", SwingConstants.CENTER);

        card.add(lblTitle); card.add(txtUser); card.add(txtPass); card.add(btnLogin); card.add(lblInfo);
        panel.add(card);
        return panel;
    }

    // --- 2. DASHBOARD (Görünüm AYNI) ---
    private JPanel createDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CLR_DARK);
        header.setPreferredSize(new Dimension(100, 70));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lbl = new JLabel("Hoşgeldin, " + currentUser.getAdSoyad() + " (" + currentUser.getRole().toUpperCase() + ")");
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        lbl.setForeground(Color.WHITE);

        JButton btnLogout = createBtn("ÇIKIŞ YAP", CLR_RED);
        btnLogout.setPreferredSize(new Dimension(120, 40));
        btnLogout.addActionListener(e -> { currentUser = null; cardLayout.show(mainPanel, "LOGIN"); });

        header.add(lbl, BorderLayout.WEST); header.add(btnLogout, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        if (currentUser.getRole().equals("ceo")) p.add(createCEOTabs(), BorderLayout.CENTER);
        else if (currentUser.getRole().equals("personel")) p.add(createStaffTabs(), BorderLayout.CENTER);
        else p.add(createMemberTabs(), BorderLayout.CENTER);

        return p;
    }

    // --- 3. ÜYE PANELİ (Görünüm AYNI) ---
    private JTabbedPane createMemberTabs() {
        JTabbedPane tabs = new JTabbedPane();

        // KİTAPLAR SEKME
        JPanel pnlBooks = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> cmbKriter = new JComboBox<>(new String[]{"Kitap Adı", "Yazar", "Kategori"});
        JTextField txtAra = new JTextField(20);
        JButton btnAra = createBtn("ARA", CLR_BLUE);
        top.add(new JLabel("Filtrele:")); top.add(cmbKriter); top.add(txtAra); top.add(btnAra);

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Kitap Adı", "Yazar", "Kategori", "Stok", "DURUM"}, 0);
        JTable table = new JTable(model);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOduncAl = createBtn("SEÇİLİ KİTABI ÖDÜNÇ AL", CLR_GREEN);
        btnOduncAl.setPreferredSize(new Dimension(250, 40));
        bottom.add(btnOduncAl);

        // Listeleme (Facade ve State Pattern Kullanır)
        Runnable listBooks = () -> {
            model.setRowCount(0);
            String criteria = switch (cmbKriter.getSelectedIndex()) { case 1 -> "yazar"; case 2 -> "kategori"; default -> "ad"; };
            List<Book> books = libraryFacade.searchBooks(criteria, txtAra.getText());
            for(Book b : books) {
                // --- (DESEN 4) STATE PATTERN: Durum metni State'den gelir ---
                model.addRow(new Object[]{b.getId(), b.getAd(), b.getYazar(), b.getKategori(), b.getStok(), b.getState().getStatusName()});
            }
        };
        listBooks.run();
        btnAra.addActionListener(e -> listBooks.run());

        // Ödünç Alma Butonu
        btnOduncAl.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Kitap Seçiniz"); return; }
            int bookId = (int) table.getValueAt(row, 0);

            String result = libraryFacade.borrowBook(bookId, currentUser.getId());
            JOptionPane.showMessageDialog(this, result);
            listBooks.run();
        });

        pnlBooks.add(top, BorderLayout.NORTH); pnlBooks.add(new JScrollPane(table), BorderLayout.CENTER); pnlBooks.add(bottom, BorderLayout.SOUTH);

        // PROFİL SEKME
        JPanel pnlProfile = new JPanel(new GridLayout(4, 2, 20, 20));
        pnlProfile.setBorder(new EmptyBorder(50, 150, 250, 150));
        JTextField txtAd = new JTextField(currentUser.getAdSoyad());
        JTextField txtTel = new JTextField(currentUser.getTelefon());
        JPasswordField txtPass = new JPasswordField();
        JButton btnUpd = createBtn("GÜNCELLE", CLR_ORANGE);

        btnUpd.addActionListener(e -> {
            libraryFacade.updateProfile(currentUser.getId(), txtAd.getText(), txtTel.getText(), new String(txtPass.getPassword()));
            JOptionPane.showMessageDialog(this, "Güncellendi");
        });

        pnlProfile.add(new JLabel("Ad Soyad:")); pnlProfile.add(txtAd);
        pnlProfile.add(new JLabel("Telefon:")); pnlProfile.add(txtTel);
        pnlProfile.add(new JLabel("Yeni Şifre:")); pnlProfile.add(txtPass);
        pnlProfile.add(new JLabel("")); pnlProfile.add(btnUpd);

        // ÖDÜNÇLERİM SEKME
        JPanel pnlLoans = new JPanel(new BorderLayout());
        DefaultTableModel lModel = new DefaultTableModel(new String[]{"Kitap", "Son Teslim", "Ceza (TL)"}, 0);
        JTable lTable = new JTable(lModel);
        JButton btnRef = createBtn("YENİLE", CLR_BLUE);

        Runnable loadLoans = () -> {
            lModel.setRowCount(0);
            libraryFacade.getUserLoans(currentUser.getId(), lModel);
        };
        loadLoans.run();
        btnRef.addActionListener(e -> loadLoans.run());

        pnlLoans.add(new JScrollPane(lTable), BorderLayout.CENTER); pnlLoans.add(btnRef, BorderLayout.SOUTH);

        tabs.addTab("Kitaplar & Ödünç Al", pnlBooks);
        tabs.addTab("Profilim", pnlProfile);
        tabs.addTab("Aldığım Kitaplar", pnlLoans);
        return tabs;
    }

    // --- 4. PERSONEL PANELİ (Görünüm AYNI) ---
    private JTabbedPane createStaffTabs() {
        JTabbedPane tabs = new JTabbedPane();

        // KİTAP EKLEME
        JPanel pnlBook = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new FlowLayout());
        JTextField tAd=new JTextField(8), tYaz=new JTextField(8), tIsbn=new JTextField(8), tStok=new JTextField(4);
        JButton btnAdd = createBtn("KİTAP EKLE", CLR_GREEN);

        DefaultTableModel bModel = new DefaultTableModel(new String[]{"ID","Ad","Stok"},0);
        JTable bTable = new JTable(bModel);

        Runnable refBooks = () -> {
            bModel.setRowCount(0);
            for(Book b : libraryFacade.searchBooks("ad", ""))
                bModel.addRow(new Object[]{b.getId(), b.getAd(), b.getStok()});
        };
        refBooks.run();

        btnAdd.addActionListener(e -> {
            // --- (DESEN 5) BUILDER PATTERN: Kitap oluşturma ---
            Book newBook = new Book.BookBuilder()
                    .setAd(tAd.getText())
                    .setYazar(tYaz.getText())
                    .setIsbn(tIsbn.getText())
                    .setStok(Integer.parseInt(tStok.getText()))
                    .setKategori("Genel")
                    .build();

            libraryFacade.addBook(newBook); // CRUD: Create
            refBooks.run(); JOptionPane.showMessageDialog(this,"Eklendi");
        });

        form.add(new JLabel("Ad:")); form.add(tAd); form.add(new JLabel("Yazar:")); form.add(tYaz);
        form.add(new JLabel("Stok:")); form.add(tStok); form.add(btnAdd);

        // --- CRUD EKSİĞİ İÇİN EK BUTONLAR (SİLME VE İADE) ---
        // Personel paneline "Sil" ve "İade Al" ekliyoruz, görüntü bozulmadan alt panele
        JPanel botPanel = new JPanel(new FlowLayout());
        JButton btnDel = createBtn("SEÇİLİ SİL (CRUD: Delete)", CLR_RED);
        JButton btnReturn = createBtn("İADE AL (Observer)", CLR_ORANGE);

        btnDel.addActionListener(e -> {
            int row = bTable.getSelectedRow();
            if(row != -1) {
                libraryFacade.deleteBook((int)bTable.getValueAt(row, 0));
                refBooks.run();
                JOptionPane.showMessageDialog(this, "Silindi");
            }
        });

        btnReturn.addActionListener(e -> {
            int row = bTable.getSelectedRow();
            if(row != -1) {
                libraryFacade.returnBook((int)bTable.getValueAt(row, 0));
                refBooks.run();
                JOptionPane.showMessageDialog(this, "İade alındı. (Observer ile bildirim gönderildi)");
            }
        });

        botPanel.add(btnDel); botPanel.add(btnReturn);

        pnlBook.add(form, BorderLayout.NORTH);
        pnlBook.add(new JScrollPane(bTable), BorderLayout.CENTER);
        pnlBook.add(botPanel, BorderLayout.SOUTH);

        // ÜYE İŞLEMLERİ
        JPanel pnlUser = new JPanel(new BorderLayout());
        DefaultTableModel uModel = new DefaultTableModel(new String[]{"ID","Ad","Tel"},0);
        JTable uTable = new JTable(uModel);

        Runnable refUsers = () -> {
            uModel.setRowCount(0);
            libraryFacade.getMembers(uModel);
        };
        refUsers.run();

        JTextField txtBID = new JTextField(5);
        JButton btnGive = createBtn("KİTAP VER", CLR_GREEN);
        JPanel bot = new JPanel(); bot.add(new JLabel("Kitap ID:")); bot.add(txtBID); bot.add(btnGive);

        btnGive.addActionListener(e -> {
            if(uTable.getSelectedRow()==-1) return;
            int uid = (int)uTable.getValueAt(uTable.getSelectedRow(),0);
            String res = libraryFacade.borrowBook(Integer.parseInt(txtBID.getText()), uid);
            JOptionPane.showMessageDialog(this, res);
        });

        pnlUser.add(new JScrollPane(uTable), BorderLayout.CENTER);
        pnlUser.add(bot, BorderLayout.SOUTH);

        tabs.addTab("Kitaplar", pnlBook);
        tabs.addTab("Ödünç Ver", pnlUser);
        return tabs;
    }

    // --- 5. CEO PANELİ (Görünüm AYNI) ---
    private JTabbedPane createCEOTabs() {
        JTabbedPane tabs = new JTabbedPane();

        JPanel pnlSet = new JPanel(null);
        JLabel l1 = new JLabel("Günlük Ceza (TL):"); l1.setBounds(50,50,200,30);
        JTextField tCeza = new JTextField(libraryFacade.getSetting("gunluk_ceza")); tCeza.setBounds(250,50,100,30);

        JLabel l2 = new JLabel("Max Ödünç Sayısı:"); l2.setBounds(50,100,200,30);
        JTextField tMax = new JTextField(libraryFacade.getSetting("max_odunc")); tMax.setBounds(250,100,100,30);

        JButton btnSave = createBtn("AYARLARI KAYDET", CLR_ORANGE);
        btnSave.setBounds(250,160,200,40);

        btnSave.addActionListener(e -> {
            libraryFacade.updateSetting("gunluk_ceza", tCeza.getText());
            libraryFacade.updateSetting("max_odunc", tMax.getText());
            JOptionPane.showMessageDialog(this,"Ayarlar Güncellendi");
        });
        pnlSet.add(l1); pnlSet.add(tCeza); pnlSet.add(l2); pnlSet.add(tMax); pnlSet.add(btnSave);

        JPanel pnlYetki = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new FlowLayout());
        JTextField uAd = new JTextField(8), uUser = new JTextField(8), uPass = new JTextField(8);
        JComboBox<String> cmbRol = new JComboBox<>(new String[]{"uye", "personel", "ceo"});
        JButton btnEkle = createBtn("EKLE / GÜNCELLE", CLR_GREEN);

        form.add(new JLabel("Ad:")); form.add(uAd); form.add(new JLabel("K.Adı:")); form.add(uUser);
        form.add(new JLabel("Şifre:")); form.add(uPass); form.add(new JLabel("Rol:")); form.add(cmbRol); form.add(btnEkle);

        DefaultTableModel modelUser = new DefaultTableModel(new String[]{"ID", "Ad", "K.Adı", "Rol"}, 0);
        JTable tblUser = new JTable(modelUser);

        Runnable loadAllUsers = () -> {
            modelUser.setRowCount(0);
            libraryFacade.getAllUsers(modelUser);
        };
        loadAllUsers.run();

        btnEkle.addActionListener(e -> {
            libraryFacade.addOrUpdateUser(uUser.getText(), uPass.getText(), (String)cmbRol.getSelectedItem(), uAd.getText());
            loadAllUsers.run();
            JOptionPane.showMessageDialog(this, "İşlem Başarılı");
        });

        pnlYetki.add(form, BorderLayout.NORTH);
        pnlYetki.add(new JScrollPane(tblUser), BorderLayout.CENTER);

        tabs.addTab("Sistem Ayarları", pnlSet);
        tabs.addTab("Yetki Yönetimi", pnlYetki);
        tabs.addTab("Personel Ekranı", createStaffTabs().getComponentAt(1));
        return tabs;
    }

    private JButton createBtn(String text, Color c) {
        JButton b = new JButton(text); b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(FONT_BOLD); b.setFocusPainted(false); b.setOpaque(true); b.setBorderPainted(false);
        return b;
    }

    // =========================================================================
    // ================== TASARIM DESENLERİ (CLASS YAPILARI) ===================
    // =========================================================================

    // --- DESEN 1: SINGLETON (DB Bağlantısı) ---
    public static class DatabaseConnection {
        private static DatabaseConnection instance;
        private Connection connection;
        private DatabaseConnection() {
            try { connection = DriverManager.getConnection("jdbc:sqlite:kutuphane_pro.db"); }
            catch (SQLException e) { e.printStackTrace(); }
        }
        public static synchronized DatabaseConnection getInstance() {
            if (instance == null) instance = new DatabaseConnection();
            return instance;
        }
        public Connection getConnection() { return connection; }
    }

    // --- ABSTRACT CLASS 1: BaseEntity ---
    public static abstract class BaseEntity {
        protected int id;
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
    }

    // --- ABSTRACT CLASS 2: BaseUser ---
    public static abstract class BaseUser extends BaseEntity {
        protected String adSoyad, role, telefon;
        public BaseUser(String ad, String r, String t) { this.adSoyad = ad; this.role = r; this.telefon=t; }
        public String getAdSoyad() { return adSoyad; }
        public String getRole() { return role; }
        public String getTelefon() { return telefon; }
    }

    // Concrete User Classes
    public static class NormalUser extends BaseUser { public NormalUser(String a, String r, String t) { super(a,r,t); } }

    // --- DESEN 2: FACTORY PATTERN (User Üretimi) ---
    public static class UserFactory {
        public static BaseUser login(String u, String p) {
            try {
                Connection c = DatabaseConnection.getInstance().getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
                ps.setString(1,u); ps.setString(2,p);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    BaseUser user = new NormalUser(rs.getString("ad_soyad"), rs.getString("role"), rs.getString("telefon"));
                    user.setId(rs.getInt("id"));
                    return user;
                }
            } catch(Exception e){}
            return null;
        }
    }

    // --- DESEN 3: OBSERVER PATTERN (Bildirim) ---
    public interface Observer { void update(String msg); }
    public static class NotificationSystem {
        private List<Observer> observers = new ArrayList<>();
        public void addObserver(Observer o) { observers.add(o); }
        public void notifyAll(String msg) { for(Observer o : observers) o.update(msg); }
    }

    // --- DESEN 4: STATE PATTERN (Kitap Durumu) ---
    public interface BookState { String getStatusName(); boolean canBorrow(); }
    public static class AvailableState implements BookState { public String getStatusName() { return "MEVCUT"; } public boolean canBorrow() { return true; } }
    public static class BorrowedState implements BookState { public String getStatusName() { return "TÜKENDİ"; } public boolean canBorrow() { return false; } }

    // --- DESEN 5: BUILDER PATTERN (Kitap) ---
    public static class Book extends BaseEntity {
        private String ad, yazar, isbn, kategori;
        private int stok;
        private BookState state;
        private Book(BookBuilder b) {
            this.ad=b.ad; this.yazar=b.yazar; this.isbn=b.isbn; this.kategori=b.kategori; this.stok=b.stok;
            this.state = (stok>0) ? new AvailableState() : new BorrowedState();
        }
        public String getAd(){return ad;} public String getYazar(){return yazar;} public String getKategori(){return kategori;}
        public int getStok(){return stok;} public BookState getState(){return state;}

        public static class BookBuilder {
            String ad, yazar, isbn, kategori; int stok;
            public BookBuilder setAd(String a){this.ad=a;return this;}
            public BookBuilder setYazar(String y){this.yazar=y;return this;}
            public BookBuilder setIsbn(String i){this.isbn=i;return this;}
            public BookBuilder setKategori(String k){this.kategori=k;return this;}
            public BookBuilder setStok(int s){this.stok=s;return this;}
            public Book build(){return new Book(this);}
        }
    }

    // --- DESEN 7: FACADE PATTERN (Tüm Logic Burada) ---
    public static class LibraryFacade {
        private Connection conn;
        private NotificationSystem notifier;

        public LibraryFacade() {
            conn = DatabaseConnection.getInstance().getConnection();
            notifier = new NotificationSystem();
            notifier.addObserver(msg -> System.out.println("SİSTEM BİLDİRİMİ: " + msg)); // Konsola log basar
        }

        public void initSystem() {
            try {
                Statement s = conn.createStatement();
                s.execute("CREATE TABLE IF NOT EXISTS settings (key TEXT PRIMARY KEY, value TEXT)");
                s.execute("INSERT OR IGNORE INTO settings VALUES ('gunluk_ceza', '5')");
                s.execute("INSERT OR IGNORE INTO settings VALUES ('odunc_suresi', '15')");
                s.execute("INSERT OR IGNORE INTO settings VALUES ('max_odunc', '3')");
                s.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, role TEXT, ad_soyad TEXT, telefon TEXT)");
                s.execute("CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY AUTOINCREMENT, isbn TEXT, ad TEXT, yazar TEXT, kategori TEXT, stok INTEGER)");
                s.execute("CREATE TABLE IF NOT EXISTS loans (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, book_id INTEGER, alis_tarihi TEXT, planlanan_iade TEXT, durum TEXT)");
                s.execute("INSERT OR IGNORE INTO users (username, password, role, ad_soyad) VALUES ('admin', 'admin', 'ceo', 'Sistem Yöneticisi')");

                // Demo Veriler (Senin istediklerin)
                ResultSet rs = s.executeQuery("SELECT count(*) FROM books");
                if(rs.next() && rs.getInt(1) < 5) {
                    s.execute("INSERT OR IGNORE INTO users (username, password, role, ad_soyad, telefon) VALUES ('ali', '123', 'uye', 'Ali Yılmaz', '555-001')");
                    s.execute("INSERT OR IGNORE INTO users (username, password, role, ad_soyad, telefon) VALUES ('ahmet', '123', 'personel', 'Ahmet Personel', '500-111')");
                    String[] books = {
                            "('9781', 'Suç ve Ceza', 'Dostoyevski', 'Roman', 5)", "('9782', 'Sefiller', 'Victor Hugo', 'Roman', 3)",
                            "('9783', 'Nutuk', 'Atatürk', 'Tarih', 10)", "('9784', 'Simyacı', 'Paulo Coelho', 'Roman', 0)",
                            "('9785', '1984', 'George Orwell', 'Bilim', 6)", "('9786', 'Harry Potter', 'Rowling', 'Fantastik', 7)",
                            "('9787', 'Kürk Mantolu Madonna', 'S. Ali', 'Roman', 5)", "('9788', 'Da Vinci', 'Dan Brown', 'Macera', 2)",
                            "('9789', 'Yüzüklerin Efendisi', 'Tolkien', 'Fantastik', 3)", "('9780', 'İnce Memed', 'Y. Kemal', 'Roman', 4)"
                    };
                    for(String b : books) s.execute("INSERT INTO books (isbn, ad, yazar, kategori, stok) VALUES " + b);
                }
            } catch(Exception e){}
        }

        public List<Book> searchBooks(String col, String val) {
            List<Book> list = new ArrayList<>();
            try {
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM books WHERE " + col + " LIKE '%" + val + "%'");
                while(rs.next()) {
                    Book b = new Book.BookBuilder()
                            .setAd(rs.getString("ad")).setYazar(rs.getString("yazar")).setKategori(rs.getString("kategori")).setStok(rs.getInt("stok")).build();
                    b.setId(rs.getInt("id"));
                    list.add(b);
                }
            } catch(Exception e){}
            return list;
        }

        public String borrowBook(int bid, int uid) {
            try {
                // Max Limit Check
                int max = Integer.parseInt(getSetting("max_odunc"));
                ResultSet rs = conn.createStatement().executeQuery("SELECT count(*) FROM loans WHERE user_id="+uid+" AND durum='aktif'");
                if(rs.next() && rs.getInt(1) >= max) return "Limit Dolu!";

                // Stock Check
                ResultSet rs2 = conn.createStatement().executeQuery("SELECT stok FROM books WHERE id="+bid);
                if(rs2.next() && rs2.getInt(1) > 0) {
                    conn.createStatement().execute("INSERT INTO loans (user_id, book_id, alis_tarihi, planlanan_iade, durum) VALUES ("+uid+","+bid+",'"+LocalDate.now()+"','"+LocalDate.now().plusDays(15)+"','aktif')");
                    conn.createStatement().execute("UPDATE books SET stok=stok-1 WHERE id="+bid);
                    return "Başarılı!";
                }
                return "Stok Yok!";
            } catch(Exception e){ return "Hata: " + e.getMessage(); }
        }

        // CRUD: Delete
        public void deleteBook(int bid) {
            try { conn.createStatement().execute("DELETE FROM books WHERE id="+bid); } catch(Exception e){}
        }

        // CRUD: Return (İade) -> Observer Tetiklenir
        public void returnBook(int bid) {
            try {
                conn.createStatement().execute("UPDATE books SET stok=stok+1 WHERE id="+bid);
                notifier.notifyAll("Kitap (ID: "+bid+") iade edildi ve stoğa eklendi!");
            } catch(Exception e){}
        }

        // CRUD: Create
        public void addBook(Book b) {
            try { conn.createStatement().execute("INSERT INTO books (ad, yazar, isbn, stok, kategori) VALUES ('"+b.getAd()+"','"+b.getYazar()+"','"+b.isbn+"',"+b.stok+",'"+b.kategori+"')"); } catch(Exception e){}
        }

        public void updateProfile(int uid, String ad, String tel, String pass) {
            try {
                conn.createStatement().execute("UPDATE users SET ad_soyad='"+ad+"', telefon='"+tel+"' WHERE id="+uid);
                if(!pass.isEmpty()) conn.createStatement().execute("UPDATE users SET password='"+pass+"' WHERE id="+uid);
            } catch(Exception e){}
        }

        public void getUserLoans(int uid, DefaultTableModel m) {
            try {
                ResultSet rs = conn.createStatement().executeQuery("SELECT b.ad, l.planlanan_iade FROM loans l JOIN books b ON l.book_id=b.id WHERE l.user_id="+uid+" AND l.durum='aktif'");
                double ceza = Double.parseDouble(getSetting("gunluk_ceza"));
                while(rs.next()){
                    long gun = ChronoUnit.DAYS.between(LocalDate.parse(rs.getString("planlanan_iade")), LocalDate.now());
                    m.addRow(new Object[]{rs.getString("ad"), rs.getString("planlanan_iade"), (gun>0 ? gun*ceza : 0)});
                }
            } catch(Exception e){}
        }

        public void getMembers(DefaultTableModel m) {
            try { ResultSet rs=conn.createStatement().executeQuery("SELECT * FROM users WHERE role='uye'");
                while(rs.next()) m.addRow(new Object[]{rs.getInt("id"), rs.getString("ad_soyad"), rs.getString("telefon")}); } catch(Exception e){}
        }
        public void getAllUsers(DefaultTableModel m) {
            try { ResultSet rs=conn.createStatement().executeQuery("SELECT * FROM users");
                while(rs.next()) m.addRow(new Object[]{rs.getInt("id"), rs.getString("ad_soyad"), rs.getString("username"), rs.getString("role")}); } catch(Exception e){}
        }
        public void addOrUpdateUser(String u, String p, String r, String a) {
            try { conn.createStatement().execute("INSERT OR REPLACE INTO users (username, password, role, ad_soyad) VALUES ('"+u+"','"+p+"','"+r+"','"+a+"')"); } catch(Exception e){}
        }
        public String getSetting(String k) {
            try { ResultSet rs = conn.createStatement().executeQuery("SELECT value FROM settings WHERE key='"+k+"'"); if(rs.next()) return rs.getString("value"); } catch(Exception e){} return "0";
        }
        public void updateSetting(String k, String v) { try{conn.createStatement().execute("UPDATE settings SET value='"+v+"' WHERE key='"+k+"'");}catch(Exception e){} }
    }
}
*/