package application;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.util.Calendar;


public class calendarApp extends Application {

    private YearMonth currentYearMonth;
    private GridPane calendarGrid;
    private Label monthLabel;
    private Label timeLabel;
    private List<Label> dayLabels;
    private List<Label> dayNames;
    private LocalDate selectedDate;
    private Button addEveButton;
    private Button saveEveButton;
    private Button aiAskButton;
    private int prevDate;
    
    private Map<LocalDate, List<String>> dateEvents = new HashMap<>();


    private List<String> events = new ArrayList<>();
    
    @Override
    public void start(Stage primaryStage) {
    	boolean loggedIn = LoginScreen.display();

        if (loggedIn) {
            // Proceed to the main calendar view
            showCalendar(primaryStage);
        }
    }
    
    public void showCalendar(Stage primaryStage) {
    	loadEventsFromFile();
    	
    	prevDate = 0;
    	
        currentYearMonth = YearMonth.now();
        selectedDate = LocalDate.now();

        primaryStage.setTitle("Advanced Calendar");

        BorderPane root = new BorderPane();

        HBox topBar = createTopBar();
        root.setTop(topBar);

        calendarGrid = new GridPane();
        root.setCenter(calendarGrid);
        
        HBox bottomBar = createBottomBar();
        root.setBottom(bottomBar);

        VBox eventBox = createEventBox();
        root.setBottom(eventBox);

        updateCalendar();

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        
        primaryStage.setOnCloseRequest(event -> {
            saveEventsToFile();
        });
        
        primaryStage.show();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.getStyleClass().add("top-bar");

        Button prevMonthButton = new Button("Previous Month");
        Button nextMonthButton = new Button("Next Month");

        monthLabel = new Label();
        monthLabel.getStyleClass().add("month-label");

        prevMonthButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        nextMonthButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        topBar.getChildren().addAll(prevMonthButton, monthLabel, nextMonthButton);

        return topBar;
    }
    
    private HBox createBottomBar() {
    	HBox bottomBar = new HBox(10);
    	bottomBar.getStyleClass().add("bottom-bar");
    	timeLabel = new Label();
    	timeLabel.getStyleClass().add("time-label");
    	bottomBar.getChildren().add(timeLabel);
    	
    	return bottomBar;
    }

    private VBox createEventBox() {
        VBox eventBox = new VBox(10);
        eventBox.getStyleClass().add("event-box");

        Label eventLabel = new Label("Events for Selected Date:");
        TextArea eventTextArea = new TextArea();
        eventTextArea.setEditable(false);
        eventTextArea.setWrapText(true);
        
        addEveButton = new Button("Add Event");
        saveEveButton = new Button("Save Text");
        aiAskButton = new Button("\"AI\" Scheduling Help");
        saveEveButton.setVisible(false);
        
        
        addEveButton.setOnAction(e -> {
        	if(eventTextArea.getText().equals("No events for selected date.")) {
        		eventTextArea.setText("");
        	}
        	eventTextArea.setEditable(true);
        	saveEveButton.setVisible(true);
        	addEveButton.setVisible(false);
        	
        });
        
        saveEveButton.setOnAction(e -> {
        	String eventText = eventTextArea.getText();
            List<String> eventsForDate = dateEvents.get(selectedDate);
            saveEveButton.setVisible(false);
            addEveButton.setVisible(true);
            
            if (eventsForDate == null) {
                eventsForDate = new ArrayList<>();
                dateEvents.put(selectedDate, eventsForDate);
            }
            
            eventsForDate.clear();
            eventsForDate.add(eventText);
            
            updateEventText();
            eventTextArea.setEditable(false);
        });
        
        // This is the "AI" that we will have in our calendar. It will read the amount of event on each day and tell you how many
        // events you have per day and for the next 7 days.
        aiAskButton.setOnAction(e -> {
            // The amount of events over 7 days.
            int totalEvents = 0;
            // Setting both buttons to false so the user can't accidently save data.
            // saveEveButton.setVisible(false);
            // addEveButton.setVisible(false);
            // String builder to store info to be used in eventTextArea.
            StringBuilder str = new StringBuilder();
            // Loop to find the day it is, increment 6 days in the future and display events for each day.
            for (int i = 0; i < 7; i++) {
                str.append(selectedDate.plusDays(i).getDayOfWeek() + "\n");
                List<String> eventsForDate = dateEvents.get(selectedDate.plusDays(i));
                
                if (eventsForDate != null) {
                    for (String event : eventsForDate) {
                        totalEvents += (event.split("\n").length);
                        str.append(event).append("\n");
                    }
                } else {
                    str.append("No events for scheduled.\n");
                }
                
                str.append("\n");
                
            }
            // Updating the TextArea to the "AI" output.
            eventTextArea.setText(str.toString() + "TOTAL EVENTS: "
                    + totalEvents);
            
            
        });
        

        eventLabel.getStyleClass().add("event-label");

        eventBox.getChildren().addAll(eventLabel, eventTextArea, addEveButton, saveEveButton, aiAskButton);

        return eventBox;
    }

