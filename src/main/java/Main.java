import app.App;

public class Main {

    public static void main(String[] args) {
        App app = new App();
        if (app.check()){
            app.run();
        }
    }
}