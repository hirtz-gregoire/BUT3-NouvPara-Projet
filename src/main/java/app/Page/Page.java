package app.Page;

import db.DB;

import java.util.Scanner;

public abstract class Page {

    private static final String pointeur = "> ";

    private DB db;

    protected Page(DB db){
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
        System.out.println("");
        return entry;
    }
}
