
package controller;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToggleGroup;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import db.DbHelper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
//import javafx.scene.control.Alert;

/**
 * Controls the cashier view including drink selection
 * the current order and saving purchases to the database 
 * 
 * @author Selsabeel Jwayyed
 */

public class CashierController {


    /** the currently selection drink */
    private String selectedDrink;

    /** provides database connections */
    private final DbHelper database = new DbHelper();

    /** the price of the selected drink */
    private BigDecimal selectedPrice;

    /** the total price of the current order  */
    private BigDecimal total = BigDecimal.ZERO;

    /**
     * stores one drink in the current order 
     * 
     * @param name the drink name 
     * @param price the drink price 
     */
    private record OrderLine(String name, BigDecimal price) {}

    /** the drinks added to the current order */
    private final List<OrderLine> orderLines = new ArrayList<>();

    /** the currently selected sweetness level */
    private String selectedSweetness;


    /**groups of avaliable ice options */
    @FXML
    private ToggleGroup iceGroup;

    /** display the sweetness options and current selection */
    @FXML
    private SplitMenuButton sweetnessBox;

    /** contains the milk tea menu buttons */
    @FXML private VBox milkTeaBox;

    /** brew tea menu buttons */
    @FXML private VBox freshBrewBox;

    /** fruit tea buttons */
    @FXML private VBox fruitTeaBox;

    /** mojitio buttons*/
    @FXML private VBox mojitoBox;

    /** blended */
    @FXML private VBox blendedBox;

    /**other  */
    @FXML private VBox otherBox;

    /** add selected drink to current order */
    @FXML
    private Button submitButton;

    /** status messages */
    @FXML
    private TextArea orderArea;

    /** order total */
    @FXML
    private Text totalText;


    /** 
     * loads clicked drink price from database and selects the drink
     * displays message if drink is missing or database error occurs
     * 
     * @param event the button action identifying the selected drink
     */
    @FXML
    private void selectItem(ActionEvent event){
        Button clickedButton = (Button) event.getSource();
        String drinkName = clickedButton.getText();

        selectedDrink = null;
        selectedPrice = null;

        String sql = "SELECT base_price FROM products WHERE name = ?";

        try (Connection connection = database.getConnection();
            PreparedStatement statement = 
                connection.prepareStatement(sql)){
            statement.setString(1, drinkName);



            try(ResultSet result = statement.executeQuery()){
                if(!result.next()){
                    orderArea.setText("Drink not found" + drinkName);
                    return;
                }
                selectedDrink = drinkName;
                selectedPrice = result.getBigDecimal("base_price");

                orderArea.setText(
                    "Selected" + selectedDrink
                    +"\nPrice: $" + selectedPrice
                );

            }
                } catch(SQLException exception){
                    orderArea.setText(
                        "Could not load price:" + exception.getMessage()
                    );
                }

                updateSubmitButton();


    }

    /** 
     * adds the selected drink name and price to the order and updates the total
     * clears the drink ice and sweetness selections for the next drink
     */
    @FXML
    private void submitDrink(){
        if (selectedDrink == null || selectedPrice == null){
            orderArea.setText("Please select a drink first.");
            return;
        }
        orderLines.add(new OrderLine(selectedDrink, selectedPrice));
        total = total.add(selectedPrice);

        // clear the current choices for the next drink
        selectedDrink = null;
        selectedPrice = null;
        iceGroup.selectToggle(null);
        selectedSweetness = null;
        sweetnessBox.setText("Choose...");
        updateSubmitButton();

        refreshOrderDisplay();
    }

    /** 
     * enables the submit button when drink ice option 
     * and sweetness level have been selected 
     */
    private void updateSubmitButton() {
        boolean ready = selectedDrink != null
            && iceGroup.getSelectedToggle() != null
            && selectedSweetness != null;
        submitButton.setDisable(!ready);
    }

    /** updates the displayed order lines and total */
    private void refreshOrderDisplay(){
        StringBuilder text = new StringBuilder();
        for (OrderLine line : orderLines){
            text.append(line.name()).append("  $").append(line.price()).append("\n");
        }
        orderArea.setText(text.toString());
        totalText.setText("TOTAL: $" + total);
    }

