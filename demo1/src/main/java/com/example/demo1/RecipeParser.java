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
            try {
                recipe.setName(doc.select("h1[itemprop=name]").first().text());
            } catch (Exception e) {
                recipe.setName("");
            }

            // Главное фото
            try {
                recipe.setMainPhoto(doc.select("img[itemprop=image]").first().attr("src"));
            } catch (Exception e) {
                recipe.setMainPhoto("");
            }

            // Описание
            try {
                recipe.setDescription(doc.select("div.article-text[itemprop=description]").first().text());
            } catch (Exception e) {
                recipe.setDescription("");
            }

            // Категории
            Elements categories = doc.select("span[itemprop=recipeCategory]");
            for (Element category : categories) {
                try {
                    recipe.getCategories().add(category.text());
                } catch (Exception e) {
                    recipe.getCategories().add("");
                }
            }

            // Ингридиенты
            Elements ingredients = doc.select("li[itemprop=recipeIngredient]");
            for (Element ingredient : ingredients) {
                try {
                    String text = ingredient.text();
                    Pattern pattern = Pattern.compile("\\(.*?\\)");
                    Matcher matcher = pattern.matcher(text);
                    text = matcher.replaceAll("").trim();
                    recipe.getIngredients().add(text);
                } catch (Exception e) {
                    recipe.getIngredients().add("");
                }
            }

            // Время приготовления
            try {
                recipe.setCookingTime(doc.select("time[itemprop=totalTime]").first().text());
            } catch (Exception e) {
                recipe.setCookingTime("");
            }

            // Калории
            try {
                recipe.setCalories(doc.select("strong[itemprop=calories]").first().text());
            } catch (Exception e) {
                recipe.setCalories("");
            }

            // Белки
            try {
                recipe.setProtein(doc.select("strong[itemprop=proteinContent]").first().text());
            } catch (Exception e) {
                recipe.setProtein("");
            }

            // Жиры
            try {
                recipe.setFat(doc.select("strong[itemprop=fatContent]").first().text());
            } catch (Exception e) {
                recipe.setFat("");
            }

            // Углеводы
            try {
                recipe.setCarbohydrates(doc.select("strong[itemprop=carbohydrateContent]").first().text());
            } catch (Exception e) {
                recipe.setCarbohydrates("");
            }

            Elements steps = doc.select("ul[itemprop=recipeInstructions] li");
            for (Element step : steps) {
                Element pElement = step.select("div p").first();
                if (pElement != null) {
                    try {
                        recipe.getCookingSteps().add(pElement.text());
                    } catch (Exception e) {
                        recipe.getCookingSteps().add("");
                    }
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
