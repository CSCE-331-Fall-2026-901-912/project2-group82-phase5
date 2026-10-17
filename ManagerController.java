package controller;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.ButtonType;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import db.DbHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.TreeMap; 

/**
 * This file controls the manager interface for menu, inventory, employee, sales report, X-report, restock report, and product usage.
 * FXML grabs the controller and uses the action handlers to ensure that everything works. Database operations run at the same time and reports
 * any of the failures that SQL gets.
 *
 * @author Dylan, Trishna, Hiya, Selsa, and Sophia
 */

public class ManagerController {
    // Data basehelper used to open connections for manager operations
    private final DbHelper database = new DbHelper();

    // Menu tab controls for the columns and text fields
    @FXML
    private TableView<MenuRow> menu_table;
    @FXML
    private TableColumn<MenuRow, String> menu_item_column;
    @FXML
    private TableColumn<MenuRow, String> menu_price_column;
    @FXML
    private TableColumn<MenuRow, String> menu_category_column;
    @FXML
    private TextField menu_category_field;
    @FXML
    private TextField menu_name_field;
    @FXML
    private TextField menu_price_field;
 
    // Inventory tab controls for columns and text fields
    @FXML
    private TableView<InventoryRow> inventory_table;
    @FXML
    private TableColumn<InventoryRow, String> inventory_item_column;
    @FXML
    private TableColumn<InventoryRow, String> inventory_quantity_column;
    @FXML
    private TextField inventory_item_field;
    @FXML
    private TextField inventory_quantity_field;
 
    // Employees tab controls for columns and text fields 
    @FXML
    private TableView<EmployeeRow> employee_table;
    @FXML
    private TableColumn<EmployeeRow, String> employee_first_name_column;
    @FXML
    private TableColumn<EmployeeRow, String> employee_last_name_column;
    @FXML
    private TableColumn<EmployeeRow, String> employee_id_column;
    @FXML
    private TableColumn<EmployeeRow, String> employee_role_column;
    @FXML
    private TextField employee_first_name_field;
    @FXML
    private TextField employee_last_name_field;
    @FXML
    private TextField employee_role_field;
 
    // Reports tab - Sales Report controls
    // This includes columns, filters for every menu item, pane controls, and report date controls
    private static final String ALL_ITEMS = "All";

    @FXML private VBox sales_report_pane;
    @FXML private VBox x_report_pane;
    @FXML private VBox restock_report_pane;

    @FXML private TableView<RestockRow> restock_report_table;
    @FXML private TableColumn<RestockRow, String> restock_item_column;
    @FXML private TableColumn<RestockRow, String> restock_current_column;
    @FXML private TableColumn<RestockRow, String> restock_minimum_column;
    @FXML private TableColumn<RestockRow, String> restock_shortfall_column;

    @FXML private TableView<XReportRow> x_report_table;
    @FXML private TableColumn<XReportRow, String> x_hour_column;
    @FXML private TableColumn<XReportRow, String> x_orders_column;
    @FXML private TableColumn<XReportRow, String> x_sales_column;
    @FXML private TableColumn<XReportRow, String> x_returns_column;
    @FXML private TableColumn<XReportRow, String> x_voids_column;
    @FXML private TableColumn<XReportRow, String> x_discards_column;
    @FXML private TableColumn<XReportRow, String> x_cash_column;
    @FXML private TableColumn<XReportRow, String> x_card_column;
    @FXML private Label x_report_totals_label;
    @FXML private DatePicker x_report_date;

    @FXML
    private TableView<SalesRow> sales_report_table;
    @FXML
    private TableColumn<SalesRow, String> sales_date_column;
    @FXML
    private TableColumn<SalesRow, String> sales_item_column;
    @FXML
    private TableColumn<SalesRow, String> sales_quantity_column;
    @FXML
    private TableColumn<SalesRow, String> sales_revenue_column;
    @FXML
    private ComboBox<String> sales_item_box;
    @FXML
    private DatePicker sales_start_date;
    @FXML
    private DatePicker sales_end_date;

