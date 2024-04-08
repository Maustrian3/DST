package dst.ass1.doc.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import dst.ass1.doc.IDocumentRepository;
import dst.ass1.jpa.model.ILocation;
import dst.ass1.jpa.util.Constants;
import org.bson.Document;

import java.util.Map;

public class DocumentRepository implements IDocumentRepository {

    @Override
    public void insert(ILocation location, Map<String, Object> locationProperties) {
        MongoDatabase monogoDb = new MongoClient().getDatabase(Constants.MONGO_DB_NAME);

        // Get the collection from the database
        MongoCollection<Document> monogoCol = monogoDb.getCollection(Constants.COLL_LOCATION_DATA);

        // Create document and populate it with the location properties
        Document document = new Document();
        document.put("location_id", location.getLocationId());
        document.put("name", location.getName());
        for (Map.Entry<String, Object> entry : locationProperties.entrySet()) {
            document.append(entry.getKey(), entry.getValue());
        }

        monogoCol.insertOne(document);

        // Add appropriate indices for efficient retrieval
        monogoCol.createIndex(Indexes.ascending("location_id"));
        monogoCol.createIndex(Indexes.geo2dsphere("geo"));
    }
}
