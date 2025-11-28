package phorestaurant.util;

import phorestaurant.model.Employee;

public class UserSession {
	private static UserSession instance;
	private Employee current_user;
	
	private UserSession() {}
	
	public static UserSession getInstance() {
		if(instance == null)
			instance = new UserSession();
		return instance;
	}
	
	public void setCurrentUser(Employee user) {
		this.current_user = user;
	}
	
	public Employee getCurrentUser() {
		return this.current_user;
	}
	
	public boolean isManager() {
		return current_user != null && current_user.getRole().equalsIgnoreCase("Manager");
	}
	
	public void logout() {
		this.current_user = null;
	}
}
