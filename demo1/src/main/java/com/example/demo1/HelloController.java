package com.example.demo1;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class HelloController extends DataBaseHandler { //ожидает передачи интерфейса, фейкового класса. Но нужно получить объект, который наследуется у интерфейса и нужен класс, который управляет зависимостями у интерфейса.
    //паттерн depencity inject


    ObservableList<String> categories = FXCollections.observableArrayList("Бульоны и супы", "Десерты", "Выпечка", "Горячие блюда");

    public void onAButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Блюда");

        VBox root = new VBox();
        root.setSpacing(30);

        ComboBox<String> categoryComboBox = new ComboBox<>(categories);
        categoryComboBox.setPromptText("Выбери категорию");
        root.getChildren().add(categoryComboBox);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(50);
        gridPane.setVgap(50);

//        addButtonsToGrid(gridPane);

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

                StackPane buttonContainer = new StackPane(); // Используем StackPane для перекрытия кнопок
                buttonContainer.setAlignment(Pos.TOP_RIGHT);

                Button heartButton = new Button("A");
                heartButton.setPrefSize(20, 20);

                heartButton.setUserData(button);

                buttonContainer.getChildren().addAll(button, heartButton);

                heartButton.setOnAction(e -> {
                    // Копируем основную кнопку в окно "Избранное"
                    Button selectedButton = (Button) heartButton.getUserData();
                    addFavoriteButtonAction(selectedButton);
                });

                gridPane.add(buttonContainer, col, row);
            }
        }
    }

    private void addFavoriteButtonAction(Button selectedButton) {
        Button clonedButton = new Button(selectedButton.getText());
        favoritesBox.getChildren().add(clonedButton);
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

        switch (selectedCategory) {
            case "Бульоны и супы":
                addProductComboBox(gridPane);
                break;
            case "Десерты":
                addEmptyPaneToGrid(gridPane);
                onCategoryNoneSelected(gridPane);
                break;
            case "Выпечка":
                addButtonsToGrid(gridPane);
                break;
            case "Горячие блюда":
                addProductButton(gridPane);
            default:
                break;
        }
    }

    private void addProductComboBox(GridPane gridPane) {
        ComboBox<String> productComboBox = new ComboBox<>();

        try {
            List<String> cities = GetNotAllData();
            ObservableList<String> cityList = FXCollections.observableArrayList(cities);

            FilteredList<String> filteredProducts = new FilteredList<>(cityList, p -> true);

            productComboBox.setItems(filteredProducts);


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

            gridPane.add(productComboBox, 0, 0);

            productComboBox.setOnAction(e -> {
                String selectedCity = productComboBox.getValue();
                if (selectedCity != null && !selectedCity.isEmpty() && cities.contains(selectedCity)) {
                    System.out.println("Выбран продукт: " + selectedCity);
                    Platform.runLater(() -> {
                        if (!productComboBox.getItems().isEmpty()) {
                            productComboBox.getSelectionModel().clearSelection();
                        }
                    });
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    private void addImageToButton(Button button) {

        RecipeParser recipeParser = new RecipeParser(); //удали меня
        Recipe recipe = recipeParser.parse("https://www.povarenok.ru/recipes/show/47352/");
        String imagePath = recipe.getMainPhoto();
        Image image = new Image(imagePath);

        ImageView imageView = new ImageView(image);

        imageView.setFitWidth(100);
        imageView.setFitHeight(100);

        button.setGraphic(imageView);

    }

    public void addProductButton(GridPane gridPane) {
        try {
            List<Recipe> recipes = getAllRecipe();
            ObservableList<Recipe> recipeList = FXCollections.observableArrayList(recipes);

            // Установка горизонтального интервала между кнопками
            gridPane.setHgap(20);

            for (int i = 0; i < recipeList.size(); i++) {
                Recipe recipe = recipeList.get(i);

                // Создание StackPane для наложения текста на изображение
                StackPane stackPane = new StackPane();

                // Создание изображения
                ImageView imageView = new ImageView(new Image(recipe.getMainPhoto()));
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                stackPane.getChildren().add(imageView);

                // Создание текста
                Label label = new Label();
                label.setText(recipe.getName());
                label.setFont(new Font("Arial", 14));
                label.setStyle("-fx-text-fill: white;");
                stackPane.getChildren().add(label);

                // Создание кнопки
                Button recipeButton = new Button();
                recipeButton.setGraphic(stackPane);
                recipeButton.setPrefSize(200, 200);

                // Добавление кнопки в GridPane
                int column = i % 3; // Определение столбца для кнопки
                int row = i / 3; // Определение строки для кнопки
                gridPane.add(recipeButton, column, row);

                // Обработчик событий для кнопки
                recipeButton.setOnAction(event -> {
                    // Создание нового окна
                    Stage newWindow = new Stage();
                    newWindow.setTitle(recipe.getName());

                    VBox root = new VBox();
                    root.setSpacing(10);
                    root.setPadding(new Insets(10));

                    Label nameRecipeTextLabel = new Label("Название: " + recipe.getName());
                    Label photoRecipeTextLabel = new Label("Главное фото: " + recipe.getMainPhoto());
                    Label descriptionRecipeTextLabel = new Label("Описание: " + recipe.getDescription());
                    Label categoryRecipeTextLabel = new Label("Категории: " + recipe.getCategories());
                    Label cookingTimeRecipeTextLabel = new Label("Время приготовления: " + recipe.getCookingTime());
                    Label caloriesRecipeTextLabel = new Label("Калории: " + recipe.getCalories());
                    Label proteinRecipeTextLabel = new Label("Белки: " + recipe.getProtein());
                    Label fatRecipeTextLabel = new Label("Жиры: " + recipe.getFat());
                    Label carbohydratesRecipeTextLabel = new Label("Углеводы: " + recipe.getCarbohydrates());
                    Label nameIngredientList = new Label("Список ингредиентов: " + recipe.getIngredients());

                    root.getChildren().addAll(nameRecipeTextLabel, photoRecipeTextLabel, descriptionRecipeTextLabel, categoryRecipeTextLabel);

                    Scene scene = new Scene(root, 800, 600);

                    newWindow.setScene(scene);

                    newWindow.show();
                });
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




    private VBox favoritesBox = new VBox();

    public void onBButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Избранное");

        VBox root = new VBox();

        Scene categoryAScene = new Scene(root, 400, 300);
        categoryAStage.setScene(categoryAScene);

        root.getChildren().add(favoritesBox);

        categoryAStage.show();
    }

    public void onCButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Корзина");

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);

        Button imageButton = new Button();
        imageButton.setPrefSize(200, 150);

        addImageToButton(imageButton);
        root.getChildren().add(imageButton);

        Scene categoryAScene = new Scene(root, 400, 300);
        categoryAStage.setScene(categoryAScene);

        categoryAStage.show();
    }

    public void onParser() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Парсер");

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        TextField urlTextField = new TextField();
        urlTextField.setPromptText("Введите URL рецепта");
        urlTextField.setPrefWidth(400);

        Button parseButton = new Button("Запустить парсер");

        parseButton.setOnAction(e -> {
            String url = urlTextField.getText();
            RecipeParser parser = new RecipeParser();
            Recipe recipe;

            if (url.isEmpty()) {
                urlTextField.setStyle("-fx-text-inner-color: red;");
                urlTextField.setText("Адрес не найден");
                return;
            } else {
                recipe = parser.parse(url);
            }

            if (recipe != null) {

                Label nameRecipeTextLabel = new Label("Название");
                TextField nameRecipeTextField = new TextField();
                nameRecipeTextField.setText(recipe.getName());


                Label photoRecipeTextLabel = new Label("Главное фото");
                TextField photoRecipeTextField = new TextField();
                photoRecipeTextField.setText(recipe.getMainPhoto());

                Label descriptionRecipeTextLabel = new Label("Описание");
                TextField descriptionRecipeTextField = new TextField();
                descriptionRecipeTextField.setText(recipe.getDescription());


                Label categoryRecipeTextLabel = new Label("Категории");
                TextField categoryRecipeTextField = new TextField();
                categoryRecipeTextField.setText(recipe.getCategories());


                Label cookingTimeRecipeTextLabel = new Label("Время приготовления");
                TextField cookingTimeRecipeTextField = new TextField();
                cookingTimeRecipeTextField.setText(recipe.getCookingTime());


                Label caloriesRecipeTextLabel = new Label("Калории");
                TextField caloriesRecipeTextField = new TextField();
                caloriesRecipeTextField.setText(recipe.getCalories());


                Label proteinRecipeTextLabel = new Label("Белки");
                TextField proteinRecipeTextField = new TextField();
                proteinRecipeTextField.setText(recipe.getProtein());


                Label fatRecipeTextLabel = new Label("Жиры");
                TextField fatRecipeTextField = new TextField();
                fatRecipeTextField.setText(recipe.getFat());


                Label carbohydratesRecipeTextLabel = new Label("Углеводы");
                TextField carbohydratesRecipeTextField = new TextField();
                carbohydratesRecipeTextField.setText(recipe.getCarbohydrates());

                Label nameIngredientList = new Label("Список ингредиентов");

                // Создание TextField для ввода нового ингредиента
                TextField newIngredientTextField = new TextField();

                // Создание ListView для отображения списка ингредиентов
                ListView<String> ingredientsList = new ListView<>();
                ingredientsList.getItems().addAll(recipe.getIngredients());

                // Создание кнопки для добавления новых ингредиентов
                Button addIngredientButton = new Button("+");

                addIngredientButton.setOnAction(event -> {
                    // Добавление текста из TextField в ListView
                    if(!newIngredientTextField.getText().isEmpty()){
                        ingredientsList.getItems().add(newIngredientTextField.getText());
                    }

                    // Очистка TextField
                    newIngredientTextField.clear();
                });

                Label nameCookingStepListText = new Label("Шаги приготовления");


                TextField newCookingStepTextField = new TextField();

                // Создание ListView для отображения списка текста шагов приготовления
                ListView<String> cookingStepListText = new ListView<>();
                cookingStepListText.getItems().addAll(recipe.getCookingStepsText());

                // Создание кнопки для добавления нового текста шагов приготовления
                Button addCookingStepTextButton = new Button("+");

                addCookingStepTextButton.setOnAction(event -> {
                    // Добавление текста из TextField в ListView
                    if(!newCookingStepTextField.getText().isEmpty()){
                        cookingStepListText.getItems().add(newCookingStepTextField.getText());
                    }
                    newCookingStepTextField.clear();
                });

                Label newCoookingStepListImg = new Label("Фото к шагу");

                // Создание TextField для ввода нового ингредиента
                TextField newCookingStepListImgField = new TextField();

                // Создание ListView для отображения списка ингредиентов
                ListView<String> cookingStepListImg = new ListView<>();
                cookingStepListImg.getItems().addAll(recipe.getCookingStepsImg());

                // Создание кнопки для добавления новых ингредиентов
                Button addCookingStepImgButton = new Button("+");

                addCookingStepImgButton.setOnAction(event -> {
                    // Добавление текста из TextField в ListView
                    if(!newCookingStepTextField.getText().isEmpty()){
                        cookingStepListImg.getItems().add(newCookingStepTextField.getText());
                    }

                    // Очистка TextField
                    newCookingStepListImgField.clear();
                });

                Button next = new Button("Далее");

                next.setPrefSize(50, 50);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setAlignment(Pos.CENTER);

                grid.add(nameRecipeTextLabel, 2, 2);
                grid.add(nameRecipeTextField, 2, 4);

                grid.add(photoRecipeTextLabel, 2, 8);
                grid.add(photoRecipeTextField, 2, 10);

                grid.add(descriptionRecipeTextLabel, 2, 14);
                grid.add(descriptionRecipeTextField, 2, 16);

                grid.add(categoryRecipeTextLabel, 2, 20);
                grid.add(categoryRecipeTextField, 2, 22);

                grid.add(cookingTimeRecipeTextLabel, 2, 26);
                grid.add(cookingTimeRecipeTextField, 2, 28);

                grid.add(caloriesRecipeTextLabel, 2, 32);
                grid.add(caloriesRecipeTextField, 2, 34);

                grid.add(proteinRecipeTextLabel, 2, 38);
                grid.add(proteinRecipeTextField, 2, 40);

                grid.add(fatRecipeTextLabel, 2, 44);
                grid.add(fatRecipeTextField, 2, 46);

                grid.add(carbohydratesRecipeTextLabel, 2, 50);
                grid.add(carbohydratesRecipeTextField, 2, 52);

                grid.add(nameIngredientList, 2, 54);

                grid.add(newIngredientTextField, 2, 56);
                grid.add(ingredientsList, 2, 57);
                grid.add(addIngredientButton, 3, 57);

                grid.add(nameCookingStepListText, 2, 59);

                grid.add(newCookingStepTextField, 2, 61);
                grid.add(cookingStepListText, 2, 62);
                grid.add(addCookingStepTextButton, 3, 62);

                grid.add(newCoookingStepListImg, 2, 64);

                grid.add(newCookingStepListImgField, 2, 66);
                grid.add(cookingStepListImg, 2, 67);
                grid.add(addCookingStepImgButton, 3, 67);

                grid.add(next, 2,70);


                ScrollPane scrollPane = new ScrollPane(grid);

                Scene scene = new Scene(scrollPane, 800, 600);
                categoryAStage.setScene(scene);
                categoryAStage.show();

                next.setOnAction(p -> {
                    recipe.setName(nameRecipeTextField.getText());
                    recipe.setMainPhoto(photoRecipeTextField.getText());
                    recipe.setDescription(descriptionRecipeTextField.getText());
                    recipe.setCategories(categoryRecipeTextField.getText());
                    recipe.setCookingTime(cookingTimeRecipeTextField.getText());
                    recipe.setCalories(caloriesRecipeTextField.getText());
                    recipe.setProtein(proteinRecipeTextField.getText());
                    recipe.setFat(fatRecipeTextField.getText());
                    recipe.setCarbohydrates(carbohydratesRecipeTextField.getText());
                    recipe.setIngredients(ingredientsList.getItems());
                    recipe.setCookingStepsText(cookingStepListText.getItems());
                    recipe.setCookingStepsImg(cookingStepListImg.getItems());

                    DataBaseHandler test = new DataBaseHandler();
                    test.createRecipeToDb(recipe);
                });


            } else {
                System.out.println("Не удалось получить информацию о рецепте.");
            }
        });

        root.getChildren().addAll(urlTextField, parseButton);
        Scene categoryAScene = new Scene(root, 800, 700);
        categoryAStage.setScene(categoryAScene);
        categoryAStage.show();
    }

}