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
            if (words[1].contains(" - ")){
                words[1] = words[1].replaceAll("(\\d+)\\s+-\\s+(\\d+)", "$1");
            }
            String[] measurement = words[1].split(" ", 2);


            if (measurement.length == 1){
                measurement = Arrays.copyOf(measurement, measurement.length + 1);
                measurement[0] = measurement[0].trim();
                measurement[1] = "По вкусу";
            }

//            System.out.println("1"+ words[0]+" 2"+ Integer.parseInt(measurement[0].trim()) + " 3" +measurement[1]);


            try(PreparedStatement statement = getDbConnection().prepareStatement("SELECT * FROM structure " +
                    "WHERE foodID = (SELECT food.id FROM food where name = ?) " +
                    "AND productID = (SELECT products.id FROM products where name = ?)")){
                statement.setString(1, recipe.getName());
                statement.setString(2, words[0].trim());
                ResultSet resultSet = statement.executeQuery();
                if(!resultSet.next()) {
                    PreparedStatement insertStatement = getDbConnection().prepareStatement("INSERT INTO structure (foodID, productID, countMeasurement, measurement) " +
                            "VALUES ((SELECT food.id FROM food where name = ?)," +
                            "(SELECT products.id FROM products where name = ?)," +
                            " ?, ?)");

                    insertStatement.setString(1, recipe.getName());
                    insertStatement.setString(2, words[0].trim());
                    if (measurement[0].isEmpty()) {
                        insertStatement.setNull(3,Types.INTEGER);
                    } else{
                        insertStatement.setFloat(3, Float.parseFloat(measurement[0].trim().replace(",",".")));
                    }
                    insertStatement.setString(4,measurement[1].trim());
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
    public List<Integer> getFavoriteFoodIds() throws SQLException {
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
        ResultSet resultSet = statement.executeQuery("SELECT foodID FROM basket");
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
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT products.name,structure.countMeasurement, structure.measurement  FROM java.structure " +
                "JOIN products ON java.structure.productID = products.id " +
                "WHERE java.structure.foodID = ?");
        stmt.setInt(1, id);
        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {

            ingredients.add(resultSet.getString(1)+ " - " +resultSet.getString(2) + " " + resultSet.getString(3));
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
    public List<Recipe> getFavoriteRecipe() throws  SQLException{
        List<Recipe> recipes = new ArrayList<>();
        List<Integer> ids = getFavoriteFoodIds();
        for (Integer id: ids){
            Recipe recipe = createRecipe(id);
            recipes.add(recipe);
        }

        return recipes;
    }
    public List<String> getBasketIngredients() throws SQLException {
        List<Integer> ids = getBasketFoodIds();
        StringBuilder builder = new StringBuilder();


        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("?");
        }

        String placeholders = builder.toString();
        String query = "SELECT products.name, productID, SUM(CountMeasurement) as TotalCountMesurement, structure.measurement " +
                "FROM structure " +
                "JOIN products on structure.productID = products.id " +
                "WHERE foodID IN (" + placeholders + ") " +
                "GROUP BY products.name, productID, structure.measurement";

        PreparedStatement pstmt = dbConnection.prepareStatement(query);
        int index = 1;
        for (Integer foodID : ids) {
            pstmt.setInt(index++, foodID);
        }
        ResultSet resultSet = pstmt.executeQuery();
        List<String> result = new ArrayList<>();
        while (resultSet.next()) {
            String measurement = resultSet.getString(3);
            if (measurement == null){
                measurement = "";
            }else {
                measurement =resultSet.getString(3);
            }
            result.add(resultSet.getString(1) + " - " + measurement +" " + resultSet.getString(4));
        }
        return result;
    }
    public boolean checkFavoriteFood(String name) throws  SQLException{
        PreparedStatement insertStatement = getDbConnection().prepareStatement("SELECT * FROM favorite WHERE favorite.foodID = (select id from food WHERE food.name = ?)");
        insertStatement.setString(1,name);
        ResultSet resultSet = insertStatement.executeQuery();

        return resultSet.next();

    }
    public boolean checkBasketFood(String name) throws  SQLException{
        PreparedStatement insertStatement = getDbConnection().prepareStatement("SELECT * FROM basket WHERE basket.foodID = (select id from food WHERE food.name = ?)");
        insertStatement.setString(1,name);
        ResultSet resultSet = insertStatement.executeQuery();

        return resultSet.next();

    }

    public void addToFavorite(String name) throws SQLException {
        PreparedStatement insertStatement = getDbConnection().prepareStatement("INSERT INTO favorite (foodID) SELECT food.id FROM food WHERE name = ?");
        insertStatement.setString(1,name);
        insertStatement.execute();

    }
    public void addToBasket(String name) throws SQLException {
        PreparedStatement insertStatement = getDbConnection().prepareStatement("INSERT INTO basket (foodID) (SELECT food.id FROM food where name = ?)");
        insertStatement.setString(1,name);
        insertStatement.execute();

    }
    public void removeFavorite(String name) throws SQLException {
        PreparedStatement deleteStatement = getDbConnection().prepareStatement("DELETE FROM favorite WHERE foodID = (SELECT food.id FROM food where name = ?)");
        deleteStatement.setString(1,name);
        deleteStatement.executeUpdate();
    }
    public void removeBasket(String name) throws SQLException {
        PreparedStatement deleteStatement = getDbConnection().prepareStatement("DELETE FROM basket WHERE foodID = (SELECT food.id FROM food where name = ?)");
        deleteStatement.setString(1,name);
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

    //Удаление в каждой таблице по foodID
    public void deleteFoodFromFavorite(Integer id) throws SQLException {
        Statement statement = getDbConnection().createStatement();
        PreparedStatement stmt = dbConnection.prepareStatement("DELETE FROM favorite WHERE foodID = ?");
        stmt.setInt(1,id);
        stmt.executeUpdate();
    }
    public void deleteFoodFromBasket(Integer id) throws SQLException {
        Statement statement = getDbConnection().createStatement();
        PreparedStatement stmt = dbConnection.prepareStatement("DELETE FROM basket WHERE foodID = ?");
        stmt.setInt(1,id);
        stmt.executeUpdate();
    }
    public void deleteFoodFromCaloric(Integer id) throws SQLException {
        Statement statement = getDbConnection().createStatement();
        PreparedStatement stmt = dbConnection.prepareStatement("DELETE FROM caloric WHERE foodID = ?");
        stmt.setInt(1,id);
        stmt.executeUpdate();
    }
    public void deleteFoodFromStructure(Integer id) throws SQLException {
        Statement statement = getDbConnection().createStatement();
        PreparedStatement stmt = dbConnection.prepareStatement("DELETE FROM structure WHERE foodID = ?");
        stmt.setInt(1,id);
        stmt.executeUpdate();
    }
    public void deleteFoodFromPreparetion(Integer id) throws SQLException {
        Statement statement = getDbConnection().createStatement();
        PreparedStatement stmt = dbConnection.prepareStatement("DELETE FROM preparation WHERE foodID = ?");
        stmt.setInt(1,id);
        stmt.executeUpdate();
    }
    public void deleteFoodFromFood(Integer id) throws SQLException {
        Statement statement = getDbConnection().createStatement();
        PreparedStatement stmt = dbConnection.prepareStatement("DELETE FROM food WHERE id = ?");
        stmt.setInt(1,id);
        stmt.executeUpdate();
    }

    //Удаляет все что связано с выбранной ядой
    public void deleteFoodByName(String name) throws  SQLException{
        Statement statement = getDbConnection().createStatement();
        //получаем id блюда которое нужно удалить
        PreparedStatement stmt = dbConnection.prepareStatement("SELECT id FROM food WHERE name = ?");
        stmt.setString(1,name);
        ResultSet resultSet = stmt.executeQuery();
        resultSet.next();
        Integer id = resultSet.getInt(1);
        //удаляем из всех таблиц все что связано с нужным блюдом
        deleteFoodFromFavorite(id);
        deleteFoodFromBasket(id);
        deleteFoodFromCaloric(id);
        deleteFoodFromStructure(id);
        deleteFoodFromPreparetion(id);
        deleteFoodFromFood(id);



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
        List<Integer> foodIDs = Arrays.asList(2, 3); // Ваш список foodID
        List<Recipe> recipes = d.getAllRecipe();

//        for (Recipe recipe: recipes){
//            System.out.println(recipe.getIngredients());
//        }
//
//        for (String ingr: d.getBasketIngredients()){
//            System.out.println(ingr);
//        }

    }
}
