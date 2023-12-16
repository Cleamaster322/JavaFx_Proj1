package com.example.demo1;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class RecipeParser {

    public Recipe parse(String urlConnect) {
        try {
            Document doc = Jsoup.connect(urlConnect).get();

            // Создаем новый объект Recipe
            Recipe recipe = new Recipe();

            // Заполняем поля объекта Recipe
            // Название
            recipe.setName(doc.select("h1[itemprop=name]").first().text());

            // Главное фото
            recipe.setMainPhoto(doc.select("img[itemprop=image]").first().attr("src"));

            // Описание
            recipe.setDescription(doc.select("div.article-text[itemprop=description]").first().text());

            // Категории
            Elements categories = doc.select("span[itemprop=recipeCategory]");
            for (Element category : categories) {
                recipe.getCategories().add(category.text());
            }

            // Ингридиенты
            Elements ingredients = doc.select("li[itemprop=recipeIngredient]");
            for (Element ingredient : ingredients) {
                String text = ingredient.text();
                Pattern pattern = Pattern.compile("\\(.*?\\)");
                Matcher matcher = pattern.matcher(text);
                text = matcher.replaceAll("").trim();
                recipe.getIngredients().add(text);
            }

            // Время приготовления
            recipe.setCookingTime(doc.select("time[itemprop=totalTime]").first().text());

            //
            recipe.setCalories(doc.select("strong[itemprop=calories]").first().text());
            recipe.setProtein(doc.select("strong[itemprop=proteinContent]").first().text());
            recipe.setFat(doc.select("strong[itemprop=fatContent]").first().text());
            recipe.setCarbohydrates(doc.select("strong[itemprop=carbohydrateContent]").first().text());

            Elements steps = doc.select("ul[itemprop=recipeInstructions] li");
            for (Element step : steps) {
                Element pElement = step.select("div p").first();
                if (pElement != null) {
                    recipe.getCookingSteps().add(pElement.text());
                }
            }

            // Возвращаем объект Recipe
            return recipe;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
