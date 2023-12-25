package com.example.demo1;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.sql.SQLException;
import java.util.List;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;


public class HelloController extends DataBaseHandler {

    public void onAButtonClick() {
        ObservableList<String> categories = FXCollections.observableArrayList("Бульоны и супы", "Десерты", "Выпечка", "Горячие блюда");
        ObservableList<String> ingredients = FXCollections.observableArrayList("Мед", "Огурец", "Помидор");
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Блюда");

        VBox root = new VBox();
        root.setSpacing(30);

        ComboBox<String> categoryComboBox = new ComboBox<>(categories);
        categoryComboBox.setPromptText("Выбери категорию");
        ComboBox<String> ingredientComboBox = new ComboBox<>(ingredients);
        ingredientComboBox.setPromptText("Выбери ингредиент");


        Button sortByTimeButtonASC = new Button("Сортировка по времени по возрастанию");
        Button sortByTimeButtonDESC = new Button("Сортировка по времени по убыванию");

        root.getChildren().addAll(categoryComboBox, ingredientComboBox, sortByTimeButtonASC, sortByTimeButtonDESC);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(50);
        gridPane.setVgap(50);

        addProductButton(gridPane, null);


        gridPane.setAlignment(Pos.CENTER);

        categoryComboBox.setOnAction(e -> {
            String selectedCategory = categoryComboBox.getValue();
            List<Recipe> recipes = getRecipesByCategory(selectedCategory);
            updateGridPane(gridPane, recipes);
        });


        sortByTimeButtonASC.setOnAction(e -> {
            try {
                List<Recipe> recipes = getSortingByFoodTime("ASC");
                updateGridPane(gridPane, recipes);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        sortByTimeButtonDESC.setOnAction(e -> {
            try {
                List<Recipe> recipes = getSortingByFoodTime("DESC");
                updateGridPane(gridPane, recipes);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        ingredientComboBox.setOnAction(e -> {
            String selectedIngredient = ingredientComboBox.getValue();
            try {
                List<Recipe> recipes = getFilteredRecipes(selectedIngredient);
                updateGridPane(gridPane, recipes);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        root.getChildren().add(gridPane);


        Scene categoryAScene = new Scene(root, 800, 600);
        categoryAStage.setScene(categoryAScene);

        categoryAStage.show();
    }

    private void updateGridPane(GridPane gridPane, List<Recipe> recipes) {
        gridPane.getChildren().clear();

        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            Button recipeButton = null;
            try {
                recipeButton = createRecipeButton(recipe);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            gridPane.add(recipeButton, i % 3, i / 3);
        }
    }

    private Button createRecipeButton(Recipe recipe) throws SQLException {

        StackPane stackPane = new StackPane();

        ImageView imageView = new ImageView(new Image(recipe.getMainPhoto()));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        stackPane.getChildren().add(imageView);

        Label label = new Label();
        label.setText(recipe.getName());
        label.setFont(new Font("Arial", 14));
        label.setStyle("-fx-text-fill: white;");
        stackPane.getChildren().add(label);

        Button recipeButton = new Button();
        recipeButton.setGraphic(stackPane);
        recipeButton.setPrefSize(200, 200);


        recipeButton.setOnAction(event -> {
            // Создание нового окна
            Stage categoryRecipeStage = new Stage();
            categoryRecipeStage.setTitle(recipe.getName());

            VBox root = new VBox();
            root.setSpacing(10);
            root.setPadding(new Insets(10));

            GridPane gridPane = new GridPane();

            HBox photoRecipeBox = new HBox(); // Создаем горизонтальный контейнер для изображения и текста
            photoRecipeBox.setSpacing(10);

            ImageView imageViewRecipe = new ImageView(new Image(recipe.getMainPhoto()));
            imageViewRecipe.setFitWidth(200);
            imageViewRecipe.setFitHeight(200);

            // Создание кнопок "Добавить в избранное" и "Добавить в корзину"
            Button addToFavoritesButton = new Button();
            updateFavoriteButton(addToFavoritesButton, recipe.getName());

            addToFavoritesButton.setOnAction(a -> {
                try {
                    boolean isInFavorites = checkFavoriteFood(recipe.getName());
                    if (isInFavorites) {
                        // Удалить из избранного
                        removeFavorite(recipe.getName());
                        updateFavoriteButton(addToFavoritesButton, recipe.getName());
                    } else {
                        // Добавить в избранное
                        addToFavorite(recipe.getName());
                        updateFavoriteButton(addToFavoritesButton, recipe.getName());
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            Button addToCartButton = new Button("Добавить в корзину");
            updateCartButton(addToCartButton, recipe.getName());

            addToCartButton.setOnAction(e -> {
                try {
                    boolean isInBasket = checkBasketFood(recipe.getName());
                    if (isInBasket){
                        removeBasket(recipe.getName());
                        updateCartButton(addToCartButton, recipe.getName());
                    } else {
                        addToBasket(recipe.getName());
                        updateCartButton(addToCartButton, recipe.getName());
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });

            Button removeRecipe = new Button("Удалить рецепт");
            removeRecipe.setOnAction(b -> {
                try {
                    deleteFoodByName(recipe.getName());
                    gridPane.getChildren().remove(recipeButton);
                    categoryRecipeStage.hide();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            });


            VBox buttonsBox = new VBox(5); // Контейнер для кнопок
            buttonsBox.setAlignment(Pos.TOP_RIGHT);
            buttonsBox.getChildren().addAll(addToFavoritesButton, addToCartButton, removeRecipe);

            Label photoRecipeTextLabel = new Label("Главное фото:");

            photoRecipeBox.getChildren().addAll(photoRecipeTextLabel, imageViewRecipe);

            Text descriptionText = new Text(recipe.getDescription());
            descriptionText.setWrappingWidth(400); // Задаем ширину, чтобы текст переносился

            VBox descriptionBox = new VBox(); // Создаем контейнер для текста
            Label descriptionLabel1 = new Label("Описание: ");
            descriptionBox.getChildren().addAll(descriptionLabel1, descriptionText);

            // Получаем список ингредиентов
            List<String> ingredients = recipe.getIngredients();

            VBox ingredientsBox = new VBox();
            Label ingredientsLabel = new Label("Список ингредиентов:");

            ingredientsBox.getChildren().add(ingredientsLabel);

            for (String ingredient : ingredients) {
                Label ingredientLabel = new Label(ingredient);
                ingredientsBox.getChildren().add(ingredientLabel);
            }

            // Получаем списки шагов приготовления и соответствующих изображений
            List<String> cookingSteps = recipe.getCookingStepsText();
            List<String> cookingImages = recipe.getCookingStepsImg();

            VBox cookingStepsBox = new VBox();
            Label cookingStepsLabel = new Label("Шаги приготовления:");

            cookingStepsBox.getChildren().add(cookingStepsLabel);

            if (cookingSteps != null && cookingImages != null && cookingSteps.size() == cookingImages.size()) {
                // Проходимся по каждому шагу приготовления и добавляем их в VBox
                for (int j = 0; j < cookingSteps.size(); j++) {
                    String step = cookingSteps.get(j);
                    String stepImage = cookingImages.get(j);

                    Label stepLabel = new Label("Шаг " + (j + 1) + ": " + step);

                    ImageView stepImageView = new ImageView(new Image(stepImage));
                    stepImageView.setFitWidth(200);
                    stepImageView.setFitHeight(200);

                    VBox stepContent = new VBox();
                    stepContent.getChildren().addAll(stepLabel, stepImageView);
                    cookingStepsBox.getChildren().add(stepContent);
                }
            } else {
                // Если данные не доступны или несоответствуют, выводим сообщение об ошибке
                Label errorLabel = new Label("Недостаточно данных о шагах приготовления или изображениях");
                cookingStepsBox.getChildren().add(errorLabel);
            }

            Label nameRecipeTextLabel = new Label("Название: " + recipe.getName());
            Label categoryRecipeTextLabel = new Label("Категории: " + recipe.getCategories());
            Label cookingTimeRecipeTextLabel = new Label("Время приготовления: " + recipe.getCookingTime());
            Label caloriesRecipeTextLabel = new Label("Калории: " + recipe.getCalories() + " ккал");
            Label proteinRecipeTextLabel = new Label("Белки: " + recipe.getProtein() + " г");
            Label fatRecipeTextLabel = new Label("Жиры: " + recipe.getFat() + " г");
            Label carbohydratesRecipeTextLabel = new Label("Углеводы: " + recipe.getCarbohydrates() + " г");


            root.getChildren().addAll(addToFavoritesButton, addToCartButton, removeRecipe, nameRecipeTextLabel, photoRecipeBox, descriptionBox, categoryRecipeTextLabel, cookingTimeRecipeTextLabel, caloriesRecipeTextLabel, proteinRecipeTextLabel, fatRecipeTextLabel, carbohydratesRecipeTextLabel, ingredientsBox, cookingStepsBox);

            ScrollPane scrollPane = new ScrollPane(root);

            Scene scene = new Scene(scrollPane, 800, 600);
            categoryRecipeStage.setScene(scene);
            categoryRecipeStage.show();
        });

        return recipeButton;
    }



    private List<Recipe> getRecipesByCategory(String category) {
        List<Recipe> recipes = new ArrayList<>();
        try {
            recipes = getCategoryRecipe(category);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return recipes;
    }

    public void addProductButton(GridPane gridPane, String category) {
        try {
            List<Recipe> recipes;
            if (category == null || category.isEmpty()) {
                recipes = getAllRecipe();
                gridPane.getChildren().clear();
            } else if (category.equals("Избранное")) {
                recipes = getFavoriteRecipe();
            } else {
                recipes = getCategoryRecipe(category);
            }
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
                    Stage categoryRecipeStage = new Stage();
                    categoryRecipeStage.setTitle(recipe.getName());

                    VBox root = new VBox();
                    root.setSpacing(10);
                    root.setPadding(new Insets(10));

                    HBox photoRecipeBox = new HBox(); // Создаем горизонтальный контейнер для изображения и текста
                    photoRecipeBox.setSpacing(10);

                    ImageView imageViewRecipe = new ImageView(new Image(recipe.getMainPhoto()));
                    imageViewRecipe.setFitWidth(200);
                    imageViewRecipe.setFitHeight(200);

                    // Создание кнопок "Добавить в избранное" и "Добавить в корзину"
                    Button addToFavoritesButton = new Button();
                    updateFavoriteButton(addToFavoritesButton, recipe.getName());

                    addToFavoritesButton.setOnAction(a -> {
                        try {
                            boolean isInFavorites = checkFavoriteFood(recipe.getName());
                            if (isInFavorites) {
                                // Удалить из избранного
                                removeFavorite(recipe.getName());
                                updateFavoriteButton(addToFavoritesButton, recipe.getName());
                            } else {
                                // Добавить в избранное
                                addToFavorite(recipe.getName());
                                updateFavoriteButton(addToFavoritesButton, recipe.getName());
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    Button addToCartButton = new Button("Добавить в корзину");
                    updateCartButton(addToCartButton, recipe.getName());

                    addToCartButton.setOnAction(e -> {
                        try {
                            boolean isInBasket = checkBasketFood(recipe.getName());
                            if (isInBasket){
                                removeBasket(recipe.getName());
                                updateCartButton(addToCartButton, recipe.getName());
                            } else {
                                addToBasket(recipe.getName());
                                updateCartButton(addToCartButton, recipe.getName());
                            }
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    });

                    Button removeRecipe = new Button("Удалить рецепт");
                    removeRecipe.setOnAction(b -> {
                        try {
                            deleteFoodByName(recipe.getName());
                            gridPane.getChildren().remove(recipeButton);
                            categoryRecipeStage.hide();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    });


                    VBox buttonsBox = new VBox(5); // Контейнер для кнопок
                    buttonsBox.setAlignment(Pos.TOP_RIGHT);
                    buttonsBox.getChildren().addAll(addToFavoritesButton, addToCartButton, removeRecipe);

                    Label photoRecipeTextLabel = new Label("Главное фото:");

                    photoRecipeBox.getChildren().addAll(photoRecipeTextLabel, imageViewRecipe);

                    Text descriptionText = new Text(recipe.getDescription());
                    descriptionText.setWrappingWidth(400); // Задаем ширину, чтобы текст переносился

                    VBox descriptionBox = new VBox(); // Создаем контейнер для текста
                    Label descriptionLabel1 = new Label("Описание: ");
                    descriptionBox.getChildren().addAll(descriptionLabel1, descriptionText);

                    // Получаем список ингредиентов
                    List<String> ingredients = recipe.getIngredients();

                    VBox ingredientsBox = new VBox();
                    Label ingredientsLabel = new Label("Список ингредиентов:");

                    ingredientsBox.getChildren().add(ingredientsLabel);

                    for (String ingredient : ingredients) {
                        Label ingredientLabel = new Label(ingredient);
                        ingredientsBox.getChildren().add(ingredientLabel);
                    }

                    // Получаем списки шагов приготовления и соответствующих изображений
                    List<String> cookingSteps = recipe.getCookingStepsText();
                    List<String> cookingImages = recipe.getCookingStepsImg();

                    VBox cookingStepsBox = new VBox();
                    Label cookingStepsLabel = new Label("Шаги приготовления:");

                    cookingStepsBox.getChildren().add(cookingStepsLabel);

                    if (cookingSteps != null && cookingImages != null && cookingSteps.size() == cookingImages.size()) {
                        // Проходимся по каждому шагу приготовления и добавляем их в VBox
                        for (int j = 0; j < cookingSteps.size(); j++) {
                            String step = cookingSteps.get(j);
                            String stepImage = cookingImages.get(j);

                            Label stepLabel = new Label("Шаг " + (j + 1) + ": " + step);

                            ImageView stepImageView = new ImageView(new Image(stepImage));
                            stepImageView.setFitWidth(200);
                            stepImageView.setFitHeight(200);

                            VBox stepContent = new VBox();
                            stepContent.getChildren().addAll(stepLabel, stepImageView);
                            cookingStepsBox.getChildren().add(stepContent);
                        }
                    } else {
                        // Если данные не доступны или несоответствуют, выводим сообщение об ошибке
                        Label errorLabel = new Label("Недостаточно данных о шагах приготовления или изображениях");
                        cookingStepsBox.getChildren().add(errorLabel);
                    }

                    Label nameRecipeTextLabel = new Label("Название: " + recipe.getName());
                    Label categoryRecipeTextLabel = new Label("Категории: " + recipe.getCategories());
                    Label cookingTimeRecipeTextLabel = new Label("Время приготовления: " + recipe.getCookingTime());
                    Label caloriesRecipeTextLabel = new Label("Калории: " + recipe.getCalories() + " ккал");
                    Label proteinRecipeTextLabel = new Label("Белки: " + recipe.getProtein() + " г");
                    Label fatRecipeTextLabel = new Label("Жиры: " + recipe.getFat() + " г");
                    Label carbohydratesRecipeTextLabel = new Label("Углеводы: " + recipe.getCarbohydrates() + " г");


                    root.getChildren().addAll(addToFavoritesButton, addToCartButton, removeRecipe, nameRecipeTextLabel, photoRecipeBox, descriptionBox, categoryRecipeTextLabel, cookingTimeRecipeTextLabel, caloriesRecipeTextLabel, proteinRecipeTextLabel, fatRecipeTextLabel, carbohydratesRecipeTextLabel, ingredientsBox, cookingStepsBox);

                    ScrollPane scrollPane = new ScrollPane(root);

                    Scene scene = new Scene(scrollPane, 800, 600);
                    categoryRecipeStage.setScene(scene);
                    categoryRecipeStage.show();
                });
            }

            // остальной код неизменен
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void updateFavoriteButton(Button button, String recipeName) {
        try {
            boolean isInFavorites = checkFavoriteFood(recipeName);
            if (isInFavorites) {
                button.setText("Удалить из избранного");
            } else {
                button.setText("Добавить в избранное");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateCartButton (Button button, String recipeName) {
        try {
            boolean isInBasket = checkBasketFood(recipeName);
            if (isInBasket) {
                button.setText("Удалить из корзины");
            } else {
                button.setText("Добавить в корзину");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void showBasket(VBox root) {
        try {
            List<String> basketIngredients = getBasketIngredients();
            root.getChildren().clear(); // Очистка панели перед добавлением новых ингредиентов
            for (String ingredient : basketIngredients) {
                Label label = new Label(ingredient);
                root.getChildren().add(label);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Вывод информации об ошибке
        }
    }



    public void onBButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Избранное");

        VBox root = new VBox();
        root.setSpacing(30);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(50);
        gridPane.setVgap(50);

        addProductButton(gridPane, "Избранное");

        gridPane.setAlignment(Pos.CENTER);

        root.getChildren().addAll(gridPane);

        ScrollPane scrollPane = new ScrollPane(gridPane);

        Scene categoryAScene = new Scene(scrollPane, 800, 600);
        categoryAStage.setScene(categoryAScene);

        categoryAStage.show();
    }

    public void onCButtonClick() {
        Stage categoryAStage = new Stage();
        categoryAStage.setTitle("Корзина");

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);

        showBasket(root);

        Scene categoryAScene = new Scene(root, 600, 500);
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


    public void onCreateRecipe() {
        Stage createRecipeStage = new Stage();
        createRecipeStage.setTitle("Создание рецепта");

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        TextField nameRecipeTextField = new TextField();
        nameRecipeTextField.setPromptText("Введите название рецепта");
        nameRecipeTextField.setPrefWidth(400);

        TextField descriptionRecipeTextField = new TextField();
        descriptionRecipeTextField.setPromptText("Введите описание рецепта");
        descriptionRecipeTextField.setPrefWidth(400);

        TextField mainPhotoRecipeTextField = new TextField();
        mainPhotoRecipeTextField.setPromptText("Введите URL главного фото рецепта");
        mainPhotoRecipeTextField.setPrefWidth(400);

        TextField categoryRecipeTextField = new TextField();
        categoryRecipeTextField.setPromptText("Введите категорию рецепта");
        categoryRecipeTextField.setPrefWidth(400);

        TextField preparationTimeRecipeTextField = new TextField();
        preparationTimeRecipeTextField.setPromptText("Введите время приготовления рецепта");
        preparationTimeRecipeTextField.setPrefWidth(400);

        TextField caloriesRecipeTextField = new TextField();
        caloriesRecipeTextField.setPromptText("Введите количество калорий в рецепте");
        caloriesRecipeTextField.setPrefWidth(400);

        TextField proteinRecipeTextField = new TextField();
        proteinRecipeTextField.setPromptText("Введите количество белка в рецепте");
        proteinRecipeTextField.setPrefWidth(400);

        TextField fatRecipeTextField = new TextField();
        fatRecipeTextField.setPromptText("Введите количество жира в рецепте");
        fatRecipeTextField.setPrefWidth(400);

        TextField carbsRecipeTextField = new TextField();
        carbsRecipeTextField.setPromptText("Введите количество углеводов в рецепте");
        carbsRecipeTextField.setPrefWidth(400);

        Label nameIngredientList = new Label("Список ингредиентов");

        // Создание TextField для ввода нового ингредиента
        TextField newIngredientTextField = new TextField();

        // Создание ListView для отображения списка ингредиентов
        ListView<String> ingredientsList = new ListView<>();
        ingredientsList.getItems().addAll();

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
        cookingStepListText.getItems().addAll();

        // Создание кнопки для добавления нового текста шагов приготовления
        Button addCookingStepTextButton = new Button("+");

        addCookingStepTextButton.setOnAction(event -> {
            // Добавление текста из TextField в ListView
            if(!newCookingStepTextField.getText().isEmpty()){
                cookingStepListText.getItems().add(newCookingStepTextField.getText());
            }
            newCookingStepTextField.clear();
        });

        Label nameCookingStepListImg = new Label("Фото к шагу");

        TextField newCookingStepImgField = new TextField();

        // Создание ListView для отображения списка текста шагов приготовления
        ListView<String> cookingStepListImg = new ListView<>();
        cookingStepListImg.getItems().addAll();

        // Создание кнопки для добавления нового текста шагов приготовления
        Button addCookingStepImgButton = new Button("+");

        addCookingStepImgButton.setOnAction(event -> {
            // Добавление текста из TextField в ListView
            if(!newCookingStepImgField.getText().isEmpty()){
                cookingStepListImg.getItems().add(newCookingStepImgField.getText());
            }
            newCookingStepImgField.clear();
        });


        Button nextButton = new Button("Далее");

        nextButton.setOnAction(e -> {
            if (nameRecipeTextField.getText().trim().isEmpty() ||
                    descriptionRecipeTextField.getText().trim().isEmpty() ||
                    mainPhotoRecipeTextField.getText().trim().isEmpty() ||
                    categoryRecipeTextField.getText().trim().isEmpty() ||
                    preparationTimeRecipeTextField.getText().trim().isEmpty() ||
                    caloriesRecipeTextField.getText().trim().isEmpty() ||
                    proteinRecipeTextField.getText().trim().isEmpty() ||
                    fatRecipeTextField.getText().trim().isEmpty() ||
                    carbsRecipeTextField.getText().trim().isEmpty()) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setHeaderText("Missing Information");
                alert.setContentText("Please fill in all fields.");
                alert.showAndWait();
            } else {
                Recipe newRecipe = new Recipe();
                newRecipe.setName(nameRecipeTextField.getText());
                newRecipe.setDescription(descriptionRecipeTextField.getText());
                newRecipe.setMainPhoto(mainPhotoRecipeTextField.getText());
                newRecipe.setCategories(categoryRecipeTextField.getText());
                newRecipe.setCookingTime(preparationTimeRecipeTextField.getText());
                newRecipe.setCalories(caloriesRecipeTextField.getText());
                newRecipe.setProtein(proteinRecipeTextField.getText());
                newRecipe.setFat(fatRecipeTextField.getText());
                newRecipe.setCarbohydrates(carbsRecipeTextField.getText());
                newRecipe.setIngredients(ingredientsList.getItems());
                newRecipe.setCookingStepsText(cookingStepListText.getItems());
                newRecipe.setCookingStepsImg(cookingStepListImg.getItems());

                createRecipeToDb(newRecipe);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        grid.add(nameRecipeTextField, 2, 2);

        grid.add(mainPhotoRecipeTextField, 2, 8);

        grid.add(descriptionRecipeTextField, 2, 16);

        grid.add(categoryRecipeTextField, 2, 18);

        grid.add(preparationTimeRecipeTextField, 2, 20);

        grid.add(caloriesRecipeTextField, 2, 22);

        grid.add(proteinRecipeTextField, 2, 24);

        grid.add(fatRecipeTextField, 2, 26);

        grid.add(carbsRecipeTextField, 2, 28);

        grid.add(nameIngredientList, 2, 30);

        grid.add(newIngredientTextField, 2, 32);
        grid.add(ingredientsList, 2, 33);
        grid.add(addIngredientButton, 3, 33);

        grid.add(nameCookingStepListText, 2, 35);

        grid.add(newCookingStepTextField, 2, 37);
        grid.add(cookingStepListText, 2, 38);
        grid.add(addCookingStepTextButton, 3, 38);

        grid.add(nameCookingStepListImg, 2, 42);

        grid.add(newCookingStepImgField, 2, 44);
        grid.add(cookingStepListImg, 2, 45);
        grid.add(addCookingStepImgButton, 3, 45);

        grid.add(nextButton, 2, 48);

        ScrollPane scrollPane = new ScrollPane(grid);

        Scene scene = new Scene(scrollPane, 800, 600);
        createRecipeStage.setScene(scene);
        createRecipeStage.show();
    }
}