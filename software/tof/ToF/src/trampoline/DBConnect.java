/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package trampoline;
import java.sql.*;

/**
 *
 * @author Andreas
 */
public class DBConnect {
    DBConnect() {
        try {
            Class.forName("SQLite.JDBCDriver");

            // connect to the database
            Connection conn = DriverManager.getConnection("jdbc:data/database");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
   
}
