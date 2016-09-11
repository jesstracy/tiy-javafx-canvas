package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import jodd.json.JsonParser;
import jodd.json.JsonSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller implements Initializable {
    @FXML
    ListView todoList;

    @FXML
    TextField todoText;

    ObservableList<ToDoItem> todoItems = FXCollections.observableArrayList();
    ArrayList<ToDoItem> savableList = new ArrayList<ToDoItem>();
    String fileName = "todos.json";

    ToDoDatabase myToDoDatabase;
    Connection conn;


    User currentUser;
//    ArrayList<ToDoItem> currentUsersToDos;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        System.out.print("Please enter your name: ");
//        Scanner inputScanner = new Scanner(System.in);
//        username = inputScanner.nextLine();
//
//        if (username != null && !username.isEmpty()) {
//            fileName = username + ".json";
//        }
//
//        System.out.println("Checking existing list ...");
//        ToDoItemList retrievedList = retrieveList();
//        if (retrievedList != null) {
//            for (ToDoItem item : retrievedList.todoItems) {
//                todoItems.add(item);
//            }
//        }
//
//        todoList.setItems(todoItems);
        Scanner myScanner = new Scanner(System.in);
        String username;
        String fullname;
        try {
            myToDoDatabase = new ToDoDatabase();
            conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
            myToDoDatabase.init();
            boolean keepLooping = true;

            if (myToDoDatabase.getNumberOfUsers(conn) == 0) {
                currentUser = enterNewUser(myScanner);
            } else {
                while (keepLooping) {
                    System.out.println("Please enter your userID, or enter \"0\" to create a new user.");
                    System.out.println("USER ID\t\tUSERNAME\t\t\t\tFULL NAME");
                    ArrayList<Integer> userIdHolder = new ArrayList<Integer>();
                    for (User user : myToDoDatabase.getAllUsers(conn)) {
                        userIdHolder.add(user.id);
                        System.out.println("   " + user.id + "\t\t" + user.username + "\t\t\t" + user.fullname);
                    }
                    System.out.println("   0" + "\t\tNew User");
                    int userSelection = myScanner.nextInt();
                    myScanner.nextLine();

                    if (userSelection == 0) {
                        // Create a new user
                        currentUser = enterNewUser(myScanner);
                        keepLooping = false;
                    } else {
                        boolean userIsInDB = false;
                        for (int id : userIdHolder) {
                            if (userSelection == id) {
                                userIsInDB = true;
                            }
                        }
                        if (userIsInDB) {
                            username = myToDoDatabase.getUserNameByID(conn, userSelection);
                            currentUser = myToDoDatabase.selectUser(conn, username);
                            keepLooping = false;
                        } else {
                            System.out.println("Sorry, that user does not exist in the database.");
                        }
                    }
                }

                // get the current user's todoitems if they have any
                ArrayList<ToDoItem> toDoItemsFromDB = myToDoDatabase.selectToDosForUser(conn, currentUser.id);
                for (ToDoItem item : toDoItemsFromDB) {
//                System.out.println(item.toString());
                    todoItems.add(item);
                }
                todoList.setItems(todoItems);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
/*
        try {
            myToDoDatabase = new ToDoDatabase();
            conn = DriverManager.getConnection(ToDoDatabase.DB_URL);
            myToDoDatabase.init();
        } catch (SQLException ex) {
            System.out.println("Exception caught getting connection or initializing database.");
            ex.printStackTrace();
        }

        Scanner myScanner = new Scanner(System.in);

        boolean keepLooping = true;
        while (keepLooping) {
            System.out.println("Please enter your email to retrieve your todos, or enter \"0\" to create a new user.");
            String username = myScanner.nextLine();
            String fullname;
            if (username.equals("0")) {
                // Make a new user
                // get username, fullname, add to DB, get id, and then create new user.
                // ACTUALLY, I WILL CREATE THE USER AFTER THE IF/ELSE BC IT WILL DO THE SAME THING EITHER WAY, VARIABLES CALLED THE SAME THING
                System.out.print("New user! What is your email? ");
                username = myScanner.nextLine();
                System.out.print("What is your full name? ");
                fullname = myScanner.nextLine();

                try {
                    myToDoDatabase.insertUser(conn, username, fullname);
                } catch (SQLException ex) {
                    System.out.println("Exception caught inserting user to DB.");
                    ex.printStackTrace();
                }
            }
            try {
                if (myToDoDatabase.getNumberOfUsers(conn) == 0) {
                    System.out.println("There are no records for this user! (No users in DB)");
                } else {
                    currentUser = myToDoDatabase.selectUser(conn, username);
                    if (currentUser == null) {
                        System.out.println("There are no records for this user!");
                    } else {
                        keepLooping = false;
                    }
                }
            } catch (SQLException ex) {
                System.out.println("Exception caught selecting user from db.");
                ex.printStackTrace();
            }
        }

        // Get the current user's todos (if they have any)
        try {
            ArrayList<ToDoItem> toDoItemsFromDB = myToDoDatabase.selectToDosForUser(conn, currentUser.id);
            for (ToDoItem item : toDoItemsFromDB) {
//                System.out.println(item.toString());
                todoItems.add(item);
            }
            todoList.setItems(todoItems);

        } catch (SQLException ex) {
            System.out.println("Caught exception selecting todos for user.");
            ex.printStackTrace();
        }
*/
    }

    public User enterNewUser(Scanner myScanner) throws SQLException {
        System.out.print("New user! What is your email? ");
        String username = myScanner.nextLine();
        System.out.print("What is your full name? ");
        String fullname = myScanner.nextLine();

        int userId = myToDoDatabase.insertUser(conn, username, fullname);

        currentUser = new User(username, fullname, userId);
        return currentUser;
    }

    public void saveToDoList() {
        if (todoItems != null && todoItems.size() > 0) {
            System.out.println("Saving " + todoItems.size() + " items in the list");
            savableList = new ArrayList<ToDoItem>(todoItems);
            System.out.println("There are " + savableList.size() + " items in my savable list");
            saveList();
        } else {
            System.out.println("No items in the ToDo List");
        }
    }

//    public void setUsername() {
//        System.out.println("Now setting username!!");
//        set the text to empty??
//    }

    public void addItem() {
        try {
            System.out.println("Adding item ...");
//            todoItems.add(new ToDoItem(todoText.getText()));

            int id = myToDoDatabase.insertToDo(conn, todoText.getText(), currentUser.id);

            ToDoItem newToDoItem = new ToDoItem(id, todoText.getText());
            todoItems.add(newToDoItem);

            todoText.setText("");

//            todoList.setItems(todoItems);
        } catch (SQLException ex) {
            System.out.println("Exception caught inserting toDo");
            ex.printStackTrace();
        }

    }

    public void removeItem() {
        try {
            ToDoItem todoItem = (ToDoItem) todoList.getSelectionModel().getSelectedItem();
            System.out.println("Removing " + todoItem.text + " ...");
            todoItems.remove(todoItem);
            myToDoDatabase.deleteToDo(conn, todoItem.getText(), currentUser.id);
        } catch (SQLException ex) {
            System.out.println("Exception caught when deleting from database");
            ex.printStackTrace();
        }
    }

    public void toggleItem() {
        try {
            System.out.println("Toggling item ...");
            ToDoItem todoItem = (ToDoItem) todoList.getSelectionModel().getSelectedItem();
            if (todoItem != null) {
                todoItem.isDone = !todoItem.isDone;
                String textString = todoItem.getText();
//                System.out.println("TOGGLING ITEM IN DB");
//                System.out.println(todoItem.id);
                myToDoDatabase.toggleToDo(conn, todoItem.id);


                todoList.setItems(null);
                todoList.setItems(todoItems);
            }
        } catch (SQLException ex) {
            System.out.println("Exception caught when making prepared statement");
            ex.printStackTrace();
        }
    }

    public void saveList() {
        try {

            // write JSON
            JsonSerializer jsonSerializer = new JsonSerializer().deep(true);
            String jsonString = jsonSerializer.serialize(new ToDoItemList(todoItems));

            System.out.println("JSON = ");
            System.out.println(jsonString);

            File sampleFile = new File(fileName);
            FileWriter jsonWriter = new FileWriter(sampleFile);
            jsonWriter.write(jsonString);
            jsonWriter.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public ToDoItemList retrieveList() {
        try {

            Scanner fileScanner = new Scanner(new File(fileName));
            fileScanner.useDelimiter("\\Z"); // read the input until the "end of the input" delimiter
            String fileContents = fileScanner.next();
            JsonParser ToDoItemParser = new JsonParser();

            ToDoItemList theListContainer = ToDoItemParser.parse(fileContents, ToDoItemList.class);
            System.out.println("==============================================");
            System.out.println("        Restored previous ToDoItem");
            System.out.println("==============================================");
            return theListContainer;
        } catch (IOException ioException) {
            // if we can't find the file or run into an issue restoring the object
            // from the file, just return null, so the caller knows to create an object from scratch
            return null;
        }
    }
    
}
