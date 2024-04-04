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

        List<Document> documents = new ArrayList<>();
        // Print all keys (fields) of the sample document
        monogoCol.aggregate(List.of(

                // Stage 1: Get all documents of type "place"
                match(eq("type", "place")),

                // Stage 2: Restrict returned fields and calculate the opening hours
                project(fields(
                        include("category"),
                        computed("openingHours",
                                new Document("$subtract", Arrays.asList("$closingHour", "$openHour"))
                        )
                )),

                // Stage 3: Group by name and calculate the average opening hours
                group("$category", avg("value", "$openingHours"))
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

        List<Document> documents = new ArrayList<>();
        monogoCol.find(and(
                        geoWithinPolygon("geo", polygon),
                        // Escape special characters in name and form regex query
                        regex("name", ".*" + Pattern.quote(name) + ".*")
                ))
                .projection(
                        fields(
                                include("location_id"),
                                excludeId()
                        )

                )
                .into(documents);

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

        // Create a filter to match documents where the "type" field equals the specified value
        Document filter = new Document("type", type);

        // Use the filter and convert into list of documents
        List<Document> documents = new ArrayList<>();
        monogoCol.find(filter).into(documents);
        ;

        return documents;
    }
}
