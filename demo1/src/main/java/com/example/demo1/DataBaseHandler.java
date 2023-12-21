package com.example.demo1;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class DataBaseHandler {
    private Properties properties = new Properties();
    private Connection dbConnection;

//    Подключение к базе данных
    public Connection getDbConnection() throws SQLException {

    try (InputStream input = new FileInputStream("src/main/resources/sql.properties")) {
        properties.load(input);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

    dbConnection = DriverManager.getConnection(properties.getProperty("database.url"), properties.getProperty("database.login"), properties.getProperty("database.pass"));
    return dbConnection;
}

    public List<String> GetNotAllData() throws SQLException {
        List<String> cities = new ArrayList<>();

        try (Statement statement = getDbConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT Name FROM world.city WHERE id < 7;");

            while (resultSet.next()) {
                String city = resultSet.getString(1);
                cities.add(city);
            }
        }

        return cities;
    }

    public void createCategory(Recipe recipe) {
        //Проверка есть ли такая категория в бд
        try(PreparedStatement statement = getDbConnection().prepareStatement("SELECT * FROM category WHERE name LIKE ?")){
            statement.setString(1, "%" + recipe.getCategories() + "%");
            ResultSet resultSet = statement.executeQuery();
            //Если нет то дабавляем ее
            if(!resultSet.next()){
                PreparedStatement insertStatement = getDbConnection().prepareStatement("INSERT INTO category (name) VALUES (?)");
                insertStatement.setString(1, recipe.getCategories());
                insertStatement.executeUpdate();
            } else {
                System.out.println("Уже есть такая категория");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createProducts(Recipe recipe){

        for (String ingredient : recipe.getIngredients()) {
            String[] words = ingredient.split(" — ");
            //Проверка есть ли такой продукт в бд
            try (PreparedStatement statement = getDbConnection().prepareStatement("SELECT * FROM products WHERE name LIKE ?")) {
                statement.setString(1, "%" + words[0].trim() + "%");
                ResultSet resultSet = statement.executeQuery();
                //Если нет то дабавляем ее
                if (!resultSet.next()) {
                    PreparedStatement insertStatement = getDbConnection().prepareStatement("INSERT INTO products (name) VALUES (?)");
                    insertStatement.setString(1, words[0].trim());
                    insertStatement.executeUpdate();
                } else {
                    System.out.println("Уже есть такой продукт");
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void createFood(Recipe recipe){
        //Проверка есть ли такая еда в бд
        try (PreparedStatement statement = getDbConnection().prepareStatement("SELECT * FROM food WHERE name LIKE ?")) {
            statement.setString(1, "%" + recipe.getName() + "%");
            ResultSet resultSet = statement.executeQuery();
            //Если нет то дабавляем ее
            if (!resultSet.next()) {
                PreparedStatement insertStatement = getDbConnection().prepareStatement("INSERT INTO food (name,categoryID,imgFood,cookTime) " +
                        "VALUES (?,(SELECT id FROM category where category.name = ?)" +
                        ",?,?)");
                insertStatement.setString(1, recipe.getName());
                insertStatement.setString(2,recipe.getCategories());
                insertStatement.setString(3,recipe.getMainPhoto());
                insertStatement.setString(4,recipe.getCookingTime());
                insertStatement.executeUpdate();
            } else {
                System.out.println("Уже есть такое блюдо");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void createStructure(Recipe recipe){
        for (String ingredient : recipe.getIngredients()) {
            String[] words = ingredient.split(" — ");
            if (words.length == 1){
                words = Arrays.copyOf(words, words.length + 1);
                words[0] = words[0].trim();
                words[1] = "";
            }


            System.out.println(words[0] + "!!!!!!" + words[1]);
            try(PreparedStatement statement = getDbConnection().prepareStatement("SELECT * FROM structure " +
                                                                                "WHERE foodID = (SELECT food.id FROM food where name = ?) " +
                                                                                "AND productID = (SELECT products.id FROM products where name = ?)")){
                statement.setString(1, recipe.getName());
                statement.setString(2, words[0].trim());
                ResultSet resultSet = statement.executeQuery();
                if(!resultSet.next()) {
                    PreparedStatement insertStatement = getDbConnection().prepareStatement("INSERT INTO structure (foodID, productID, countMeasurement) " +
                                                                                            "VALUES ((SELECT food.id FROM food where name = ?)," +
                                                                                            "(SELECT products.id FROM products where name = ?)" +
                                                                                            ",?)");

                    insertStatement.setString(1, recipe.getName());
                    insertStatement.setString(2, words[0].trim());
                    insertStatement.setString(3, words[1]);
                    insertStatement.executeUpdate();
                } else {
                System.out.println("Уже есть такая структура");
            }


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void createCaloric(Recipe recipe){
        try(PreparedStatement statement = getDbConnection().prepareStatement("SELECT foodID FROM caloric WHERE foodID = (SELECT food.id FROM food where name = ?) ")){

            statement.setString(1, recipe.getName());
            ResultSet resultSet = statement.executeQuery();

            if(!resultSet.next()) {

                PreparedStatement insertStatement = getDbConnection().prepareStatement("INSERT INTO caloric (foodID, proteins, fats, carbohydrates, calories) " +
                                                                                            "VALUES((SELECT food.id FROM food where name = ?), ?, ?, ?, ?)");


                insertStatement.setString(1, recipe.getName());
                insertStatement.setFloat(2, Float.parseFloat(recipe.getProtein()));
                insertStatement.setFloat(3, Float.parseFloat(recipe.getFat()));
                insertStatement.setFloat(4, Float.parseFloat(recipe.getCarbohydrates()));
                insertStatement.setFloat(5, Float.parseFloat(recipe.getCalories()));
                insertStatement.executeUpdate();

            } else {
                System.out.println("Уже есть такое БЖУ");
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPreparationText(Recipe recipe) {
        int i = 1;
        for (String textRow : recipe.getCookingStepsText()){
            try (PreparedStatement statement = getDbConnection().prepareStatement("SELECT foodID FROM preparation " +
                                                                                    "WHERE foodID = (SELECT food.id FROM food where name = ?) " +
                                                                                    "AND step = ?")) {

                statement.setString(1, recipe.getName());
                statement.setInt(2,i);
                ResultSet resultSet = statement.executeQuery();

                if (!resultSet.next()) {

                    PreparedStatement insertStatement = getDbConnection().prepareStatement("INSERT INTO preparation (foodID, step, description,img) " +
                            "VALUES((SELECT food.id FROM food where name = ?), ?, ?, ?)");


                    insertStatement.setString(1, recipe.getName());
                    insertStatement.setInt(2, i);
                    insertStatement.setString(3,textRow);
                    insertStatement.setString(4,recipe.getCookingStepsImg().get(i-1));
                    insertStatement.executeUpdate();

                } else {
                    System.out.println("Уже есть такое Шагу блюда" + recipe.getName());
                }


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            i++;
        }
    }

    public void createRecipeToDb(Recipe recipe){

        try(Statement statement = getDbConnection().createStatement()) {
            createCategory(recipe);
            createProducts(recipe);
            createFood(recipe);
            createStructure(recipe);
            createCaloric(recipe);
            createPreparationText(recipe);



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    //получение всех id блюд
    public List<Integer> getFoodIds() throws  SQLException{
        Statement statement = getDbConnection().createStatement();
        List<Integer> ids = new ArrayList<>();
        ResultSet resultSet = statement.executeQuery("SELECT id FROM food");

        while (resultSet.next()) {
            ids.add(resultSet.getInt(1));
        }
        return ids;
    }
    //получение id блюд по определенной категории
    public List<Integer> getFoodIds(String category) throws SQLException {
        Statement statement = getDbConnection().createStatement();
        List<Integer> ids = new ArrayList<>();
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT id FROM food WHERE categoryID = (SELECT id from category WHere name = ?)");
        stmt.setString(1, category);
        ResultSet resultSet = stmt.executeQuery();

        while (resultSet.next()) {
            ids.add(resultSet.getInt(1));
        }
        return ids;
    }
    public List<Integer> getFavoriteFoodIds(String category) throws SQLException {
        Statement statement = getDbConnection().createStatement();
        List<Integer> ids = new ArrayList<>();
        ResultSet resultSet = statement.executeQuery("SELECT foodID FROM favorite");
        while (resultSet.next()) {
            ids.add(resultSet.getInt(1));
        }
        return ids;
    }
    public List<Integer> getBasketFoodIds() throws SQLException {
        Statement statement = getDbConnection().createStatement();
        List<Integer> ids = new ArrayList<>();
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT foodID FROM basket");
        ResultSet resultSet = stmt.executeQuery();

        while (resultSet.next()) {
            ids.add(resultSet.getInt(1));
        }
        return ids;
    }


    public String getFoodName(int id) throws SQLException {
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT name FROM food WHERE id = ?");
        stmt.setInt(1, id); // Задаем значение для первого (и единственного) параметра
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();
        return resultSet.getString(1);
    }
    public String getFoodMainPhoto(int id) throws SQLException {
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT imgFood FROM food WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();

        return resultSet.getString(1);
    }
    public String getFoodCategory(int id) throws SQLException{
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT name FROM category WHERE id = (SELECT categoryID FROM food WHERE id = ?)");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();

        return resultSet.getString(1);
    }
    public String getFoodCookTime(int id) throws SQLException{
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT cookTime FROM food WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();

        return resultSet.getString(1);
    }
    public String getFoodDescription(int id) throws SQLException{
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT mainDescription FROM food WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();

        return resultSet.getString(1);
    }

    public List<String> getFoodIngredients(int id) throws  SQLException{
        List<String> ingredients = new ArrayList<>();
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT products.name,structure.countMeasurement FROM java.structure " +
                                                                    "JOIN products ON java.structure.productID = products.id " +
                                                                    "WHERE java.structure.foodID = ?");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {

            ingredients.add(resultSet.getString(1)+ " - " +resultSet.getString(2));
        }

        return ingredients;
    }
    public String getFoodCalories(int id) throws SQLException{
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT calories FROM caloric WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();

        return resultSet.getString(1);
    }
    public String getFoodProtein(int id) throws SQLException{
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT proteins FROM caloric WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();

        return resultSet.getString(1);
    }
    public String getFoodFat(int id) throws SQLException{
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT fats FROM caloric WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();

        return resultSet.getString(1);
    }
    public String getFoodCarbohydrates(int id) throws SQLException{
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT Carbohydrates FROM caloric WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();

        return resultSet.getString(1);
    }
    public List<String> getFoodCookingStepsText(int id) throws  SQLException{
        List<String> FoodCookingStepsText = new ArrayList<>();
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT description FROM java.preparation WHERE foodID = ?");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            FoodCookingStepsText.add(resultSet.getString(1));
        }

        return FoodCookingStepsText;
    }    public List<String> getFoodCookingStepsImg(int id) throws  SQLException{
        List<String> FoodCookingStepsImg = new ArrayList<>();
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT preparation.img FROM java.preparation WHERE foodID = ?");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            FoodCookingStepsImg.add(resultSet.getString(1));
        }

        return FoodCookingStepsImg;
    }

    //Фабрика
    public Recipe createRecipe(int id) throws SQLException {
        Recipe recipe = new Recipe();
        recipe.setName(getFoodName(id));
        recipe.setMainPhoto(getFoodMainPhoto(id));
        recipe.setCategories(getFoodCategory(id));
        recipe.setCookingTime(getFoodCookTime(id));
        recipe.setDescription(getFoodDescription(id));
        recipe.setIngredients(getFoodIngredients(id));
        recipe.setCalories(getFoodCalories(id));
        recipe.setProtein(getFoodProtein(id));
        recipe.setFat(getFoodFat(id));
        recipe.setCarbohydrates(getFoodCarbohydrates(id));
        recipe.setCookingStepsText(getFoodCookingStepsText(id));
        recipe.setCookingStepsImg(getFoodCookingStepsImg(id));

        return recipe;
    }
    //Все рецепты для выбранной категории
    public List<Recipe> getAllRecipe() throws SQLException {
        List<Recipe> recipes = new ArrayList<>();
        List<Integer> ids = getFoodIds();

        for (Integer id: ids){
            Recipe recipe = createRecipe(id);
            recipes.add(recipe);
        }

        return recipes;
    }
    //Все рецепты по выбранной категории
    public List<Recipe> getCategoryRecipe(String category) throws SQLException {
        List<Recipe> recipes = new ArrayList<>();
        List<Integer> ids = getFoodIds(category);

        for (Integer id: ids){
            Recipe recipe = createRecipe(id);
            recipes.add(recipe);
        }

        return recipes;
    }
    public List<Recipe> getBasketRecipe() throws SQLException {
        List<Recipe> recipes = new ArrayList<>();
        List<Integer> ids = getBasketFoodIds();

        for (Integer id: ids){
            Recipe recipe = createRecipe(id);
            recipes.add(recipe);
        }

        return recipes;
    }

    public void addToFavorite(int id) throws SQLException {
        PreparedStatement insertStatement = getDbConnection().prepareStatement("INSERT INTO favorite (foodID) VALUES (?)");
        insertStatement.setInt(1,id);
        insertStatement.execute();

    }
    public void addToBasket(int id) throws SQLException {
        PreparedStatement insertStatement = getDbConnection().prepareStatement("INSERT INTO basket (foodID) VALUES (?)");
        insertStatement.setInt(1,id);
        insertStatement.execute();

    }
    public void removeFavorite(int id) throws SQLException {
        PreparedStatement deleteStatement = getDbConnection().prepareStatement("DELETE FROM favorite WHERE foodID = ?");
        deleteStatement.setInt(1, id);
        deleteStatement.executeUpdate();
    }
    public void removeBasket(int id) throws SQLException {
        PreparedStatement deleteStatement = getDbConnection().prepareStatement("DELETE FROM basket WHERE foodID = ?");
        deleteStatement.setInt(1, id);
        deleteStatement.executeUpdate();
    }

    public List<Integer> selectByFilteredProductIds(String product) throws SQLException {
        Statement statement = getDbConnection().createStatement();
        List<Integer> ids = new ArrayList<>();
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT foodID FROM structure WHERE productID = (SELECT id FROM products WHERE name = ?)");
        stmt.setString(1, product);
        ResultSet resultSet = stmt.executeQuery();

        while (resultSet.next()) {
            ids.add(resultSet.getInt(1));
        }
        return ids;
    }
    public List<Recipe> GetFilteredRecipes() throws SQLException {
        List<Recipe> recipes = new ArrayList<>();
        for (Integer id: selectByFilteredProductIds("Картофель")){
            Recipe recipe = createRecipe(id);
            recipes.add(recipe);
        }
        return recipes;
    }

    public static void sortRecipesByName(List<Recipe> recipes) {
        recipes.sort(new Comparator<Recipe>() {
            @Override
            public int compare(Recipe r1, Recipe r2) {
                return r1.getName().compareTo(r2.getName());
            }
        });
    }


    public static void main(String[] args) throws SQLException {
        DataBaseHandler d = new DataBaseHandler();
        List<Recipe> recipes = d.getCategoryRecipe("Бульоны и супы");


    }
    }
