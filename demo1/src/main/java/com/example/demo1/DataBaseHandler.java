package com.example.demo1;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
public class DataBaseHandler {
    Properties properties = new Properties();
    Connection dbConnection;
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

    public void GetAllData() throws SQLException {

        Statement statement = getDbConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT Name FROM world.city WHERE id < 10;");

        while (resultSet.next()){
            System.out.println(resultSet.getString(1));
        }
    }

}