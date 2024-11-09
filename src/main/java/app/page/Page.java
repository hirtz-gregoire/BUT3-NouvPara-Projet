package app.page;

import app.App;
import db.DB;

import java.util.Optional;
import java.util.Scanner;

public abstract class Page {

    private static final String pointeur = "> ";

    protected App app;
    protected Optional<DB> db;

    protected Page(App app, Optional<DB> db){
        this.app = app;
        this.db = db;
    }

    public void run(){
        show();
        String entry = requestUserEntry();
        exec(entry);
    }

    protected abstract void show();
    protected abstract void exec(String entry);

    protected String requestUserEntry(){
        Scanner scanner = new Scanner(System.in);
        System.out.print(pointeur);
        String entry = scanner.nextLine();
        System.out.println(" ");
        return entry;
    }
}
