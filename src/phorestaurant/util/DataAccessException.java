package phorestaurant.util;

public class DataAccessException extends RuntimeException {
	public DataAccessException(String msg) {
		super(msg);
	}
	
	public DataAccessException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
