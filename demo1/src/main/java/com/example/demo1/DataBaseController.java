package com.example.demo1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseController {
    public static void main(String[] args) {
        Connection connection = null;
        try {
            // Загружаем драйвер JDBC для SQLite
            Class.forName("org.sqlite.JDBC");
            // Устанавливаем соединение с базой данных
            String url = "jdbc:sqlite:/com/DataBase/5_Ocean.db";
            connection = DriverManager.getConnection(url);
            // Проверяем успешность подключения
            if (connection != null) {
                System.out.println("Подключение к базе данных SQLite установлено.");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Не удалось найти драйвер JDBC.");

        } catch (SQLException e) {
            System.out.println("Не удалось установить соединение с базой данных.");

        } finally {
            try {
                if (connection != null) {
                    // Закрываем соединение с базой данных
                    connection.close();
                    System.out.println("Подключение к базе данных SQLite закрыто.");
                }
            } catch (SQLException e) {
                System.out.println("Не удалось закрыть соединение с базой данных.");
            }
        }
    }
}