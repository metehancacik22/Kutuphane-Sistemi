package view;

import database.DatabaseConnection;
import logic.LibraryFacade;
import logic.UserFactory;
import model.BaseUser;
import model.Book;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class HelloApplication extends JFrame {

    // --- KOYU MOD (DARK THEME) RENK PALETİ ---
    private final Color CLR_BG      = new Color(44, 62, 80);
    private final Color CLR_CARD    = new Color(52, 73, 94);
    private final Color CLR_TXT     = new Color(236, 240, 241);

    private final Color CLR_BLUE    = new Color(52, 152, 219);
    private final Color CLR_ORANGE  = new Color(230, 126, 34);
    private final Color CLR_RED     = new Color(231, 76, 60);
    private final Color CLR_GREEN   = new Color(46, 204, 113);

    private final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 24);
    private final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);

    private final LibraryFacade libraryFacade;
    private BaseUser currentUser;

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        DatabaseConnection.getInstance();
        SwingUtilities.invokeLater(() -> new HelloApplication().setVisible(true));
    }

    public HelloApplication() {
        libraryFacade = new LibraryFacade();
        libraryFacade.initSystem();

        setTitle("Kütüphane Yönetim Sistemi - PRO");
        setSize(1350, 850); // Ekranı biraz genişlettik
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(CLR_BG);

        mainPanel.add(createLoginPanel(), "LOGIN");
        add(mainPanel);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CLR_BG);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CLR_CARD);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(30, 30, 30), 1), new EmptyBorder(40, 50, 40, 50)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("SİSTEM GİRİŞİ");
        lblTitle.setFont(FONT_TITLE); lblTitle.setForeground(CLR_TXT); lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0; card.add(lblTitle, gbc);

        JTextField txtUser = createCustomTextField();
        JPasswordField txtPass = createCustomPasswordField();

        gbc.gridy++; card.add(createLabel("Kullanıcı Adı"), gbc);
        gbc.gridy++; card.add(txtUser, gbc);
        gbc.gridy++; card.add(createLabel("Şifre"), gbc);
        gbc.gridy++; card.add(txtPass, gbc);

        JButton btnLogin = createCustomButton("GİRİŞ YAP", CLR_BLUE, Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(200, 45));
        gbc.gridy++; gbc.insets = new Insets(25, 0, 10, 0);
        card.add(btnLogin, gbc);

        JLabel lblInfo = new JLabel("<html><center>Admin: admin/admin | Üye: ali/123 | Personel: ahmet/123</center></html>");
        lblInfo.setForeground(Color.GRAY);
        gbc.gridy++; card.add(lblInfo, gbc);

        btnLogin.addActionListener(e -> {
            BaseUser user = UserFactory.login(txtUser.getText(), new String(txtPass.getPassword()));
            if(user != null) {
                currentUser = user;
                mainPanel.add(createDashboardPanel(), "DASHBOARD");
                cardLayout.show(mainPanel, "DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this, "Hatalı Giriş Bilgileri");
            }
        });

        panel.add(card);
        return panel;
    }

    private JPanel createDashboardPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CLR_BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 30));
        header.setPreferredSize(new Dimension(100, 70));
        header.setBorder(new EmptyBorder(0, 30, 0, 30));

        JLabel lblLogo = new JLabel("KÜTÜPHANE YÖNETİMİ");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(CLR_TXT);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        userPanel.setOpaque(false);

        JLabel lblUser = new JLabel(currentUser.getAdSoyad());
        lblUser.setForeground(CLR_BLUE);
        lblUser.setFont(FONT_BOLD);

        JButton btnLogout = createCustomButton("ÇIKIŞ", CLR_RED, Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(80, 30));
        btnLogout.addActionListener(e -> { currentUser = null; cardLayout.show(mainPanel, "LOGIN"); });

        userPanel.add(lblUser); userPanel.add(btnLogout);
        header.add(lblLogo, BorderLayout.WEST); header.add(userPanel, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_BOLD);
        tabs.setBackground(CLR_BG);
        tabs.setForeground(CLR_BG);

        if (currentUser.getRole().equals("ceo")) tabs = createCEOTabs();
        else if (currentUser.getRole().equals("personel")) tabs = createStaffTabs();
        else tabs = createMemberTabs();

        p.add(tabs, BorderLayout.CENTER);
        return p;
    }

    // --- 3. ÜYE EKRANI (DÜZELTİLDİ: İADE ETME EKLENDİ) ---
    private JTabbedPane createMemberTabs() {
        JTabbedPane tabs = new JTabbedPane();

        // KİTAP ARAMA
        JPanel pnlBooks = new JPanel(new BorderLayout(20, 20));
        pnlBooks.setBackground(CLR_BG);
        pnlBooks.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchBar.setBackground(CLR_CARD);
        searchBar.setBorder(new LineBorder(CLR_BLUE, 1));

        JComboBox<String> cmbKriter = new JComboBox<>(new String[]{"Kitap Adı", "Yazar", "Kategori"});
        JTextField txtAra = new JTextField(20);
        JButton btnAra = createCustomButton("KİTAP ARA", CLR_BLUE, Color.WHITE);
        searchBar.add(createLabel("Filtre:")); searchBar.add(cmbKriter); searchBar.add(txtAra); searchBar.add(btnAra);

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Kitap Adı", "Yazar", "Kategori", "Yayınevi", "Durum", "Stok"}, 0);
        JTable table = createCustomTable(model);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.setBackground(CLR_BG);
        JButton btnOduncAl = createCustomButton("SEÇİLİ KİTABI ÖDÜNÇ AL", CLR_GREEN, Color.WHITE);
        btnOduncAl.setPreferredSize(new Dimension(250, 45));
        bottom.add(btnOduncAl);

        Runnable listBooks = () -> {
            model.setRowCount(0);
            String criteria = switch (cmbKriter.getSelectedIndex()) { case 1 -> "yazar"; case 2 -> "kategori"; default -> "ad"; };
            List<Book> books = libraryFacade.searchBooks(criteria, txtAra.getText());
            for(Book b : books) model.addRow(new Object[]{b.getId(), b.getAd(), b.getYazar(), b.getKategori(), b.getYayinevi(), b.getState().getStatusName(), b.getStok()});
        };
        listBooks.run();
        btnAra.addActionListener(e -> listBooks.run());

        btnOduncAl.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Lütfen bir kitap seçin."); return; }
            int bookId = (int) table.getValueAt(row, 0);
            String result = libraryFacade.borrowBook(bookId, currentUser.getId());
            JOptionPane.showMessageDialog(this, result);
            listBooks.run();
        });

        pnlBooks.add(searchBar, BorderLayout.NORTH); pnlBooks.add(new JScrollPane(table), BorderLayout.CENTER); pnlBooks.add(bottom, BorderLayout.SOUTH);

        // PROFİL
        JPanel pnlProfile = new JPanel(new GridBagLayout()); pnlProfile.setBackground(CLR_BG);
        JPanel proCard = new JPanel(new GridLayout(5, 2, 10, 20)); proCard.setBackground(CLR_CARD); proCard.setBorder(new EmptyBorder(30,30,30,30));
        JTextField txtAd = createCustomTextField(); txtAd.setText(currentUser.getAdSoyad());
        JTextField txtTel = createCustomTextField(); txtTel.setText(currentUser.getTelefon());
        JPasswordField txtPass = createCustomPasswordField();
        JButton btnUpd = createCustomButton("GÜNCELLE", CLR_ORANGE, Color.WHITE);
        proCard.add(createLabel("Ad Soyad:")); proCard.add(txtAd); proCard.add(createLabel("Telefon:")); proCard.add(txtTel);
        proCard.add(createLabel("Yeni Şifre:")); proCard.add(txtPass); proCard.add(new JLabel("")); proCard.add(btnUpd);
        pnlProfile.add(proCard);
        btnUpd.addActionListener(e -> {
            libraryFacade.updateProfile(currentUser.getId(), txtAd.getText(), txtTel.getText(), new String(txtPass.getPassword()));
            JOptionPane.showMessageDialog(this, "Profil Güncellendi.");
        });

        // KİTAPLARIM VE İADE İŞLEMİ
        JPanel pnlLoans = new JPanel(new BorderLayout()); pnlLoans.setBackground(CLR_BG);
        DefaultTableModel lModel = new DefaultTableModel(new String[]{"Kitap ID", "Kitap Adı", "İade Tarihi", "Ceza Durumu"}, 0);
        JTable lTable = createCustomTable(lModel);

        JPanel pnlLoanBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); pnlLoanBottom.setBackground(CLR_BG);
        JButton btnIade = createCustomButton("KİTABI İADE ET", CLR_ORANGE, Color.WHITE); // Yeni buton
        btnIade.setPreferredSize(new Dimension(200, 40));
        pnlLoanBottom.add(btnIade);

        Runnable loadLoans = () -> { lModel.setRowCount(0); libraryFacade.getUserLoans(currentUser.getId(), lModel); };
        loadLoans.run();

        btnIade.addActionListener(e -> {
            int row = lTable.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "İade edilecek kitabı seçiniz."); return; }
            int bookId = (int) lTable.getValueAt(row, 0); // 0. Kolon Kitap ID

            libraryFacade.returnBook(bookId, currentUser.getId());
            JOptionPane.showMessageDialog(this, "Kitap başarıyla iade edildi.");
            loadLoans.run(); // Listeyi yenile
        });

        pnlLoans.add(new JScrollPane(lTable), BorderLayout.CENTER); pnlLoans.add(pnlLoanBottom, BorderLayout.SOUTH);

        tabs.addTab("Kitap Ara", pnlBooks);
        tabs.addTab("Profilim", pnlProfile);
        tabs.addTab("Aldığım Kitaplar", pnlLoans);

        // Sekme değişince otomatik yenile
        tabs.addChangeListener(e -> { if (tabs.getSelectedIndex() == 2) loadLoans.run(); });

        return tabs;
    }

    // --- 4. PERSONEL EKRANI (DÜZELTİLDİ: DETAYLI EKLEME VE SADECE ÜYE EKLEME) ---
    private JTabbedPane createStaffTabs() {
        JTabbedPane tabs = new JTabbedPane();

        // 1. KİTAP YÖNETİMİ (Detaylı)
        JPanel pnlBook = new JPanel(new BorderLayout(10,10)); pnlBook.setBackground(CLR_BG); pnlBook.setBorder(new EmptyBorder(10,10,10,10));

        // Form Alanı (Grid Layout ile düzenli)
        JPanel form = new JPanel(new GridLayout(2, 8, 5, 5)); form.setBackground(CLR_CARD); form.setBorder(new EmptyBorder(10,10,10,10));
        JTextField tAd=createCustomTextField(), tYaz=createCustomTextField(), tYay=createCustomTextField();
        JTextField tYil=createCustomTextField(), tKat=createCustomTextField(), tIsbn=createCustomTextField(), tStok=createCustomTextField();

        form.add(createLabel("Kitap Adı:")); form.add(tAd);
        form.add(createLabel("Yazar:")); form.add(tYaz);
        form.add(createLabel("Yayınevi:")); form.add(tYay);
        form.add(createLabel("Baskı Yılı:")); form.add(tYil);
        form.add(createLabel("Kategori:")); form.add(tKat);
        form.add(createLabel("ISBN:")); form.add(tIsbn);
        form.add(createLabel("Stok:")); form.add(tStok);

        JButton btnAdd = createCustomButton("KAYDET", CLR_GREEN, Color.WHITE);
        form.add(btnAdd);

        DefaultTableModel bModel = new DefaultTableModel(new String[]{"ID","Kitap Adı","Yazar","Yayınevi","Yıl","Kategori","ISBN","Stok"},0);
        JTable bTable = createCustomTable(bModel);

        Runnable refBooks = () -> {
            bModel.setRowCount(0);
            for(Book b : libraryFacade.searchBooks("ad", ""))
                bModel.addRow(new Object[]{b.getId(), b.getAd(), b.getYazar(), b.getYayinevi(), b.getBaskiYili(), b.getKategori(), b.getIsbn(), b.getStok()});
        };
        refBooks.run();

        btnAdd.addActionListener(e -> {
            try {
                Book newBook = new Book.BookBuilder()
                        .setAd(tAd.getText()).setYazar(tYaz.getText()).setYayinevi(tYay.getText())
                        .setBaskiYili(Integer.parseInt(tYil.getText())).setKategori(tKat.getText())
                        .setIsbn(tIsbn.getText()).setStok(Integer.parseInt(tStok.getText())).build();
                libraryFacade.addBook(newBook); refBooks.run(); JOptionPane.showMessageDialog(this,"Kitap Eklendi");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Hatalı Giriş! Stok ve Yıl sayı olmalı."); }
        });

        JPanel botPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); botPanel.setBackground(CLR_BG);
        JButton btnDel = createCustomButton("SEÇİLİ KİTABI SİL", CLR_RED, Color.WHITE);
        btnDel.addActionListener(e -> {
            if(bTable.getSelectedRow() != -1) { libraryFacade.deleteBook((int)bTable.getValueAt(bTable.getSelectedRow(), 0)); refBooks.run(); }
        });
        botPanel.add(btnDel);

        pnlBook.add(form, BorderLayout.NORTH); pnlBook.add(new JScrollPane(bTable), BorderLayout.CENTER); pnlBook.add(botPanel, BorderLayout.SOUTH);

        // 2. ÜYE İŞLEMLERİ (Sadece Üye Ekleme)
        JPanel pnlMemberAdd = new JPanel(new GridBagLayout()); pnlMemberAdd.setBackground(CLR_BG);
        JPanel memCard = new JPanel(new GridLayout(6, 2, 10, 20)); memCard.setBackground(CLR_CARD); memCard.setBorder(new EmptyBorder(30,30,30,30));

        JTextField mAd = createCustomTextField();
        JTextField mUser = createCustomTextField();
        JTextField mPass = createCustomTextField();
        JTextField mTel = createCustomTextField();
        JButton btnMemAdd = createCustomButton("YENİ ÜYE OLUŞTUR", CLR_BLUE, Color.WHITE);

        memCard.add(createLabel("Üye Adı Soyadı:")); memCard.add(mAd);
        memCard.add(createLabel("Kullanıcı Adı:")); memCard.add(mUser);
        memCard.add(createLabel("Şifre:")); memCard.add(mPass);
        memCard.add(createLabel("Telefon:")); memCard.add(mTel);
        memCard.add(new JLabel("Rol:")); memCard.add(createLabel("ÜYE (Standart)"));
        memCard.add(new JLabel("")); memCard.add(btnMemAdd);

        btnMemAdd.addActionListener(e -> {
            libraryFacade.addOrUpdateUser(mUser.getText(), mPass.getText(), "uye", mAd.getText(), mTel.getText());
            JOptionPane.showMessageDialog(this, "Yeni Üye Başarıyla Eklendi!");
            mAd.setText(""); mUser.setText(""); mPass.setText(""); mTel.setText("");
        });
        pnlMemberAdd.add(memCard);

        // 3. ÖDÜNÇ VERME
        JPanel pnlUser = new JPanel(new BorderLayout()); pnlUser.setBackground(CLR_BG);
        DefaultTableModel uModel = new DefaultTableModel(new String[]{"ID","Ad Soyad","Telefon"},0);
        JTable uTable = createCustomTable(uModel);
        Runnable refUsers = () -> { uModel.setRowCount(0); libraryFacade.getMembers(uModel); };
        refUsers.run();

        JPanel bot = new JPanel(); bot.setBackground(CLR_CARD);
        JTextField txtBID = new JTextField(5);
        JButton btnGive = createCustomButton("MANUEL ÖDÜNÇ VER", CLR_ORANGE, Color.WHITE);
        bot.add(new JLabel("Kitap ID:")); bot.add(txtBID); bot.add(btnGive);

        btnGive.addActionListener(e -> {
            if(uTable.getSelectedRow()==-1) return;
            String res = libraryFacade.borrowBook(Integer.parseInt(txtBID.getText()), (int)uTable.getValueAt(uTable.getSelectedRow(),0));
            JOptionPane.showMessageDialog(this, res);
        });
        pnlUser.add(new JScrollPane(uTable), BorderLayout.CENTER); pnlUser.add(bot, BorderLayout.SOUTH);

        tabs.addTab("Kitap Yönetimi", pnlBook);
        tabs.addTab("Üye Ekleme", pnlMemberAdd);
        tabs.addTab("Üye Listesi & Ödünç", pnlUser);
        tabs.addChangeListener(e -> { if(tabs.getSelectedIndex()==2) refUsers.run(); });

        return tabs;
    }

    // --- 5. CEO EKRANI ---
    private JTabbedPane createCEOTabs() {
        JTabbedPane tabs = new JTabbedPane();
        JPanel pnlSet = new JPanel(null); pnlSet.setBackground(CLR_CARD);

        JLabel l1 = createLabel("Günlük Ceza (TL):"); l1.setBounds(50,50,200,30);
        JTextField tCeza = createCustomTextField(); tCeza.setText(libraryFacade.getSetting("gunluk_ceza")); tCeza.setBounds(250,50,100,30);

        JLabel l2 = createLabel("Max Kitap Hakkı:"); l2.setBounds(50,100,200,30);
        JTextField tMax = createCustomTextField(); tMax.setText(libraryFacade.getSetting("max_odunc")); tMax.setBounds(250,100,100,30);

        JButton btnSave = createCustomButton("AYARLARI KAYDET", CLR_RED, Color.WHITE); btnSave.setBounds(250,160,200,40);

        btnSave.addActionListener(e -> {
            libraryFacade.updateSetting("gunluk_ceza", tCeza.getText());
            libraryFacade.updateSetting("max_odunc", tMax.getText());
            JOptionPane.showMessageDialog(this,"Ayarlar Kaydedildi");
        });
        pnlSet.add(l1); pnlSet.add(tCeza); pnlSet.add(l2); pnlSet.add(tMax); pnlSet.add(btnSave);

        JPanel pnlYetki = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new FlowLayout()); form.setBackground(CLR_CARD);
        JTextField uAd = new JTextField(8), uUser = new JTextField(8), uPass = new JTextField(8);
        JComboBox<String> cmbRol = new JComboBox<>(new String[]{"uye", "personel", "ceo"});
        JButton btnEkle = createCustomButton("KULLANICI EKLE", CLR_GREEN, Color.WHITE);

        form.add(new JLabel("Ad:")); form.add(uAd); form.add(new JLabel("K.Adı:")); form.add(uUser);
        form.add(new JLabel("Şifre:")); form.add(uPass); form.add(new JLabel("Rol:")); form.add(cmbRol); form.add(btnEkle);

        DefaultTableModel modelUser = new DefaultTableModel(new String[]{"ID", "Ad", "K.Adı", "Rol"}, 0);
        JTable tblUser = createCustomTable(modelUser);
        Runnable loadAllUsers = () -> { modelUser.setRowCount(0); libraryFacade.getAllUsers(modelUser); };
        loadAllUsers.run();

        btnEkle.addActionListener(e -> {
            libraryFacade.addOrUpdateUser(uUser.getText(), uPass.getText(), (String)cmbRol.getSelectedItem(), uAd.getText(), "000");
            loadAllUsers.run();
        });

        pnlYetki.add(form, BorderLayout.NORTH); pnlYetki.add(new JScrollPane(tblUser), BorderLayout.CENTER);
        tabs.addTab("Sistem Ayarları", pnlSet);
        tabs.addTab("Kullanıcılar", pnlYetki);
        tabs.addTab("Personel Ekranı", createStaffTabs().getComponentAt(0)); // Personelin kitap ekranını buraya da koyduk
        return tabs;
    }

    // --- YARDIMCI METOTLAR ---
    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(CLR_TXT);
        l.setFont(FONT_NORMAL);
        return l;
    }

    private JButton createCustomButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JTextField createCustomTextField() {
        JTextField t = new JTextField(20);
        t.setFont(FONT_NORMAL);
        t.setBackground(CLR_BG);
        t.setForeground(CLR_TXT);
        t.setCaretColor(Color.WHITE);
        t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(CLR_BLUE, 1), new EmptyBorder(5, 5, 5, 5)));
        return t;
    }

    private JPasswordField createCustomPasswordField() {
        JPasswordField t = new JPasswordField(20);
        t.setBackground(CLR_BG);
        t.setForeground(CLR_TXT);
        t.setCaretColor(Color.WHITE);
        t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(CLR_BLUE, 1), new EmptyBorder(5, 5, 5, 5)));
        return t;
    }

    private JTable createCustomTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(CLR_CARD);
        table.setForeground(CLR_TXT);
        table.setGridColor(new Color(60, 60, 60));
        table.setSelectionBackground(CLR_BLUE);
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(30, 30, 30));
        header.setForeground(CLR_TXT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(CLR_BG);

        return table;
    }
}