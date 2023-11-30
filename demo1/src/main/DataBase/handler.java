import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLiteDB implements DatabaseInterface {
    private Connection connection;

    public SQLiteDB() {
        // Инициализация подключения к базе данных SQLite
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:path/to/your/database.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createRecipe(Recipe recipe) {
        String sql = "INSERT INTO recipes (recipe_name, ingredients, instructions) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, recipe.getName());
            statement.setString(2, recipe.getIngredients());
            statement.setString(3, recipe.getInstructions());

            // Выполнение запроса на добавление рецепта
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Обработка исключений при работе с базой данных
        }
    }

    // Другие методы интерфейса DatabaseInterface

    public void closeConnection() {
        // Закрытие соединения с базой данных
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}