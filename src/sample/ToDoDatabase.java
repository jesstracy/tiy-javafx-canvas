package sample;

import org.h2.tools.Server;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by jessicatracy on 9/8/16.
 */
public class ToDoDatabase {
    public final static String DB_URL = "jdbc:h2:./main";

    //Initializes database and creates table if doesn't already exist
    public void init() throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS todos (id IDENTITY, text VARCHAR, is_done BOOLEAN)");
    }

    //Adds a new to do
    public void insertToDo(Connection conn, String text) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO todos VALUES (NULL, ?, false)");
        stmt.setString(1, text);
        stmt.execute();
    }

    //delete a specific to do
    public void deleteToDo(Connection conn, String text) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM todos WHERE text = ?");
        stmt.setString(1, text);
        stmt.execute();
    }

    public static ArrayList<ToDoItem> selectToDos(Connection conn) throws SQLException {
        ArrayList<ToDoItem> items = new ArrayList<>();
//        /*
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM todos");
        while (results.next()) {
            int id = results.getInt("id");
            String text = results.getString("text");
            boolean isDone = results.getBoolean("is_done");
            items.add(new ToDoItem(id, text, isDone));
        }
//        */
        return items;
    }

    public void toggleToDo(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE todos SET is_done = NOT is_done WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

}
