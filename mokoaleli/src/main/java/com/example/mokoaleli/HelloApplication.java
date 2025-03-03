package com.example.mokoaleli;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class HelloApplication extends Application {
    private File selectedFile;
    private File[] currentFiles;
    private int currentIndex;
    private VBox mainContainer;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);

        showMainMenu();

        Scene scene = new Scene(mainContainer, 800, 600);
        primaryStage.setTitle("Fashion Gallery");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showMainMenu() {
        mainContainer.getChildren().clear();

        Label title = new Label("Fashion Gallery");

        HBox buttonContainer = new HBox(20);
        buttonContainer.setAlignment(Pos.CENTER);

        Button menButton = new Button("Men");
        Button womenButton = new Button("Women");
        Button kidsButton = new Button("Kids");

        menButton.setOnAction(e -> loadGallery("men"));
        womenButton.setOnAction(e -> loadGallery("women"));
        kidsButton.setOnAction(e -> loadGallery("kids"));

        buttonContainer.getChildren().addAll(menButton, womenButton, kidsButton);
        mainContainer.getChildren().addAll(title, buttonContainer);
    }

    private void loadGallery(String category) {
        mainContainer.getChildren().clear();
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);

        File folder = new File("C:/Users/Administrator/IdeaProjects/demo2/target/classes/images/" +
                (category.equals("women") ? "ladies" : category));
        if (!folder.exists() || !folder.isDirectory()) {
            showError("Category folder does not exist.");
            return;
        }

        currentFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
        if (currentFiles == null || currentFiles.length == 0) {
            showError("No images found in this category.");
            return;
        }
        Arrays.sort(currentFiles);

        int col = 0, row = 0;
        for (File file : currentFiles) {
            try {
                ImageView imageView = createThumbnail(file);
                imageView.setOnMouseClicked(e -> showFullImage(file, category));
                grid.add(imageView, col, row);
                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            } catch (IOException e) {
                showError("Error loading images.");
            }
        }

        Button backButton = new Button("Main Menu");
        backButton.setOnAction(e -> showMainMenu());

        mainContainer.getChildren().addAll(scrollPane, backButton);
    }

    private void showFullImage(File file, String category) {
        mainContainer.getChildren().clear();
        selectedFile = file;

        try {
            ImageView imageView = new ImageView(new Image(new FileInputStream(file)));
            imageView.setFitWidth(400);
            imageView.setFitHeight(400);
            imageView.setPreserveRatio(true);

            Button prevButton = new Button("Previous");
            Button nextButton = new Button("Next");
            Button backButton = new Button("Back");
            Button downloadButton = new Button("Download");

            prevButton.setOnAction(e -> navigateImage(-1, category));
            nextButton.setOnAction(e -> navigateImage(1, category));
            backButton.setOnAction(e -> loadGallery(category));
            downloadButton.setOnAction(e -> downloadImage());

            HBox navBox = new HBox(10, prevButton, nextButton, backButton, downloadButton);
            navBox.setAlignment(Pos.CENTER);

            mainContainer.getChildren().addAll(imageView, navBox);
        } catch (IOException e) {
            showError("Error displaying image.");
        }
    }

    private void navigateImage(int step, String category) {
        int newIndex = currentIndex + step;
        if (newIndex >= 0 && newIndex < currentFiles.length) {
            currentIndex = newIndex;
            showFullImage(currentFiles[currentIndex], category);
        }
    }

    private void downloadImage() {
        if (selectedFile != null) {
            File downloadDir = new File("downloads");
            if (!downloadDir.exists()) {
                downloadDir.mkdir();
            }
            File dest = new File(downloadDir, selectedFile.getName());
            try {
                Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showInfo("Download Complete", "Image saved to: " + dest.getAbsolutePath());
            } catch (IOException e) {
                showError("Failed to download image");
            }
        }
    }

    private ImageView createThumbnail(File file) throws IOException {
        ImageView imageView = new ImageView(new Image(new FileInputStream(file)));
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
