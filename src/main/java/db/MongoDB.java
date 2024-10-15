package db;

import com.mongodb.client.MongoClient;

public class MongoDB implements DB{

    private MongoClient mongoClient;

    public MongoDB(String uri) {

    }
}