    //Charts tab, this includes the start/end date controls, rows to present the menu and inventory table, and restock report. 
    @FXML
    private DatePicker table_startdate_picker;
    @FXML
    private DatePicker table_enddate_picker;
    @FXML
    private BarChart<String, Number> bar_chart;

    private final ObservableList<MenuRow> menuRows =
        FXCollections.observableArrayList();
    private final ObservableList<InventoryRow> inventoryRows =
        FXCollections.observableArrayList();
    private final ObservableList<EmployeeRow> employeeRows =
        FXCollections.observableArrayList();
    private final ObservableList<SalesRow> salesRows =
        FXCollections.observableArrayList();
    private final ObservableList<XReportRow> xReportRows =
        FXCollections.observableArrayList();
    private final ObservableList<RestockRow> restockRows =
        FXCollections.observableArrayList();
//  Menu 
//  This validates the menu fields, inserts a product, clears fields, reloads the menu, and output a message if any invalid inputs/failures happen.  
    @FXML
    private void add_menu_item(){
        String name = menu_name_field.getText().trim();
        String category = menu_category_field.getText().trim();
        BigDecimal price;
 
        try{
            price = new BigDecimal(menu_price_field.getText().trim());
        } catch (NumberFormatException exception){
            showMessage("Enter a valid price, like 4.50.");
            return;
        }
        if (name.isEmpty()){
            showMessage("Enter a menu item name.");
            return;
        }
        if (category.isEmpty()){
            category = "Other";
        }
 
        String sql = "INSERT INTO products (name, category, base_price) VALUES (?, ?, ?)";
 
        try (Connection connection = database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)){
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setBigDecimal(3, price);
            statement.executeUpdate();
 
            menu_name_field.clear();
            menu_category_field.clear();
            menu_price_field.clear();
            loadMenu();
        } catch (SQLException exception){
            showMessage("Could not add menu item: " + exception.getMessage());
        }
    }
 
    // validates fields and updates the selected product with its original name. It holds the original category when the category is blank
 
    @FXML
    private void update_menu_item(){
        MenuRow selected = menu_table.getSelectionModel().getSelectedItem();
        if (selected == null){
            showMessage("Select a menu item first.");
            return;
        }
 
        String name = menu_name_field.getText().trim();
        String category = menu_category_field.getText().trim();
        BigDecimal price;
 
        try{
            price = new BigDecimal(menu_price_field.getText().trim());
        } catch (NumberFormatException exception){
            showMessage("Enter a valid price, like 4.50.");
            return;
        }
        if (name.isEmpty()){
            showMessage("Enter a menu item name.");
            return;
        }
        if (category.isEmpty()){
            category = selected.getCategory();
        }
 
        String sql = "UPDATE products SET name = ?, category = ?, base_price = ? WHERE name = ?";
 
        try (Connection connection = database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)){
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setBigDecimal(3, price);
            statement.setString(4, selected.getName());
            statement.executeUpdate();
 
            loadMenu();
        } catch (SQLException exception){
            showMessage("Could not update menu item: " + exception.getMessage());
        }
    }
 
 
    //  Inventory 
    //  Inserts ingredients using an entered name and quantity. 
 
    @FXML
    private void add_inventory_item(){
        String name = inventory_item_field.getText().trim();
        BigDecimal quantity;
 
        try{
            quantity = new BigDecimal(inventory_quantity_field.getText().trim());
        } catch (NumberFormatException exception){
            showMessage("Enter a valid quantity, like 250.");
            return;
        }
        if (name.isEmpty()){
            showMessage("Enter an inventory item name.");
            return;
        }
 
        // the screen only has name and quantity: category and unit get placeholders,
        // cost_per_unit and reorder_level use the table defaults
        String sql = "INSERT INTO ingredients (name, category, stock_quantity, unit) "
            + "VALUES (?, 'Other', ?, 'units')";
 
        try (Connection connection = database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)){
            statement.setString(1, name);
            statement.setBigDecimal(2, quantity);
            statement.executeUpdate();
 
            inventory_item_field.clear();
            inventory_quantity_field.clear();
            loadInventory();
        } catch (SQLException exception){
            showMessage("Could not add inventory item: " + exception.getMessage());
        }
    }
 
    // Updates the selected ingredient stock quantity and modification time. It reports missing selection, invalid quantity, or any other failures.
    
    @FXML
    private void update_quantity_item(){
        InventoryRow selected = inventory_table.getSelectionModel().getSelectedItem();
        if (selected == null){
            showMessage("Select an inventory item first.");
            return;
        }
 
        BigDecimal quantity;
 
        try{
            quantity = new BigDecimal(inventory_quantity_field.getText().trim());
        } catch (NumberFormatException exception){
            showMessage("Enter a valid quantity, like 250.");
            return;
        }
 
        String sql = "UPDATE ingredients SET stock_quantity = ?, updated_at = NOW() WHERE name = ?";
 
        try (Connection connection = database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)){
            statement.setBigDecimal(1, quantity);
            statement.setString(2, selected.getName());
            statement.executeUpdate();
 
            loadInventory();
        } catch (SQLException exception){
            showMessage("Could not update quantity: " + exception.getMessage());
        }
    }
    
    //  Employees 
    //  Inserts employees after checking the first/last name and role. This data comes straight from the database to get the employee ID. 
 
    @FXML
    private void add_employee(){
        String firstName = employee_first_name_field.getText().trim();
        String lastName = employee_last_name_field.getText().trim();
        String role = employee_role_field.getText().trim();
 
        if (firstName.isEmpty() || lastName.isEmpty() || role.isEmpty()){
            showMessage("Enter a first name, last name, and role.");
            return;
        }
 
        // employee_id is assumed to be generated by the database
        String sql = "INSERT INTO employees (fname, lname, role) VALUES (?, ?, ?)";
 
        try (Connection connection = database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)){
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, role);
            statement.executeUpdate();
 
            clearEmployeeFields();
            loadEmployees();
        } catch (SQLException exception){
            showMessage("Could not add employee: " + exception.getMessage());
        }
    }
 
    // updates selected employee by ID after validating the name and role fields. It reloads after success and displays validation or errors. 

    @FXML
    private void update_employee(){
        EmployeeRow selected = employee_table.getSelectionModel().getSelectedItem();
        if (selected == null){
            showMessage("Select an employee first.");
            return;
        }
 
        String firstName = employee_first_name_field.getText().trim();
        String lastName = employee_last_name_field.getText().trim();
        String role = employee_role_field.getText().trim();
 
        if (firstName.isEmpty() || lastName.isEmpty() || role.isEmpty()){
            showMessage("Enter a first name, last name, and role.");
            return;
        }
 
        String sql = "UPDATE employees SET fname = ?, lname = ?, role = ? WHERE employee_id = ?";
 
        try (Connection connection = database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)){
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, role);
            statement.setInt(4, selected.getId());
            statement.executeUpdate();
 
            loadEmployees();
        } catch (SQLException exception){
            showMessage("Could not update employee: " + exception.getMessage());
        }
    }
 
    // double checks with manager before removing an employee and then it delets it 

    @FXML
    private void remove_employee(){
        EmployeeRow selected = employee_table.getSelectionModel().getSelectedItem();
        if (selected == null){
            showMessage("Select an employee first.");
            return;
        }
 
        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Remove " + selected.getFirstName() + " " + selected.getLastName() + "?",
            ButtonType.YES, ButtonType.NO
        );
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES){
            return;
        }
 
        String sql = "DELETE FROM employees WHERE employee_id = ?";
 
        try (Connection connection = database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)){
            statement.setInt(1, selected.getId());
            statement.executeUpdate();
 
            clearEmployeeFields();
            loadEmployees();
        } catch (SQLException exception){
            showMessage("Could not remove employee: " + exception.getMessage());
        }
    }
 
 
    //  Reports 
 
    // Shows the Sales Report pane and hides the other panes in the same StackPane
    @FXML
    private void show_sales_report(){
        for (Node pane : sales_report_pane.getParent().getChildrenUnmodifiable()){
            pane.setVisible(false);
        }
        sales_report_pane.setVisible(true);
    }

    private void showOnlyPane(VBox pane) {
        for (Node p : pane.getParent().getChildrenUnmodifiable()) {
            p.setVisible(false);
        }
        pane.setVisible(true);
    }

    // shows report pane and generates report for selected and any shortages

    @FXML private void show_x_report() { 
        showOnlyPane(x_report_pane);
        generate_x_report(); 
    }
    @FXML private void show_restock_report() {
        showOnlyPane(restock_report_pane);
        generate_restock_report();
    }

    // loads hourly counts of orders and sales for a selected date and then updates the totals

    @FXML private void generate_x_report() {
        LocalDate reportDate = x_report_date.getValue();
        if (reportDate == null){
            xReportRows.clear();
            x_report_totals_label.setText("Pick a date to run the X-Report.");
            return;
        }
        String sql =
            "SELECT EXTRACT(HOUR FROM order_time)::int AS hr, "
            + "COUNT(*) AS orders, COALESCE(SUM(order_total), 0) AS sales "
            + "FROM orders WHERE order_date = ? "
            + "GROUP BY hr ORDER BY hr";

        // hour -> {orders, sales} as read from the database
        TreeMap<Integer, Integer> ordersByHour = new TreeMap<>();
        Map<Integer, BigDecimal> salesByHour = new TreeMap<>();

        try (Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, reportDate.toString()); // order_date is TEXT

            try (ResultSet result = statement.executeQuery()){
                while (result.next()){
                    ordersByHour.put(result.getInt("hr"), result.getInt("orders"));
                    salesByHour.put(result.getInt("hr"), result.getBigDecimal("sales"));
                }
            }
        } catch (SQLException exception){
            // no popup: this also runs on startup, and the other loaders already show alerts
            xReportRows.clear();
            x_report_totals_label.setText("Could not run X-Report: " + exception.getMessage());
            return;
        }

        xReportRows.clear();
        if (ordersByHour.isEmpty()){
            x_report_totals_label.setText("Totals: no sales recorded for this day.");
            return;
        }

        // Range of hours to show: first sale of the day through the current hour
        int firstHour = ordersByHour.firstKey();
        int lastHour = ordersByHour.lastKey();
        if (reportDate.equals(LocalDate.now())) {
            lastHour = Math.max(lastHour, LocalTime.now().getHour());
        }

        // Find the busiest hour by sales (first one wins a tie)
        int peakHour = firstHour;
        BigDecimal peakSales = BigDecimal.valueOf(-1);
        for (int hour = firstHour; hour <= lastHour; hour++){
            BigDecimal sales = salesByHour.getOrDefault(hour, BigDecimal.ZERO);
            if (sales.compareTo(peakSales) > 0){
                peakSales = sales;
                peakHour = hour;
            }
        }

        int totalOrders = 0;
        BigDecimal totalSales = BigDecimal.ZERO;
        for (int hour = firstHour; hour <= lastHour; hour++){
            int orders = ordersByHour.getOrDefault(hour, 0);
            BigDecimal sales = salesByHour.getOrDefault(hour, BigDecimal.ZERO);
            xReportRows.add(new XReportRow(hour, orders, sales, hour == peakHour));
            totalOrders += orders;
            totalSales = totalSales.add(sales);
        }

        x_report_totals_label.setText(
            "Totals: " + totalOrders + " orders, $"
            + totalSales.setScale(2, RoundingMode.HALF_UP)
            + "   |   Busiest hour: " + XReportRow.hourLabel(peakHour)
            + " ($" + peakSales.setScale(2, RoundingMode.HALF_UP) + ")");
    }
    @FXML private void generate_restock_report() {
        String sql =
            "SELECT name, stock_quantity, reorder_level FROM ingredients "
            + "WHERE stock_quantity < reorder_level "
            + "ORDER BY (reorder_level - stock_quantity) DESC, name";

        try (Connection connection = database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()){
            restockRows.clear();
            while (result.next()){
                restockRows.add(new RestockRow(
                    result.getString("name"),
                    result.getBigDecimal("stock_quantity"),
                    result.getBigDecimal("reorder_level")
                ));
            }
        } catch (SQLException exception){
            showMessage("Could not generate restock report: " + exception.getMessage());
        }
    }


    // Start/end dates are optional (blank = no limit). The end date includes the whole day.
    // Rows are grouped by menu item, each group sorted by date and time and followed
    // by a Total row.
    @FXML
    private void generate_sales_report(){
        LocalDate start = sales_start_date.getValue();
        LocalDate end = sales_end_date.getValue();
        String choice = sales_item_box.getValue();
        boolean allItems = choice == null || choice.equals(ALL_ITEMS);

        if (start != null && end != null && start.isAfter(end)){
            showMessage("The start date must be on or before the end date.");
            return;
        }

        String sql =
            "SELECT order_date, order_time, menu_item, COUNT(*) AS amount_sold, "
            + "SUM(order_total) AS revenue FROM orders WHERE 1 = 1";
        if (start != null){
            sql += " AND order_date >= ?";
        }
        if (end != null){
            sql += " AND order_date <= ?";
        }
        if (!allItems){
            sql += " AND menu_item = ?";
        }
        sql += " GROUP BY order_date, order_time, menu_item"
            + " ORDER BY menu_item, order_date, order_time";

        try (Connection connection = database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql)){
            int index = 1;
            if (start != null){
                statement.setString(index++, start.toString());
            }
            if (end != null){
                statement.setString(index++, end.toString());
            }
            if (!allItems){
                statement.setString(index++, choice);
            }

            salesRows.clear();
            String currentItem = null;
            int itemCount = 0;
            BigDecimal itemRevenue = BigDecimal.ZERO;

            try (ResultSet result = statement.executeQuery()){
                while (result.next()){
                    String item = result.getString("menu_item");

                    // New menu item: close out the previous one with a Total row
                    if (currentItem != null && !currentItem.equals(item)){
                        salesRows.add(new SalesRow("Total", currentItem, itemCount, itemRevenue, true));
                        itemCount = 0;
                        itemRevenue = BigDecimal.ZERO;
                    }
                    currentItem = item;

                    // Date and time together, like 2026-09-24 14:32:10
                    String dateTime = result.getString("order_date");
                    java.sql.Time time = result.getTime("order_time");
                    if (time != null){
                        dateTime = dateTime + " " + time;
                    }

                    int count = result.getInt("amount_sold");
                    BigDecimal revenue = result.getBigDecimal("revenue");
                    salesRows.add(new SalesRow(dateTime, item, count, revenue, false));
                    itemCount += count;
                    itemRevenue = itemRevenue.add(revenue);
                }
            }

            if (currentItem == null){
                showMessage("No sales found for those filters.");
                return;
            }
            salesRows.add(new SalesRow("Total", currentItem, itemCount, itemRevenue, true));
        } catch (SQLException exception){
            showMessage("Could not generate sales report: " + exception.getMessage());
        }
    }

    // plots the new number of records for each menu item within the selected dates. It goes by order of most to least. 
    @FXML
    private void generate_usage_chart(){
        LocalDate start = table_startdate_picker.getValue();
        LocalDate end = table_enddate_picker.getValue();

        if(start == null || end == null){
            showMessage("Choose both a start date and an end date.");
            return;
        }

        if(start.isAfter(end)){
            showMessage("The start date must be before the end date.");
            return;
        }

        String sql  = "SELECT menu_item, COUNT(*) AS amount_sold " + "FROM orders " + "WHERE order_date::date BETWEEN ? AND ? " + "GROUP by menu_item " + "ORDER BY amount_sold DESC";
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Orders");

        try(Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setDate(1, Date.valueOf(start));
            statement.setDate(2, Date.valueOf(end));
            try(ResultSet result = statement.executeQuery()){
                while(result.next()){
                    series.getData().add(new XYChart.Data<>(result.getString("menu_item"), result.getInt("amount_sold")));
                }
            }

            bar_chart.setAnimated(false);
            ((javafx.scene.chart.CategoryAxis) bar_chart.getXAxis()).setAnimated(false);
            bar_chart.getData().clear();
            bar_chart.setTitle("Product Usage");
            bar_chart.getXAxis().setLabel("Menu Item");
            bar_chart.getYAxis().setLabel("Orders");
            if(series.getData().isEmpty()){
                showMessage("No orders found for that data range.");
            } else {
                bar_chart.getData().add(series);
            }
        } catch (SQLException exception){
            showMessage("Could not generate chart: " + exception.getMessage());
        }
    }


    //  Loading 
    //  Reloads product names, categories, and prices straight from the data and refreshes the filter choices. 
    private void loadMenu(){
        String sql = "SELECT name, category, base_price FROM products ORDER BY name"; 
        try (Connection connection = database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()){
            menuRows.clear();
            while (result.next()){
                menuRows.add(new MenuRow(
                    result.getString("name"),
                    result.getString("category"),
                    result.getBigDecimal("base_price")
                ));
            }
        } catch (SQLException exception){
            showMessage("Could not load menu: " + exception.getMessage());
        }
        refreshSalesItems();
    }

    // Fills the Sales Report drop-down: "All" first, then every menu item
    private void refreshSalesItems(){
        String current = sales_item_box.getValue();

        ObservableList<String> choices = FXCollections.observableArrayList();
        choices.add(ALL_ITEMS);
        for (MenuRow row : menuRows){
            choices.add(row.getName());
        }
        sales_item_box.setItems(choices);

        if (current != null && choices.contains(current)){
            sales_item_box.setValue(current);
        } else {
            sales_item_box.setValue(ALL_ITEMS);
        }
    }

    // Reloads ingredient names and stock quantities, sorted by name. 
 
    private void loadInventory(){
        String sql = "SELECT name, stock_quantity FROM ingredients ORDER BY name";
 
        try (Connection connection = database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()){
            inventoryRows.clear();
            while (result.next()){
                inventoryRows.add(new InventoryRow(
                    result.getString("name"),
                    result.getBigDecimal("stock_quantity")
                ));
            }
        } catch (SQLException exception){
            showMessage("Could not load inventory: " + exception.getMessage());
        }
    }
 
    // Reloads employees names, IDs, roles, and ID.  
    
    private void loadEmployees(){
        String sql = "SELECT employee_id, fname, lname, role FROM employees ORDER BY employee_id";
 
        try (Connection connection = database.getConnection();
            PreparedStatement statement =
                connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery()){
            employeeRows.clear();
            while (result.next()){
                employeeRows.add(new EmployeeRow(
                    result.getInt("employee_id"),
                    result.getString("fname"),
                    result.getString("lname"),
                    result.getString("role")
                ));
            }
        } catch (SQLException exception){
            showMessage("Could not load employees: " + exception.getMessage());
        }
    }
 
     //  Helpers 
     
 
    private void clearEmployeeFields(){
        employee_first_name_field.clear();
        employee_last_name_field.clear();
        employee_role_field.clear();
    }
 
    /* 
    * displays a warning dialog containg this assigned message
    * @param message warning message to display 
    */
    
    private void showMessage(String message){
        Alert alert = new Alert(Alert.AlertType.WARNING, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
 
    // intializes table bindings, report row styles, and selection listeners after FXML injection. Loads menu, inventory, employee data, and
    // and sets the X-report date to today. 
 
    @FXML
    private void initialize() {
        menu_item_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getName()));
        menu_price_column.setCellValueFactory(
            c -> new SimpleStringProperty("$" + c.getValue().getPrice()));
        menu_category_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getCategory()));
        menu_table.setItems(menuRows);
 
        inventory_item_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getName()));
        inventory_quantity_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getQuantity().toPlainString()));
        inventory_table.setItems(inventoryRows);
 
        employee_first_name_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getFirstName()));
        employee_last_name_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getLastName()));
        employee_id_column.setCellValueFactory(
            c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        employee_role_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getRole()));
        employee_table.setItems(employeeRows);
 
        sales_date_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getDate()));
        sales_item_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getItem()));
        sales_quantity_column.setCellValueFactory(
            c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        sales_revenue_column.setCellValueFactory(
            c -> new SimpleStringProperty(
                "$" + c.getValue().getRevenue().setScale(2, RoundingMode.HALF_UP)));
        sales_report_table.setItems(salesRows);

        x_hour_column.setCellValueFactory(
            c -> new SimpleStringProperty(XReportRow.hourLabel(c.getValue().getHour())));
        x_orders_column.setCellValueFactory(
            c -> new SimpleStringProperty(String.valueOf(c.getValue().getOrders())));
        x_sales_column.setCellValueFactory(
            c -> new SimpleStringProperty(
                "$" + c.getValue().getSales().setScale(2, RoundingMode.HALF_UP)));
        // The orders table has no returns / voids / discards / payment-method data yet,
        // so these columns show n/a instead of a misleading 0. Once the database tracks
        // them, replace these with real values from getReturns(), getCash(), etc.
        x_returns_column.setCellValueFactory(c -> new SimpleStringProperty("n/a"));
        x_voids_column.setCellValueFactory(c -> new SimpleStringProperty("n/a"));
        x_discards_column.setCellValueFactory(c -> new SimpleStringProperty("n/a"));
        x_cash_column.setCellValueFactory(c -> new SimpleStringProperty("n/a"));
        x_card_column.setCellValueFactory(c -> new SimpleStringProperty("n/a"));
        x_report_table.setItems(xReportRows);
        x_report_table.setPlaceholder(new Label("No sales recorded for this day."));

        restock_item_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getName()));
        restock_current_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getCurrent().toPlainString()));
        restock_minimum_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getMinimum().toPlainString()));
        restock_shortfall_column.setCellValueFactory(
            c -> new SimpleStringProperty(c.getValue().getShortfall().toPlainString()));
        restock_report_table.setItems(restockRows);
        restock_report_table.setPlaceholder(new Label("Nothing needs restocking right now."));

        x_report_table.setRowFactory(table -> new TableRow<XReportRow>(){
            /*
            * Updates the displayed row and resets or applies its report specific styling when the row is reused. 
            * @param row report row to display, or null when no row is assigned 
            * @param empty whether the table row is empty
            */
            @Override
            protected void updateItem(XReportRow row, boolean empty){
                super.updateItem(row, empty);
                if (empty || row == null){
                    setStyle("");
                } else if (row.isPeak()){
                    setStyle("-fx-font-weight: bold; -fx-background-color: #d8f0d8;");
                } else if (row.getOrders() == 0){
                    setStyle("-fx-text-fill: #888888;");
                } else {
                    setStyle("");
                }
            }
        });

        // Total rows are shown in bold with a gray background
        sales_report_table.setRowFactory(table -> new TableRow<SalesRow>(){
            /*
            * Updates the displayed row and resets or applies its report specific styling when the row is reused. 
            * @param row report row to display, or null when no row is assigned 
            * @param empty whether the table row is empty
            */
            @Override
            protected void updateItem(SalesRow row, boolean empty){
                super.updateItem(row, empty);
                if (!empty && row != null && row.isTotal()){
                    setStyle("-fx-font-weight: bold; -fx-background-color: #e0e0e0;");
                } else {
                    setStyle("");
                }
            }
        });

        // Clicking a row fills the text fields
        menu_table.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldRow, row) -> {
                if (row != null){
                    menu_name_field.setText(row.getName());
                    menu_category_field.setText(row.getCategory());
                    menu_price_field.setText(row.getPrice().toPlainString());
                }
            });
 
        inventory_table.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldRow, row) -> {
                if (row != null){
                    inventory_item_field.setText(row.getName());
                    inventory_quantity_field.setText(row.getQuantity().toPlainString());
                }
            });
 
        employee_table.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldRow, row) -> {
                if (row != null){
                    employee_first_name_field.setText(row.getFirstName());
                    employee_last_name_field.setText(row.getLastName());
                    employee_role_field.setText(row.getRole());
                }
            });
 
        loadMenu();
        loadInventory();
        loadEmployees();
        x_report_date.setValue(LocalDate.now());
        x_report_date.valueProperty().addListener(
            (observable, oldDate, newDate) -> generate_x_report());
        generate_x_report();
    }
 
 
    //  Row classes
    public static class MenuRow {
        private final String name;
        private final String category;
        private final BigDecimal price;
 
        public MenuRow(String name, String category, BigDecimal price){
            this.name = name;
            this.category = category;
            this.price = price;
        }
        public String getName(){ return name; }
        // @return item name
        public String getCategory(){ return category; }
        // @return item category
        public BigDecimal getPrice(){ return price; }
        // @return item price
        /*
        *@param name item or ingredient name
        *@param category menu item category
        *@param price menu item base price
        @return the item or ingredient name
        */
    }
 
    public static class InventoryRow {
        private final String name;
        private final BigDecimal quantity;
 
        public InventoryRow(String name, BigDecimal quantity){
            this.name = name;
            this.quantity = quantity;
        }
        public String getName(){ return name; }
        public BigDecimal getQuantity(){ return quantity; }
        // @param name item or ingredient name
        // @param quantity stock
    }

        /*
        * @param employee id
        * @param employee first name
        * @param employee last name
        * @param employee role
        */
 
    public static class EmployeeRow {
        private final int id;
        private final String firstName;
        private final String lastName;
        private final String role;
 
        public EmployeeRow(int id, String firstName, String lastName, String role){
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
        }
        // @return the employee id
        public int getId(){ return id; }
        // @return the employee first name
        public String getFirstName(){ return firstName; }
        // @return the employee last name 
        public String getLastName(){ return lastName; }
        // @return the employee role 
        public String getRole(){ return role; }
    }
 
    public static class SalesRow {
        private final String date;
        private final String item;
        private final int quantity;
        private final BigDecimal revenue;
        private final boolean total;

        /*
        * @param date displayed date and time
        * @param item menu
        * @param quantity of stock for inventory
        * @param revenue sales for this row
        * @param total whether this row is per-item total
        */

        public SalesRow(String date, String item, int quantity, BigDecimal revenue, boolean total){
            this.date = date;
            this.item = item;
            this.quantity = quantity;
            this.revenue = revenue;
            this.total = total;
        }
        // @return displayed date and time
        public String getDate(){ return date; }
        // @return the menu item name 
        public String getItem(){ return item; }
        // @return the quantity stored in this row
        public int getQuantity(){ return quantity; }
        // @return the sales revenue
        public BigDecimal getRevenue(){ return revenue; }
        // @return true if this is row per item total
        public boolean isTotal(){ return total; }
    }

    public static class XReportRow {
        private final int hour;
        private final int orders;
        private final BigDecimal sales;
        private final boolean peak;

        /*
        * @param hour of day from 0 through 23
        * @param number of order records for this hour
        * @param sales revenue for this hour
        * @param peak whether this hour as highest sales
        */
        public XReportRow(int hour, int orders, BigDecimal sales, boolean peak){
            this.hour = hour;
            this.orders = orders;
            this.sales = sales;
            this.peak = peak;            
        }
        // @return the hour of day in 24 hour form
        public int getHour(){ return hour; }
        // @return the hourly number of order records
        public int getOrders(){ return orders; }
        // @return the hourly sales revenue
        public BigDecimal getSales(){ return sales; }
        // @return true if this row marks the peak sales hour; false otherwise
        public boolean isPeak(){ return peak; }

        // @param hour of day
        // @return formatted hour
        // 14 -> "2 PM", 0 -> "12 AM"
        public static String hourLabel(int hour){
            int twelve = hour % 12 == 0 ? 12 : hour % 12;
            return twelve + (hour < 12 ? " AM" : " PM");
        }
        
    }

    public static class RestockRow {
        private final String name;
        private final BigDecimal current;
        private final BigDecimal minimum;

        // @param name item
        // @param current stock
        // @param minimum stock quantity

        public RestockRow(String name, BigDecimal current, BigDecimal minimum){
            this.name = name;
            this.current = current;
            this.minimum = minimum;
            // @ingredient name
        }
        public String getName(){ return name; }
        // @return current stock quantity
        public BigDecimal getCurrent(){ return current; }
        // @return the minimum stock quantity
        public BigDecimal getMinimum(){ return minimum; }
        // @return the minimum stock minus current  stock
        public BigDecimal getShortfall(){ return minimum.subtract(current); }
    }

}

