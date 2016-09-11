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
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        String todoText = "UnitTest-ToDo";

        // adding a call to insertUser, so we have a user to add todos for
        String username = "unittester@tiy.com";
        String fullName = "Unit Tester";
        int userID = todoDatabase.insertUser(conn, username, fullName);

        todoDatabase.insertToDo(conn, todoText, userID);

        // make sure we can retrieve the todo we just created
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM todos where text = ?");
        stmt.setString(1, todoText);
        ResultSet results = stmt.executeQuery();
        assertNotNull(results);
        // count the records in results to make sure we get what we expected
        int numResults = 0;
        while (results.next()) {
            numResults++;
        }

        assertEquals(1, numResults);

        todoDatabase.deleteToDo(conn, todoText, userID);
        // make sure we remove the test user we added earlier
        todoDatabase.deleteUser(conn, username);

        // make sure there are no more records for our test todo
        results = stmt.executeQuery();
        numResults = 0;
        while (results.next()) {
            numResults++;
        }
        assertEquals(0, numResults);
    }

    @Test
    public void testInsertUser() throws Exception {
        Connection conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
        String userUsername = "User--username-1";
        String userFullname = "User--fullname-1";

        // INSERT USER
        todoDatabase.insertUser(conn, userUsername, userFullname);

        // MAKE SURE THE LIST IS NOT NULL
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, userUsername);
        ResultSet results = stmt.executeQuery();
        assertNotNull(results);

        //MAKE SURE THERE'S JUST ONE USER INSERTED
        int numUsers = 0;
        while (results.next()) {
            numUsers++;
        }
        assertEquals(1, numUsers);

        // DELETE THE USER
        todoDatabase.deleteUser(conn, userUsername);

        // MAKE SURE THE USER GOT DELETED
        //(should just reUse results here)
        ResultSet results2 = stmt.executeQuery();
        //(and numUsers here (don't declare a new one))
        int numUsers2 = 0;
        while (results2.next()) {
            numUsers2++;
        }
        assertEquals(0, numUsers2);
    }

    @Test
    public void testSelectAllToDos() throws Exception {
        Connection conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
        String firstToDoText = "UnitTest-ToDo1";
        String secondToDoText = "UnitTest-ToDo2";

        ArrayList<ToDoItem> todos = todoDatabase.selectToDos(conn);
        int todosBefore = todos.size();

        // adding a call to insertUser, so we have a user to add todos for
        String username = "unittester@tiy.com";
        String fullName = "Unit Tester";
        int userID = todoDatabase.insertUser(conn, username, fullName);


        todoDatabase.insertToDo(conn, firstToDoText, userID);
        todoDatabase.insertToDo(conn, secondToDoText, userID);

        todos = todoDatabase.selectToDos(conn);
        System.out.println("Found " + todos.size() + " todos in the database");

        // changed this because what if there's already 2 in there?
        assertTrue("There should be at least 2 todos in the database (there are " +
                todos.size() + ")", todos.size() >= todosBefore + 2);
        // not just = because other people could be adding to it at the same time!

        todoDatabase.deleteToDo(conn, firstToDoText, userID);
        todoDatabase.deleteToDo(conn, secondToDoText, userID);

        todoDatabase.deleteUser(conn, username);
    }

//    /*
    @Test
    public void testToggleToDo() throws Exception {
        Connection conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
        String toDoText = "UnitTest-ToDo-1";

        // adding a call to insertUser, so we have a user to add todos for
        String username = "unittester@tiy.com";
        String fullName = "Unit Tester";
        int userID = todoDatabase.insertUser(conn, username, fullName);

        todoDatabase.insertToDo(conn, toDoText, userID);

        // This gave me an error saying "No data is available" at the id = results.getInt("id") line
//        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM todos WHERE text = ?");
//        stmt.setString(1, toDoText);
//        ResultSet results = stmt.executeQuery();

//        int id = results.getInt("id");


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

        todoDatabase.deleteToDo(conn, toDoText, userID);

        holdAllToDos = todoDatabase.selectToDos(conn);
        System.out.println("Size should be zero: " + holdAllToDos.size());

        todoDatabase.deleteUser(conn, username);

    }
//    */

    @Test
    public void testInsertToDoForUser() throws Exception {
        Connection conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
        String todoText = "UnitTest-ToDo";
        String todoText2 = "UnitTest-ToDo2";

        // adding a call to insertUser, so we have a user to add todos for
        String username = "unittester@tiy.com";
        String fullName = "Unit Tester";
        int userID = todoDatabase.insertUser(conn, username, fullName);

        String username2 = "unitester2@tiy.com";
        String fullName2 = "Unit Tester 2";
        int userID2 = todoDatabase.insertUser(conn, username2, fullName2);

        todoDatabase.insertToDo(conn, todoText, userID);
        todoDatabase.insertToDo(conn, todoText2, userID2);

        // make sure each user only has one todo item
        ArrayList<ToDoItem> todosUser1 = todoDatabase.selectToDosForUser(conn, userID);
        ArrayList<ToDoItem> todosUser2 = todoDatabase.selectToDosForUser(conn, userID2);

        assertEquals(1, todosUser1.size());
        assertEquals(1, todosUser2.size());

        // make sure each todo item matches
        ToDoItem todoUser1 = todosUser1.get(0);
        assertEquals(todoText, todoUser1.text);
        ToDoItem todoUser2 = todosUser2.get(0);
        assertEquals(todoText2, todoUser2.text);

        todoDatabase.deleteToDo(conn, todoText, userID);
        todoDatabase.deleteToDo(conn, todoText2, userID2);
        // make sure we remove the test user we added earlier
        todoDatabase.deleteUser(conn, username);
        todoDatabase.deleteUser(conn, username2);

    }

    @Test
    public void testSelectUser() throws Exception {
        Connection conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
        String username = "User-username---1";
        String fullname = "User-fullname";

        todoDatabase.insertUser(conn, username, fullname);

//        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
//        stmt.setString(1, username);
//        ResultSet results = stmt.executeQuery();
//        results.next();
//        String usernameFromDB = results.getString("username");
//        String fullnameFromDB = results.getString("fullname");
//        int idFromDB = results.getInt("id");

        User myTestUser = todoDatabase.selectUser(conn, username);
        assertEquals(myTestUser.username, username);

        todoDatabase.deleteUser(conn, username);
    }

    @Test
    public void testGetNumberOfUsers() throws Exception {
        Connection conn = DriverManager.getConnection(ToDoDatabase.DB_URL);

        int numUsersBefore = todoDatabase.getNumberOfUsers(conn);
//        assertEquals(0, numUsersBefore);

        String username = "User-user-1";
        String fullname = "User-fullname";

        todoDatabase.insertUser(conn, username, fullname);

        int numUsersAfter = todoDatabase.getNumberOfUsers(conn);
//        assertEquals(1, numUsersAfter);

        String username2 = "User-2--user";
        String fullname2 = "User-fullname2";

        todoDatabase.insertUser(conn, username2, fullname2);

        int numUsersAfter2 = todoDatabase.getNumberOfUsers(conn);
//        assertEquals(2, numUsersAfter2);

        todoDatabase.deleteUser(conn, username);
        todoDatabase.deleteUser(conn, username2);

    }




}