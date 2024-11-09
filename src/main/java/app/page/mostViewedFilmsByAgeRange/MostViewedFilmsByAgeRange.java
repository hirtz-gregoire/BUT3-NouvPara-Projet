package app.page.mostViewedFilmsByAgeRange;

import app.App;
import app.page.Menu;
import app.page.Page;
import db.DB;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MostViewedFilmsByAgeRange extends Page {

    public MostViewedFilmsByAgeRange(App app, Optional<DB> db){
        super(app, db);
    }

    protected void show(){
        String value = """
                Définir les bordes de la catégorie d'age voulue
                ex: ag1 ag2
                (2 ages separé par un espace)
                """;
        System.out.print(value);
    }

    protected void exec(String entry){
        List<Integer> ages = Arrays.stream(entry.split(" ")).map(v -> Integer.parseInt(v)).toList();
        System.out.println(ages);
        List<Document> res = db.get().getMostViewedFilmsByAgeRange(ages.get(0), ages.get(1));
        System.out.printf("Resultats: %d\n", res.size());
        System.out.print("Voulez vous afficher les résultats? (a/y/n)\n");
        entry = this.requestUserEntry().toLowerCase();
        if (entry.equals("a") || entry.equals("all")){
            for (Document doc : res){
                System.out.printf("%s\n", doc.toJson());
            }
            System.out.println(" ");
        } else if (entry.equals("y") || entry.equals("yes")) {
            int count = 0;
            for (Document doc : res){
                System.out.printf("%s\n", doc.toJson());
                count++;
                if (count >= 10){
                    System.out.printf("\nPour continue appuyer sur enter, sinon taper exit\n");
                    entry = this.requestUserEntry().toLowerCase();
                    if (entry.equals("exit")){
                        break;
                    }else{
                        count = 0;
                    }

                }
            }
            System.out.println(" ");
        }
        app.menu = Menu.HOME;
    }
}
