package app.page.insertRatingInFilmsEmbedded;

import app.App;
import app.page.Menu;
import app.page.Page;
import db.DB;
import org.bson.Document;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class InsertRatingInFilmsEmbedded extends Page {

    public InsertRatingInFilmsEmbedded(App app, Optional<DB> db){
        super(app, db);
    }

    protected void show(){
        String value = """
                Insertion nouveau film
                idUser idFilm note
                """;
        System.out.print(value);
    }

    protected void exec(String entry){
        List<String> param = Arrays.stream(entry.split(" ")).toList();
        if (param.size() != 3){
            System.out.println("Erreur nombre paramètres");
            app.menu = Menu.HOME;
            return;
        }

        String idUser = param.getFirst();
        String idFilm = param.get(1);
        int note = Integer.parseInt(param.get(2));
        long time = Instant.now().toEpochMilli();

        if (note < 0 || note > 5){
            System.out.println("Note invalide :  valeur valide [0, 5]");
            app.menu = Menu.HOME;
            return;
        }

        boolean res = db.get().insertRatingInFilmsEmbedded(idUser, idFilm, note, time);
        if (res){
            System.out.println("Insertion validé");
        }else{
            System.out.println("Insertion impossible");
        }
        System.out.println(" ");
        app.menu = Menu.HOME;
    }
}
