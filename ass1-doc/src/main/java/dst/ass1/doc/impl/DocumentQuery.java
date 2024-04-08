package dst.ass1.doc.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import dst.ass1.doc.IDocumentQuery;
import dst.ass1.jpa.util.Constants;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

public class DocumentQuery implements IDocumentQuery {

    private MongoDatabase monogoDb;

    public DocumentQuery(MongoDatabase monogoDb) {
        this.monogoDb = monogoDb;
    }

    @Override
    public List<Document> getAverageOpeningHoursPerCategory() {
        // Get the collection from the database
        MongoCollection<Document> monogoCol = monogoDb.getCollection(Constants.COLL_LOCATION_DATA);

        // Define the example query to match documents of type "place"
        Document exampleQuery = new Document("type", "place");

        // Define the projection to include the "category" field and calculate the opening hours
        Document projection = new Document("$project", new Document()
                .append("category", 1)
                .append("openingHours", new Document("$subtract", Arrays.asList("$closingHour", "$openHour"))));

        // Define the grouping to group by "category" and calculate the average opening hours
        Document grouping = new Document("$group", new Document()
                .append("_id", "$category")
                .append("value", new Document("$avg", "$openingHours")));

        // Execute the aggregation pipeline
        List<Document> documents = new ArrayList<>();
        monogoCol.aggregate(Arrays.asList(
                match(exampleQuery), // Stage 1: Get all documents of type "place"
                projection, // Stage 2: Restrict returned fields and calculate the opening hours
                grouping // Stage 3: Group by name and calculate the average opening hours
        )).into(documents);

        // Logging output
        for (Document document : documents) {
            System.out.println(document.toJson());
        }

        return documents;
    }


    @Override
    public List<Document> findDocumentsByNameWithinPolygon(String name, List<List<Double>> polygon) {
        // Get the collection from the database
        MongoCollection<Document> monogoCol = monogoDb.getCollection(Constants.COLL_LOCATION_DATA);

        // Create an example query to match documents within the specified polygon and with the specified name
        Document exampleQuery = new Document("$and", Arrays.asList(
                new Document("geo", new Document("$geoWithin", new Document("$polygon", polygon))),
                // Escape special characters in name and form regex query
                new Document("name", new Document("$regex", ".*" + Pattern.quote(name) + ".*"))
        ));

        // Create a projection to include "location_id" field and exclude "_id" field
        Document projection = new Document("location_id", 1).append("_id", 0);

        // Execute the example query with projection and convert the results into a list of documents
        List<Document> documents = new ArrayList<>();
        monogoCol.find(exampleQuery).projection(projection).into(documents);

        // Logging output
        for (Document document : documents) {
            System.out.println(document.toJson());
        }

        return documents;
    }


    @Override
    public List<Document> findDocumentsByType(String type) {
        // Get the collection from the database
        MongoCollection<Document> monogoCol = monogoDb.getCollection(Constants.COLL_LOCATION_DATA);

        // Create an example query to match documents where the "type" field equals the specified value
        Document exampleQuery = new Document("type", type);

        // Execute the example query and convert the results into a list of documents
        List<Document> documents = new ArrayList<>();
        monogoCol.find(exampleQuery).into(documents);

        return documents;
    }
}
