package com.example.demo1;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HelloController { //ожидает передачи интерфейса, фейкового класса. Но нужно получить объект, который наследуется у интерфейса и нужен класс, который управляет зависимостями у интерфейса.
    //паттерн depencity inject
    public void onAButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Category A");

        // Создание пустого макета
        VBox root = new VBox();

        // Создание сцены и установление размеров
        Scene categoryAScene = new Scene(root, 400, 300);
        categoryAStage.setScene(categoryAScene);

        // Отображение
        categoryAStage.show();
    }

    public void onBButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Category B");

        VBox root = new VBox();

        Scene categoryAScene = new Scene(root, 400, 300);
        categoryAStage.setScene(categoryAScene);

        categoryAStage.show();
    }

    public void onCButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Category C");

        VBox root = new VBox();

        Scene categoryAScene = new Scene(root, 400, 300);
        categoryAStage.setScene(categoryAScene);

        categoryAStage.show();
    }

    public void onFavoritesButtonClick() {
        Stage favoritesStage = new Stage();
        favoritesStage.setTitle("Favourites");

        VBox root = new VBox();

        Scene favoritesScene = new Scene(root, 400, 300);
        favoritesStage.setScene(favoritesScene);

        favoritesStage.show();
    }




}