package controller;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Main application class for the ShareTea POS system.
 * 
 * @author Trishna Rao
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(
            makeTab("Cashier", "/controller/cashierview.fxml"),
            makeTab("Manager", "/controller/NewManagerView.fxml")
        );

        stage.setTitle("ShareTea POS");
        stage.setScene(new Scene(tabs, 800, 600));
        stage.show();
    }

    /**
     * Dynamically creates a non-closable tab by loading its FXML view layout.
     * 
     * @param title The text label to display on the tab header.
     * @param path The resource path string pointing to the FXML file.
     * @return A configured JavaFX Tab containing the loaded FXML view.
     * @throws IOException If the FXML resource cannot be found or fails to load.
     */
    private Tab makeTab(String title, String path) throws IOException {
        URL url = getClass().getResource(path);
        if (url == null) {
            throw new IOException("Could not find " + path + " in src/main/resources");
        }
        Tab tab = new Tab(title, FXMLLoader.load(url));
        tab.setClosable(false);
        return tab;
    }

    public static void main(String[] args) {
        launch(args);
    }
}