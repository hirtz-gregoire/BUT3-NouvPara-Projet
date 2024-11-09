package app;

import app.page.home.Home;
import app.page.Menu;
import app.page.insertRatingInFilmsEmbedded.InsertRatingInFilmsEmbedded;
import app.page.mostViewedFilmsByAgeRange.MostViewedFilmsByAgeRange;
import app.page.mostViewedFilmsByProfession.MostViewedFilmsByProfession;
import app.page.movieByGenre.MovieByGenre;
import app.page.top5RatedFilms.Top5RatedFilms;
import app.page.userByJob.UserByJob;
import db.DB;
import db.MongoDB;

import java.util.Optional;

public class App {

    private Optional<DB> db = Optional.of(new MongoDB());
    public Menu menu = Menu.HOME;

    private boolean isFinished = false;


    public boolean check() {
        return true;
    }

    public void run() {
        while (!isFinished) {
            switch (menu) {
                case HOME -> new Home(this, db).run();
                case MOVIE_BY_GENRE -> new MovieByGenre(this, db).run();
                case USER_BY_JOB -> new UserByJob(this, db).run();
                case MOST_VIEWED_FILMS_BY_PROFESSION -> new MostViewedFilmsByProfession(this, db).run();
                case MOST_VIEWED_FILMS_BY_AGE_RANGE -> new MostViewedFilmsByAgeRange(this, db).run();
                case TOP5_RATED_FILMS -> new Top5RatedFilms(this, db).run();
                case INSERT_RATING_IN_FILMS_EMBEDDED -> new InsertRatingInFilmsEmbedded(this, db).run();
            }
            System.out.println("============================");
        }
    }

    public void setFinished(){
        isFinished = true;
    }
}
