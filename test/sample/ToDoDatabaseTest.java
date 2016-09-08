package sample;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by jessicatracy on 9/8/16.
 */
public class ToDoDatabaseTest {
    static ToDoDatabase todoDatabase = null;

    public ToDoDatabaseTest() {
        super();
        System.out.println("Building a new ToDoDatabaseTest instance ****");
    }

    @Before
    public void setUp() throws Exception {
        if (todoDatabase == null) {
            todoDatabase = new ToDoDatabase();
            todoDatabase.init();
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testInit() throws Exception {
        // test to make sure we can access the new database
        Connection conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
        PreparedStatement todoQuery = conn.prepareStatement("SELECT * FROM todos");
        ResultSet results = todoQuery.executeQuery();
        assertNotNull(results);

    }

//    @Test
//    public void testInsertToDo() throws Exception {
//        Connection conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
//        String todoText = "UnitTest-ToDo";
//        todoDatabase.insertToDo(conn, todoText);
//        // make sure we can retrieve the to do we just created
//        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM todos where text = ?");
//        stmt.setString(1, todoText);
//        ResultSet results = stmt.executeQuery();
//        assertNotNull(results);
//        // count the records in results to make sure we get what we expected
//        int numResults = 0;
//        while (results.next()) {
//            numResults++;
//        }
//        assertEquals(1, numResults);
//    }

    @Test
    public void testInsertToDo() throws Exception {
        Connection conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
        String todoText = "UnitTest-ToDo";

        todoDatabase.insertToDo(conn, todoText);

        // make sure we can retrieve the to do we just created
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM todos WHERE text = ?");
        stmt.setString(1, todoText);
        ResultSet results = stmt.executeQuery();
        assertNotNull(results);
        // count the records in results to make sure we get what we expected
        int numResults = 0;
        while (results.next()) {
            numResults++;
        }

        assertEquals(1, numResults);

        todoDatabase.deleteToDo(conn, todoText);

        // make sure there are no more records for our test to do
        results = stmt.executeQuery();
        numResults = 0;
        while (results.next()) {
            numResults++;
        }
        assertEquals(0, numResults);
    }

    @Test
    public void testSelectAllToDos() throws Exception {
        Connection conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
        String firstToDoText = "UnitTest-ToDo1";
        String secondToDoText = "UnitTest-ToDo2";

        ArrayList<ToDoItem> todos = todoDatabase.selectToDos(conn);
        int todosBefore = todos.size();

        todoDatabase.insertToDo(conn, firstToDoText);
        todoDatabase.insertToDo(conn, secondToDoText);

        todos = todoDatabase.selectToDos(conn);
        System.out.println("Found " + todos.size() + " todos in the database");

        // changed this because what if there's already 2 in there?
        assertTrue("There should be at least 2 todos in the database (there are " +
                todos.size() + ")", todos.size() >= todosBefore + 2);
        // not just = because other people could be adding to it at the same time!

        todoDatabase.deleteToDo(conn, firstToDoText);
        todoDatabase.deleteToDo(conn, secondToDoText);
    }

    // doesn't work
    @Test
    public void testToggleToDo() throws Exception {
        Connection conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
        String toDoText = "UnitTest-ToDo-1";

        todoDatabase.insertToDo(conn, toDoText);

        // This gave me an error saying "No data is available" at the id = results.getInt("id") line
//        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM todos WHERE text = ?");
//        stmt.setString(1, toDoText);
//        ResultSet results = stmt.executeQuery();

//        int id = results.getInt("id");

        // This way still gave me an error
//        int id = 0;
//        while (results.next()) {
//            id = results.getInt("id");
//        }

        ArrayList<ToDoItem> holdAllToDos = todoDatabase.selectToDos(conn);
        boolean beforeIsDone = holdAllToDos.get(0).isDone;
        int id = holdAllToDos.get(0).id;

        todoDatabase.toggleToDo(conn, id);

        holdAllToDos = todoDatabase.selectToDos(conn);
        boolean afterIsDone = holdAllToDos.get(0).isDone;


//        PreparedStatement stmt2 = conn.prepareStatement("SELECT * FROM todos WHERE id = ?");
//        stmt2.setInt(1, id);
//        ResultSet results2 = stmt2.executeQuery();
//        boolean is_done = results2.getBoolean("is_done");
//        assertFalse(is_done);


        assertFalse(beforeIsDone == afterIsDone);

        todoDatabase.deleteToDo(conn, toDoText);

        holdAllToDos = todoDatabase.selectToDos(conn);
        System.out.println("Size should be zero: " + holdAllToDos.size());

    }



}