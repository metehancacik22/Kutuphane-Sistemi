package model;

public class Book extends BaseEntity {
    private String ad, yazar, isbn, kategori, yayinevi;
    private int stok, baskiYili;
    private BookState state;

    private Book(BookBuilder b) {
        this.ad=b.ad; this.yazar=b.yazar; this.isbn=b.isbn;
        this.kategori=b.kategori; this.stok=b.stok;
        this.yayinevi = b.yayinevi; this.baskiYili = b.baskiYili;
        this.state = (stok>0) ? new AvailableState() : new BorrowedState();
    }

    public String getAd(){return ad;}
    public String getYazar(){return yazar;}
    public String getKategori(){return kategori;}
    public int getStok(){return stok;}
    public String getIsbn(){return isbn;}
    public String getYayinevi() { return yayinevi; }
    public int getBaskiYili() { return baskiYili; }
    public BookState getState(){return state;}

    public static class BookBuilder {
        String ad, yazar, isbn, kategori, yayinevi;
        int stok, baskiYili;

        public BookBuilder setAd(String a){this.ad=a;return this;}
        public BookBuilder setYazar(String y){this.yazar=y;return this;}
        public BookBuilder setIsbn(String i){this.isbn=i;return this;}
        public BookBuilder setKategori(String k){this.kategori=k;return this;}
        public BookBuilder setYayinevi(String y){this.yayinevi=y;return this;}
        public BookBuilder setStok(int s){this.stok=s;return this;}
        public BookBuilder setBaskiYili(int b){this.baskiYili=b;return this;}

        public Book build(){return new Book(this);}
    }
}