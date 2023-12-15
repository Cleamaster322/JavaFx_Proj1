package com.example.demo1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;

public class RecipeParser {
    public static void main(String[] args) {
        try {
            // Отправьте HTTP-запрос на URL рецепта
            Document doc = Jsoup.connect("https://www.povarenok.ru/recipes/show/47352/").get();

            // Извлечение информации о рецепте
            String recipeTitle = doc.select("meta[itemprop=name]").first().text();
            System.out.println(content.attr("content"));
            // Вывод информации о рецепте
            System.out.println("Title: " + recipeTitle);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
