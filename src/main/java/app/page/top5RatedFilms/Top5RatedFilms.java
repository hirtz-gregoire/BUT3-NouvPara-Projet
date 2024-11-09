package app.page.top5RatedFilms;

import app.App;
import app.page.Menu;
import app.page.Page;
import db.DB;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Top5RatedFilms extends Page {

    public Top5RatedFilms(App app, Optional<DB> db){
        super(app, db);
    }

    protected void show(){
        String value = """
                Choisir le nombre minimal de vote pour etre pris en compte
                """;
        System.out.print(value);
    }

    protected void exec(String entry){
        List<Document> res = db.get().getTop5RatedFilms(Integer.parseInt(entry));
        for (Document doc : res){
            System.out.printf("%s\n", doc.toJson());
        }
        System.out.println(" e");
        app.menu = Menu.HOME;
    }
}
