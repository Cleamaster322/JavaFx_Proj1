package com.example.demo1;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

            //ингридиенты
            Elements ingredients = doc.select("li[itemprop=recipeIngredient]");
            ArrayList<String> ingredientList = new ArrayList<>();
            for (Element ingredient : ingredients) {
                String text = ingredient.text();
                Pattern pattern = Pattern.compile("\\(.*?\\)");
                Matcher matcher = pattern.matcher(text);
                text = matcher.replaceAll("").trim();
                ingredientList.add(text);
            }

            //время приготовления
            Element timeElement = doc.select("time[itemprop=totalTime]").first();
            String totalTime = timeElement.text();

            //бжу
            String calories = doc.select("strong[itemprop=calories]").first().text();
            String protein = doc.select("strong[itemprop=proteinContent]").first().text();
            String fat = doc.select("strong[itemprop=fatContent]").first().text();
            String carbohydrates = doc.select("strong[itemprop=carbohydrateContent]").first().text();

            //шаги приготовления
            Elements steps = doc.select("li[itemprop=recipeInstructions]");
            ArrayList<String> stepList = new ArrayList<>();
            for (Element step : steps) {
                String text = step.select("p").first().text();
                stepList.add(text);
            }

            //вывод, удали меня
            System.out.println(name.text());
            System.out.println(description.text());
            for (String category : categoryList) {
                System.out.println(category);
            }
            for (String ingredient : ingredientList) {
                System.out.println(ingredient);
            }
            System.out.println("Время приготовления: " + totalTime);
            System.out.println("Калории: " + calories);
            System.out.println("Белки: " + protein);
            System.out.println("Жиры: " + fat);
            System.out.println("Углеводы: " + carbohydrates);
            for (String step : stepList) {
                System.out.println(step);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
