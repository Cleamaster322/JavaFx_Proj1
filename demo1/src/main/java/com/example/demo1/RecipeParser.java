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
            Element name = doc.select("h1[itemprop=name]").first();
            Element description = doc.select("h1[itemprop=description]").first();
            System.out.println(name.text());
            System.out.println(description.text());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
