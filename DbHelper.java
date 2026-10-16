package db;

import java.sql.*;


/**
 * Helper class for handling database operations via JDBC for the Sharetea POS system.
 * @author
 */
public class DbHelper {
    private static final String URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu:5432/squad_82_db";
    private static final String USER = "squad_82";
    private static final String PASS = "82";

    /**
     * Gets a new database connection.
     * @return Connection to the PostgreSQL database.
     * @throws SQLException if connection fails.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    
    // 1. Cashier: submit order
    /**
     * Inserts a new order into the orders table.
     * @param order_id The order ID.
     * @param order_date The order date as a string.
     * @param menu_item The menu item name.
     * @param order_total The total cost of the order.
     * @param order_time The time the order was placed.
     * @return The generated order_id on success, or -1 if not successful.
     * @throws SQLException if query execution fails.
     */
    public int submitOrder(int order_id, String order_date, String menu_item, double order_total, java.sql.Time order_time) throws SQLException {
        String sql = "INSERT INTO orders (order_id, order_date, menu_item, order_total, order_time) VALUES (?, ?, ?, ?, ?) RETURNING order_id";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, order_id);
            pst.setString(2, order_date); // TEXT
            pst.setString(3, menu_item);
            pst.setDouble(4, order_total);
            pst.setTime(5, order_time);
            ResultSet rs = pst.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    // 2. Manager: view all products (menu items)
    /**
     * Retrieves all products from the products table.
     * @return ResultSet containing product records.
     * @throws SQLException if a database access error occurs.
     */
    public ResultSet getProducts() throws SQLException {
        String sql = "SELECT * FROM products";
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // 3. Manager: add a product
    /**
     * Inserts a new product into the products table.
     * @param name Product name.
     * @param category Product category.
     * @param base_price Base price of the product.
     * @param description Description of the product.
     * @throws SQLException if the insert fails.
     */
    public void addProduct(String name, String category, double base_price, String description) throws SQLException {
        String sql = "INSERT INTO products (name, category, base_price, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, name);
            pst.setString(2, category);
            pst.setDouble(3, base_price);
            pst.setString(4, description);
            pst.executeUpdate();
        }
    }

