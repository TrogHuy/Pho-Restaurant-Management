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
    exports phorestaurant.view;
    exports phorestaurant.controller;
    exports phorestaurant.dao;
    exports phorestaurant.model;
    exports phorestaurant.util;
    
    opens phorestaurant.view to javafx.fxml;
    opens phorestaurant.controller to javafx.fxml;
}