package db;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.*;

import java.util.ArrayList;
import java.util.List;

public class MongoDB implements DB{

    private final String uri = "mongodb://localhost:27017";
    private final String databaseName = "info";

    private MongoDatabase mgDatabase;


    public MongoDB() {
        this.mgDatabase = MongoClients.create(uri).getDatabase(databaseName);
    }

    @Override
    public List<Document> getFilmsByGenre(List<String> genres) {
        MongoCollection<Document> collection = mgDatabase.getCollection("films_embedded");
        List<Document> films = collection.find(Filters.in("genres", genres))
                .projection(Projections.include("title", "date"))
                .sort(Sorts.descending("date"))
                .into(new ArrayList<>());
        return films;
    }

    @Override
    public List<Document> getUserByJob(List<String> occupations) {
        MongoCollection<Document> collection = mgDatabase.getCollection("films_embedded");

        // Pipeline d'agrégation pour obtenir une liste unique d'utilisateurs avec une occupation spécifique
        List<Document> users = collection.aggregate(List.of(
                Aggregates.unwind("$users"),  // Décompose le tableau `users`
                Aggregates.match(Filters.in("users.occupation", occupations)),  // Filtre par occupation
                Aggregates.group(
                        new Document("name", "$users.name").append("age", "$users.age"), // Groupe par `name` et `age` pour éviter les doublons
                        Accumulators.first("name", "$users.name"),
                        Accumulators.first("age", "$users.age")
                ),
                Aggregates.project(Projections.fields(
                        Projections.include("name", "age"),
                        Projections.excludeId()
                ))
        )).into(new ArrayList<>());

        return users;
    }

    @Override
    public List<Document> getMostViewedFilmsByProfession(String profession) {
        MongoCollection<Document> collection = mgDatabase.getCollection("films_embedded");

        // Pipeline d'agrégation pour obtenir les films les plus vus par une profession spécifique
        List<Document> films = collection.aggregate(List.of(
                Aggregates.unwind("$users"),  // Décompose le tableau `users`
                Aggregates.match(Filters.eq("users.occupation", profession)),  // Filtre par profession
                Aggregates.group(
                        new Document("title", "$title")
                                .append("date", "$date")
                                .append("genres", "$genres"), // Grouper par titre, date et genres
                        Accumulators.sum("viewCount", 1) // Compte les occurrences
                ),
                // Projete les champs depuis `_id` pour qu'ils apparaissent directement dans le résultat
                Aggregates.project(Projections.fields(
                        Projections.computed("title", "$_id.title"),
                        Projections.computed("date", "$_id.date"),
                        Projections.computed("genres", "$_id.genres"),
                        Projections.include("viewCount"),
                        Projections.excludeId()
                )),
                Aggregates.sort(Sorts.descending("viewCount")) // Trie par le nombre de vues (décroissant)
        )).into(new ArrayList<>());

        return films;
    }

    @Override
    public List<Document> getMostViewedFilmsByAgeRange(int age1, int age2) {
        MongoCollection<Document> collection = mgDatabase.getCollection("films_embedded");

        // Pipeline d'agrégation pour obtenir les films les plus vus par une tranche d'âge spécifique
        List<Document> films = collection.aggregate(List.of(
                Aggregates.unwind("$users"),  // Décompose le tableau `users`
                Aggregates.match(Filters.and(
                        Filters.gte("users.age", age1), // Filtre pour `age >= age1`
                        Filters.lte("users.age", age2)  // Filtre pour `age <= age2`
                )),
                Aggregates.group(
                        new Document("title", "$title")
                                .append("date", "$date")
                                .append("genres", "$genres"), // Grouper par titre, date et genres
                        Accumulators.sum("viewCount", 1) // Compte les occurrences
                ),
                Aggregates.project(Projections.fields(
                        Projections.computed("title", "$_id.title"),
                        Projections.computed("date", "$_id.date"),
                        Projections.computed("genres", "$_id.genres"),
                        Projections.include("viewCount"),
                        Projections.excludeId()
                )),
                Aggregates.sort(Sorts.descending("viewCount")) // Trie par le nombre de vues (décroissant)
        )).into(new ArrayList<>());

        return films;
    }

