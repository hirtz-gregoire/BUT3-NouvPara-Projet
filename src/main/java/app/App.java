package app;

import app.Page.Home;
import app.Page.Menu;
import db.DB;

public class App {

    private boolean isFinished = false;
    private Menu menu = Menu.HOME;

    private DB db;

    public boolean check(){
        return true;
    }

    public void run(){
        while(!isFinished){
            switch (menu){
                case HOME -> new Home(db).run();
            }
        }
    }


}
