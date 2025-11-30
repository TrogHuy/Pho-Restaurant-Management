package phorestaurant.view;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Callback;
import javafx.scene.layout.Priority;



import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import java.util.LinkedHashSet;
import java.util.Set;

import phorestaurant.controller.EmployeeController;
import phorestaurant.controller.OrderController;
import phorestaurant.controller.InventoryController;
import phorestaurant.controller.MenuController;
import phorestaurant.controller.FinanceController;

import phorestaurant.model.Employee;
import phorestaurant.model.MenuItem;
import phorestaurant.model.Ingredients;
import phorestaurant.model.Order;
import phorestaurant.model.OrderItem;
import phorestaurant.model.Recipe;
import phorestaurant.model.Transaction;


import phorestaurant.util.UserSession;
import java.util.List;

public class View extends Application {

    private Stage primaryStage;

    // Controllers – GUI only talks to these
    private final EmployeeController employeeController = new EmployeeController();
    private final OrderController orderController = new OrderController();
    private final InventoryController inventoryController = new InventoryController();
    private final MenuController menuController = new MenuController();
    private final FinanceController financeController = new FinanceController();
    
    private final Label orderTotalLabel = new Label("Total: 0.0");
    private TableView<Ingredients> inventoryTable;
    private final ObservableList<MenuItem> allMenu = FXCollections.observableArrayList();
    
    private TableView<MenuItem> posMenuTable;
    private TableView<MenuItem> menuTable;
    private TableView<Recipe> recipeTable;
    private ObservableList<MenuItem> posMenuMaster;      // base list from DB
    private FilteredList<MenuItem> posMenuFiltered;      // used for search/category
    
    
    private void refreshRecipeTable(int itemId) {
        if (recipeTable == null) return;
        try {
            recipeTable.setItems(
                FXCollections.observableArrayList(
                    menuController.getRecipeForItem(itemId)   // new method, see step 2
                )
            );
        } catch (Exception ex) {
            ex.printStackTrace();
            recipeTable.getItems().clear();
        }
    }

    private void applyMenuFilter(FilteredList<MenuItem> filteredMenu,
                                 ToggleGroup catGroup,
                                 ToggleButton allBtn,
                                 String keyword) {

        ToggleButton selectedCatBtn = (ToggleButton) catGroup.getSelectedToggle();
        String selectedCat = (selectedCatBtn == null || selectedCatBtn == allBtn)
                ? null
                : selectedCatBtn.getText();

        String kw = (keyword == null ? "" : keyword.trim().toLowerCase());

        filteredMenu.setPredicate(item -> {
            if (item == null) return false;

            // category filter
            if (selectedCat != null) {
                String cat = item.getCategory();
                if (cat == null || !cat.equalsIgnoreCase(selectedCat)) {
                    return false;
                }
            }

            // search filter
            if (!kw.isEmpty()) {
                String name = item.getName() == null ? "" : item.getName().toLowerCase();
                String desc = item.getDescription() == null ? "" : item.getDescription().toLowerCase();
                String cat = item.getCategory() == null ? "" : item.getCategory().toLowerCase();

                return name.contains(kw) || desc.contains(kw) || cat.contains(kw);
            }

            return true;
        });
    }


    // Current draft order for POS tab
    private Order currentOrder;
    
    private void loadMenuFromDB() {
        try {
            allMenu.setAll(menuController.getFullMenu());
        } catch (Exception ex) {
            ex.printStackTrace();
            allMenu.clear();
        }
    }
    
    private void refreshEmployeeTable(TableView<Employee> table) {
        try {
            List<Employee> employees = employeeController.viewAllEmployees();
            table.setItems(FXCollections.observableArrayList(employees));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void reloadPosMenuTable() {
        if (posMenuMaster == null) return;
        try {
            posMenuMaster.setAll(menuController.getFullMenu());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Pho Restaurant System");
        showLoginScreen();
        primaryStage.show();
    }

    // ==========================
    // 1) LOGIN SCREEN
    // ==========================
    private void showLoginScreen() {
        Label title = new Label("Pho Restaurant - Login");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label idLabel = new Label("Employee ID:");
        TextField idField = new TextField();
        idField.setPromptText("e.g. 1");

        Label roleLabel = new Label("Role:");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Manager", "Staff", "Owner", "Janitor");
        roleBox.setPromptText("Select role");

        Button loginButton = new Button("Login");
        Label messageLabel = new Label();

        HBox row1 = new HBox(10, idLabel, idField);
        row1.setAlignment(Pos.CENTER_LEFT);

        HBox row2 = new HBox(10, roleLabel, roleBox);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(15, title, row1, row2, loginButton, messageLabel);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        loginButton.setOnAction(e -> {
            String idText = idField.getText().trim();
            String role = roleBox.getValue();

            if (idText.isEmpty() || role == null) {
                messageLabel.setText("Please enter ID and select role.");
                return;
            }

            try {
                int id = Integer.parseInt(idText);

                // BACKEND: EmployeeController.login()
                boolean ok = employeeController.login(id, role);

                if (ok) {
                    Employee current = UserSession.getInstance().getCurrentUser();
                    System.out.println("Logged in as: " + current.getName() + " (" + current.getRole() + ")");
                    showMainScreen();
                } else {
                    messageLabel.setText("Login failed. Check ID/role.");
                }
            } catch (NumberFormatException ex) {
                messageLabel.setText("ID must be a number.");
            }
        });

        Scene scene = new Scene(root, 400, 250);
        primaryStage.setScene(scene);
    }

    // ==========================
    // 2) MAIN SCREEN (DASHBOARD)
    // ==========================
    private void showMainScreen() {
        Employee current = UserSession.getInstance().getCurrentUser();
        boolean isManager = UserSession.getInstance().isManager();

        // Top bar: title + welcome + logout
        Label title = new Label("Pho Restaurant – Main Screen");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label welcome = new Label(
                current != null
                        ? "Welcome, " + current.getName() + " (" + current.getRole() + ")"
                        : "Welcome!"
        );

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            employeeController.logout();   // use your controller
            showLoginScreen();
        });
        
        HBox topBar = new HBox(20, title, welcome, logoutButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));

        // Center: TabPane for each controller
        TabPane tabPane = new TabPane();

        Tab staffTab = new Tab("Staff", createStaffPane());
        Tab ordersTab = new Tab("Orders / POS", createOrdersPane());
        Tab inventoryTab = new Tab("Inventory", createInventoryPane());
        Tab menuTab = new Tab("Menu", createMenuPane());
        Tab financeTab = new Tab("Finance", createFinancePane());

        if (isManager) {
            tabPane.getTabs().addAll(
                    ordersTab,
                    menuTab,
                    inventoryTab,
                    staffTab,
                    financeTab      // visible only for manager
            );
        } else {
            tabPane.getTabs().addAll(
                    ordersTab,
                    menuTab,
                    inventoryTab
            );
        }

        staffTab.setClosable(false);
        ordersTab.setClosable(false);
        inventoryTab.setClosable(false);
        menuTab.setClosable(false);
        financeTab.setClosable(false);


        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setScene(scene);
    }

