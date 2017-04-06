package main;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;

public class Database {
	Connection c = null;
	Statement stmt = null;
	ResultSet rs = null;
	String dbName;
	
	//JM Constructor, reads the name of the database file to work with.
	public Database(String nameOfDatabase) {
		dbName = nameOfDatabase;
	}

//***PUBLIC API***
	/**
	 * addAccount will instantiate a user's details into the database.
	 * @param account
	 * @param password - As account does not store password. Directly pass to DB.
	 * @author James
	 */
	public boolean addAccount(Account account, String password)
	{
		if(account instanceof Customer)
		{
			Customer c = (Customer) account;
			
			return CreateDataEntry("Customer", c.getFirstName(),
							c.getLastName(), c.getEmail(), c.getPhoneNumber(),
							c.username, password, "Customer");
		}
		else if(account instanceof BusinessOwner)
		{
			BusinessOwner bo = (BusinessOwner) account;
			
			return CreateDataEntry("BusinessOwner", bo.username, password, "BusinessOwner");
		}

		return false;
	}
	
	/**
	 * addEmployee will instantiate an employee into the database.
	 * @param employee
	 * @author James
	 */
	public boolean addEmployee(Employee employee)
	{
		if(CreateDataEntry("Employee", employee.getFirstName(), employee.getLastName(), employee.getEmail(), 
				employee.getPhoneNumber(), Integer.toString(employee.ID)))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * addShift will instantiate a shift into the database, which
	 * is connected to an employee.
	 * @param shift
	 * @author James
	 */
	public boolean addShift(Shift shift)
	{
		if(CreateShift(shift.getDay(), shift.getTime(), shift.ID, shift.getEmployeeID()))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if a user with the specified username exists in the database
	 * @author krismania
	 */
	public boolean accountExists(String username)
	{
		return validateUsername(username) != null;
	}
	
	/**
	 * Returns the account specified by the given username, or null if none
	 * is found.
	 * @author krismania
	 */
	public Account getAccount(String username)
	{		
		Class<? extends Account> type = validateUsername(username);
		
		try
		{
			openConnection();
			if (type.equals(Customer.class))
			{
				try (ResultSet customerQuery = stmt.executeQuery(
								String.format("SELECT * FROM Customer WHERE Username = '%s'", username)))
				{
					if (customerQuery.next())
					{
						// get info
						String first = rs.getString("Firstname");
				        String last = rs.getString("Lastname");
				        String email = rs.getString("Email");
				        String phone = rs.getString("Phone");
				        String usr = rs.getString("Username");
				        
				        // create customer obj and return it
				        return new Customer(usr, first, last, email, phone);
					}
				}
			}
			else if (type.equals(BusinessOwner.class))
			{
				try (ResultSet boQuery = stmt.executeQuery(
								String.format("SELECT * FROM BusinessOwner WHERE Username = '%s'", username)))
				{
					if (boQuery.next())
					{
						// get info
						String usr = rs.getString("Username");
						String businessName = "[business_name]";
						// TODO: store business name in db
						
						// create obj and return
						return new BusinessOwner(usr, businessName);
					}
				}
			}
			closeConnection();
		}
		catch (SQLException e)
		{
			// TODO: logging
		}
			
		return null;
	}
	
	public Employee getEmployee(int id)
	{
		return null;
	}
	
	public Shift getShift(int id)
	{
		return null;
	}
	
	public Account login(String username, String password)
	{
		return null;
	}

	
//***CREATE METHODS***JM
	public void CreateDatabase()
	{
		//JM Initialize a connection
		try
		{
			Class.forName("org.sqlite.JDBC");
			//JM Attempts to get the connection to DB file after 'sqlite:<name here>'
			openConnection();
			setupScript();
			closeConnection();
		}
		catch (Exception e)
		{
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}
	
	//JM CreateDatabaseTable() will create a table within the database.
	//JM Param = Variable number of Strings (Array)
	public void CreateDatabaseTable(String... strings)
	{
		int primaryKeyId = 1;
		StringBuilder strBuilder = new StringBuilder();
		
		for(int i = 0; i < strings.length; i++)
		{
			
			//JM If first element of array
			if(i==0) 
			{
				//JM Insert create table statement, and open bracket.
				strBuilder.append("CREATE TABLE " + strings[i] + "(");			
			}
			//JM If last element of array
			else if(i+primaryKeyId == strings.length)
			{
				//JM Insert Primary Key element and close bracket [!].
				if(i+1 == strings.length)
				{
					strBuilder.append("PRIMARY KEY (" + strings[i] + "))");			
				}
			}
			//JM If any element in between first and last
			else
			{
				//JM Split with a comma
				strBuilder.append(strings[i] + ", ");
			}
		}
		
		//JM Temporary work around - may need to change in Assignment 2
		//JM If table is schedule
		if(strings[0].equals("Schedule"))
		{
			//Delete previous ) and add foreign key.
			strBuilder.deleteCharAt(strBuilder.length() - 1);
			strBuilder.append(", FOREIGN KEY (EmpID) references"
					+ " Employee (EmpID))");	
		}
		
		String sql = strBuilder.toString();
		
		try 
		{
			openConnection();
			stmt = c.createStatement();
			stmt.executeUpdate(sql);
			closeConnection();
		
		} catch (SQLException e) {
			//JM Catch if table already exists

			//e.printStackTrace();
			
		} catch (Exception e) {
			//JM Handles errors for Class.forName
			e.printStackTrace();
		}
		
	}
	
	//JM Insert data into database.
	private boolean CreateDataEntry(String...strings) 
	{

		StringBuilder strBuilder = new StringBuilder();
		
		for(int i = 0; i < strings.length; i++)
		{
			//JM If first element of array
			if(i==0) 
			{
				//JM Insert into statement, with values and open bracket.
				strBuilder.append("INSERT INTO " + strings[i] + " VALUES (");			
			}
			//JM If last element of array
			else if(i+1 == strings.length)
			{
				//JM Add close bracket.
				strBuilder.append("'" + strings[i] + "')");			
			}
			//JM If any element in between first and last
			else
			{
				//JM Split with a comma
				strBuilder.append("'"+ strings[i] + "', ");
			}
		}
		
		//JM Convert string array, into a string.
		String sql = strBuilder.toString();
		try
		{
			openConnection();
			stmt = c.createStatement();
			stmt.executeUpdate(sql);
			closeConnection();
			return true;
		} catch(SQLException e) {
			//JM Handle errors for JDBC
			//e.printStackTrace();
			return false;
		} catch(Exception e) {
		    //JM Handle errors for Class.forName
		    e.printStackTrace();
		}
		return false;
	}
	
	private boolean CreateShift(DayOfWeek day, Time time, int iD, int employeeID) 
	{
		String sql = "INSERT INTO Schedule VALUES ('"
				+ day + "', '" + time + "', '" + iD + "'"
						+ ", '" + employeeID + "')";
		
		try
		{
			openConnection();
			stmt = c.createStatement();
			stmt.executeUpdate(sql);
			closeConnection();
			return true;
		} catch(SQLException e) {
			//JM Handle errors for JDBC
			//e.printStackTrace();
			return false;
		} catch(Exception e) {
		    //JM Handle errors for Class.forName
		    e.printStackTrace();
		}
		return false;
	}
	
//***VALIDATION METHODS***JM

	/**
	 * Returns a class object describing which type of user {@code username} is,
	 * or null if the username is not found.
	 * @author James
	 * @author krismania
	 */
	private Class<? extends Account> validateUsername(String username) 
	{		
		String query = "SELECT Username, Type "
				+ "FROM (SELECT Username, Type from Customer "
				+ "UNION "
				+ "SELECT Username, Type from BusinessOwner"
				+ ") a "
				+ "WHERE Username = '"+username+"'";
		
		try 
		{
			openConnection();
			stmt = c.createStatement();
			rs = stmt.executeQuery(query);
			
			while(rs.next())
			{
				String type = rs.getString("Type");
				
				if(type.equals("BusinessOwner"))
				{
					return BusinessOwner.class;
				}
				else if (type.equals("Customer"))
				{
					return Customer.class;
				}
			}
			closeConnection();
			
			
		} catch (SQLException e) {
			//JM Catch if table already exists
			e.printStackTrace();
		} catch (Exception e) {
			//JM Handles errors for Class.forName
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean validatePassword(String username, String password, String tableName)
	{
		String sql = String.format("SELECT password FROM %s WHERE username='%s'", tableName, username);
		
		try
		{
			openConnection();
			stmt = c.createStatement();
			rs = stmt.executeQuery(sql);
			rs.next();
			if (rs.getString(1).equals(password)) {
				closeConnection();
				return true;
			}
			closeConnection();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	//Employee JM
	public boolean validateEmpID(String empID) 
	{
		boolean duplicated = false;
		
		String query = String.format("SELECT EmpID FROM %s WHERE EmpID='%s'", "Employee", empID);
		
		try 
		{
			openConnection();
			stmt = c.createStatement();
			rs = stmt.executeQuery(query);
			if(rs != null) {
				while(rs.next()){
				
					closeConnection();
					duplicated = true;
				}
			}
		}
		catch (SQLException e) {
			
			
		} catch (Exception e) {
			//JM Handles errors for Class.forName
			
		}
		
		return duplicated;
	}
	
	/*JM Enabled generic update to specific data, depending on Username.
	* Params = table, the table you wish to update data in
	* userName = Username of specific user
	* dataToInput = the actual string you wish to insert as the update
	* valueToUpdate = the value you wish to update. ie. Name, Password, Username etc.
	*/
	public boolean updateDataEntry(String table, String userName, String dataToInput, String valueToUpdate)
	{
		String sql = String.format("UPDATE " + table + " SET " + valueToUpdate 
				+ "='%s' WHERE Username='%s'", dataToInput, userName);
		
		boolean exists = false;
		
		try
		{
			if(userName != null)
			{
				exists = accountExists(userName);
			}
			
			if(exists)
			{
				openConnection();
				stmt = c.createStatement();
				stmt.executeUpdate(sql);
				System.out.println("Value has been updated for: " + userName);
				closeConnection();
				return true;
			}
			else 
			{
				closeConnection();
				return false;
			}
			
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (NullPointerException s)
		{
			s.printStackTrace();
		}
		return false;
	}
	
//***RETRIEVE METHODS***JM
	//JM Obtain Data values from tables
	public void getCustomerDataEntries() 
	{		
		try{
			openConnection();
			stmt = c.createStatement();
			
			//JM Selected all constraints for a customer
			String sql = "SELECT Firstname, Lastname, Email, Phone,"
					+ "Username, Password FROM Customer";
			
			rs = stmt.executeQuery(sql);
			while(rs.next()){
		         //Retrieve by column name
		         String first = rs.getString("Firstname");
		         String last = rs.getString("Lastname");
		         String email = rs.getString("Email");
		         String phone = rs.getString("Phone");
		         String Username = rs.getString("Username");
		         String Password = rs.getString("Password");

		         //Display values
		         System.out.println("First: " + first);
		         System.out.println("Last: " + last);
		         System.out.println("Email: " + email);
		         System.out.println("Phone: " + phone);
		         System.out.println("Username: " + Username);
		         System.out.println("Password: " + Password);
		         System.out.println();
		      }
			closeConnection();
		} catch(SQLException e) {
			//JM Handle errors for JDBC
		    e.printStackTrace();
		} catch(Exception e) {
		    //JM Handle errors for Class.forName
		    e.printStackTrace();
		}
	}
	
	public void getBusinessOwnerDataEntries() 
	{		
		try{
			openConnection();
			stmt = c.createStatement();
			
			//JM Selected all constraints for a customer
			String sql = "SELECT Firstname, Lastname, Email, Phone,"
					+ "Username, Password FROM BusinessOwner";
			
			rs = stmt.executeQuery(sql);
			while(rs.next()){
		         //Retrieve by column name
		         String first = rs.getString("Firstname");
		         String last = rs.getString("Lastname");
		         String email = rs.getString("Email");
		         String phone = rs.getString("Phone");
		         String Username = rs.getString("Username");
		         String Password = rs.getString("Password");

		         //Display values
		         System.out.println("First: " + first);
		         System.out.println("Last: " + last);
		         System.out.println("Email: " + email);
		         System.out.println("Phone: " + phone);
		         System.out.println("Username: " + Username);
		         System.out.println("Password: " + Password);
		         System.out.println();
		      }
			closeConnection();
		} catch(SQLException e) {
			//JM Handle errors for JDBC
		    e.printStackTrace();
		} catch(Exception e) {
		    //JM Handle errors for Class.forName
		    e.printStackTrace();
		}
	}

	//public ArrayList<String> getShifts() {
		//ArrayList<String> Shifts = new ArrayList<String>();
	//}

	public void getEmployeeDataEntries() 
	{		
		try{
			openConnection();
			stmt = c.createStatement();
			
			String sql = "SELECT EmpID, Firstname, Lastname, Email, Phone"
					+ " FROM Employee";
			
			rs = stmt.executeQuery(sql);
			while(rs.next()){
		         //Retrieve by column name
			     String id = rs.getString("EmpID"); 
		         String first = rs.getString("Firstname");
		         String last = rs.getString("Lastname");
		         String email = rs.getString("Email");
		         String phone = rs.getString("Phone");

		         //Display values
		         System.out.println("ID: " + id);
		         System.out.println("First: " + first);
		         System.out.println("Last: " + last);
		         System.out.println("Email: " + email);
		         System.out.println("Phone: " + phone);
		         
		         System.out.println();
		      }
			closeConnection();
		} catch(SQLException e) {
			//JM Handle errors for JDBC
		    e.printStackTrace();
		} catch(Exception e) {
		    //JM Handle errors for Class.forName
		    e.printStackTrace();
		}
	}
	
	/**
	 * Temp method to find the highest ID
	 * TODO: this may need to be removed
	 * @author krismania
	 */
	public String getLastEmployeeID()
	{
		String id = "E000"; // if no employee is found, E000 will be returned
		try
		{
			openConnection();
			stmt = c.createStatement();
			
			String sql = "SELECT EmpID FROM Employee ORDER BY EmpID DESC";
			rs = stmt.executeQuery(sql);
			
			// we only care about the first result. -kg
			rs.next();
			id = rs.getString("EmpID");
			
			closeConnection();
		}
		catch(SQLException e)
		{
			//JM Handle errors for JDBC
		    e.printStackTrace();
		}
		catch(Exception e)
		{
		    //JM Handle errors for Class.forName
		    e.printStackTrace();
		}
		
		return id;
	}
	
	/**
	 * Temp method to find the highest ID
	 * TODO: this WILL need to be removed
	 * @author krismania
	 */
	public String getLastShiftID()
	{
		String id = "S000"; // if no employee is found, E000 will be returned
		try
		{
			openConnection();
			stmt = c.createStatement();
			
			String sql = "SELECT Shift_ID FROM Schedule ORDER BY Shift_ID DESC";
			rs = stmt.executeQuery(sql);
			
			// we only care about the first result. -kg
			rs.next();
			id = rs.getString("Shift_ID");
			
			closeConnection();
		}
		catch(SQLException e)
		{
			//JM Handle errors for JDBC
		    e.printStackTrace();
		}
		catch(Exception e)
		{
		    //JM Handle errors for Class.forName
		    e.printStackTrace();
		}
		
		return id;
	}
	
	public ArrayList<String> getShifts()
	{
		ArrayList<String> Shifts = new ArrayList<String>();
		try
		{
			openConnection();
			stmt = c.createStatement();
			
			//JM Selected all constraints for a customer
			String sql = "SELECT * FROM Employee NATURAL JOIN Schedule";
			
			rs = stmt.executeQuery(sql);
			while(rs.next()){
		         //JM Retrieve by column name
		         String first = rs.getString("Firstname");
		         String last = rs.getString("Lastname");
		         String shiftID = rs.getString("Shift_ID");
		         String day = rs.getString("Day");
		         String time = rs.getString("Time");

		         //JM Testing! Display values
		         String empAndShift = ("\nName: " + first + " " + last + 
		        		 " - Shift ID: " + shiftID);
		         String andTime = ("Day and Time: " + day + ", " + time);
		         String combined = String.format("%s\n%s\n", empAndShift, andTime);
		         System.out.println(combined);
		         Shifts.add(combined);
		      }
			closeConnection();
			return Shifts;
		}catch(SQLException e) {
			//JM Handle errors for JDBC
		    e.printStackTrace();
		} catch(Exception e) {
		    //JM Handle errors for Class.forName
		    e.printStackTrace();
		}
		return Shifts;
	}
	
//***CONNECTION METHODS***JM
	private boolean openConnection() throws SQLException {
		c = DriverManager.getConnection("jdbc:sqlite:" + dbName + ".db");
		if(c != null) 
		{
			return true;
		}
		return false;
	}
	
	private boolean closeConnection() throws SQLException {
		
		if(stmt != null)
		{
			stmt.close();
			stmt = null;
		}
		if(rs != null)
		{
			rs.close();
			rs = null;
		}
		if(c != null) {
			c.close();
			c = null;
			return true;
		}
		else
		{
			System.out.println("Connection failed to close.");
			return false;
		}
	}

//***SCRIPT METHODS***JM
	private void setupScript() {
		//Customer Table
		CreateDatabaseTable("Customer", "Firstname varchar(255)", "Lastname varchar(255)",
				"Email varchar(255)", "Phone varchar(10)", "Username varchar(15)",
				"Password varchar(15)","Type varchar(13)", "Username");
		
		//BusinessOwner Table
		CreateDatabaseTable("BusinessOwner", "Username varchar(15)",
				"Password varchar(15)", "Type varchar(13)", "Username");
		
		//Employee Table
		CreateDatabaseTable("Employee", "Firstname varchar(255)", "Lastname varchar(255)",
				"Email varchar(255)", "Phone varchar(10)", "EmpID varchar(20)", "EmpID");
		
		//Schedule Table
		CreateDatabaseTable("Schedule", "Day varchar(9)", "Time varchar(9)", "Shift_ID varchar(20)",
				"EmpID varchar(20)", "Shift_ID"); //Schedule also has a foreign key for EmpID.
		
		CreateDataEntry("Customer", "James", "McLennan", "testing@testing.com", 
				"0400000000", "JamesRulez", "james", "Customer");
		
		CreateDataEntry("BusinessOwner", "John", "Doe", "rabbits@rocks.com",
				"0400000000", "JohnRulez", "john", "BusinessOwner");
		
		CreateDataEntry("Employee", "Fred", "Cutshair", "fred.cutshair@thebesthairshop.com", 
				"0400000000", "E001");
		
		CreateDataEntry("Employee", "Bob", "Shaveshair", "bob.shaveshair@thebesthairshop.com", 
				"0400000000", "E002");
		
		CreateDataEntry("Schedule", "Monday", "Morning", "S001", "E001");
		CreateDataEntry("Schedule", "Tuesday", "Afternoon", "S002", "E001");
		CreateDataEntry("Schedule", "Wednesday", "Evening", "S003", "E001");
		CreateDataEntry("Schedule", "Sunday", "Afternoon", "S004", "E002");
	}
}