    private void updateCalendar() {
        monthLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        String[] weekdayWeekend = {"Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat"};
        Calendar cal = Calendar.getInstance();
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        
        if (dayLabels != null) {
            calendarGrid.getChildren().removeAll(dayLabels);
            calendarGrid.getChildren().removeAll(dayNames);
        }

        dayLabels = new ArrayList<>();
        dayNames = new ArrayList<>();

        int daysInMonth = currentYearMonth.lengthOfMonth();
        LocalDate firstDay = currentYearMonth.atDay(1);

        for (int i = 0; i < weekdayWeekend.length; i++) {
            Label dayName = new Label(weekdayWeekend[i]);
            dayName.getStyleClass().add("day-name");
            dayName.setMinWidth(40);
            dayNames.add(dayName);
            calendarGrid.add(dayName, i, 0);
        }

        for (int i = 0; i < daysInMonth; i++) {
            Label dayLabel = new Label(String.valueOf(i + 1));
            dayLabel.getStyleClass().add("day-label");
            dayLabel.setMinWidth(50);

            dayLabels.add(dayLabel);

            final int day = i + 1;
            dayLabel.setOnMouseClicked(e -> {
                if (prevDate != 0) {
                    colorCode(prevDate, dayOfMonth);
                }
                selectedDate = LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonth(), day);
                dayLabel.setStyle("-fx-border-color: red;-fx-background-color: red; -fx-border-width: 1px;");
                prevDate = day;
                updateEventText();
            });

            int row = (i + firstDay.getDayOfWeek().getValue()) / 7 + 1;
            int col = (i + firstDay.getDayOfWeek().getValue()) % 7;
            calendarGrid.add(dayLabel, col, row);

            if(i+1 == dayOfMonth) {
            	dayLabel.setStyle("-fx-border-color: green; -fx-background-color: green; -fx-border-width: 1px;");
            } else {
            	dayLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
            }
        }

        updateEventText();
    }


    private void colorCode(int day, int dayOfMonth) {
        Label dayLabel = dayLabels.get(day - 1); 
        if(day == dayOfMonth) {
        	dayLabel.setStyle("-fx-border-color: green; -fx-background-color: green; -fx-border-width: 1px;");
        } else {
        	dayLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
        }
        
    }
    

    private void updateEventText() {
    	StringBuilder eventText = new StringBuilder();
        
        List<String> eventsForDate = dateEvents.get(selectedDate);
        if (eventsForDate != null) {
            for (String event : eventsForDate) {
                eventText.append(event).append("\n");
            }
        } else {
            eventText.append("No events for selected date.");
        }

        TextArea eventTextArea = (TextArea) ((VBox) ((BorderPane) calendarGrid.getParent()).getBottom()).getChildren().get(1);
        
        // Additional code for holidays
          PublicHolidays holiday = new PublicHolidays();
          String holidayVar = holiday.Holidays(selectedDate);
          if (holidayVar != null && !eventText.toString().contains(holidayVar)) {
              String publicHolidayText = "Public Holiday: " + holidayVar + "\n";
              eventText.insert(0, publicHolidayText);
          }
        // End additional code.
              
        eventTextArea.setText(eventText.toString());
    }
    
    private void saveEventsToFile() {
    	String username = LoginScreen.getUsername() + "Events.dat";
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(username))) {
            outputStream.writeObject(dateEvents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadEventsFromFile() {
    	String username = LoginScreen.getUsername() + "Events.dat";
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(username))) {
            dateEvents = (Map<LocalDate, List<String>>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
        		e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