 // ==========================
 // STAFF TAB (EmployeeController)
 // ==========================
 private VBox createStaffPane() {
     Label title = new Label("Staff Management (EmployeeController)");
     title.setStyle("-fx-font-weight: bold;");

     Label info = new Label("Only managers can add/delete employees. Others are view-only.");

     // ---- Table of employees ----
     TableView<Employee> table = new TableView<>();

     TableColumn<Employee, Integer> colId = new TableColumn<>("ID");
     colId.setCellValueFactory(new PropertyValueFactory<>("ID")); // assumes getID()

     TableColumn<Employee, String> colName = new TableColumn<>("Name");
     colName.setCellValueFactory(new PropertyValueFactory<>("name")); // getName()

     TableColumn<Employee, String> colRole = new TableColumn<>("Role");
     colRole.setCellValueFactory(new PropertyValueFactory<>("role")); // getRole()

     TableColumn<Employee, Integer> colSalary = new TableColumn<>("Salary");
     colSalary.setCellValueFactory(new PropertyValueFactory<>("salary")); // getSalary()

     table.getColumns().addAll(colId, colName, colRole, colSalary);
     table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

     // Load data first time
     refreshEmployeeTable(table);

     // ---- Form to add / delete employees ----
     TextField nameField = new TextField();
     nameField.setPromptText("Full name");

     ComboBox<String> roleBox = new ComboBox<>();
     roleBox.getItems().addAll("Manager", "Staff", "Owner", "Janitor");
     roleBox.setPromptText("Role");

     TextField salaryField = new TextField();
     salaryField.setPromptText("Salary (VND, e.g. 1000000)");

     TextField deleteIdField = new TextField();
     deleteIdField.setPromptText("Employee ID to delete");

     Button addButton = new Button("Add Employee");
     Button deleteButton = new Button("Delete Employee");

     Label statusLabel = new Label();

     // ---- Add employee button ----
     addButton.setOnAction(e -> {
         String name = nameField.getText().trim();
         String role = roleBox.getValue();
         String salaryText = salaryField.getText().trim();

         if (name.isEmpty() || role == null || salaryText.isEmpty()) {
             statusLabel.setText("Please enter name, role and salary.");
             return;
         }

         int salary;
         try {
             salary = Integer.parseInt(salaryText);
         } catch (NumberFormatException ex) {
             statusLabel.setText("Salary must be a number.");
             return;
         }

         boolean ok = employeeController.addNewEmployee(name, role, salary);
         if (ok) {
             statusLabel.setText("Employee added successfully.");
             nameField.clear();
             salaryField.clear();
             roleBox.getSelectionModel().clearSelection();
             refreshEmployeeTable(table);
         } else {
             statusLabel.setText("Add failed (maybe not manager, or salary too low).");
         }
     });

     // ---- Delete employee button ----
     deleteButton.setOnAction(e -> {
         String idText = deleteIdField.getText().trim();
         if (idText.isEmpty()) {
             statusLabel.setText("Enter an ID to delete.");
             return;
         }

         int id;
         try {
             id = Integer.parseInt(idText);
         } catch (NumberFormatException ex) {
             statusLabel.setText("ID must be a number.");
             return;
         }

         boolean ok = employeeController.deleteEmployee(id);
         if (ok) {
             statusLabel.setText("Employee deleted.");
             deleteIdField.clear();
             refreshEmployeeTable(table);
         } else {
             statusLabel.setText("Delete failed (not manager, invalid ID, or trying to delete yourself).");
         }
     });

     HBox addRow = new HBox(10,
             new Label("Name:"), nameField,
             new Label("Role:"), roleBox,
             new Label("Salary:"), salaryField,
             addButton
     );
     addRow.setAlignment(Pos.CENTER_LEFT);

     HBox deleteRow = new HBox(10,
             new Label("Delete ID:"), deleteIdField,
             deleteButton
     );
     deleteRow.setAlignment(Pos.CENTER_LEFT);

     VBox root = new VBox(10,
             title,
             info,
             table,
             addRow,
             deleteRow,
             statusLabel
     );
     root.setPadding(new Insets(10));
     root.setAlignment(Pos.TOP_LEFT);

     return root;
 }
	//==========================
	//ORDERS / POS TAB (OrderController + MenuController)
	//==========================
	private BorderPane createOrdersPane() {
	
	    // ---------- LEFT: MENU TABLE + FILTERS ----------
	    Label menuTitle = new Label("Menu");
	    menuTitle.setStyle("-fx-font-weight: bold;");

	    posMenuTable = new TableView<>();

	    TableColumn<MenuItem, String> mColName = new TableColumn<>("Dish");
	    mColName.setCellValueFactory(new PropertyValueFactory<>("name"));

	    TableColumn<MenuItem, Double> mColPrice = new TableColumn<>("Price");
	    mColPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

	    posMenuTable.getColumns().addAll(mColName, mColPrice);
	    posMenuTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    posMenuTable.setPrefWidth(280);
	    posMenuTable.setPrefHeight(400);

	    // load all menu items into MASTER list
	    posMenuMaster = FXCollections.observableArrayList();
	    try {
	        posMenuMaster.setAll(menuController.getFullMenu());
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }

	    // filtered list for search + category
	    posMenuFiltered = new FilteredList<>(posMenuMaster, item -> true);
	    posMenuTable.setItems(posMenuFiltered);

	    // ---- category buttons ----
	    Set<String> categories = new LinkedHashSet<>();
	    for (MenuItem item : posMenuMaster) {
	        if (item.getCategory() != null && !item.getCategory().isEmpty()) {
	            categories.add(item.getCategory());
	        }
	    }

	    ToggleGroup catGroup = new ToggleGroup();

	    ToggleButton allBtn = new ToggleButton("All");
	    allBtn.setToggleGroup(catGroup);
	    allBtn.setSelected(true);

	    HBox catBar = new HBox(8);
	    catBar.setAlignment(Pos.CENTER_LEFT);
	    catBar.setPadding(new Insets(5, 0, 5, 0));
	    catBar.getChildren().addAll(new Label("Category:"), allBtn);

	    for (String cat : categories) {
	        ToggleButton btn = new ToggleButton(cat);
	        btn.setToggleGroup(catGroup);
	        catBar.getChildren().add(btn);
	    }

	    // ---- search box ----
	    TextField searchField = new TextField();
	    searchField.setPromptText("Search dish…");
	    Button searchBtn = new Button("Search");
	    Button resetSearchBtn = new Button("Reset");

	    catGroup.selectedToggleProperty().addListener((obs, oldT, newT) ->
	            applyMenuFilter(posMenuFiltered, catGroup, allBtn, searchField.getText())
	    );

	    HBox searchRow = new HBox(8, new Label("Search:"), searchField, searchBtn, resetSearchBtn);
	    searchRow.setAlignment(Pos.CENTER_LEFT);

	    searchBtn.setOnAction(e ->
	            applyMenuFilter(posMenuFiltered, catGroup, allBtn, searchField.getText())
	    );

	    resetSearchBtn.setOnAction(e -> {
	        searchField.clear();
	        catGroup.selectToggle(allBtn);
	        posMenuFiltered.setPredicate(item -> true);
	    });

	    searchField.setOnAction(e ->
	            applyMenuFilter(posMenuFiltered, catGroup, allBtn, searchField.getText())
	    );

	    VBox left = new VBox(5, menuTitle, searchRow, catBar, posMenuTable);
	    left.setPadding(new Insets(10));
	
	  // =====================================================
	  // RIGHT SIDE
	  // =====================================================
	
	  // ---------- RIGHT TOP: CURRENT ORDER ----------
	  Label orderStatusLabel = new Label("No active order.");
	  orderStatusLabel.setStyle("-fx-text-fill: #555;");
	
	  Button newDineBtn = new Button("New Dine-In");
	  Button newTakeAwayBtn = new Button("New Take-Away");
	  Button newGrabBtn = new Button("New Grab");
	
	  HBox typeBox = new HBox(10, newDineBtn, newTakeAwayBtn, newGrabBtn);
	  typeBox.setAlignment(Pos.CENTER_LEFT);
	
	  TextField qtyField = new TextField();
	  qtyField.setPromptText("Qty");
	  qtyField.setPrefWidth(60);
	
	  Button addItemBtn = new Button("Add Selected Item");
	  Button removeItemBtn = new Button("Remove Selected");
	
	  HBox addBox = new HBox(10,
	          new Label("Qty:"), qtyField, addItemBtn, removeItemBtn);
	  addBox.setAlignment(Pos.CENTER_LEFT);
	
	  TableView<OrderItem> orderTable = new TableView<>();
	
	  TableColumn<OrderItem, String> colItemName = new TableColumn<>("Item");
	  colItemName.setCellValueFactory(
	          c -> new SimpleStringProperty(c.getValue().getMenuItem().getName())
	  );
	
	  TableColumn<OrderItem, Integer> colQty = new TableColumn<>("Qty");
	  colQty.setCellValueFactory(
	          c -> new SimpleIntegerProperty(c.getValue().getQuantity()).asObject()
	  );
	
	  TableColumn<OrderItem, Double> colSub = new TableColumn<>("Subtotal");
	  colSub.setCellValueFactory(
	          c -> new SimpleDoubleProperty(c.getValue().getSubTotal()).asObject()
	  );
	
	  orderTable.getColumns().addAll(colItemName, colQty, colSub);
	  orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	
	  orderTotalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
	
	  Button placeOrderBtn = new Button("Place Order");
	  HBox bottomButtons = new HBox(10, placeOrderBtn, orderTotalLabel);
	  bottomButtons.setAlignment(Pos.CENTER_RIGHT);
	
	  VBox currentOrderBox = new VBox(8,
	          new Label("Current POS Order"),
	          typeBox,
	          orderStatusLabel,
	          addBox,
	          orderTable,
	          bottomButtons
	  );
	  currentOrderBox.setPadding(new Insets(10));
	
	  // ---------- RIGHT BOTTOM: ALL ORDERS ----------
	  Label allOrdersTitle = new Label("All Orders (Complete / Cancel)");
	  allOrdersTitle.setStyle("-fx-font-weight: bold;");
	
	  TableView<Order> allOrdersTable = new TableView<>();
	
	  TableColumn<Order, Integer> ordIdCol = new TableColumn<>("ID");
	  ordIdCol.setCellValueFactory(
	          c -> new SimpleIntegerProperty(c.getValue().getID()).asObject()
	  );
	
	  TableColumn<Order, String> ordTypeCol = new TableColumn<>("Type");
	  ordTypeCol.setCellValueFactory(
	          c -> new SimpleStringProperty(c.getValue().getOrderType())
	  );
	
	  TableColumn<Order, String> ordStatusCol = new TableColumn<>("Status");
	  ordStatusCol.setCellValueFactory(
	          c -> new SimpleStringProperty(c.getValue().getOrderStatus())
	  );
	
	  TableColumn<Order, Double> ordTotalCol = new TableColumn<>("Total");
	  ordTotalCol.setCellValueFactory(
	          c -> new SimpleDoubleProperty(c.getValue().getTotalPrice()).asObject()
	  );
	
	  TableColumn<Order, Void> actionCol = new TableColumn<>("Actions");
	  actionCol.setCellFactory(col -> new TableCell<>() {
	      private final Button completeBtn = new Button("Complete");
	      private final Button cancelBtn = new Button("Cancel");
	      private final HBox box = new HBox(5, completeBtn, cancelBtn);
	
	      {
	          completeBtn.setOnAction(e -> {
	              Order o = getTableView().getItems().get(getIndex());
	              if (orderController.completeOrder(o.getID())) {
	                  refreshAllOrdersTable(allOrdersTable);
	              }
	          });
	
	          cancelBtn.setOnAction(e -> {
	              Order o = getTableView().getItems().get(getIndex());
	              if (orderController.cancelOrder(o.getID())) {
	                  refreshAllOrdersTable(allOrdersTable);
	              }
	          });
	      }
	
	      @Override
	      protected void updateItem(Void item, boolean empty) {
	          super.updateItem(item, empty);
	          setGraphic(empty ? null : box);
	      }
	  });
	
	  allOrdersTable.getColumns().addAll(ordIdCol, ordTypeCol, ordStatusCol, ordTotalCol, actionCol);
	  allOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	
	  Button refreshOrdersBtn = new Button("Refresh Orders");
	  refreshOrdersBtn.setOnAction(e -> refreshAllOrdersTable(allOrdersTable));
	
	  VBox allOrdersBox = new VBox(8, allOrdersTitle, refreshOrdersBtn, allOrdersTable);
	  allOrdersBox.setPadding(new Insets(10));
	
	  refreshAllOrdersTable(allOrdersTable);   // first load
	
	  VBox right = new VBox(10, currentOrderBox, new Separator(), allOrdersBox);
	  right.setPadding(new Insets(5));
	
	  // ---------- WIRE CURRENT ORDER BUTTONS ----------
	  newDineBtn.setOnAction(e -> {
	      currentOrder = orderController.createNewDraftOrder("DineIn");
	      orderStatusLabel.setText("New Dine-In order started.");
	      refreshOrderTable(orderTable);
	      orderTotalLabel.setText("Total: " + currentOrder.getTotalPrice());
	  });
	
	  newTakeAwayBtn.setOnAction(e -> {
	      currentOrder = orderController.createNewDraftOrder("Pickup");
	      orderStatusLabel.setText("New Take-Away order started.");
	      refreshOrderTable(orderTable);
	      orderTotalLabel.setText("Total: " + currentOrder.getTotalPrice());
	  });
	
	  newGrabBtn.setOnAction(e -> {
	      currentOrder = orderController.createNewDraftOrder("Grab");
	      orderStatusLabel.setText("New Grab order started.");
	      refreshOrderTable(orderTable);
	      orderTotalLabel.setText("Total: " + currentOrder.getTotalPrice());
	  });
	
	  addItemBtn.setOnAction(e -> {
		    if (currentOrder == null) {
		        orderStatusLabel.setText("Create a draft order first.");
		        return;
		    }

		    MenuItem selected = posMenuTable.getSelectionModel().getSelectedItem();
		    if (selected == null) {
		        orderStatusLabel.setText("Select a menu item.");
		        return;
		    }

		    String qtyText = qtyField.getText().trim();
		    if (qtyText.isEmpty()) {
		        orderStatusLabel.setText("Enter quantity.");
		        return;
		    }

		    try {
		        int qty = Integer.parseInt(qtyText);
		        orderController.addItemToDraft(currentOrder, selected.getID(), qty);
		        refreshOrderTable(orderTable);
		        orderTotalLabel.setText("Total: " + currentOrder.getTotalPrice());
		        orderStatusLabel.setText("Item added.");
		    } catch (NumberFormatException ex) {
		        orderStatusLabel.setText("Qty must be a number.");
		    }
		});

	
	  removeItemBtn.setOnAction(e -> {
		    if (currentOrder == null) return;
		    OrderItem selected = orderTable.getSelectionModel().getSelectedItem();
		    if (selected == null) return;
	
		    currentOrder.removeItem(selected);  // use model logic
		    refreshOrderTable(orderTable);
		    orderTotalLabel.setText("Total: " + currentOrder.getTotalPrice());
		    orderStatusLabel.setText("Item removed.");
		});
	
	  placeOrderBtn.setOnAction(e -> {
	      if (currentOrder == null) {
	          orderStatusLabel.setText("No active order.");
	          return;
	      }
	      if (currentOrder.getItems().isEmpty()) {
	          orderStatusLabel.setText("Cannot place an empty order.");
	          return;
	      }
	
	      boolean ok = orderController.placeOrder(currentOrder);
	      if (ok) {
	          orderStatusLabel.setText("Order placed successfully.");
	          currentOrder = null;
	          refreshOrderTable(orderTable);
	          orderTotalLabel.setText("Total: 0.0");
	          refreshAllOrdersTable(allOrdersTable);
	          reloadInventoryTable();  // your existing inventory refresher
	      } else {
	          orderStatusLabel.setText("Failed to place order.");
	      }
	  });
	
	  // ---------- ROOT ----------
	  BorderPane root = new BorderPane();
	  root.setLeft(left);
	  root.setCenter(right);
	  return root;
	}


