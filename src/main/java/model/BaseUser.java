package model;

public abstract class BaseUser extends BaseEntity {
    protected String adSoyad, role, telefon;

    public BaseUser(String ad, String r, String t) {
        this.adSoyad = ad;
        this.role = r;
        this.telefon = t;
    }

    public String getAdSoyad() { return adSoyad; }
    public String getRole() { return role; }
    public String getTelefon() { return telefon; }

}