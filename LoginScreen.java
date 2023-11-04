package application;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginScreen {
	
	private static ArrayList<String> logInfo = new ArrayList<String>();
	private static String username;
	
    public static boolean display() {
        Stage loginStage = new Stage();
        loginStage.setTitle("Login");
        
        logInfo = loadUseLog(logInfo);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label usernameLabel = new Label("Username:");
        TextField usernameInput = new TextField();
        Label passwordLabel = new Label("Password:");
        Label errorMsg = new Label("Invalid Login");
        errorMsg.setVisible(false);
        errorMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
        PasswordField passwordInput = new PasswordField();
        Button loginButton = new Button("Login");
        Button newAccount = new Button("Create Account");
        Button createAccount = new Button("Login");
        createAccount.setVisible(false);

        loginButton.setOnAction(e -> {
        	String[] useName = new String[2];
            useName[0] = usernameInput.getText();
            useName[1] = passwordInput.getText();

            if (isValidLogin(useName[0], useName[1])) {
            	username = useName[0];
                loginStage.close();
            } else {
            	errorMsg.setVisible(true);
            } 
        });
        
        newAccount.setOnAction(e -> {
            errorMsg.setVisible(false);
            loginButton.setVisible(false);
            newAccount.setVisible(false);
            createAccount.setVisible(true);
        });
        
        createAccount.setOnAction(e -> {
            logInfo.add(usernameInput.getText());
            logInfo.add(passwordInput.getText());
            username = usernameInput.getText();
            saveNewAcc(logInfo);
            loginStage.close();
        });
        
        loginStage.setOnCloseRequest(event -> {
            Platform.exit();
        });

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameInput, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordInput, 1, 1);
        grid.add(loginButton, 1, 2);
        grid.add(createAccount, 1, 2);
        grid.add(newAccount, 2, 2);
        grid.add(errorMsg, 1, 3);

        Scene scene = new Scene(grid, 400, 200);
        loginStage.setScene(scene);

        loginStage.showAndWait();
        return true;
    }

    private static boolean isValidLogin(String username, String password) {
    	boolean b = false;
    	for(int i = 0; i < logInfo.size(); i+=2) {
    		if(logInfo.get(i).equals(username) && logInfo.get(i+1).equals(password)) {
    			username = logInfo.get(i);
    			b = true;
    		}
    	}
        return b;
    } 
    
    public static String getUsername() {
    	return username;
    }
    
    private static void saveNewAcc(ArrayList<String> useLog) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("useLog.dat"))) {
            outputStream.writeObject(useLog);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<String> loadUseLog(ArrayList<String> logInfo) {
    try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("useLog.dat"))) {
    	return logInfo = (ArrayList<String>)inputStream.readObject();
    } catch (IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    		return logInfo;
    }
    }
}