    /** 
     * loads products from the database and creates menu buttons by category
     * displays error message if database query fails 
     */
    private void loadMenuItems() {
        String query = "SELECT name, category FROM products ORDER BY name";
        // Clear all boxes first
        milkTeaBox.getChildren().clear();
        freshBrewBox.getChildren().clear();
        fruitTeaBox.getChildren().clear();
        mojitoBox.getChildren().clear();
        blendedBox.getChildren().clear();
        otherBox.getChildren().clear();

        try (Connection conn = database.getConnection();
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery()) {

                while (rs.next()) {
                    String itemName = rs.getString("name");
                    String category = rs.getString("category");
                    Button menuBtn = new Button(itemName);

                    // Map DB category to UI category, ignoring case
                    String uiCat = switch (category == null ? "" : category.trim().toUpperCase()) {
                        case "MILK TEA" -> "MILK TEA";
                        case "FRESH BREW" -> "FRESH BREW";
                        case "FRUIT TEA" -> "FRUIT TEA";
                        case "MOJITO" -> "MOJITO";
                        case "BLENDED" -> "BLENDED";
                        default -> "OTHER";
                    };

                    VBox box = switch (uiCat) {
                        case "MILK TEA" -> milkTeaBox;
                        case "FRESH BREW" -> freshBrewBox;
                        case "FRUIT TEA" -> fruitTeaBox;
                        case "MOJITO" -> mojitoBox;
                        case "BLENDED" -> blendedBox;
                        default -> otherBox;
                    };
                    menuBtn.setMaxWidth(Double.MAX_VALUE);
                    menuBtn.setWrapText(true);
                    // Add action handler as needed
                    menuBtn.setOnAction(this::selectItem);
                    box.getChildren().add(menuBtn);
                }
            } 
        catch (SQLException e) {
            // Handle exception
            orderArea.setText("Could not load menu: " + e.getMessage());
        }
    }

    /** removes the last drink added to the order 
     * updates the total 
     * displays message if order is empty 
     */
    @FXML
    private void voidOrder() {
        if (orderLines.isEmpty()){
            orderArea.setText("Nothing to void.");
            return;
        }
        OrderLine removed = orderLines.remove(orderLines.size() - 1);
        total = total.subtract(removed.price());
        refreshOrderDisplay();
    }

    /** saves each drink as a separate database row in one transaction
     * clears the order after a sucessful commit 
     * attempts to roll back changes and displays a message if SQL error occues
     */
    @FXML
    private void checkout() {
        if (orderLines.isEmpty()){
            orderArea.setText("Please add a drink first.");
            return;
        }
        try (Connection connection = database.getConnection()){
            connection.setAutoCommit(false);

            try (Statement lock = connection.createStatement()){
                lock.execute("LOCK TABLE orders IN EXCLUSIVE MODE");

                String sql =
                    "INSERT INTO orders"
                    + "(order_id, order_date, menu_item, order_total, order_time)"
                    + "SELECT COALESCE(MAX(order_id), 0) + 1,"
                    + "?, ?, ?, ? FROM orders";

                try (PreparedStatement statement = connection.prepareStatement(sql)){
                    for (OrderLine line : orderLines){
                        statement.setString(1, LocalDate.now().toString());
                        statement.setString(2, line.name());
                        statement.setBigDecimal(3, line.price());
                        statement.setTime(4, Time.valueOf(LocalTime.now()));
                        statement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException exception){
                connection.rollback();
                throw exception;
            }

            orderLines.clear();
            total = BigDecimal.ZERO;
            orderArea.setText("Order saved successfully!");
            totalText.setText("TOTAL: $0.00");
        } catch (SQLException exception){
            orderArea.setText("Order was not saved:" + exception.getMessage());
        }
    }

    /** sets the initial display registers ice and sweetness selectiion 
     * updates the submit button and loads the menu
     */
    @FXML
    private void initialize() {
        orderArea.setText("Select a drink to begin.");
        totalText.setText("TOTAL: $0.00");

        iceGroup.selectedToggleProperty().addListener(
            (observable, oldToggle, newToggle) -> updateSubmitButton());

        for (MenuItem item : sweetnessBox.getItems()) {
            item.setOnAction(e -> {
                selectedSweetness = item.getText();
                sweetnessBox.setText(selectedSweetness);
                updateSubmitButton();
            });
        }

        updateSubmitButton();
        // Load menu items from database
        loadMenuItems();
    }
}
