/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package supwatch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.Configuration;

/**
 *
 * @author Kevin
 */
public class SQL_Wrapper {

    Configuration config;
    Connection connect;
    Logger log;

    public SQL_Wrapper(Configuration config, Logger log) {
        this.config = config;
    }

    public void connectToDB() throws ClassNotFoundException, SQLException {

        // This will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");

        // Setup the connection with the DB
        connect = DriverManager
                .getConnection(
                String.format("jdbc:mysql://%s/%s?user=%s&password=%s",
                config.getString("Settings.Host"),
                config.getString("Settings.Database"),
                config.getString("Settings.User"),
                config.getString("Settings.Password")));
        create_Table();
    }

    public void close() {
        try {
            connect.close();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Schliessen der Verbingen fehlgeschlagen", ex.getMessage());
        }
    }

    public Statement getStatment() {
        try {
            if (connect != null && !connect.isClosed()) {
                return connect.createStatement();
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Erstellen eines Statements fehlgeschlagen", ex.getMessage());
        }
        return null;
    }

    public void create_Table() {
        Statement stat = getStatment();
        try {
            stat.execute(
                    "CREATE TABLE IF NOT EXISTS Supwatch ("
                    + "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                    + "cmd VARCHAR(100)"
                    + "sup VARCHAR(100)"
                    + "com VARCHAR(100)"
                    + "player VARCHAR(100)"
                    + "time INT"
                    + "cur_timestamp TIMESTAMP(8)"
                    + ");");
            stat.close();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "create_Table", ex);
        }
    }

    public boolean write(String cmd, String sup, String player, String com, int Time) {
        try {
            try (PreparedStatement preparedStatement = connect
                            .prepareStatement("insert into  SUPWATCH values (default, ?, ?, ?, ?, ? ,default)")) {
                preparedStatement.setString(1, cmd);
                preparedStatement.setString(2, sup);
                preparedStatement.setString(3, com);
                preparedStatement.setString(4, player.toLowerCase());
                preparedStatement.setInt(5, Time);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "write", ex);
            return false;
        }
        return true;
    }

    public ResultSet read(String name, String mod) {
        ResultSet rs = null;
        try {
            Statement s = getStatment();
            if (mod.equalsIgnoreCase("all")) {
                rs = s.executeQuery(String.format("Select * from SUPWATCH where player = '%s'", name.toLowerCase()));
            } else {
                rs = s.executeQuery(String.format("Select * from SUPWATCH where player = '%s' and cmd = '%s'", name.toLowerCase(), mod));
            }
            s.close();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "read", ex);
        }
        return rs;
    }

    public boolean isOpen() {
        if (this.connect == null) {
            return false;
        }
        try {
            return !this.connect.isClosed();
        } catch (SQLException ex) {
        }
        return false;
    }
}
