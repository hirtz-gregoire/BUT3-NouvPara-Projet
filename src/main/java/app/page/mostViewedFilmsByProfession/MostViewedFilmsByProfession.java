package app.page.mostViewedFilmsByProfession;

import app.App;
import app.page.Menu;
import app.page.Page;
import db.DB;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MostViewedFilmsByProfession extends Page {

    public MostViewedFilmsByProfession(App app, Optional<DB> db){
        super(app, db);
    }

    protected void show(){
        String value = """
                Choisir la profession du filtre
                """;
        System.out.print(value);
    }

    protected void exec(String entry){
        List<Document> res = db.get().getMostViewedFilmsByProfession(entry);
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