    @Override
    public List<Document> getTop5RatedFilms(int minVotes) {
        MongoCollection<Document> collection = mgDatabase.getCollection("films_embedded");

        // Pipeline d'agrégation pour obtenir le Top 5 des films notés avec un nombre minimum de votes
        List<Document> films = collection.aggregate(List.of(
                Aggregates.unwind("$users"),  // Décompose le tableau `users`
                Aggregates.group(
                        new Document("title", "$title")
                                .append("date", "$date")
                                .append("genres", "$genres"), // Grouper par titre, date et genres
                        Accumulators.avg("averageRating", "$users.rating"), // Calcule la note moyenne
                        Accumulators.sum("userCount", 1) // Compte le nombre d'utilisateurs ayant noté
                ),
                Aggregates.match(Filters.gte("userCount", minVotes)), // Filtre par nombre minimum de votes
                Aggregates.sort(Sorts.descending("averageRating")), // Trie par note moyenne (décroissant)
                Aggregates.limit(5), // Limite aux 5 meilleurs films
                Aggregates.project(Projections.fields(
                        Projections.computed("title", "$_id.title"),
                        Projections.computed("date", "$_id.date"),
                        Projections.computed("genres", "$_id.genres"),
                        Projections.include("averageRating", "userCount"),
                        Projections.excludeId()
                ))
        )).into(new ArrayList<>());

        return films;
    }

    @Override
    public boolean insertRatingInFilmsEmbedded(String userId, String filmId, int rating, long timestamp) {
        MongoCollection<Document> filmsEmbeddedCollection = mgDatabase.getCollection("films_embedded");

        // Convertit l'ID du film en entier si nécessaire
        Object filmIdValue;
        try {
            filmIdValue = Integer.parseInt(filmId);
        } catch (NumberFormatException e) {
            filmIdValue = filmId;
        }

        // Vérifie si le film existe dans `films_embedded`
        Document film = filmsEmbeddedCollection.find(Filters.eq("_id", filmIdValue)).first();
        if (film == null) {
            System.out.println("Film non trouvé dans films_embedded. Insertion annulée.");
            return false;
        }

        // Si la vérification de l'utilisateur est temporairement supprimée, continuez directement
        // au contrôle de notation par l'utilisateur pour ce film

        // Utilise une variable temporaire pour gérer la conversion de `userId`
        Object tempUserIdValue;
        try {
            tempUserIdValue = Integer.parseInt(userId); // Essaie de convertir en entier
        } catch (NumberFormatException e) {
            tempUserIdValue = userId; // Si la conversion échoue, garde `userId` comme chaîne
        }
        final Object userIdValue = tempUserIdValue;

        // Vérifie si l'utilisateur a déjà noté ce film spécifique
        List<Document> users = (List<Document>) film.get("users");
        boolean userAlreadyRated = users.stream().anyMatch(user -> userIdValue.equals(user.get("_id")));
        if (userAlreadyRated) {
            System.out.println("L'utilisateur a déjà noté ce film. Insertion annulée.");
            return false;
        }

        // Crée la note à insérer pour cet utilisateur
        Document ratingDocument = new Document("_id", userIdValue)
                .append("rating", rating)
                .append("timestamp", timestamp);

        // Ajoute la note dans le tableau `users` du document du film dans `films_embedded`
        filmsEmbeddedCollection.updateOne(
                Filters.eq("_id", filmIdValue),
                Updates.push("users", ratingDocument)
        );

        System.out.println("Note insérée avec succès pour le film dans films_embedded.");
        return true;
    }





    @Override
    public boolean userExists(Object userId) {
        MongoCollection<Document> filmsEmbeddedCollection = mgDatabase.getCollection("films_embedded");

        // Utilise une conversion pour gérer l'ID utilisateur comme chaîne ou entier
        Object userIdValue;
        if (userId instanceof String) {
            try {
                userIdValue = Integer.parseInt((String) userId); // Essaie de le convertir en entier
            } catch (NumberFormatException e) {
                userIdValue = userId; // Sinon, reste une chaîne
            }
        } else {
            userIdValue = userId;
        }

        // Vérifie l'existence de l'utilisateur avec `userIdValue` dans `films_embedded`
        Document user = filmsEmbeddedCollection.find(Filters.elemMatch("users", Filters.eq("_id", userIdValue))).first();

        return user != null;
    }





    @Override
        public boolean hasUserAlreadyRatedFilm(String filmId, String userId) {
        MongoCollection<Document> filmsEmbeddedCollection = mgDatabase.getCollection("films_embedded");

        // Recherche dans `films_embedded` pour un film avec `filmId` et un utilisateur `userId` ayant déjà noté ce film
        Document result = filmsEmbeddedCollection.find(Filters.and(
                Filters.eq("_id", filmId), // Vérifie l'ID du film
                Filters.elemMatch("users", Filters.eq("_id", userId)) // Vérifie la présence de l'utilisateur dans `users`
        )).first();

        // Retourne true si un document est trouvé (utilisateur a déjà noté), false sinon
        return result != null;
    }

}
