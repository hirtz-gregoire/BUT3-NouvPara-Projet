package app.page.home;

import app.App;
import app.page.Menu;
import app.page.Page;
import db.DB;
import java.util.Optional;

public class Home extends Page {

    public Home(App app, Optional<DB> db){
        super(app, db);
    }

    protected void show(){
        String value = """
                Accueil
                1. Films par genres
                2. Utilisateurs par professions
                3. Films les plus vus/notés pour une profession donnée
                4. Films les plus vus/notés par un catégorie d'âge
                5. Top5 films notés
                6. Insertion nouvelle note pour un film
                0. Exit
                """;
        System.out.print(value);
    }

    protected void exec(String entry){
        switch (entry){
            case "0" -> app.setFinished();
            case "1" -> app.menu = Menu.MOVIE_BY_GENRE;
            case "2" -> app.menu = Menu.USER_BY_JOB;
            case "3" -> app.menu = Menu.MOST_VIEWED_FILMS_BY_PROFESSION;
            case "4" -> app.menu = Menu.MOST_VIEWED_FILMS_BY_AGE_RANGE;
            case "5" -> app.menu = Menu.TOP5_RATED_FILMS;
            case "6" -> app.menu = Menu.INSERT_RATING_IN_FILMS_EMBEDDED;
            default -> System.out.println("ERROR");
        }
    }
}
