# Kütüphane Yönetim Sistemi

Bu proje, Çetin ve Ufuk ile birlikte geliştirilen basit yapılı bir kütüphane yönetim sistemidir. Sistem, Java programlama dili kullanılarak geliştirilmiş ve kod tekrarını azaltmak, sürdürülebilirliği artırmak amacıyla belirli tasarım desenleri uygulanmıştır.

- Çetin Github profil linki = https://github.com/cetin-ckaya
- Ufuk Github profil linki = https://github.com/1230505036

## Proje Özeti

Kütüphane sisteminde üç farklı yetki seviyesine sahip kullanıcı bulunmaktadır:
- Üye
- Personel
- Admin

Her kullanıcı rolü, kendine ait yetkiler ve sorumluluklar doğrultusunda sistem içerisinde işlem yapabilmektedir.

## Kullanıcı Rolleri ve Yetkiler

### Üye
- Kendi profil bilgilerini görüntüleyebilir
- Şifre, telefon numarası gibi kişisel bilgilerini güncelleyebilir
- Kitapları kitap adı, yazar, ISBN veya kategori gibi kriterlere göre arayabilir
- Stokta bulunan kitapları ödünç alabilir
- Yalnızca kendi ödünç ve profil bilgilerine erişebilir

### Personel
- Üyelerle ilgili işlemler yapabilir
- Üyeleri listeleyebilir ve ad veya üye numarasına göre arama yapabilir
- Sisteme yeni kitap ekleyebilir
- Kitapları belirli kriterlere göre listeleyebilir
- Kitap ödünç verme ve iade işlemlerini gerçekleştirebilir
- Kitap iade sırasında gecikme gününü hesaplayarak, gecikme varsa belirlenen kurallara göre ödeme alabilir

### Admin
- Kullanıcıları sistemde tanımlayabilir
- Kullanıcılara rol atayabilir ve yetkilerini düzenleyebilir
- Maksimum ödünç alma süresini belirleyebilir
- Gecikme durumunda uygulanacak ceza tutarlarını ayarlayabilir

## Uygulamanın Çalıştırılması

Uygulamayı başlatmak için HelloApplication dosyasının çalıştırılması yeterlidir. Program çalıştırıldığında JavaFX tabanlı grafiksel kullanıcı arayüzü açılır ve sistem üzerinden işlemler gerçekleştirilebilir.

