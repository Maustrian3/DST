package dst.ass1.jooq.dao.impl;

import dst.ass1.jooq.connection.DataSource;
import dst.ass1.jooq.dao.IRiderPreferenceDAO;
import dst.ass1.jooq.model.IRiderPreference;
import dst.ass1.jooq.model.impl.RiderPreference;
import org.jooq.BatchBindStep;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dst.ass1.jooq.model.public_.tables.Preference.PREFERENCE;
import static dst.ass1.jooq.model.public_.tables.RiderPreference.RIDER_PREFERENCE;

public class RiderPreferenceDAO implements IRiderPreferenceDAO {

    private DSLContext getConnection() {
        try {
            return DataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IRiderPreference findById(Long id) {
        DSLContext dslContext = getConnection();

        // Find the rider record
        Record riderRecord = dslContext.select()
                .from(RIDER_PREFERENCE)
                .where(RIDER_PREFERENCE.RIDER_ID.eq(id))
                .fetchOne();

        if (riderRecord == null) {
            return null;
        }

        RiderPreference riderPreference = new RiderPreference();
        riderPreference.setRiderId(riderRecord.get(RIDER_PREFERENCE.RIDER_ID));
        riderPreference.setVehicleClass(riderRecord.get(RIDER_PREFERENCE.VEHICLE_CLASS));
        riderPreference.setArea(riderRecord.get(RIDER_PREFERENCE.AREA));

        // Find the preferences associated with the rider
        Map<String, String> preferences = dslContext.select(PREFERENCE.PREF_KEY, PREFERENCE.PREF_VALUE)
                .from(PREFERENCE)
                .where(PREFERENCE.RIDER_ID.eq(id))
                .fetchMap(PREFERENCE.PREF_KEY, PREFERENCE.PREF_VALUE);

        riderPreference.setPreferences(preferences);

        return riderPreference;
    }

    @Override
    public List<IRiderPreference> findAll() {
        var dslContext = getConnection();

        List<Record> riderRecords = dslContext.select()
                .from(RIDER_PREFERENCE
                        .leftJoin(PREFERENCE) // Left join to include riders with no preferences
                        .on(RIDER_PREFERENCE.RIDER_ID.eq(PREFERENCE.RIDER_ID)))
                .fetch();

        Map<Long, RiderPreference> riderMap = new HashMap<>();
        for (Record record : riderRecords) {
            Long riderId = record.get(RIDER_PREFERENCE.RIDER_ID);

            // Put new rider into map if not already present
            if (!riderMap.containsKey(riderId)) {
                RiderPreference rider = new RiderPreference();
                rider.setRiderId(riderId);
                rider.setVehicleClass(record.get(RIDER_PREFERENCE.VEHICLE_CLASS));
                rider.setArea(record.get(RIDER_PREFERENCE.AREA));
                riderMap.put(riderId, rider);
            }

            // Add linked preferences if they exist
            if (record.get(PREFERENCE.PREF_KEY) != null) {

                RiderPreference rider = riderMap.get(riderId);

                // If rider has no preferences set yet, create a new map
                if (rider.getPreferences() == null)
                    rider.setPreferences(new HashMap<>());

                // Add preferences to rider
                rider.getPreferences().put(
                        record.get(PREFERENCE.PREF_KEY),
                        record.get(PREFERENCE.PREF_VALUE)
                );
            }
        }

        // Return all rider preferences
        return new ArrayList<>(riderMap.values());
    }

    @Override
    public IRiderPreference insert(IRiderPreference model) {
        DSLContext dslContext = getConnection();

        dslContext.transaction((Configuration trx) -> {
            // Insert Rider Preference
            trx.dsl().insertInto(RIDER_PREFERENCE)
                    .set(RIDER_PREFERENCE.RIDER_ID, model.getRiderId())
                    .set(RIDER_PREFERENCE.VEHICLE_CLASS, model.getVehicleClass())
                    .set(RIDER_PREFERENCE.AREA, model.getArea())
                    .execute();

            // Insert preferences
            Map<String, String> preferences = model.getPreferences();
            if (preferences != null && !preferences.isEmpty()) {
                BatchBindStep batch = trx.dsl().batch(
                        trx.dsl().insertInto(
                                        PREFERENCE,
                                        PREFERENCE.RIDER_ID,
                                        PREFERENCE.PREF_KEY,
                                        PREFERENCE.PREF_VALUE)
                                .values(
                                        (Long) null,
                                        null,
                                        null)
                );

                for (Map.Entry<String, String> entry : preferences.entrySet()) {
                    batch.bind(
                            model.getRiderId(),
                            entry.getKey(),
                            entry.getValue()
                    );
                }
                batch.execute();
            }
        });

        return model;
    }

    @Override
    public void delete(Long id) {
        DSLContext dslContext = getConnection();

        dslContext.transaction((Configuration trx) -> {
            // Delete preferences associated with riderId
            trx.dsl().deleteFrom(PREFERENCE)
                    .where(PREFERENCE.RIDER_ID.eq(id))
                    .execute();

            // Delete rider preference
            trx.dsl().deleteFrom(RIDER_PREFERENCE)
                    .where(RIDER_PREFERENCE.RIDER_ID.eq(id))
                    .execute();
        });
    }

    @Override
    public void updatePreferences(IRiderPreference model) {
        getConnection().batched((Configuration trx) -> {
            // Update RiderPreference fields
            trx.dsl().update(RIDER_PREFERENCE)
                    .set(RIDER_PREFERENCE.VEHICLE_CLASS, model.getVehicleClass())
                    .set(RIDER_PREFERENCE.AREA, model.getArea())
                    .where(RIDER_PREFERENCE.RIDER_ID.eq(model.getRiderId()))
                    .execute();

            // Update or insert preferences
            for (var e : model.getPreferences().entrySet()) {
                if (trx.dsl().fetchExists(
                        trx.dsl().selectOne()
                                .from(PREFERENCE)
                                .where(PREFERENCE.RIDER_ID.eq(model.getRiderId())
                                        .and(PREFERENCE.PREF_KEY.eq(e.getKey()))))) {
                    // Preference key exists, update the record
                    trx.dsl()
                            .update(PREFERENCE)
                            .set(PREFERENCE.PREF_VALUE, e.getValue())
                            .where(PREFERENCE.RIDER_ID.eq(model.getRiderId())
                                    .and(PREFERENCE.PREF_KEY.eq(e.getKey())))
                            .execute();
                } else {
                    // Preference key does not exist, insert a new record
                    trx.dsl()
                            .insertInto(PREFERENCE)
                            .set(PREFERENCE.RIDER_ID, model.getRiderId())
                            .set(PREFERENCE.PREF_VALUE, e.getValue())
                            .set(PREFERENCE.PREF_KEY, e.getKey())
                            .execute();
                }
            }
        });
    }
}
