package db;

import org.bson.Document;
import java.util.List;

public interface DB {

    List<Document> getFilmsByGenre(List<String> genre);
    List<Document> getUserByJob(List<String> occupation);
    List<Document> getMostViewedFilmsByProfession(String profession);
    List<Document> getMostViewedFilmsByAgeRange(int age1, int age2);
    List<Document> getTop5RatedFilms(int minVotes);
    boolean insertRatingInFilmsEmbedded(String userId, String filmId, int rating, long timestamp);
    boolean userExists(Object userId);
    boolean hasUserAlreadyRatedFilm(String filmId, String userId);
}
