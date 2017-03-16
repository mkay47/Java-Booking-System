package main;
import java.util.Scanner;

public class ABS
{
	Scanner sc = new Scanner(System.in);
	UserDatabase db = new UserDatabase("awesomeSauce");
	
	public ABS()
	{
		db.CreateDatabase();
	}
	
	
	/*
	 * Main menu loop. -kg
	 */
	
	public void mainMenu() {
		String[] options = {"Business Owner", "Customer", "Register", "[debug] print customer db", "Exit"};
		Menu menu = new Menu(sc, options, "Appointment Booking System");
		
		// main loop
		boolean exit = false;
		while (!exit)
		{
			switch (menu.prompt())
			{
			case "Business Owner":
				businessOwnerMenu();
				break;
			case "Customer":
				customerMenu();
				break;
			case "Register":
				customerRegister();
				break;
			case "[debug] print customer db":
				db.getCustomerDataEntries();
				break;
			case "Exit":
				exit = true;
				break;
			}
		}
	}
	
	
	/*
	 * Business owner submenu. -kg
	 */
	
	private void businessOwnerMenu()
	{
		String[] options = {"Add a new employee", "Add working times/dates for next month",
				"View summary of bookings", "View employee availability for next 7 days", "Log out"};
		Menu menu = new Menu (sc, options, "Business Owner Menu");
		
		// main loop
		boolean exit = false;
		while (!exit)
		{
			switch (menu.prompt())
			{
			case "Log out":
				exit = true;
				break;
			}
		}
	}
	
	
	/*
	 * Customer submenu. -kg
	 */
	
	private void customerMenu()
	{
		// Log in
		if (customerLogin())
		{
			String[] options = {"View available days/times", "Log out"};
			Menu menu = new Menu (sc, options, "Customer Menu");
			
			// main loop
			boolean exit = false;
			while (!exit)
			{
				switch (menu.prompt())
				{
				case "Log out":
					exit = true;
					break;
				}
			}
		}
	}
	
	
	/*
	 * Register a new customer, adapts code from Richard's menu. -kg
	 */
	
	private void customerRegister()
	{
		String username;
		String password;
		String firstName;
		String lastName;
		String email;
		String phoneNumber;
		boolean created;
		
		// get username/password -kg
		System.out.print("username: "); username = sc.nextLine();
		System.out.print("password: "); password = sc.nextLine();
		
		// test password -kg
		// TN Added username null check in password length validation
		if ((Account.passwordAccepted(password)) && (username != null))
		{
			System.out.println("Password OK!");
			System.out.println();
		}
		else
		{
			// if password is unacceptable, end account creation here. -kg
			System.out.println("Invalid password.");
			System.out.println();
			return;
		}
		
		// collect customer info -kg
		System.out.print("Enter your first name: "); firstName = sc.nextLine();
		System.out.print("Enter your last name: "); lastName = sc.nextLine();
		System.out.print("Enter an email address: "); email = sc.nextLine();
		System.out.print("Enter a contact number: "); phoneNumber = sc.nextLine();
		
		// create the Customer instance -kg
		Customer customer = new Customer(username, firstName, lastName, email, phoneNumber);
		
		// store customer in db -kg
		created = db.CreateDataEntry("Customers", firstName, lastName, email, phoneNumber, username, password);
		
		//JM Check if customer was created successfully
		if(created) 
		{
			System.out.println("\nAccount Created!\n");
		}
		else 
		{
			System.out.println("\nUsername already exists. Please try again.\n");
		}
//		db.insert(customer);
//		db.setPassword(username, password);
	}
	
	private boolean customerLogin()
	{
		String username;
		String password;
		String tableName = "Customers";
		
		System.out.print("Username: "); username = sc.nextLine();
		System.out.print("Password: "); password = sc.nextLine();
		
		// check if username exists
		if (db.checkUsername(username, tableName))
		{
			if (db.checkPassword(username, password, tableName))
			{
				return true;
			}
			else {
				System.out.println("Invalid password.\n");
			}
		}
		else {
			System.out.println("Invalid username.\n");
		}
		return false;
	}
	
}
