package com.example.demo1;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;

public class HelloController extends DataBaseHandler { //ожидает передачи интерфейса, фейкового класса. Но нужно получить объект, который наследуется у интерфейса и нужен класс, который управляет зависимостями у интерфейса.
    //паттерн depencity inject

    public void onAButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Блюда");

        try {
            GetAllData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        // Создание пустого макета
        GridPane root = new GridPane();
        root.setHgap(50);
        root.setVgap(50);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                Button button = new Button("Блюдо " + (index + 1));
                button.setPrefSize(150, 100);
                root.add(button, col, row);
            }
        }

        root.setAlignment(Pos.CENTER);
//        root.setHalignment(root, HPos.CENTER);

        // Создание сцены и установление размеров
        Scene categoryAScene = new Scene(root, 800, 600);
        categoryAStage.setScene(categoryAScene);

        // Отображение
        categoryAStage.show();
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

    public void onFavoritesButtonClick() {
        Stage favoritesStage = new Stage();
        favoritesStage.setTitle("Favourites");

        VBox root = new VBox();

        Scene favoritesScene = new Scene(root, 400, 300);
        favoritesStage.setScene(favoritesScene);

        favoritesStage.show();
    }




}