    // 4. Manager: update a product
    /**
     * Updates an existing product in the products table.
     * @param name Product name (used as the identifier).
     * @param category New category.
     * @param base_price New base price.
     * @param description New description.
     * @throws SQLException if update fails.
     */
    public void updateProduct(String name, String category, double base_price, String description) throws SQLException {
        String sql = "UPDATE products SET category = ?, base_price = ?, description = ? WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, category);
            pst.setDouble(2, base_price);
            pst.setString(3, description);
            pst.setString(4, name);
            pst.executeUpdate();
        }
    }

    // 5. Manager: view all inventory items (ingredients)
    /**
     * Retrieves all ingredients from the ingredients table.
     * @return ResultSet containing all ingredient records.
     * @throws SQLException if a database access error occurs.
     */
    public ResultSet getIngredients() throws SQLException {
        String sql = "SELECT * FROM ingredients";
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // 6. Manager: add ingredient (inventory)
    /**
     * Inserts a new ingredient into the inventory.
     * @param name Ingredient name.
     * @param category Ingredient category.
     * @param quantity Stock quantity.
     * @param unit Measurement unit.
     * @param cost Cost per unit.
     * @param reorderLevel Reorder level trigger.
     * @throws SQLException if insert fails.
     */
    public void addIngredient(String name, String category, double quantity, String unit, double cost, double reorderLevel) throws SQLException {
        String sql = "INSERT INTO ingredients (name, category, stock_quantity, unit, cost_per_unit, reorder_level, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, name);
            pst.setString(2, category);
            pst.setDouble(3, quantity);
            pst.setString(4, unit);
            pst.setDouble(5, cost);
            pst.setDouble(6, reorderLevel);
            pst.executeUpdate();
        }
    }

    // 7. Manager: update ingredient quantity/attributes
    /**
     * Updates an ingredient's quantity, cost, or reorder level.
     * @param ingredientId Ingredient ID.
     * @param newQuantity Updated quantity value.
     * @param costPerUnit Updated cost per unit.
     * @param reorderLevel Updated reorder level.
     * @throws SQLException if update fails.
     */
    public void updateIngredient(int ingredientId, double newQuantity, double costPerUnit, double reorderLevel) throws SQLException {
        String sql = "UPDATE ingredients SET stock_quantity = ?, cost_per_unit = ?, reorder_level = ?, updated_at = NOW() WHERE ingredient_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setDouble(1, newQuantity);
            pst.setDouble(2, costPerUnit);
            pst.setDouble(3, reorderLevel);
            pst.setInt(4, ingredientId);
            pst.executeUpdate();
        }
    }

    // 8. Manager: add topping
    /**
     * Inserts a new topping into the toppings table.
     * @param topping_id Topping ID.
     * @param ingredient_id Linked ingredient ID.
     * @param name Name of the topping.
     * @param extra_price Extra price for topping.
     * @param portion_size Portion size.
     * @throws SQLException if insert fails.
     */
    public void addTopping(int topping_id, int ingredient_id, String name, double extra_price, double portion_size) throws SQLException {
        String sql = "INSERT INTO toppings (topping_id, ingredient_id, name, extra_price, portion_size) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, topping_id);
            pst.setInt(2, ingredient_id);
            pst.setString(3, name);
            pst.setDouble(4, extra_price);
            pst.setDouble(5, portion_size);
            pst.executeUpdate();
        }
    }

    // 9. Manager: update topping
    /**
     * Updates a topping in the toppings table.
     * @param topping_id Topping ID.
     * @param ingredient_id Linked ingredient ID.
     * @param name Name.
     * @param extra_price Extra price.
     * @param portion_size Portion size.
     * @throws SQLException if update fails.
     */
    public void updateTopping(int topping_id, int ingredient_id, String name, double extra_price, double portion_size) throws SQLException {
        String sql = "UPDATE toppings SET ingredient_id = ?, name = ?, extra_price = ?, portion_size = ? WHERE topping_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, ingredient_id);
            pst.setString(2, name);
            pst.setDouble(3, extra_price);
            pst.setDouble(4, portion_size);
            pst.setInt(5, topping_id);
            pst.executeUpdate();
        }
    }

    // 10. Manager: view all toppings
    /**
     * Retrieves all toppings.
     * @return ResultSet with all topping records.
     * @throws SQLException if query fails.
     */
    public ResultSet getToppings() throws SQLException {
        String sql = "SELECT * FROM toppings";
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // 11. Manager: sales report per day
    /**
     * Gets a daily sales report.
     * @param date The date for which to run the report (string format).
     * @return ResultSet containing the sales report.
     * @throws SQLException if query fails.
     */
    public ResultSet getDailySalesReport(String date) throws SQLException {
        String sql = "SELECT COUNT(*) AS total_orders, SUM(order_total) AS total_sales FROM orders WHERE order_date = ?";
        Connection conn = getConnection();
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, date); // order_date is TEXT
        return pst.executeQuery();
    }

    // 12. Manager: view employees
    /**
     * Retrieves all employees.
     * @return ResultSet with all employee records.
     * @throws SQLException if query fails.
     */
    public ResultSet getEmployees() throws SQLException {
        String sql = "SELECT * FROM employees";
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // 13. Manager: add employee
    /**
     * Adds a new employee.
     * @param lname Last name of the employee.
     * @param fname First name of the employee.
     * @param role Role of the employee.
     * @throws SQLException if insert fails.
     */
    public void addEmployee(String lname, String fname, String role) throws SQLException {
    String sql = "INSERT INTO employees (lname, fname, role) VALUES (?, ?, ?)";
    try (Connection conn = getConnection();
         PreparedStatement pst = conn.prepareStatement(sql)) {
        pst.setString(1, lname);
        pst.setString(2, fname);
        pst.setString(3, role);
        pst.executeUpdate();
    }
}
}
