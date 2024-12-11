import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

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
	
	static Scene scene;
	static User user;
	static int gymId;
	
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
		
		openWindowContext();
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
	
	public static void loadScene(JFrame window, Scene newScene) {
		// Clear window.
		Container pane = window.getContentPane();
		while (pane.getComponentCount() > 0) { pane.remove(0); }
		window.setContentPane(new JPanel(null));
		int width = window.getWidth() - 13; // Real width of displayed window.
		int height = window.getHeight() - 35; // Real height of displayed window.
		scene = newScene;
		switch (newScene) {
			case Login -> {
				JLabel emailLabel = new JLabel();
				emailLabel.setText("Enter your email:");
				emailLabel.setBounds(width / 2 - 80, height / 4, 160, 20);
				window.add(emailLabel);
				
				JTextField emailField = new JTextField();
				emailField.setBounds(width / 2 - 80, height / 4 + 20, 160, 20);
				window.add(emailField);

				JLabel passwordLabel = new JLabel();
				passwordLabel.setText("Enter your password:");
				passwordLabel.setBounds(width / 2 - 80, height / 4 + 40, 160, 20);
				window.add(passwordLabel);
				
				JPasswordField passwordField = new JPasswordField();
				passwordField.setBounds(width / 2 - 80, height / 4 + 60, 160, 20);
				window.add(passwordField);
				
				JLabel errorLabel = new JLabel();
				errorLabel.setText("");
				errorLabel.setBounds(width / 2 - 100, height / 4 + 140, 200, 20);
				errorLabel.setForeground(Color.RED);
				window.add(errorLabel);
				
				JButton loginButton = new JButton();
				loginButton.setText("Log In");
				loginButton.setBounds(width / 2 - 80, height / 4 + 100, 160, 20);
				loginButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String email = emailField.getText();
						String password = new String(passwordField.getPassword());
						try {
							ResultSet result = statement.executeQuery("SELECT * FROM users WHERE email=\""+safeFormat(email)+"\"");
							if (!result.next()) {
								errorLabel.setText("No user with matching email found.");
								return;
							}
							if (!hash(password).equals(result.getString(3))) {
								errorLabel.setText("Incorrect password.");
								return;
							}
							user = new User((int)result.getObject(1), (String)result.getObject(2), (String)result.getObject(4), (boolean)result.getObject(5));
							result.close();
						} catch (SQLException ex) {
							errorLabel.setText("Connection failure.");
							return;
						}
						errorLabel.setText("Success!");
						errorLabel.setForeground(Color.GREEN);
						loadScene(window, Scene.Home);
					}
				});
				window.add(loginButton);

				JLabel registerLabel1 = new JLabel();
				registerLabel1.setText("No account?");
				registerLabel1.setBounds(width / 2 - 80, height * 3 / 4, 160, 20);
				window.add(registerLabel1);

				JLabel registerLabel2 = new JLabel();
				registerLabel2.setText("Click here to make one:");
				registerLabel2.setBounds(width / 2 - 80, height * 3 / 4 + 20, 160, 20);
				window.add(registerLabel2);
				
				JButton registerButton = new JButton();
				registerButton.setText("Register");
				registerButton.setBounds(width / 2 - 80, height * 3 / 4 + 40, 160, 20);
				registerButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scene.Register);
					}
				});
				window.add(registerButton);
				
				JLabel continueLabel = new JLabel();
				continueLabel.setText("Or continue without one:");
				continueLabel.setBounds(width / 2 - 80, height * 3 / 4 + 60, 160, 20);
				window.add(continueLabel);
				
				JButton continueButton = new JButton();
				continueButton.setText("Continue");
				continueButton.setBounds(width / 2 - 80, height * 3 / 4 + 80, 160, 20);
				continueButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scene.HomeNoUser);
					}
				});
				window.add(continueButton);
			}
			case Register -> {
				JLabel emailLabel = new JLabel();
				emailLabel.setText("Enter your email:");
				emailLabel.setBounds(width / 2 - 80, height / 4, 160, 20);
				window.add(emailLabel);
				
				JTextField emailField = new JTextField();
				emailField.setBounds(width / 2 - 80, height / 4 + 20, 160, 20);
				window.add(emailField);

				JLabel passwordLabel = new JLabel();
				passwordLabel.setText("Enter a password:");
				passwordLabel.setBounds(width / 2 - 80, height / 4 + 40, 160, 20);
				window.add(passwordLabel);
				
				JPasswordField passwordField = new JPasswordField();
				passwordField.setBounds(width / 2 - 80, height / 4 + 60, 160, 20);
				window.add(passwordField);

				JLabel zipcodeLabel = new JLabel();
				zipcodeLabel.setText("Enter your Zip Code:");
				zipcodeLabel.setBounds(width / 2 - 80, height / 4 + 80, 160, 20);
				window.add(zipcodeLabel);
				
				JTextField zipcodeField = new JTextField();
				zipcodeField.setBounds(width / 2 - 80, height / 4 + 100, 160, 20);
				window.add(zipcodeField);
				
				JLabel errorLabel = new JLabel();
				errorLabel.setText("");
				errorLabel.setBounds(width / 2 - 100, height / 4 + 160, 200, 20);
				errorLabel.setForeground(Color.RED);
				window.add(errorLabel);
				
				JButton registerButton = new JButton();
				registerButton.setText("Register");
				registerButton.setBounds(width / 2 - 80, height / 4 + 140, 160, 20);
				registerButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String email = emailField.getText();
						String password = new String(passwordField.getPassword());
						String zipcode = zipcodeField.getText();
						try {
							ResultSet result = statement.executeQuery("SELECT * FROM users WHERE email='"+safeFormat(email)+"'");
							if (result.next()) {
								errorLabel.setText("A user with this email already exists.");
								return;
							}
							statement.executeUpdate("INSERT INTO users (email, password, zip_code) VALUES ('"+safeFormat(email)+"', '"+safeFormat(hash(password))+"', '"+safeFormat(zipcode)+"')");
							
						} catch (SQLException ex) {
							errorLabel.setText("Connection failure.");
							return;
						}
						errorLabel.setText("Success!");
						errorLabel.setForeground(Color.GREEN);
						loadScene(window, Scene.Login);
					}
				});
				
				JButton backButton = new JButton();
				backButton.setText("Go Back");
				backButton.setBounds(width / 2 - 80, height * 3 / 4, 160, 20);
				backButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scene.Login);
					}
				});
				window.add(backButton);
			}
			case Home -> {
				JButton accountButton = new JButton();
				accountButton.setText("Manage Account");
				accountButton.setBounds(width - 160, 0, 160, 40);
				accountButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scene.Account);
					}
				});
				window.add(accountButton);

				JButton logoutButton = new JButton();
				logoutButton.setText("Log Out");
				logoutButton.setBounds(width - 160, 40, 160, 40);
				logoutButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						user = null;
						loadScene(window, Scene.Login);
					}
				});
				window.add(logoutButton);
				
				if (user.isAdmin()) {
					JButton adminButton = new JButton();
					adminButton.setText("Admin Tools");
					adminButton.setBounds(width - 160, 100, 160, 40);
					adminButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							user = null;
							loadScene(window, Scene.AdminTools);
						}
					});
					window.add(adminButton);
				}
				
				JButton gymsInZipCodeButton = new JButton();
				gymsInZipCodeButton.setText("Gyms in your Zip Code");
				gymsInZipCodeButton.setBounds(width / 4 - 80, height * 3 / 4 - 20, 160, 40);
				window.add(gymsInZipCodeButton);
				
				JButton searchButton = new JButton();
				searchButton.setText("Search for Gyms");
				searchButton.setBounds(width / 2 - 80, height * 3 / 4 - 20, 160, 40);
				searchButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scene.GymSearch);
					}
				});
				window.add(searchButton);
			}
			case HomeNoUser -> {
				JButton loginButton = new JButton();
				loginButton.setText("Log In");
				loginButton.setBounds(width - 160, 0, 160, 40);
				loginButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scene.Login);
					}
				});
				window.add(loginButton);
				
				JButton searchButton = new JButton();
				searchButton.setText("Search for Gyms");
				searchButton.setBounds(width / 2 - 80, height * 3 / 4 - 20, 160, 40);
				searchButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scene.GymSearch);
					}
				});
				window.add(searchButton);
			}
			case GymSearch -> {
				JPanel listPanel = new JPanel();
				listPanel.setLayout(null);
				
				JScrollPane scrollPane = new JScrollPane(listPanel);
				scrollPane.setBounds(0, 80, width, height - 80);
				scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setMaximumSize(new Dimension(width, height - 80));
				window.add(scrollPane);
				
				JLabel searchLabel = new JLabel();
				searchLabel.setText("Enter zip code, name, or desired equipment:");
				searchLabel.setBounds(0, 0, width, 40);
				window.add(searchLabel);
				
				JButton backButton = new JButton();
				backButton.setText("Back");
				backButton.setBounds(width - 160, 0, 160, 40);
				backButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (user == null) {
							loadScene(window, Scene.HomeNoUser);
						} else {
							loadScene(window, Scene.Home);
						}
					}
				});
				window.add(backButton);
				
				JTextField searchField = new JTextField();
				searchField.setBounds(0, 40, width, 40);
				searchField.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						listPanel.removeAll();
						
						int y = 0;
						try {
							ResultSet result = statement.executeQuery("SELECT id, name, address FROM gyms WHERE zip_code='"+searchField.getText()+"' OR name LIKE '%"+searchField.getText()+"%' OR type LIKE '%"+searchField.getText()+"%'");
							while (result.next()) {
								JLabel gymName = new JLabel();
								gymName.setText(result.getString(2));
								gymName.setBounds(20, 10 + y, width, 20);
								listPanel.add(gymName);
								
								JLabel gymAddress = new JLabel();
								gymAddress.setText(result.getString(3));
								gymAddress.setBounds(20, 30 + y, width, 20);
								listPanel.add(gymAddress);

								int id = (int)result.getObject(1);
								JButton viewButton = new JButton();
								viewButton.setText("Details");
								if (user == null || !user.isAdmin()) {
									viewButton.setBounds(width - 160, 10 + y, 120, 40);
								} else {
									viewButton.setBounds(width - 240, 10 + y, 120, 40);
								}
								viewButton.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										gymId = id;
										loadScene(window, Scene.GymDetails);
									}
								});
								listPanel.add(viewButton);
								
								if (user != null && user.isAdmin()) {
									JButton editButton = new JButton();
									editButton.setText("Edit");
									editButton.setBounds(width - 120, 10 + y, 80, 20);
									editButton.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											gymId = id;
											loadScene(window, Scene.GymEdit);
										}
									});
									listPanel.add(editButton);
									
									JButton deleteButton = new JButton();
									deleteButton.setText("Delete");
									deleteButton.setBounds(width - 120, 30 + y, 80, 20);
									deleteButton.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											try {
												statement.executeUpdate("DELETE FROM gyms WHERE id="+id);
											} catch (SQLException ex) {
												
											}
											loadScene(window, Scene.GymSearch);
										}
									});
									listPanel.add(deleteButton);
								}
								
								y += 60;
							}
						} catch (SQLException ex) {
							ex.printStackTrace();
						}
						
						listPanel.setPreferredSize(new Dimension(width, y));
						listPanel.revalidate();
						listPanel.repaint();
						
						scrollPane.setViewportView(listPanel);
						scrollPane.revalidate();
						scrollPane.repaint();
						
						window.revalidate();
					}
				});
				window.add(searchField);
			}
			case GymDetails -> {
				JButton backButton = new JButton();
				backButton.setText("Home");
				backButton.setBounds(width - 160, 0, 160, 40);
				backButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (user == null) {
							loadScene(window, Scene.HomeNoUser);
						} else {
							loadScene(window, Scene.Home);
						}
					}
				});
				window.add(backButton);
				
				ResultSet gymResult, reviewResult, userResult;
				try {
					gymResult = statement.executeQuery("SELECT name, address, zip_code, type, hours, equipment FROM gyms WHERE id="+gymId);
					if (!gymResult.next()) {
						JLabel errorLabel = new JLabel();
						errorLabel.setText("Connection failed.");
						errorLabel.setForeground(Color.RED);
						errorLabel.setBounds(width / 2 - 160, height / 2 - 20, 320, 40);
						window.add(errorLabel);
						return;
					}

					JLabel nameLabel = new JLabel();
					nameLabel.setText(gymResult.getString(1));
					Font font = nameLabel.getFont();
					nameLabel.setFont(new Font(font.getName(), font.getStyle(), font.getSize() * 2));
					nameLabel.setBounds(20, 20, width - 20, 40);
					window.add(nameLabel);

					JLabel addressLabel = new JLabel();
					addressLabel.setText(gymResult.getString(2));
					addressLabel.setBounds(20, 60, width - 20, 20);
					window.add(addressLabel);

					JLabel zipcodeLabel = new JLabel();
					zipcodeLabel.setText(gymResult.getString(3));
					zipcodeLabel.setBounds(20, 80, width - 20, 20);
					window.add(zipcodeLabel);

					JLabel typeLabel = new JLabel();
					typeLabel.setText("Type: " + gymResult.getString(4));
					typeLabel.setBounds(20, 120, width - 20, 20);
					window.add(typeLabel);
					
					JLabel hoursLabel = new JLabel();
					hoursLabel.setText("Open: " + gymResult.getString(5));
					hoursLabel.setBounds(20, 140, width - 20, 20);
					window.add(hoursLabel);

					JLabel equipmentLabel = new JLabel();
					equipmentLabel.setText("Equipment: " + gymResult.getString(6));
					equipmentLabel.setBounds(20, 160, width - 20, 20);
					window.add(equipmentLabel);
					
					JPanel reviewPanel = new JPanel();
					reviewPanel.setLayout(null);
					JScrollPane reviewScroll = new JScrollPane();
					reviewScroll.setBounds(width - 340, 60, 320, height - 80);
					reviewScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
					reviewScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					reviewScroll.setMaximumSize(new Dimension(width, height - 80));
					window.add(reviewScroll);
					
					double avgRating = 0.0;
					int ratingCount = 0;
					int y = 0;
					reviewResult = statement.executeQuery("SELECT rating, review, user_id FROM ratingsandreviews WHERE gym_id="+gymId);
					while (reviewResult.next()) {
						avgRating += (int)reviewResult.getObject(1);
						++ratingCount;

						Statement userStatement = connection.createStatement();
						userResult = userStatement.executeQuery("SELECT email FROM users WHERE id="+reviewResult.getString(3));
						String username;
						if (userResult.next()) {
							username = userResult.getString(1);
						} else {
							username = "[DELETED]";
						}
						
						JLabel userLabel = new JLabel();
						userLabel.setText(username);
						userLabel.setBounds(10, 10 + y, 160, 20);
						reviewPanel.add(userLabel);

						JLabel ratingLabel = new JLabel();
						ratingLabel.setText(reviewResult.getString(3));
						ratingLabel.setBounds(280, 10 + y, 20, 20);
						reviewPanel.add(ratingLabel);

						JLabel reviewLabel = new JLabel();
						reviewLabel.setText("<html>"+reviewResult.getString(2)+"</html>");
						reviewLabel.setBounds(10, 10 + y, 300, 80);
						reviewPanel.add(reviewLabel);
						
						y += 80;
					}
					if (ratingCount > 0) {
						avgRating /= ratingCount;
					}
					reviewPanel.setPreferredSize(new Dimension(320, y));
					reviewPanel.revalidate();
					reviewPanel.repaint();
					reviewScroll.setViewportView(reviewPanel);
					reviewScroll.revalidate();
					reviewScroll.repaint();

					JLabel ratingLabel = new JLabel();
					ratingLabel.setText(String.format("Rating: %.1f (%d Reviews)", avgRating, ratingCount));
					ratingLabel.setBounds(20, 200, width - 20, 20);
					window.add(ratingLabel);
				} catch (SQLException ex) {
					ex.printStackTrace();
					JLabel errorLabel = new JLabel();
					errorLabel.setText("Connection failed.");
					errorLabel.setForeground(Color.RED);
					errorLabel.setBounds(width / 2 - 160, height / 2 - 20, 320, 40);
					window.add(errorLabel);
					return;
				}
			}
		}
		window.revalidate();
	}
	
	public static void openWindowContext() {
		scene = Scene.Login;
		
		JFrame window = new JFrame();
		window.setTitle("Gym Rating System");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setBounds(0, 0, 1280, 720);
		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				loadScene(window, scene);
			}
		});
		window.setContentPane(new JPanel(null));
		
		loadScene(window, Scene.Login);
		
		window.setVisible(true);
	}
}
