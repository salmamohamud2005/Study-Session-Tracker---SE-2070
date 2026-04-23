import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class StudyTrackerApp extends Application
{

    Stage stage;

    Label title;
    Label subjectLabel;
    Label goalLabel;
    Label timeLabel;
    Label messageLabel;
    Label totalLabel;
    Label statusLabel;
    Label warningLabel;
    Label performanceLabel;

    Button startButton;
    Button endButton;
    Button pauseButton;
    Button resumeButton;
    Button logsButton;
    Button resetButton;
    Button clearLogsButton;
    Button homeButton;
    Button exitButton;

    Timeline timer;

    int seconds = 0;
    int goalMinutes = 0;

    boolean running = false;
    boolean paused = false;

    String subject = "";

    double totalMinutes = 0;

    LocalDateTime startTime;
    LocalDateTime endTime;

    String fileName = "study_sessions.txt";

    public void start(Stage primaryStage)
    {
        stage = primaryStage;

        title = new Label("Study Session Timer");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        subjectLabel = new Label("Subject: None");
        subjectLabel.setStyle("-fx-font-size: 17px;");

        goalLabel = new Label("Goal: 0 minutes");
        goalLabel.setStyle("-fx-font-size: 17px;");

        timeLabel = new Label("Time: 00:00:00");
        timeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        statusLabel = new Label("Status: Home screen");
        statusLabel.setStyle("-fx-font-size: 15px;");

        warningLabel = new Label("Warning: None");
        warningLabel.setStyle("-fx-font-size: 15px;");

        performanceLabel = new Label("Performance: None");
        performanceLabel.setStyle("-fx-font-size: 15px;");

        messageLabel = new Label("Click start to begin.");
        messageLabel.setStyle("-fx-font-size: 15px;");

        totalLabel = new Label("Saved Total Minutes: 0.00");
        totalLabel.setStyle("-fx-font-size: 15px;");

        startButton = new Button("Start Study Session");
        startButton.setPrefWidth(220);
        startButton.setPrefHeight(40);

        endButton = new Button("End Study Session");
        endButton.setPrefWidth(220);
        endButton.setPrefHeight(40);
        endButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        endButton.setDisable(true);

        pauseButton = new Button("Pause Study Session");
        pauseButton.setPrefWidth(220);
        pauseButton.setPrefHeight(40);
        pauseButton.setDisable(true);

        resumeButton = new Button("Resume Study Session");
        resumeButton.setPrefWidth(220);
        resumeButton.setPrefHeight(40);
        resumeButton.setDisable(true);

        logsButton = new Button("Previous Study Logs");
        logsButton.setPrefWidth(220);
        logsButton.setPrefHeight(40);

        resetButton = new Button("Reset Current Session");
        resetButton.setPrefWidth(220);
        resetButton.setPrefHeight(40);

        clearLogsButton = new Button("Clear All Logs");
        clearLogsButton.setPrefWidth(220);
        clearLogsButton.setPrefHeight(40);

        homeButton = new Button("Go to Home Screen");
        homeButton.setPrefWidth(220);
        homeButton.setPrefHeight(40);

        exitButton = new Button("Exit App");
        exitButton.setPrefWidth(220);
        exitButton.setPrefHeight(40);

        startButton.setOnAction(e -> startSession());
        endButton.setOnAction(e -> endSession());
        pauseButton.setOnAction(e -> pauseSession());
        resumeButton.setOnAction(e -> resumeSession());
        logsButton.setOnAction(e -> showLogs());
        resetButton.setOnAction(e -> resetSession());
        clearLogsButton.setOnAction(e -> clearLogs());
        homeButton.setOnAction(e -> goHome());
        exitButton.setOnAction(e -> Platform.exit());

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        root.getChildren().addAll(
                title,
                subjectLabel,
                goalLabel,
                timeLabel,
                statusLabel,
                warningLabel,
                performanceLabel,
                startButton,
                endButton,
                pauseButton,
                resumeButton,
                logsButton,
                resetButton,
                clearLogsButton,
                homeButton,
                exitButton,
                messageLabel,
                totalLabel
        );

        Scene scene = new Scene(root, 500, 730);

        primaryStage.setTitle("Study Tracker App");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        updateTotalMinutes();
    }

    public void startSession()
    {
        if (running)
        {
            messageLabel.setText("A session is already running.");
            return;
        }

        TextInputDialog subjectBox = new TextInputDialog();
        subjectBox.initOwner(stage);
        subjectBox.setTitle("Subject");
        subjectBox.setHeaderText("New Study Session");
        subjectBox.setContentText("What subject will you be studying today?");
        subjectBox.getDialogPane().setPrefSize(400, 200);

        Optional<String> subjectAnswer = subjectBox.showAndWait();

        if (subjectAnswer.isEmpty())
        {
            messageLabel.setText("Session cancelled.");
            return;
        }

        String s = subjectAnswer.get().trim();

        if (s.equals(""))
        {
            messageLabel.setText("Enter a subject.");
            return;
        }

        TextInputDialog goalBox = new TextInputDialog();
        goalBox.initOwner(stage);
        goalBox.setTitle("Goal");
        goalBox.setHeaderText("Set Your Study Goal");
        goalBox.setContentText("How many minutes would you like to study for?");
        goalBox.getDialogPane().setPrefSize(400, 200);

        Optional<String> goalAnswer = goalBox.showAndWait();

        if (goalAnswer.isEmpty())
        {
            messageLabel.setText("Session cancelled.");
            return;
        }

        int g;

        try
        {
            g = Integer.parseInt(goalAnswer.get().trim());

            if (g <= 0)
            {
                messageLabel.setText("Goal must be more than 0.");
                return;
            }
        }
        catch (Exception e)
        {
            messageLabel.setText("Enter a number for the goal.");
            return;
        }

        subject = s;
        goalMinutes = g;
        seconds = 0;
        running = true;
        paused = false;
        startTime = LocalDateTime.now();
        endTime = null;

        subjectLabel.setText("Subject: " + subject);
        goalLabel.setText("Goal: " + goalMinutes + " minutes");
        timeLabel.setText("Time: 00:00:00");
        statusLabel.setText("Status: Session running");
        warningLabel.setText("Warning: None");
        performanceLabel.setText("Performance: Low");
        messageLabel.setText("Session started.");

        startButton.setStyle("-fx-background-color: lightgreen;");
        endButton.setDisable(false);
        pauseButton.setDisable(false);
        resumeButton.setDisable(true);
        logsButton.setDisable(true);

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    public void updateTimer()
    {
        seconds++;

        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;

        timeLabel.setText(String.format("Time: %02d:%02d:%02d", h, m, s));

        checkWarning();
        checkPerformance();
    }

    public void pauseSession()
    {
        if (!running)
        {
            messageLabel.setText("No session is running.");
            return;
        }

        if (paused)
        {
            messageLabel.setText("Session is already paused.");
            return;
        }

        timer.pause();
        paused = true;

        statusLabel.setText("Status: Session paused");
        messageLabel.setText("Session paused.");
        pauseButton.setDisable(true);
        resumeButton.setDisable(false);
        startButton.setStyle("-fx-background-color: khaki;");
    }

    public void resumeSession()
    {
        if (!running)
        {
            messageLabel.setText("No session is running.");
            return;
        }

        if (!paused)
        {
            messageLabel.setText("Session is already running.");
            return;
        }

        timer.play();
        paused = false;

        statusLabel.setText("Status: Session running");
        messageLabel.setText("Session resumed.");
        pauseButton.setDisable(false);
        resumeButton.setDisable(true);
        startButton.setStyle("-fx-background-color: lightgreen;");
    }

    public void endSession()
    {
        if (!running)
        {
            messageLabel.setText("No session is running.");
            return;
        }

        if (timer != null)
        {
            timer.stop();
        }

        running = false;
        paused = false;
        endTime = LocalDateTime.now();

        double minutesStudied = seconds / 60.0;
        boolean metGoal = minutesStudied >= goalMinutes;

        saveSession(subject, minutesStudied, goalMinutes, metGoal, startTime, endTime);
        updateTotalMinutes();

        startButton.setStyle("");
        endButton.setDisable(true);
        pauseButton.setDisable(true);
        resumeButton.setDisable(true);
        logsButton.setDisable(false);

        statusLabel.setText("Status: Session ended");
        checkWarning();
        checkPerformance();

        String text;

        if (metGoal)
        {
            text = String.format(
                    "You studied %s for %.2f minutes.\nYou met your goal of %d minutes.",
                    subject, minutesStudied, goalMinutes
            );
            messageLabel.setText(String.format("You studied %.2f minutes and met your goal.", minutesStudied));
        }
        else
        {
            text = String.format(
                    "You studied %s for %.2f minutes.\nYou did not meet your goal of %d minutes.",
                    subject, minutesStudied, goalMinutes
            );
            messageLabel.setText(String.format("You studied %.2f minutes and did not meet your goal.", minutesStudied));
        }

        Alert box = new Alert(Alert.AlertType.NONE);
        box.initOwner(stage);
        box.setTitle("Session Ended");
        box.setHeaderText(null);
        box.setContentText(text + "\n\nYour data was saved.");
        box.getDialogPane().setPrefSize(430, 220);

        ButtonType homeChoice = new ButtonType("Go Home");
        ButtonType stayChoice = new ButtonType("Stay Here");
        box.getButtonTypes().setAll(homeChoice, stayChoice);

        Optional<ButtonType> answer = box.showAndWait();

        if (answer.isPresent() && answer.get() == homeChoice)
        {
            goHome();
        }
    }

    public void goHome()
    {
        if (timer != null)
        {
            timer.stop();
        }

        running = false;
        paused = false;
        seconds = 0;
        goalMinutes = 0;
        subject = "";
        startTime = null;
        endTime = null;

        subjectLabel.setText("Subject: None");
        goalLabel.setText("Goal: 0 minutes");
        timeLabel.setText("Time: 00:00:00");
        statusLabel.setText("Status: Home screen");
        warningLabel.setText("Warning: None");
        performanceLabel.setText("Performance: None");
        messageLabel.setText("Back on home screen.");

        startButton.setStyle("");
        endButton.setDisable(true);
        pauseButton.setDisable(true);
        resumeButton.setDisable(true);
        logsButton.setDisable(false);

        updateTotalMinutes();
    }

    public void resetSession()
    {
        if (timer != null)
        {
            timer.stop();
        }

        running = false;
        paused = false;
        seconds = 0;
        goalMinutes = 0;
        subject = "";
        startTime = null;
        endTime = null;

        subjectLabel.setText("Subject: None");
        goalLabel.setText("Goal: 0 minutes");
        timeLabel.setText("Time: 00:00:00");
        statusLabel.setText("Status: Session reset");
        warningLabel.setText("Warning: None");
        performanceLabel.setText("Performance: None");
        messageLabel.setText("Current session reset.");

        startButton.setStyle("");
        endButton.setDisable(true);
        pauseButton.setDisable(true);
        resumeButton.setDisable(true);
        logsButton.setDisable(false);
    }

    public void clearLogs()
    {
        Alert box = new Alert(Alert.AlertType.CONFIRMATION);
        box.initOwner(stage);
        box.setTitle("Clear Logs");
        box.setHeaderText("Delete all saved logs?");
        box.setContentText("This will remove all saved study sessions.");

        Optional<ButtonType> answer = box.showAndWait();

        if (answer.isPresent() && answer.get() == ButtonType.OK)
        {
            try
            {
                PrintWriter out = new PrintWriter(new FileWriter(fileName));
                out.close();
                totalMinutes = 0;
                totalLabel.setText("Saved Total Minutes: 0.00");
                messageLabel.setText("All logs cleared.");
            }
            catch (Exception e)
            {
                messageLabel.setText("Could not clear logs.");
            }
        }
        else
        {
            messageLabel.setText("Clear logs cancelled.");
        }
    }

    public void showLogs()
    {
        StringBuilder allLogs = new StringBuilder();

        try
        {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = in.readLine()) != null)
            {
                allLogs.append(line).append("\n");
            }

            in.close();

            if (allLogs.length() == 0)
            {
                allLogs.append("No previous study logs found.");
            }
        }
        catch (Exception e)
        {
            allLogs.append("No previous study logs found.");
        }

        TextArea area = new TextArea();
        area.setText(allLogs.toString());
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefWidth(500);
        area.setPrefHeight(350);

        Alert box = new Alert(Alert.AlertType.NONE);
        box.initOwner(stage);
        box.setTitle("Previous Study Logs");
        box.setHeaderText(null);
        box.getDialogPane().setContent(area);
        box.getDialogPane().setPrefSize(550, 430);
        box.getButtonTypes().setAll(ButtonType.OK);
        box.showAndWait();
    }

    public void checkWarning()
    {
        double mins = seconds / 60.0;

        if (running && mins >= 90)
        {
            warningLabel.setText("Warning: long study session");
        }
        else
        {
            warningLabel.setText("Warning: None");
        }
    }

    public void checkPerformance()
    {
        double mins = seconds / 60.0;

        if (!running && seconds == 0)
        {
            performanceLabel.setText("Performance: None");
        }
        else if (mins < 20)
        {
            performanceLabel.setText("Performance: Low");
        }
        else if (mins < 60)
        {
            performanceLabel.setText("Performance: Medium");
        }
        else
        {
            performanceLabel.setText("Performance: High");
        }
    }

    public void saveSession(String sub, double mins, int goal, boolean met,
                            LocalDateTime start, LocalDateTime end)
    {
        DateTimeFormatter form = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss a");

        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(fileName, true));
            out.println("Subject: " + sub);
            out.println("Started: " + start.format(form));
            out.println("Ended: " + end.format(form));
            out.printf("Minutes Studied: %.2f%n", mins);
            out.println("Goal Minutes: " + goal);
            out.println("Completed Goal: " + met);
            out.println("-------------------");
            out.close();
        }
        catch (Exception e)
        {
            messageLabel.setText("Could not save data.");
        }
    }

    public void updateTotalMinutes()
    {
        double total = 0;

        try
        {
            File f = new File(fileName);

            if (!f.exists())
            {
                totalLabel.setText("Saved Total Minutes: 0.00");
                return;
            }

            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String line;

            while ((line = in.readLine()) != null)
            {
                if (line.startsWith("Minutes Studied: "))
                {
                    String part = line.substring(17).trim();
                    total = total + Double.parseDouble(part);
                }
            }

            in.close();
        }
        catch (Exception e)
        {
            total = 0;
        }

        totalMinutes = total;
        totalLabel.setText(String.format("Saved Total Minutes: %.2f", totalMinutes));
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}