package core;

import java.sql.*;

public class UserStore {

    public static boolean isValid(String username, String password) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db")) {
            Statement stmt = conn.createStatement();
            //  CONCATENAÇÃO PERIGOSA
            String query = "SELECT * FROM usuarios WHERE username = '" + username +
                           "' AND password = '" + password + "'";
            ResultSet rs = stmt.executeQuery(query);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
