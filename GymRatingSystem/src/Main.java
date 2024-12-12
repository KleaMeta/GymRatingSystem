import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigInteger;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.*;

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
	
	static Scenes scene; // Denotes the current screen of the application.
	static User user; // Is null if no user is logged in, otherwise contains all relevant data about the user.
	static int gymId; // Stores the ID of the currently viewed gym for easily loading on scene startup.
	static String lastSearch; // Saves the last search made by the user so that it may be inputted automatically.
	
	// Reformats the string to be written safely to the SQL database.
	static String safeFormat(String string) {
		return string.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
	}
	
	// Hashes the password for comparing to passwords inside the database. This is done to ensure that in the event of a database leak, passwords cannot be easily obtained for every user.
	// However, I didn't bother actually hashing the password. But it can be done later on. This method is implemented everywhere a password is referenced.
	public static String hash(String password) {
		// A placeholder for an actually secure hash function later on.
		return password;
	}
	
	// The starting point of the program.
	public static void main(String[] args) throws SQLException {
		connection = DriverManager.getConnection(URL, USER, PASSWORD); // Establishes a connection route with the database.
		statement = connection.createStatement();
		user = null;
		
		// Initializes the window.
		openWindowContext();
	}
	
	// Possibly the most spaghettified code in existence.
	// This loads the various scenes to the open window.
	public static void loadScene(JFrame window, Scenes newScene) {
		// Clear window.
		Container pane = window.getContentPane();
		while (pane.getComponentCount() > 0) { pane.remove(0); }
		window.setContentPane(new JPanel(null)); // Setting the content pane to a JPanel with a null LayoutManager allows us to place objects directly at code-specified positions.
		int width = window.getWidth() - 13; // Real width of displayed window.
		int height = window.getHeight() - 35; // Real height of displayed window.
		scene = newScene;
		// Enhanced switch-cases my beloved.
		// Doing this allows us to define separate scopes for each individual case, so we don't have to worry about repeating variable names and breaching scope.
		// This switch in particular instructs the program to load the various elements depending on what scene is open.
		// Also, we would have made these Scenes individual classes but since this is a fairly lightweight program, we figured this wouldn't be too much of a problem.
		switch (newScene) {
			// This screen allows users to log in, or continue without doing so.
			// Additionally, they may click on the Register button to begin to register a new user.
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
				// In the case of buttons, the ActionListener allows us to supply a method to run on button click.
				loginButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String email = emailField.getText();
						String password = new String(passwordField.getPassword());
						try {
							ResultSet result = statement.executeQuery("SELECT * FROM users WHERE email='"+safeFormat(email)+"'");
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
						loadScene(window, Scenes.Home);
					}
				});
				window.add(loginButton);

				JLabel registerLabel1 = new JLabel();
				registerLabel1.setText("No account?");
				registerLabel1.setBounds(width / 2 - 80, height * 3 / 4 - 60, 160, 20);
				window.add(registerLabel1);

				JLabel registerLabel2 = new JLabel();
				registerLabel2.setText("Click here to make one:");
				registerLabel2.setBounds(width / 2 - 80, height * 3 / 4 - 20, 160, 20);
				window.add(registerLabel2);
				
				JButton registerButton = new JButton();
				registerButton.setText("Register");
				registerButton.setBounds(width / 2 - 80, height * 3 / 4, 160, 20);
				registerButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scenes.Register);
					}
				});
				window.add(registerButton);
				
				JLabel continueLabel = new JLabel();
				continueLabel.setText("Or continue without one:");
				continueLabel.setBounds(width / 2 - 80, height * 3 / 4 + 40, 160, 20);
				window.add(continueLabel);
				
				JButton continueButton = new JButton();
				continueButton.setText("Continue");
				continueButton.setBounds(width / 2 - 80, height * 3 / 4 + 60, 160, 20);
				continueButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scenes.Home);
					}
				});
				window.add(continueButton);
			}
			// This screen allows users to register their own new user, allowing them to post reviews for gyms.
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
				errorLabel.setBounds(width / 2 - 100, height / 4 + 180, 200, 20);
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
						// Don't create a user with blank strings. We aren't making that mistake again.
						if (email.isBlank() || password.isBlank() || zipcode.isBlank()) {
							errorLabel.setText("Blank text fields.");
							return;
						}
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
						loadScene(window, Scenes.Login);
					}
				});
				window.add(registerButton);
				
				JButton backButton = new JButton();
				backButton.setText("Go Back");
				backButton.setBounds(width / 2 - 80, height * 3 / 4, 160, 20);
				backButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scenes.Login);
					}
				});
				window.add(backButton);
			}
			// The home screen. From here users can either manage their account or search for new gyms.
			case Home -> {
				if (user != null) {
					JButton accountButton = new JButton();
					accountButton.setText("Manage Account");
					accountButton.setBounds(width - 180, 20, 160, 40);
					accountButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							loadScene(window, Scenes.Account);
						}
					});
					window.add(accountButton);

					JButton logoutButton = new JButton();
					logoutButton.setText("Log Out");
					logoutButton.setBounds(width - 180, 60, 160, 40);
					logoutButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							user = null;
							loadScene(window, Scenes.Login);
						}
					});
					window.add(logoutButton);
				} else {
					JButton loginButton = new JButton();
					loginButton.setText("Log In");
					loginButton.setBounds(width - 180, 20, 160, 40);
					loginButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							loadScene(window, Scenes.Login);
						}
					});
					window.add(loginButton);
				}
				
				JButton searchButton = new JButton();
				searchButton.setText("Search for Gyms");
				searchButton.setBounds(width / 2 - 80, height * 3 / 4 - 20, 160, 40);
				searchButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scenes.GymSearch);
					}
				});
				window.add(searchButton);
			}
			// This screen allows users to search for gyms by either Zip Code, Name, or Equipment.
			// Admins can also edit, delete, or add new gyms from this screen.
			case GymSearch -> {
				// This was weird to get working, but we managed to get a panel that can be scrolled on.
				JPanel listPanel = new JPanel();
				listPanel.setLayout(null); // Once again, the layout is set to null to allow us to place GUI elements at any location we desire.
				
				JScrollPane scrollPane = new JScrollPane(listPanel);
				scrollPane.setBounds(20, 100, width - 40, height - 120);
				// Lets the vertical scroll bar always be visible and the horizontal scroll bar never be visible.
				// The pane looks neater this way.
				scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setMaximumSize(new Dimension(width - 40, height - 120));
				window.add(scrollPane);
				
				JLabel searchLabel = new JLabel();
				searchLabel.setText("Enter zip code, name, or desired equipment:");
				searchLabel.setBounds(20, 40, width - 40, 20);
				window.add(searchLabel);
				
				JButton backButton = new JButton();
				backButton.setText("Back");
				backButton.setBounds(width - 180, 20, 160, 40);
				backButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scenes.Home);
					}
				});
				window.add(backButton);
				
				JTextField searchField = new JTextField();
				searchField.setText(lastSearch);
				searchField.setBounds(20, 70, width - 130, 20);
				// In the case of text fields, the ActionListener allows us to supply a method to run when pressing enter on the keyboard.
				searchField.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						lastSearch = searchField.getText();
						listPanel.removeAll();
						int y = 0;
						try {
							ResultSet result = statement.executeQuery("SELECT id, name, address, zip_code FROM gyms WHERE zip_code='"+lastSearch+"' OR name LIKE '%"+lastSearch+"%' OR equipment LIKE '%"+lastSearch+"%' ORDER BY name");
							while (result.next()) {
								// Allows admins to easily hide gyms while editing/creating them, making them only visible to other admins.
								if (result.getString(2).startsWith("-hide-") && (user == null || !user.isAdmin())) { continue; }
								
								JLabel gymName = new JLabel();
								gymName.setText(result.getString(2));
								gymName.setBounds(20, 10 + y, width, 20);
								listPanel.add(gymName);
								
								JLabel gymAddress = new JLabel();
								gymAddress.setText(result.getString(3) + " (" + result.getString(4) + ")");
								gymAddress.setBounds(20, 30 + y, width, 20);
								listPanel.add(gymAddress);

								int id = (int)result.getObject(1);
								JButton viewButton = new JButton();
								viewButton.setText("Details");
								if (user == null || !user.isAdmin()) {
									viewButton.setBounds(width - 200, 10 + y, 120, 40);
								} else {
									viewButton.setBounds(width - 280, 10 + y, 120, 40);
								}
								viewButton.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										gymId = id;
										loadScene(window, Scenes.GymDetails);
									}
								});
								listPanel.add(viewButton);
								
								if (user != null && user.isAdmin()) {
									JButton editButton = new JButton();
									editButton.setText("Edit");
									editButton.setBounds(width - 160, 10 + y, 80, 20);
									editButton.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											gymId = id;
											loadScene(window, Scenes.GymEdit);
										}
									});
									listPanel.add(editButton);
									
									JButton deleteButton = new JButton();
									deleteButton.setText("Delete");
									deleteButton.setBounds(width - 160, 30 + y, 80, 20);
									deleteButton.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(ActionEvent e) {
											try {
												statement.executeUpdate("DELETE FROM gyms WHERE id="+id);
											} catch (SQLException ex) {
												
											}
											loadScene(window, Scenes.GymSearch);
										}
									});
									listPanel.add(deleteButton);
								}
								
								y += 60;
							}
						} catch (SQLException ex) {
							ex.printStackTrace();
						}
						
						if (user != null && user.isAdmin()) {
							JButton newButton = new JButton();
							newButton.setText("Add New");
							newButton.setBounds(width - 200, 10 + y, 120, 40);
							newButton.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									try {
										statement.executeUpdate("INSERT INTO gyms (name, address, zip_code, type, hours, equipment) VALUES ('-hide- New Gym', 'Address', 'Zip Code', 'Gym', 'Hours', 'Equipment')");
										ResultSet nextIdResult = statement.executeQuery("SELECT LAST_INSERT_ID()");
										if (!nextIdResult.next()) {
											System.out.println("God damn it.");
										}
										BigInteger nextId = (BigInteger)nextIdResult.getObject(1);
										gymId = nextId.intValue();
										loadScene(window, Scenes.GymEdit);
									} catch (SQLException ex) {
										ex.printStackTrace();
									}
								}
							});
							listPanel.add(newButton);
						}
						
						listPanel.setPreferredSize(new Dimension(width, (user != null && user.isAdmin()) ? (y + 60) : y));
						listPanel.revalidate();
						listPanel.repaint();
						
						scrollPane.setViewportView(listPanel);
						scrollPane.revalidate();
						scrollPane.repaint();
						
						window.revalidate();
					}
				});
				window.add(searchField);
				
				// A search button exists too because it looks neater.
				JButton searchButton = new JButton();
				searchButton.setText("Search");
				searchButton.setBounds(width - 100, 70, 80, 20);
				// Copies the same method as in the search text field.
				searchButton.addActionListener(searchField.getActionListeners()[0]);
				window.add(searchButton);
				
				// Perform a search when the screen is loaded.
				searchField.getActionListeners()[0].actionPerformed(null);
			}
			// This screen allows users to view more details about a specific gym.
			case GymDetails -> {
				JButton backButton = new JButton();
				backButton.setText("Home");
				backButton.setBounds(width - 180, 20, 160, 40);
				backButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scenes.Home);
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
					reviewScroll.setBounds(width - 340, 80, 320, height - 100);
					reviewScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
					reviewScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					reviewScroll.setMaximumSize(new Dimension(width, height - 100));
					window.add(reviewScroll);
					
					double avgRating = 0.0;
					int ratingCount = 0;
					int y = 0;
					reviewResult = statement.executeQuery("SELECT rating, review, user_id FROM ratingsandreviews WHERE gym_id="+gymId);
					while (reviewResult.next()) {
						avgRating += (int)reviewResult.getObject(1);
						++ratingCount;
						
						// We need to create a new statement here, because two ResultSets cannot be active on the same statement.
						Statement userStatement = connection.createStatement();
						userResult = userStatement.executeQuery("SELECT email FROM users WHERE id="+reviewResult.getString(3));
						String username;
						if (userResult.next()) {
							username = userResult.getString(1);
						} else {
							username = "[DELETED]";
						}
						userStatement.close(); // Sure, close the Statement even though it closes automatically. Why not?
						
						JLabel userLabel = new JLabel();
						userLabel.setText(username);
						userLabel.setBounds(10, 10 + y, 160, 20);
						reviewPanel.add(userLabel);

						JLabel ratingLabel = new JLabel();
						ratingLabel.setText(reviewResult.getString(1));
						ratingLabel.setBounds(280, 10 + y, 20, 20);
						reviewPanel.add(ratingLabel);

						JLabel reviewLabel = new JLabel();
						reviewLabel.setVerticalAlignment(SwingConstants.TOP);
						reviewLabel.setText("<html>"+reviewResult.getString(2)+"</html>");
						reviewLabel.setBounds(10, 30 + y, 280, 50);
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
					// Format the float to only have one decimal place.
					ratingLabel.setText(String.format("Rating: %.1f (%d Reviews)", avgRating, ratingCount));
					ratingLabel.setBounds(20, 200, width - 20, 20);
					window.add(ratingLabel);
					
					if (user != null) {
						JLabel reviewPostLabel = new JLabel();
						reviewPostLabel.setText("Leave a review!");
						reviewPostLabel.setBounds(20, height - 420, 480, 20);
						window.add(reviewPostLabel);

						JLabel ratingPostLabel = new JLabel();
						ratingPostLabel.setText("Your Rating (1-5): ");
						ratingPostLabel.setBounds(20, height - 400, 480, 20);
						window.add(ratingPostLabel);
						
						ButtonGroup ratingButtonGroup = new ButtonGroup();
						JRadioButton ratingButtons[] = new JRadioButton[5];
						for (int i = 0; i < 5; ++i) {
							ratingButtons[i] = new JRadioButton();
							ratingButtons[i].setBounds(160 + 20 * i, height - 400, 20, 20);
							ratingButtonGroup.add(ratingButtons[i]);
							window.add(ratingButtons[i]);
						}
						
						JTextArea reviewField = new JTextArea();
						reviewField.setText("Leave a review!");
						reviewField.setBounds(30, height - 370, 460, 300);
						window.add(reviewField);
						
						JButton reviewClearButton = new JButton();
						reviewClearButton.setText("Clear");
						reviewClearButton.setBounds(20, height - 60, 160, 40);
						reviewClearButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								reviewField.setText("");
							}
						});
						window.add(reviewClearButton);
						
						JButton reviewPostButton = new JButton();
						reviewPostButton.setText("Post");
						reviewPostButton.setBounds(340, height - 60, 160, 40);
						reviewPostButton.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									int i;
									for (i = 0; i < 5; ++i) {
										if (ratingButtons[i].isSelected()) {
											break;
										}
									}
									if (i == 5) {
										return;
									}
									statement.executeUpdate("INSERT INTO ratingsandreviews (gym_id, user_id, rating, review) VALUES ("+gymId+", "+user.getId()+", "+(i+1)+", '"+safeFormat(reviewField.getText())+"')");
									loadScene(window, scene);
								} catch (SQLException ex) {
									ex.printStackTrace();
								}
							}
						});
						window.add(reviewPostButton);
					}
				} catch (SQLException ex) {
					ex.printStackTrace();
					JLabel errorLabel = new JLabel();
					errorLabel.setText("Connection failed.");
					errorLabel.setForeground(Color.RED);
					errorLabel.setBounds(width / 2 - 160, height / 2 - 20, 320, 40);
					window.add(errorLabel);
					break;
				}
			}
			// This screen allows admins to edit the details of gyms.
			case GymEdit -> {
				if (user == null || !user.isAdmin()) { break; }
				JButton backButton = new JButton();
				backButton.setText("Home");
				backButton.setBounds(width - 180, 20, 160, 40);
				backButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scenes.Home);
					}
				});
				window.add(backButton);
				
				ResultSet gymResult;
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

					JTextField nameField = new JTextField();
					nameField.setText(gymResult.getString(1));
					Font font = nameField.getFont();
					nameField.setFont(new Font(font.getName(), font.getStyle(), font.getSize() * 2));
					nameField.setBounds(20, 20, 640, 40);
					window.add(nameField);

					JTextField addressField = new JTextField();
					addressField.setText(gymResult.getString(2));
					addressField.setBounds(20, 60, 320, 20);
					window.add(addressField);

					JTextField zipcodeField = new JTextField();
					zipcodeField.setText(gymResult.getString(3));
					zipcodeField.setBounds(20, 80, 320, 20);
					window.add(zipcodeField);

					JTextField typeField = new JTextField();
					typeField.setText(gymResult.getString(4));
					typeField.setBounds(20, 120, 320, 20);
					window.add(typeField);
					
					JTextField hoursField = new JTextField();
					hoursField.setText(gymResult.getString(5));
					hoursField.setBounds(20, 140, 320, 20);
					window.add(hoursField);

					JTextField equipmentField = new JTextField();
					equipmentField.setText(gymResult.getString(6));
					equipmentField.setBounds(20, 160, 320, 20);
					window.add(equipmentField);
					
					gymResult.close();
					
					JButton updateButton = new JButton();
					updateButton.setText("Update");
					updateButton.setBounds(20, 200, 160, 40);
					updateButton.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								statement.executeUpdate("UPDATE gyms SET name='"+safeFormat(nameField.getText())+"', address='"+safeFormat(addressField.getText())+"', zip_code='"+safeFormat(zipcodeField.getText())+"', type='"+safeFormat(typeField.getText())+"', hours='"+safeFormat(hoursField.getText())+"', equipment='"+safeFormat(equipmentField.getText())+"' WHERE id="+gymId);
								loadScene(window, Scenes.GymDetails);
							} catch (SQLException ex) {
								ex.printStackTrace();
								JLabel errorLabel = new JLabel();
								errorLabel.setText("Update failed.");
								errorLabel.setForeground(Color.RED);
								errorLabel.setBounds(20, 240, 320, 20);
								window.add(errorLabel);
							}
						}
					});
					window.add(updateButton);
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
			// This screen allows users to manage their account, changing their email, zip code, or password.
			case Account -> {
				if (user == null) { break; }
				JButton backButton = new JButton();
				backButton.setText("Home");
				backButton.setBounds(width - 180, 20, 160, 40);
				backButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						loadScene(window, Scenes.Home);
					}
				});
				window.add(backButton);
				
				JLabel emailLabel = new JLabel();
				emailLabel.setText("Email: " + user.getEmail());
				emailLabel.setBounds(20, 20, 320, 20);
				window.add(emailLabel);

				JTextField emailField = new JTextField();
				emailField.setText(user.getEmail());
				emailField.setBounds(20, 40, 320, 20);
				window.add(emailField);
				
				JLabel zipcodeLabel = new JLabel();
				zipcodeLabel.setText("Zip Code: " + user.getZipCode());
				zipcodeLabel.setBounds(20, 80, 320, 20);
				window.add(zipcodeLabel);

				JTextField zipcodeField = new JTextField();
				zipcodeField.setText(user.getZipCode());
				zipcodeField.setBounds(20, 100, 320, 20);
				window.add(zipcodeField);
				
				JLabel passwordLabel = new JLabel();
				passwordLabel.setText("Change password?");
				passwordLabel.setBounds(20, 140, 320, 20);
				window.add(passwordLabel);

				JPasswordField passwordField = new JPasswordField();
				passwordField.setText("");
				passwordField.setBounds(20, 160, 320, 20);
				window.add(passwordField);
				
				JButton updateButton = new JButton();
				updateButton.setText("Update");
				updateButton.setBounds(20, 200, 160, 40);
				updateButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String email = emailField.getText();
						String password = new String(passwordField.getPassword());
						String zipcode = zipcodeField.getText();
						if (email.isBlank() || zipcode.isBlank()) { return; }
						
						try {
							if (password.isBlank()) {
								// Don't update the password if it's left blank.
								statement.executeUpdate("UPDATE users SET email='"+safeFormat(email)+"', zip_code='"+safeFormat(zipcode)+"' WHERE id="+user.getId());
							} else {
								// Otherwise, do!
								statement.executeUpdate("UPDATE users SET email='"+safeFormat(email)+"', zip_code='"+safeFormat(zipcode)+"', password='"+safeFormat(password)+"' WHERE id="+user.getId());
							}
							loadScene(window, Scenes.Home);
						} catch (SQLException ex) {
							ex.printStackTrace();
							JLabel errorLabel = new JLabel();
							errorLabel.setText("Update failed.");
							errorLabel.setForeground(Color.RED);
							errorLabel.setBounds(20, 240, 320, 20);
							window.add(errorLabel);
						}
					}
				});
				window.add(updateButton);
				
				JButton deleteButton = new JButton();
				deleteButton.setText("Delete");
				deleteButton.setBounds(200, 200, 160, 40);
				deleteButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							statement.executeUpdate("DELETE FROM users WHERE id="+user.getId());
							user = null;
							loadScene(window, Scenes.Home);
						} catch (SQLException ex) {
							ex.printStackTrace();
							JLabel errorLabel = new JLabel();
							errorLabel.setText("Update failed.");
							errorLabel.setForeground(Color.RED);
							errorLabel.setBounds(20, 240, 320, 20);
							window.add(errorLabel);
						}
					}
				});
				window.add(deleteButton);
			}
		}
		window.revalidate();
	}
	
	// Initializes the window.
	public static void openWindowContext() {
		scene = Scenes.Login;
		
		JFrame window = new JFrame();
		window.setTitle("Gym Rating System");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setBounds(0, 0, 1280, 720);
		// This tells the window to reload the current screen when resized.
		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				loadScene(window, scene);
			}
		});
		window.setContentPane(new JPanel(null)); // Setting the content pane to a JPanel with a null LayoutManager allows us to place objects directly at code-specified positions.
		
		loadScene(window, Scenes.Login);
		
		window.setVisible(true);
	}
}