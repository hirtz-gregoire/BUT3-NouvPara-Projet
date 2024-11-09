
# Projet NoSQL
Pour une lecture facile, utiliser un lecteur markdow
 - en local
 - en ligne : <a href="https://github.com/hirtz-gregoire/BUT3-NouvPara-Projet/tree/master/resources/doc">ici</a>

## Grégoire HIRTZ<br>RA-IL2

# Mise en place

## Via Docker (recommandé)
- Lancement conteneur docker complet avec collections final
```bash
bash install_docker.sh
```
- Nettoyage du conteneur a la fin du test du programme
```bash
bash clear_docker.sh
```

## Sur mongo hôte
```bash
bash install.sh
```
- Nettoyage du conteneur a la fin du test du programme
```bash
bash clear.sh
```



# Questions

## 1. Pour une vision purement relationnelle, donner un diagramme UML modélisant les données de films et utilisateurs.

### Collection Utilisateurs
```
+---------------------------------------------------+
|                    Utilisateur                    |
+---------------------------------------------------+
| - _id : int                                       |
| - name : String                                   |
| - gender : String                                 |
| - age : int                                       |
| - occupation : List<String>                       |
| - movies : List<MovieInfo>                        |
+---------------------------------------------------+

+-------------------------------+
|           MovieInfo           |
+-------------------------------+
| - movieid : int               |
| - rating : int                |
| - timestamp : long            |
+-------------------------------+
```
<details>
  <summary>Script de nettoyage</summary>

```
// Parcourt tous les utilisateurs pour mettre à jour le champ `occupation`
db.utilisateurs.find({}).forEach(user => {
    // Transformation de `occupation` en tableau si ce n'est pas déjà un tableau
    const occupationList = user.occupation.split('/').map(item => item.trim());

    // Mise à jour du document avec le champ `occupation` sous forme de tableau
    db.utilisateurs.updateOne(
        { _id: user._id },
        { $set: { occupation: occupationList } }
    );
});

// indexation movie.movieid
db.utilisateurs.createIndex({ "movies.movieid": 1 })

```
</details>


### Collection Films
```
+---------------------------------------------------+
|                       Film                        |
+---------------------------------------------------+
| - _id : int                                       |
| - title : String                                  |
| - genres : List<String>                           |
| - date : int                                      |
| - users : List<UserInfo>                          |
+---------------------------------------------------+

+-------------------------------+
|           UserInfo            |
+-------------------------------+
| - userId : int                |
| - rating : int                |
| - timestamp : long            |
+-------------------------------+
```
<details>
  <summary>Script de nettoyage</summary>

```
// Parcourt tous les films pour transformer `genres` en tableau
db.films.find({}).forEach(film => {
    // Transformation de `genres` en tableau si ce n'est pas déjà un tableau
    const genresList = film.genres.split('|').map(item => item.trim());

    // Mise à jour du document avec le champ `genres` sous forme de tableau
    db.films.updateOne(
        { _id: film._id },
        { $set: { genres: genresList } }
    );
});




// Parcourt tous les films pour extraire la date et nettoyer le titre
db.films.find({}).forEach(film => {
    // Utiliser une expression régulière pour extraire le titre et la date
    const match = film.title.match(/^(.*)\s\((\d{4})\)$/);

    if (match) {
        const title = match[1]; // Le titre sans la date
        const date = parseInt(match[2]); // Convertit l'année en entier

        // Met à jour le document avec le titre nettoyé et le champ `date`
        db.films.updateOne(
            { _id: film._id },
            { $set: { title: title, date: date } }
        );
    }
});
```
</details>


### Amélioration Apporté au collections existante

- transformation de la chaine de caractère des occupations des utilisateurs en liste
- transformation de la chaine de caracrère des genres des films en liste
- ajout d'un champ "date" dans les films, extrait de la parenthèse initialement apres le titre du film

##  2. Les collections films et utilisateurs sont modélisées à la "linking". Pour une modélisation à la "embedding", décrire les deux types de document :

### a. contenant toutes les informations des utilisateurs ;
```
+---------------------------------------------------+
|                   Utilisateur                     |
+---------------------------------------------------+
| - _id : int                                       |
| - name : String                                   |
| - gender : String                                 |
| - age : int                                       |
| - occupation : List<String>                       |
| - movies : List<Film>                             |
+---------------------------------------------------+

Classe Film {
    - _id : int
    - title : String
    - genres : List<String>
    - date : int
    - rating : int
    - timestamp : long
}
```
<details>
  <summary>Script de création</summary>

