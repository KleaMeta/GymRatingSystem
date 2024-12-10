import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.util.Scanner;

public class Main {
	// As of yet the database has only been hosted locally, so these values are subject to change across devices.
	// If you're using MySQL to host the database, the URL will be "jdbc:mysql://localhost:3306/gym_rating_system".
	// The User and Password depend on your configuration of MySQL upon installation.
	static final String URL = "";
	static final String USER = "";
	static final String PASSWORD = "";
	// For obvious reasons the method for getting the password should be made more secure but I can't think of any good way to do that that wouldn't be overkill for this project.
	
	static Connection connection;
	static Statement statement;
	
	static User user;
	
	// Reformats the string to be handled safely in SQL.
	static String safeFormat(String string) {
		return string.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
	}
	
	static String hash(String password) {
		// A placeholder for an actually secure hash function later on.
		return password;
	}
	
	public static void main(String[] args) throws SQLException {
		connection = DriverManager.getConnection(URL, USER, PASSWORD);
		statement = connection.createStatement();
		user = null;
		
		// openWindowContext();
		openCommandContext();
	}
	
	public static void openCommandContext() throws SQLException {
		Scanner scanner = new Scanner(System.in);
		
		String cmd;
		while (true) {
			System.out.print("> ");
			cmd = scanner.nextLine();
			try {
				switch (cmd) {
					case "help" -> {
						System.out.println("exit - Exit the program.");
						System.out.println("login - Log in with a particular user.");
						System.out.println("register - Register a new user.");
						System.out.println("query - (ADMIN) Make an SQL query to the database.");
						System.out.println("update - (ADMIN) Push an SQL query to the database.");
						System.out.println("add-gym - (ADMIN) Add a new gym to the database.");
						System.out.println("remove-gym - (ADMIN) Remove a gym from the database.");
						System.out.println("edit-gym - (ADMIN) Edit a gym in the database.");
						System.out.println("gyms-in-my-area - (USER) View gyms in your zip code.");
						System.out.println("review - (USER) Post a review for a Gym.");
					}
					case "exit" -> {
						System.out.println("Exiting program.");
						scanner.close();
						return;
					}
					case "login" -> {
						System.out.print("Email: ");
						String email = scanner.nextLine();
						System.out.print("Password: ");
						String password = scanner.nextLine();
						
						ResultSet result = statement.executeQuery("SELECT * FROM users WHERE email=\""+safeFormat(email)+"\"");
						if (!result.next()) {
							System.out.println("No user with this email exists.");
							continue;
						}
						if (!hash(password).equals(result.getString(3))) {
							System.out.println("Login failed.");
							continue;
						}
						System.out.println("Login successful!");
						user = new User((int)result.getObject(1), (String)result.getObject(2), (String)result.getObject(4), (boolean)result.getObject(5));
					}
					case "register" -> {
						System.out.print("Email: ");
						String email = scanner.nextLine();
						System.out.print("Password: ");
						String password = scanner.nextLine();
						System.out.print("Zip Code: ");
						String zipcode = scanner.nextLine();
						
						ResultSet result = statement.executeQuery("SELECT * FROM users WHERE email='"+safeFormat(email)+"'");
						if (result.next()) {
							System.out.println("A user with this email already exists.");
							continue;
						}
						
						try {
							statement.executeUpdate("INSERT INTO users (email, password, zip_code) VALUES ('"+safeFormat(email)+"', '"+safeFormat(hash(password))+"', '"+safeFormat(zipcode)+"')");
						} catch (SQLException e) {
							System.out.println("Failed to register new user.");
							e.printStackTrace();
							continue;
						}
						System.out.println("Registered new user! Don't forget to log in with it.");
					}
					case "query" -> {
						if (user == null) {
							System.out.println("You must be logged in to perform this action.");
							continue;
						}
						if (!user.isAdmin()) {
							System.out.println("You must be an admin to perform this action.");
							continue;
						}
						
						System.out.print("Query: ");
						String query = scanner.nextLine();
						
						ResultSet result = statement.executeQuery(query);
						ResultSetMetaData metadata = result.getMetaData();
						int cols = metadata.getColumnCount();
						for (int i = 1; i <= cols; ++i) {
							System.out.printf("%-40s  ", metadata.getColumnName(i));
						}
						System.out.println();
						while (result.next()) {
							for (int i = 1; i <= cols; ++i) {
								System.out.printf("%-40s  ", result.getString(i));
							}
							System.out.println();
						}
					}
					case "update" -> {
						if (user == null) {
							System.out.println("You must be logged in to perform this action.");
							continue;
						}
						if (!user.isAdmin()) {
							System.out.println("You must be an admin to perform this action.");
							continue;
						}
						
						System.out.print("Query: ");
						String query = scanner.nextLine();
						
						statement.executeUpdate(query);
					}
					case "add-gym" -> {
						if (user == null) {
							System.out.println("You must be logged in to perform this action.");
							continue;
						}
						if (!user.isAdmin()) {
							System.out.println("You must be an admin to perform this action.");
							continue;
						}
						
						System.out.print("Name: ");
						String name = scanner.nextLine();
						System.out.print("Address: ");
						String address = scanner.nextLine();
						System.out.print("Zip Code: ");
						String zipcode = scanner.nextLine();
						System.out.print("Type: ");
						String type = scanner.nextLine();
						System.out.print("Hours: ");
						String hours = scanner.nextLine();
						System.out.print("Equipment: ");
						String equipment = scanner.nextLine();
	
						statement.executeUpdate("INSERT INTO gyms (name, address, zip_code, type, hours, equipment) VALUES ("+name+", "+address+", "+zipcode+", "+type+", "+hours+", "+equipment+")");
						System.out.println("Gym added.");
					}
					case "remove-gym" -> {
						if (user == null) {
							System.out.println("You must be logged in to perform this action.");
							continue;
						}
						if (!user.isAdmin()) {
							System.out.println("You must be an admin to perform this action.");
							continue;
						}
						
						System.out.print("Gym ID: ");
						String gym = scanner.nextLine();
						
						statement.executeUpdate("DELETE FROM gyms WHERE id="+gym);
						System.out.println("Gym removed.");
					}
					case "gyms-in-my-area" -> {
						if (user == null) {
							System.out.println("You must be logged in to perform this action.");
							continue;
						}
						
						ResultSet result = statement.executeQuery("SELECT name, address, zip_code, type, hours, equipment FROM gyms WHERE zip_code='"+user.getZipCode()+"'");
						ResultSetMetaData metadata = result.getMetaData();
						int cols = metadata.getColumnCount();
						for (int i = 1; i <= cols; ++i) {
							System.out.printf("%-40s  ", metadata.getColumnName(i));
						}
						System.out.println();
						while (result.next()) {
							for (int i = 1; i <= cols; ++i) {
								System.out.printf("%-40s  ", result.getString(i));
							}
							System.out.println();
						}
					}
					case "review" -> {
						if (user == null) {
							System.out.println("You must be logged in to perform this action.");
							continue;
						}
	
						// Note: In the GUI context the users won't have to know the gym ID, and it'll be easier for the code to get on its own.
						// I could have also had this be by name but two gyms with the same name could potentially exist so in the real program we will be retrieving it by ID.
						System.out.println("Enter the ID of the gym you would like to review.");
						System.out.print("Gym ID: ");
						String gym = scanner.nextLine();
						
						ResultSet result = statement.executeQuery("SELECT * FROM gyms WHERE id="+gym);
						if (!result.next()) {
							System.out.println("No gym with that ID exists.");
							continue;
						}
						
						result = statement.executeQuery("SELECT * FROM ratingsandreviews WHERE user_id="+user.getId()+" AND gym_id="+gym);
						if (result.next()) {
							System.out.println("You've already posted a review for this gym. Would you like to replace it?");
							System.out.print("Y/N: ");
							String yorn = scanner.nextLine();
							if (yorn.toUpperCase().equals("Y")) {
								statement.executeUpdate("DELETE FROM ratingsandreviews WHERE id="+result.getString(1));
							} else {
								continue;
							}
						}
						System.out.print("Rating: ");
						String rating = scanner.nextLine();
						System.out.println("Review (type \"END\" in a new line to end the review): ");
						String review = "";
						String reviewInput = scanner.nextLine();
						if (!reviewInput.equals("END")) {
							review = reviewInput;
							reviewInput = scanner.nextLine();
						}
						while (!reviewInput.equals("END")) {
							review += "\\r\\n" + safeFormat(reviewInput);
							reviewInput = scanner.nextLine();
						}
						statement.executeUpdate("INSERT INTO ratingsandreviews (gym_id, user_id, rating, review) VALUES ("+gym+", "+user.getId()+", "+rating+", '"+review+"')");
					}
					default -> {
						System.out.println("Command not recognized. Please try again.");
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	// A placeholder. For now all functionality is implemented in the Command Context and a proper GUI for it will be created later.
	public static void openWindowContext() {
		JFrame window = new JFrame();
		
		DefaultTableModel model = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable output = new JTable();
		output.setModel(model);
		
		JScrollPane scroll = new JScrollPane(output);
		scroll.setVisible(true);
		
		JTextField input = new JTextField();
		input.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ResultSet result = statement.executeQuery(input.getText());
					ResultSetMetaData metadata = result.getMetaData();
					int cols = metadata.getColumnCount();
					model.setColumnCount(0);
					model.setRowCount(0);
					for (int i = 1; i <= cols; ++i) {
						model.addColumn(metadata.getColumnName(i));
					}
					while (result.next()) {
						String[] rowData = new String[cols];
						for (int i = 0; i < cols; ++i) {
							rowData[i] = result.getString(i+1);
						}
						model.addRow(rowData);
					}
					output.revalidate();
					input.setText("");
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		});
		
		JButton button = new JButton();
		button.setText("");
		
		window.setTitle("Gym Rating System");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.add(input, BorderLayout.NORTH);
		window.add(scroll, BorderLayout.SOUTH);
		window.setSize(1280, 720);
		window.setVisible(true);
	}
}