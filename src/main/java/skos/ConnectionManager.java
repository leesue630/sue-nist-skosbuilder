package skos;

/**
 * Created by lns16 on 7/27/2017.
 */

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    private Driver driver;

    private ConnectionManager() throws SQLException {
        Driver driver = getDriver();
        DriverManager.registerDriver(driver);
    }

    private Driver getDriver() {
        String driverClassName = "oracle.jdbc.driver.OracleDriver";
        try {
            return (Driver) Class.forName(driverClassName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Connection getConnection() throws SQLException {
        String sid = "OAGISRTDB";
        String url = "jdbc:oracle:thin:@giotto:1521/" + sid;
        String username = "oagi";
        String password = "oagi";

        return DriverManager.getConnection(url, username, password);
    }

    private static ConnectionManager instance;

    public static synchronized ConnectionManager getInstance() {
        try{
            if (instance == null) {
                instance = new ConnectionManager();
            }
            return instance;
        } catch (SQLException e) {
            System.out.println("SQLException!!");
            return null;
        }
    }

}