```
const cursor = db.utilisateurs.aggregate([
    {
        $unwind: "$movies" // Décompose chaque film dans `movies`
    },
    {
        $lookup: {
            from: "films", // Collection `films`
            localField: "movies.movieid", // Champ `movieid` dans `movies`
            foreignField: "_id",          // Champ `_id` dans `films`
            as: "movieDetails"
        }
    },
    {
        $unwind: "$movieDetails" // Intègre les détails du film (s'assure qu'il y a un seul objet `movieDetails`)
    },
    {
        $addFields: {
            "movies.title": "$movieDetails.title",    // Ajoute le titre du film dans `movies`
            "movies.date": "$movieDetails.date",      // Ajoute la date de sortie dans `movies`
            "movies.genres": "$movieDetails.genres"   // Ajoute les genres sous forme de liste dans `movies`
        }
    },
    {
        $project: {
            movieDetails: 0,          // Supprime `movieDetails` (informations temporaires du film)
            "movies.movieid": 0       // Supprime `movieid` des films intégrés pour plus de clarté
        }
    },
    {
        $group: {
            _id: "$_id",                           // Regroupe par ID utilisateur
            name: { $first: "$name" },
            gender: { $first: "$gender" },
            age: { $first: "$age" },
            occupation: { $first: "$occupation" }, // Utilise `occupation` sous forme de liste
            movies: { $push: "$movies" }          // Regroupe tous les films avec détails dans un tableau
        }
    }
], { allowDiskUse: true }); // Utilisation du disque pour les grosses agrégations

// 2. Créer la collection `utilisateurs_embedded` et insérer les documents un par un
cursor.forEach(document => {
    db.utilisateurs_embedded.insertOne(document);
});
```

</details>



### b. contenant toutes les informations des films.
```
+---------------------------------------------------+
|                       Film                        |
+---------------------------------------------------+
| - _id : int                                       |
| - title : String                                  |
| - genres : List<String>                           |
| - date : int                                      |
| - users : List<User>                              |
+---------------------------------------------------+

Classe User {
    - _id : int
    - name : String
    - gender : String
    - age : int
    - occupation : List<String>
    - rating : int
    - timestamp : long
}
```
<details>
  <summary>Script de création</summary>

```
const cursor = db.films.aggregate([
    {
        $lookup: {
            from: "utilisateurs",              // Collection `utilisateurs`
            localField: "_id",                 // Champ `_id` dans `films` (identifiant du film)
            foreignField: "movies.movieid",    // Champ `movieid` dans `movies` de `utilisateurs`
            as: "userRatings"
        }
    },
    {
        $unwind: {
            path: "$userRatings",
            preserveNullAndEmptyArrays: true   // Conserve les films sans utilisateurs associés
        }
    },
    {
        $unwind: {
            path: "$userRatings.movies",
            preserveNullAndEmptyArrays: true   // Conserve les utilisateurs sans notes correspondantes pour ce film
        }
    },
    {
        $match: {                             // Filtre pour ne conserver que les notes du film en cours
            $or: [
                { $expr: { $eq: ["$userRatings.movies.movieid", "$_id"] } },
                { "userRatings": null }
            ]
        }
    },
    {
        $project: {
            title: 1,
            date: 1,                          // Inclut `date` dans le résultat final
            genres: 1,                        // Inclut `genres` sous forme de liste dans le résultat final
            "userRatings._id": 1,
            "userRatings.name": 1,
            "userRatings.gender": 1,
            "userRatings.age": 1,
            "userRatings.occupation": 1,      // Conserve `occupation` sous forme de liste
            "userRatings.movies.rating": 1,
            "userRatings.movies.timestamp": 1
        }
    },
    {
        $group: {
            _id: "$_id",                       // Groupement par ID de film
            title: { $first: "$title" },
            date: { $first: "$date" },         // Ajoute `date` au document final
            genres: { $first: "$genres" },     // Ajoute `genres` sous forme de liste au document final
            users: {
                $push: {
                    userId: "$userRatings._id",
                    name: "$userRatings.name",
                    gender: "$userRatings.gender",
                    age: "$userRatings.age",
                    occupation: "$userRatings.occupation", // Utilise `occupation` sous forme de liste
                    rating: "$userRatings.movies.rating",
                    timestamp: "$userRatings.movies.timestamp"
                }
            }
        }
    },
    {
        $out: "films_embedded" // Sauvegarde le résultat dans `films_embedded`
    }
], { allowDiskUse: true });
```
</details>


## 3. Créer une BD sur MongoDB et une collection (a ou b) qui se prête bien aux fonctionnalités ci-dessous de l'application à réaliser. Les données de cette collection seront copiées à partir de « films.json » et « utilisateurs.json ».

Pour cette BD, nous allons utiliser la version de films en embedding que nous appelerons films_embedded. Pour cela, on utile la commande décrite plus haut au moment de la descritpion d'une collection films en embedding. Cette procédure reste acces longue (environ 5-10min)? C'est pourquoi une copie complète de la collection esr disponible dans ce projet et que la collection est déjà importé dans la bd


## 4. Écrire une application en java permettant de manipuler la BD de (3) via l’API pour accéder à MongoDB. L’application doit implémenter les fonctionnalités suivantes :

### a. Afficher tous les films (titre et date de parution) ayant un genre donnée. On classera les films par ordre décroissant de l'année de parution
### b. Afficher tous les utilisateurs (nom et âge) ayant une profession donnée
### c. Afficher les films (titre, date et genre) les plus vus/notés par une profession donnée
### d. Afficher les films (titre, date et genre) les plus vus/notés par une catégorie d'âges donnée
### e. Afficher pour le Top5 des films notés, leur titre, note et le nombre des utilisateurs qui les ont notés
### f. Insérer une note pour un film donné et un utilisateur donné.