	 private void refreshOrderTable(TableView<OrderItem> orderTable) {
		    if (currentOrder == null) {
		        orderTable.getItems().clear();
		        orderTotalLabel.setText("Total: 0.0");
		        return;
		    }
	
		    ObservableList<OrderItem> data =
		            FXCollections.observableArrayList(currentOrder.getItems());
		    orderTable.setItems(data);
	
		    double total = 0.0;
		    for (OrderItem it : currentOrder.getItems()) {
		        total += it.getSubTotal();
		    }
		    orderTotalLabel.setText(String.format("Total: %.2f", total));
		}
	 private void refreshAllOrdersTable(TableView<Order> table) {
		    try {
		        table.setItems(FXCollections.observableArrayList(
		                orderController.getAllOrders()
		        ));
		    } catch (Exception ex) {
		        ex.printStackTrace();
		    }
		}
	 private void reloadInventoryTable() {
		    if (inventoryTable == null) return;
		    try {
		        ObservableList<Ingredients> data =
		                FXCollections.observableArrayList(inventoryController.viewInventory());
		        inventoryTable.setItems(data);
		    } catch (Exception ex) {
		        ex.printStackTrace();
		    }
		}


	// ==========================
	// INVENTORY TAB (InventoryController)
	// ==========================
	private VBox createInventoryPane() {
	    Label title = new Label("Inventory (InventoryController)");
	    title.setStyle("-fx-font-weight: bold;");

	    // ----- TABLE: show all ingredients -----
	    inventoryTable = new TableView<>();
	    
	    TableColumn<Ingredients, Integer> colId = new TableColumn<>("ID");
	    colId.setPrefWidth(60); // optional: make it narrow
	    colId.setCellValueFactory(cellData ->
	    	new SimpleIntegerProperty(cellData.getValue().getID()).asObject());

	    TableColumn<Ingredients, String> colName = new TableColumn<>("Ingredient");
	    colName.setCellValueFactory(new PropertyValueFactory<>("name"));

	    TableColumn<Ingredients, Double> colStock = new TableColumn<>("Stock");
	    colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

	    TableColumn<Ingredients, String> colUnit = new TableColumn<>("Unit");
	    colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));

	    inventoryTable.getColumns().addAll(colId, colName, colStock, colUnit);
	    inventoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    
	    inventoryTable.setPrefHeight(300);
	    VBox.setVgrow(inventoryTable, Priority.ALWAYS);

	    // first load
	    reloadInventoryTable();

	    // =======================================
	    // 1) RESTOCK section
	    // =======================================
	    Label restockTitle = new Label("Restock Ingredient");
	    restockTitle.setStyle("-fx-font-weight: bold;");

	    TextField restockIdField = new TextField();
	    restockIdField.setPromptText("Ingredient ID");

	    TextField restockAmountField = new TextField();
	    restockAmountField.setPromptText("Amount to add");

	    Button restockBtn = new Button("Restock");
	    Label restockStatus = new Label();

	    restockBtn.setOnAction(e -> {
	        String idText = restockIdField.getText().trim();
	        String amtText = restockAmountField.getText().trim();
	        if (idText.isEmpty() || amtText.isEmpty()) {
	            restockStatus.setText("Enter ID and amount.");
	            return;
	        }
	        try {
	            int id = Integer.parseInt(idText);
	            double amt = Double.parseDouble(amtText);

	            boolean ok = inventoryController.restockIngredient(id, amt);
	            if (ok) {
	                restockStatus.setText("Restocked successfully.");
	                reloadInventoryTable();
	            } else {
	                restockStatus.setText("Restock failed – only managers can restock.");
	            }
	        } catch (NumberFormatException ex) {
	            restockStatus.setText("ID and amount must be numbers.");
	        }
	    });

	    HBox restockRow = new HBox(10,
	            new Label("ID:"), restockIdField,
	            new Label("Amount:"), restockAmountField,
	            restockBtn
	    );
	    restockRow.setAlignment(Pos.CENTER_LEFT);

	    // =======================================
	    // 2) ADD NEW INGREDIENT section
	    // =======================================
	    Label addTitle = new Label("Add New Ingredient");
	    addTitle.setStyle("-fx-font-weight: bold;");

	    TextField addNameField = new TextField();
	    addNameField.setPromptText("Name");

	    TextField addQtyField = new TextField();
	    addQtyField.setPromptText("Initial qty");

	    TextField addUnitField = new TextField();
	    addUnitField.setPromptText("Unit (kg, g, ml...)");

	    Button addBtn = new Button("Add Ingredient");
	    Label addStatus = new Label();

	    addBtn.setOnAction(e -> {
	        String name = addNameField.getText().trim();
	        String qtyText = addQtyField.getText().trim();
	        String unit = addUnitField.getText().trim();

	        if (name.isEmpty() || qtyText.isEmpty() || unit.isEmpty()) {
	            addStatus.setText("Enter name, qty and unit.");
	            return;
	        }

	        try {
	            double qty = Double.parseDouble(qtyText);
	            boolean ok = inventoryController.addNewIngredient(name, qty, unit);
	            if (ok) {
	                addStatus.setText("Ingredient added.");
	                addNameField.clear();
	                addQtyField.clear();
	                addUnitField.clear();
	                reloadInventoryTable();
	            } else {
	                addStatus.setText("Add failed – only managers can add or qty < 0.");
	            }
	        } catch (NumberFormatException ex) {
	            addStatus.setText("Quantity must be a number.");
	        }
	    });

	    HBox addRow1 = new HBox(10,
	            new Label("Name:"), addNameField,
	            new Label("Qty:"), addQtyField
	    );
	    addRow1.setAlignment(Pos.CENTER_LEFT);

	    HBox addRow2 = new HBox(10,
	            new Label("Unit:"), addUnitField,
	            addBtn
	    );
	    addRow2.setAlignment(Pos.CENTER_LEFT);

	    // =======================================
	    // 3) DELETE INGREDIENT section
	    // =======================================
	    Label deleteTitle = new Label("Delete Ingredient");
	    deleteTitle.setStyle("-fx-font-weight: bold;");

	    TextField deleteIdField = new TextField();
	    deleteIdField.setPromptText("Ingredient ID");

	    Button deleteBtn = new Button("Delete");
	    Label deleteStatus = new Label();

	    deleteBtn.setOnAction(e -> {
	        String idText = deleteIdField.getText().trim();
	        if (idText.isEmpty()) {
	            deleteStatus.setText("Enter ID to delete.");
	            return;
	        }
	        try {
	            int id = Integer.parseInt(idText);
	            boolean ok = inventoryController.deleteIngredient(id);
	            if (ok) {
	                deleteStatus.setText("Ingredient deleted.");
	                deleteIdField.clear();
	                reloadInventoryTable();
	            } else {
	            	deleteStatus.setText("Delete failed – only managers can delete or item not exist");
	            }
	        } catch (NumberFormatException ex) {
	            deleteStatus.setText("ID must be a number.");
	        }
	    });

	    HBox deleteRow = new HBox(10,
	            new Label("ID:"), deleteIdField,
	            deleteBtn
	    );
	    deleteRow.setAlignment(Pos.CENTER_LEFT);

	    // =======================================
	    // 4) CHECK LOW STOCK section
	    // =======================================
	    Label lowStockTitle = new Label("Check Low Stock");
	    lowStockTitle.setStyle("-fx-font-weight: bold;");

	    TextField lowIdField = new TextField();
	    lowIdField.setPromptText("Ingredient ID");

	    TextField thresholdField = new TextField();
	    thresholdField.setPromptText("Threshold");

	    Button checkBtn = new Button("Check");
	    Label lowStockStatus = new Label();

	    checkBtn.setOnAction(e -> {
	        String idText = lowIdField.getText().trim();
	        String thText = thresholdField.getText().trim();
	        if (idText.isEmpty() || thText.isEmpty()) {
	            lowStockStatus.setText("Enter ID and threshold.");
	            return;
	        }
	        try {
	            int id = Integer.parseInt(idText);
	            double th = Double.parseDouble(thText);

	            boolean low = inventoryController.checkLowStock(id, th);
	            if (low) {
	                lowStockStatus.setText("⚠ Stock is LOW (<= threshold).");
	            } else {
	                lowStockStatus.setText("✓ Stock is OK.");
	            }
	        } catch (NumberFormatException ex) {
	            lowStockStatus.setText("ID and threshold must be numbers.");
	        }
	    });

	    HBox lowRow = new HBox(10,
	            new Label("ID:"), lowIdField,
	            new Label("Threshold:"), thresholdField,
	            checkBtn
	    );
	    lowRow.setAlignment(Pos.CENTER_LEFT);

	    // =======================================
	    // ROOT LAYOUT
	    // =======================================
	    VBox root = new VBox(
	            10,
	            title,
	            inventoryTable,
	            restockTitle,
	            restockRow,
	            restockStatus,
	            new Separator(),
	            addTitle,
	            addRow1,
	            addRow2,
	            addStatus,
	            new Separator(),
	            deleteTitle,
	            deleteRow,
	            deleteStatus,
	            new Separator(),
	            lowStockTitle,
	            lowRow,
	            lowStockStatus
	    );
	    root.setPadding(new Insets(10));
	    root.setAlignment(Pos.TOP_LEFT);
	    return root;
	}


	// ==========================
	// MENU TAB (MenuController)
	// ==========================
	private VBox createMenuPane() {
	    Label title = new Label("Menu (MenuController)");
	    title.setStyle("-fx-font-weight: bold;");

	    // ---------- TABLE: menu items ----------
	    menuTable = new TableView<>();

	    TableColumn<MenuItem, Integer> colId = new TableColumn<>("ID");
	    colId.setCellValueFactory(c ->
	            new SimpleIntegerProperty(c.getValue().getID()).asObject());
	    colId.setPrefWidth(50);

	    TableColumn<MenuItem, String> colName = new TableColumn<>("Dish");
	    colName.setCellValueFactory(new PropertyValueFactory<>("name"));

	    TableColumn<MenuItem, Double> colPrice = new TableColumn<>("Price");
	    colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

	    TableColumn<MenuItem, String> colCat = new TableColumn<>("Category");
	    colCat.setCellValueFactory(new PropertyValueFactory<>("category"));

	    menuTable.getColumns().addAll(colId, colName, colPrice, colCat);
	    menuTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    menuTable.setPrefHeight(220);

	    // ---- load menu from DB into allMenu ----
	    loadMenuFromDB();

	    FilteredList<MenuItem> filteredMenu =
	            new FilteredList<>(allMenu, item -> true);
	    menuTable.setItems(filteredMenu);

	    // ======================================================
	    // SEARCH + CATEGORY FILTER
	    // ======================================================
	    TextField searchField = new TextField();
	    searchField.setPromptText("Search dish...");
	    Button searchBtn = new Button("Search");
	    Button resetSearchBtn = new Button("Reset");

	    HBox searchRow = new HBox(8, new Label("Search:"), searchField, searchBtn, resetSearchBtn);
	    searchRow.setAlignment(Pos.CENTER_LEFT);

	    // category buttons
	    Set<String> categories = new LinkedHashSet<>();
	    for (MenuItem item : allMenu) {
	        if (item.getCategory() != null && !item.getCategory().isEmpty()) {
	            categories.add(item.getCategory());
	        }
	    }

	    ToggleGroup catGroup = new ToggleGroup();
	    ToggleButton allBtn = new ToggleButton("All");
	    allBtn.setToggleGroup(catGroup);
	    allBtn.setSelected(true);

	    HBox catBar = new HBox(8, new Label("Category:"), allBtn);
	    catBar.setAlignment(Pos.CENTER_LEFT);

	    for (String cat : categories) {
	        ToggleButton btn = new ToggleButton(cat);
	        btn.setToggleGroup(catGroup);
	        catBar.getChildren().add(btn);
	    }

	    catGroup.selectedToggleProperty().addListener((obs, o, n) ->
	            applyMenuFilter(filteredMenu, catGroup, allBtn, searchField.getText()));

	    searchBtn.setOnAction(e ->
	            applyMenuFilter(filteredMenu, catGroup, allBtn, searchField.getText()));

	    resetSearchBtn.setOnAction(e -> {
	        searchField.clear();
	        catGroup.selectToggle(allBtn);
	        filteredMenu.setPredicate(item -> true);
	    });

	    searchField.setOnAction(e ->
	            applyMenuFilter(filteredMenu, catGroup, allBtn, searchField.getText()));

	    // ======================================================
	    // ADD / DELETE DISH
	    // ======================================================
	    TextField nameField = new TextField();
	    nameField.setPromptText("Dish name");

	    TextField priceField = new TextField();
	    priceField.setPromptText("Price (e.g. 55000)");

	    TextField descField = new TextField();
	    descField.setPromptText("Description");

	    TextField catField = new TextField();
	    catField.setPromptText("Category (Pho, Side Dish, Drink...)");

	    Button addDishBtn = new Button("Add Dish");
	    Label statusLabel = new Label();

	    TextField deleteIdField = new TextField();
	    deleteIdField.setPromptText("Item ID to delete");
	    Button deleteDishBtn = new Button("Delete Dish");

	    addDishBtn.setOnAction(e -> {
	        String name = nameField.getText().trim();
	        String priceText = priceField.getText().trim();
	        String desc = descField.getText().trim();
	        String cat = catField.getText().trim();

	        if (name.isEmpty() || priceText.isEmpty() || cat.isEmpty()) {
	            statusLabel.setText("Name, price and category are required.");
	            return;
	        }

	        double price;
	        try {
	            price = Double.parseDouble(priceText);
	        } catch (NumberFormatException ex) {
	            statusLabel.setText("Price must be a number.");
	            return;
	        }

	        boolean ok = menuController.createNewDish(name, desc, price, cat);
	        if (ok) {
	            statusLabel.setText("Dish added successfully (manager only).");
	            loadMenuFromDB();
	            reloadPosMenuTable();
	            nameField.clear();
	            priceField.clear();
	            descField.clear();
	            catField.clear();
	        } else {
	            statusLabel.setText("Add failed (not manager or DB error).");
	        }
	    });

	    deleteDishBtn.setOnAction(e -> {
	        String idText = deleteIdField.getText().trim();
	        if (idText.isEmpty()) {
	            statusLabel.setText("Enter an item ID to delete.");
	            return;
	        }
	        int id;
	        try {
	            id = Integer.parseInt(idText);
	        } catch (NumberFormatException ex) {
	            statusLabel.setText("ID must be a number.");
	            return;
	        }

	        boolean ok = menuController.deleteDish(id);
	        if (ok) {
	            statusLabel.setText("Dish deleted (manager only).");
	            loadMenuFromDB();
	            reloadPosMenuTable();
	            deleteIdField.clear();
	        } else {
	            statusLabel.setText("Delete failed (not manager or invalid ID).");
	        }
	    });

	    HBox addRow = new HBox(8,
	            new Label("Name:"), nameField,
	            new Label("Price:"), priceField,
	            new Label("Desc:"), descField,
	            new Label("Cat:"), catField,
	            addDishBtn
	    );
	    addRow.setAlignment(Pos.CENTER_LEFT);

	    HBox deleteRow = new HBox(8,
	            new Label("Delete item ID:"), deleteIdField,
	            deleteDishBtn
	    );
	    deleteRow.setAlignment(Pos.CENTER_LEFT);

	    // ======================================================
	    // RECIPE SECTION (for selected dish)
	    // ======================================================
	    Label recipeTitle = new Label(
	            "Recipe for selected dish (item_id / ingredient_id / quantity_needed)");
	    recipeTitle.setStyle("-fx-font-weight: bold;");

	    recipeTable = new TableView<>();

	    TableColumn<Recipe, Integer> recItemCol = new TableColumn<>("Item ID");
	    recItemCol.setCellValueFactory(
	            c -> new SimpleIntegerProperty(c.getValue().getItemID()).asObject());

	    TableColumn<Recipe, Integer> recIngCol = new TableColumn<>("Ingredient ID");
	    recIngCol.setCellValueFactory(
	            c -> new SimpleIntegerProperty(c.getValue().getIngredientID()).asObject());

	    TableColumn<Recipe, Double> recQtyCol = new TableColumn<>("Qty needed");
	    recQtyCol.setCellValueFactory(
	            c -> new SimpleDoubleProperty(c.getValue().getQuantityNeeded()).asObject());

	    recipeTable.getColumns().addAll(recItemCol, recIngCol, recQtyCol);
	    recipeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    recipeTable.setPrefHeight(150);

	    // load recipe whenever a dish is selected
	    menuTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
	        if (newSel != null) {
	            refreshRecipeTable(newSel.getID());
	        } else {
	            recipeTable.getItems().clear();
	        }
	    });

	    // controls to add recipe line
	    TextField ingIdField = new TextField();
	    ingIdField.setPromptText("Ingredient ID");

	    TextField qtyNeededField = new TextField();
	    qtyNeededField.setPromptText("Qty needed");

	    Button addRecipeBtn = new Button("Add Recipe Line");
	    Label recipeStatus = new Label();

	    addRecipeBtn.setOnAction(e -> {
	        MenuItem selectedDish = menuTable.getSelectionModel().getSelectedItem();
	        if (selectedDish == null) {
	            recipeStatus.setText("Select a dish first.");
	            return;
	        }

	        String ingText = ingIdField.getText().trim();
	        String qtyText = qtyNeededField.getText().trim();
	        if (ingText.isEmpty() || qtyText.isEmpty()) {
	            recipeStatus.setText("Ingredient ID and quantity are required.");
	            return;
	        }

	        try {
	            int ingId = Integer.parseInt(ingText);
	            double qty = Double.parseDouble(qtyText);

	            boolean ok = menuController.addRecipe(selectedDish.getID(), ingId, qty);
	            if (ok) {
	                recipeStatus.setText("Recipe line added (manager only).");
	                refreshRecipeTable(selectedDish.getID());
	                ingIdField.clear();
	                qtyNeededField.clear();
	            } else {
	                recipeStatus.setText("Add failed (not manager or DB error).");
	            }
	        } catch (NumberFormatException ex) {
	            recipeStatus.setText("IDs and quantity must be numbers.");
	        }
	    });

	    HBox recipeAddRow = new HBox(8,
	            new Label("Ingredient ID:"), ingIdField,
	            new Label("Qty:"), qtyNeededField,
	            addRecipeBtn
	    );
	    recipeAddRow.setAlignment(Pos.CENTER_LEFT);

	    // ---------- ROOT LAYOUT ----------
	    VBox root = new VBox(10,
	            title,
	            searchRow,
	            catBar,
	            menuTable,
	            addRow,
	            deleteRow,
	            statusLabel,
	            new Separator(),
	            recipeTitle,
	            recipeTable,
	            recipeAddRow,
	            recipeStatus
	    );
	    root.setPadding(new Insets(10));
	    root.setAlignment(Pos.TOP_LEFT);
	    return root;
	}

	// ==========================
	// FINANCE TAB (FinanceController)
	// ==========================
	private VBox createFinancePane() {
	    Label title = new Label("Finance (FinanceController)");
	    title.setStyle("-fx-font-weight: bold;");

	    // -------------------- ORDERS TABLE (top) --------------------
	    Label ordersLabel = new Label("Orders");
	    TableView<Order> ordersTable = new TableView<>();

	    TableColumn<Order, Integer> oIdCol = new TableColumn<>("ID");
	    oIdCol.setCellValueFactory(c ->
	            new SimpleIntegerProperty(c.getValue().getID()).asObject());

	    TableColumn<Order, String> oTypeCol = new TableColumn<>("Type");
	    oTypeCol.setCellValueFactory(c ->
	            new SimpleStringProperty(c.getValue().getOrderType()));

	    TableColumn<Order, String> oStatusCol = new TableColumn<>("Status");
	    oStatusCol.setCellValueFactory(c ->
	            new SimpleStringProperty(c.getValue().getOrderStatus()));

	    TableColumn<Order, Double> oTotalCol = new TableColumn<>("Total");
	    oTotalCol.setCellValueFactory(c ->
	            new SimpleDoubleProperty(c.getValue().getTotalPrice()).asObject());

	    ordersTable.getColumns().addAll(oIdCol, oTypeCol, oStatusCol, oTotalCol);
	    ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    ordersTable.setPrefHeight(220);

	    Button refreshOrdersBtn = new Button("Refresh Orders");
	    refreshOrdersBtn.setOnAction(e -> {
	        try {
	            ordersTable.setItems(FXCollections.observableArrayList(
	                    orderController.getAllOrders()
	            ));
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    });
	    // initial load
	    refreshOrdersBtn.fire();

	    HBox ordersHeader = new HBox(10, ordersLabel, refreshOrdersBtn);
	    ordersHeader.setAlignment(Pos.CENTER_LEFT);

	    // -------------------- RECORD PAYMENT (middle) --------------------
	    Label recordTitle = new Label("Record Payment");
	    recordTitle.setStyle("-fx-font-weight: bold;");

	    TextField orderIdField = new TextField();
	    orderIdField.setPromptText("Order ID");
	    orderIdField.setPrefWidth(100);

	    TextField amountField = new TextField();
	    amountField.setPromptText("Amount");
	    amountField.setPrefWidth(150);

	    ComboBox<String> methodBox = new ComboBox<>();
	    methodBox.getItems().addAll("Cash", "Card", "GrabPay", "Momo", "Other eWallet");
	    methodBox.setPromptText("Payment method");
	    methodBox.setPrefWidth(150);

	    Button recordBtn = new Button("Record Payment");
	    Label paymentStatusLabel = new Label();

	    // when selecting an order, auto-fill ID + amount
	    ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
	        if (newSel != null) {
	            orderIdField.setText(String.valueOf(newSel.getID()));
	            amountField.setText(String.valueOf(newSel.getTotalPrice()));
	        }
	    });

	    HBox recordRow1 = new HBox(10,
	            new Label("Order ID:"), orderIdField,
	            new Label("Amount:"), amountField
	    );
	    recordRow1.setAlignment(Pos.CENTER_LEFT);

	    HBox recordRow2 = new HBox(10,
	            new Label("Method:"), methodBox,
	            recordBtn
	    );
	    recordRow2.setAlignment(Pos.CENTER_LEFT);

	    // -------------------- TOTAL REVENUE (label + button) --------------------
	    Label totalRevenueLabel = new Label("Current total: 0.00");
	    Button refreshRevenueBtn = new Button("Refresh Revenue");

	    Runnable refreshRevenue = () -> {
	        try {
	            double total = financeController.getTotalRevenue();
	            totalRevenueLabel.setText(String.format("Current total: %.2f", total));
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    };
	    refreshRevenueBtn.setOnAction(e -> refreshRevenue.run());
	    refreshRevenue.run();   // initial

	    HBox revenueRow = new HBox(10, refreshRevenueBtn, totalRevenueLabel);
	    revenueRow.setAlignment(Pos.CENTER_LEFT);

	    // -------------------- TRANSACTIONS TABLE (bottom, big) --------------------
	    Label transLabel = new Label("Transactions");
	    transLabel.setStyle("-fx-font-weight: bold;");

	    TableView<Transaction> transTable = new TableView<>();

	    TableColumn<Transaction, Integer> tIdCol = new TableColumn<>("Trans ID");
	    tIdCol.setCellValueFactory(c ->
	            new SimpleIntegerProperty(c.getValue().getTransactionId()).asObject());

	    TableColumn<Transaction, Integer> tOrderCol = new TableColumn<>("Order ID");
	    tOrderCol.setCellValueFactory(c ->
	            new SimpleIntegerProperty(c.getValue().getOrderId()).asObject());

	    TableColumn<Transaction, String> tMethodCol = new TableColumn<>("Method");
	    tMethodCol.setCellValueFactory(c ->
	            new SimpleStringProperty(c.getValue().getPaymentMethod()));

	    TableColumn<Transaction, Double> tAmountCol = new TableColumn<>("Amount");
	    tAmountCol.setCellValueFactory(c ->
	            new SimpleDoubleProperty(c.getValue().getAmountPaid()).asObject());

	    TableColumn<Transaction, String> tTimeCol = new TableColumn<>("Timestamp");
	    tTimeCol.setCellValueFactory(c ->
	            new SimpleStringProperty(c.getValue().getTimestamp().toString()));

	    transTable.getColumns().addAll(tIdCol, tOrderCol, tMethodCol, tAmountCol, tTimeCol);
	    transTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	    transTable.setPrefHeight(220);

	    Runnable refreshTransactions = () -> {
	        try {
	            transTable.setItems(FXCollections.observableArrayList(
	                    financeController.getAllTransactions()
	            ));
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    };
	    refreshTransactions.run();   // initial

	    // --- hook Record Payment button ---
	    recordBtn.setOnAction(e -> {
	        String orderIdText = orderIdField.getText().trim();
	        String amountText = amountField.getText().trim();
	        String method = methodBox.getValue();

	        if (orderIdText.isEmpty() || amountText.isEmpty() || method == null) {
	            paymentStatusLabel.setText("Please fill Order ID, Amount and Method.");
	            return;
	        }

	        try {
	            int orderId = Integer.parseInt(orderIdText);
	            double amount = Double.parseDouble(amountText);

	            boolean ok = financeController.processPayment(orderId, amount, method);
	            if (ok) {
	                paymentStatusLabel.setText("Payment recorded.");
	                refreshRevenue.run();
	                refreshTransactions.run();
	            } else {
	                paymentStatusLabel.setText("Payment failed.");
	            }
	        } catch (NumberFormatException ex) {
	            paymentStatusLabel.setText("Order ID and Amount must be numbers.");
	        }
	    });

	    // -------------------- ROOT LAYOUT --------------------
	    VBox root = new VBox(
	            10,
	            title,
	            ordersHeader,
	            ordersTable,
	            new Separator(),
	            recordTitle,
	            recordRow1,
	            recordRow2,
	            paymentStatusLabel,
	            new Separator(),
	            new Label("Total Revenue (all time)"),
	            revenueRow,
	            new Separator(),
	            transLabel,
	            transTable
	    );
	    root.setPadding(new Insets(10));
	    root.setAlignment(Pos.TOP_LEFT);
	    return root;
	}



    // ==========================
    // MAIN
    // ==========================
    public static void main(String[] args) {
        launch(args);
    }
}
