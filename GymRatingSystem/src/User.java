public class User {
	private int id;
	private String email;
	private String zipCode;
	private boolean admin;
	
	public int getId() { return id; }
	public String getEmail() { return email; }
	public boolean isAdmin() { return admin; }
	public String getZipCode() { return zipCode; }
	
	public User(int id, String email, String zipCode, boolean admin) {
		this.id = id;
		this.email = email;
		this.zipCode = zipCode;
		this.admin = admin;
	}
}