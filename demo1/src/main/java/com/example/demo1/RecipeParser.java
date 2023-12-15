package com.example.demo1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class RecipeParser {
    public static void main(String[] args) {
        try {
            // Отправьте HTTP-запрос на URL рецепта
            Document doc = Jsoup.connect("https://www.povarenok.ru/recipes/show/47352/").get();

            // название
            Element name = doc.select("h1[itemprop=name]").first();
            //описание
            Element description = doc.select("div.article-text[itemprop=description]").first();
            //категория
            Elements categories = doc.select("span[itemprop=recipeCategory]");
            ArrayList<String> categoryList = new ArrayList<>();
            for (Element category : categories) {
                categoryList.add(category.text());
            }


            //вывод, удали меня
            System.out.println(name.text());
            System.out.println(description.text());
            for (String category : categoryList) {
                System.out.println(category);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
