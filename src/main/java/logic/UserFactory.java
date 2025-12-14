package logic;

import database.DatabaseConnection;
import model.BaseUser;
import model.NormalUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//kullanıcı oluşturma oluşturma FACTORY method
public class UserFactory {
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
        } catch(Exception e){ e.printStackTrace(); }
        return null;
    }
}