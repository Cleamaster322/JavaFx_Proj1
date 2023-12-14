package com.example.demo1;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


public class HelloController extends DataBaseHandler { //ожидает передачи интерфейса, фейкового класса. Но нужно получить объект, который наследуется у интерфейса и нужен класс, который управляет зависимостями у интерфейса.
    //паттерн depencity inject

    ObservableList<String> categories = FXCollections.observableArrayList("Первое", "Второе", "Компот");
    public void onAButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Блюда");

//        try {
//            GetAllData();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }

        VBox root = new VBox();
        root.setSpacing(30);

        ComboBox<String> categoryComboBox = new ComboBox<>(categories);
        categoryComboBox.setPromptText("Выбери категорию");
        root.getChildren().add(categoryComboBox);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(50);
        gridPane.setVgap(50);

        addButtonsToGrid(gridPane);

        gridPane.setAlignment(Pos.CENTER);

        categoryComboBox.setOnAction(e -> {
            String selectedCategory = categoryComboBox.getValue();
            updateGridPane(gridPane, selectedCategory);
        });

        root.getChildren().add(gridPane);


        Scene categoryAScene = new Scene(root, 800, 600);
        categoryAStage.setScene(categoryAScene);

        categoryAStage.show();
    }

    private void addButtonsToGrid(GridPane gridPane) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                Button button = new Button("Блюдо " + (index + 1));
                button.setPrefSize(150, 100);
                gridPane.add(button, col, row);
            }
        }
    }

    private void addCitiesToEmptyPane(Pane emptyPane, List<String> cities) {
        VBox cityContainer = new VBox();
        cityContainer.setSpacing(10);

        for (String city : cities) {
            Label cityLabel = new Label(city);
            cityContainer.getChildren().add(cityLabel);
        }

        emptyPane.getChildren().add(cityContainer);
    }

    private void addEmptyPaneToGrid(GridPane gridPane) {
        Pane emptyPane = new Pane();
        emptyPane.setStyle("-fx-background-color: lightgray;");
        emptyPane.setPrefSize(350, 200);

        gridPane.add(emptyPane, 0, 0, 3, 3);
    }


    private void updateGridPane(GridPane gridPane, String selectedCategory) {
        gridPane.getChildren().clear(); // Очищаем содержимое GridPane

        // Добавляем новые кнопки в зависимости от выбранной категории
        switch (selectedCategory) {
            case "Первое":
                addProductComboBox(gridPane);
                break;
            case "Второе":
                addEmptyPaneToGrid(gridPane);
                onCategoryNoneSelected(gridPane);
                break;
            case "Компот":
                addButtonsToGrid(gridPane);
                break;
            default:
                break;
        }
    }

    private void addProductComboBox(GridPane gridPane) {
        // Создаем новый ComboBox
        ComboBox<String> productComboBox = new ComboBox<>();

        // Создаем список продуктов
        ObservableList<String> products = FXCollections.observableArrayList(
                "Бекон",
                "Яйцо",
                "Картофель",
                "Творог",
                "Сыр",
                "Хлеб"
        );

        // Создаем FilteredList, который будет фильтровать продукты
        FilteredList<String> filteredProducts = new FilteredList<>(products, p -> true);

        // Устанавливаем FilteredList в качестве элементов ComboBox
        productComboBox.setItems(filteredProducts);

        // Устанавливаем ComboBox как редактируемый
        productComboBox.setEditable(true);

        // Добавляем слушатель к свойству textProperty ComboBox
        productComboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            // Если выбранный элемент равен текущему вводу, то не фильтруем список
            if (productComboBox.getSelectionModel().getSelectedItem() == null ||
                    !productComboBox.getSelectionModel().getSelectedItem().equals(productComboBox.getEditor().getText())) {
                // Фильтруем список продуктов
                filteredProducts.setPredicate(product -> product.toLowerCase().contains(newValue.toLowerCase()));
            }
        });

        // Добавляем ComboBox в GridPane
        gridPane.add(productComboBox, 0, 0);

        // Устанавливаем обработчик событий для ComboBox
        productComboBox.setOnAction(e -> {
            String selectedProduct = productComboBox.getValue();
            if (selectedProduct != null && !selectedProduct.isEmpty() && products.contains(selectedProduct)) {
                System.out.println("Выбран продукт: " + selectedProduct);
                Platform.runLater(() -> {
                    if (!productComboBox.getItems().isEmpty()) {
                        productComboBox.getSelectionModel().clearSelection(); // Снятие выбора
                    }
                });
            }
        });

    }



    public void onCategorySelected(){
        try {
            GetAllData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onCategoryNoneSelected(GridPane gridPane) { // Добавлен аргумент gridPane
        try {
            List<String> cities = GetNotAllData();

            addCitiesToEmptyPane((Pane) gridPane.getChildren().get(0), cities);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void onBButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Избранное");

        VBox root = new VBox();

        Scene categoryAScene = new Scene(root, 400, 300);
        categoryAStage.setScene(categoryAScene);

        categoryAStage.show();
    }

    public void onCButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Корзина");

        VBox root = new VBox();

        Scene categoryAScene = new Scene(root, 400, 300);
        categoryAStage.setScene(categoryAScene);

        categoryAStage.show();
    }
}