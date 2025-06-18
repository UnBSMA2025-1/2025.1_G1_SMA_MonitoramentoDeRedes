package core;

import java.sql.*;

public class DBInitializer {
    public static void init() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:users.db")) {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS usuarios (username TEXT, password TEXT)");
            stmt.execute("DELETE FROM usuarios"); // limpa tudo
            stmt.execute("INSERT INTO usuarios VALUES ('admin', '1234')");
            stmt.execute("INSERT INTO usuarios VALUES ('root', 'root')");
            stmt.execute("INSERT INTO usuarios VALUES ('user', 'senha')");
            stmt.execute("INSERT INTO usuarios VALUES ('teste', 'teste')");
            System.out.println("[DB] Banco inicializado.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
