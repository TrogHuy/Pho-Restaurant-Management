/**
 * 
 */
/**
 * 
 */
module Pho_restaurant {
	requires java.sql;
	
	requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    
    // exports phorestaurant; (after creating app remove comment sign)
    
    opens phorestaurant.view to javafx.fxml;
    opens phorestaurant.controller to javafx.fxml;
}