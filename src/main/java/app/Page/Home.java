package app.Page;

import db.DB;

public class Home extends Page{

    public Home(DB db){
        super(db);
    }

    protected void show(){
        String value = """
                Page d'Accueil
                1.
                2.
                3.
                4.
                """;
        System.out.print(value);
    }

    protected void exec(String entry){

    }
}
