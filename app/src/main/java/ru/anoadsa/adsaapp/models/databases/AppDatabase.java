package ru.anoadsa.adsaapp.models.databases;


import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import ru.anoadsa.adsaapp.models.daos.IncidentDao;
import ru.anoadsa.adsaapp.models.daos.UserDao;
import ru.anoadsa.adsaapp.models.data.Incident;
import ru.anoadsa.adsaapp.models.data.User;

@Database(
        entities = {User.class, Incident.class},
        version = 4
//        ,
//        autoMigrations = {
//                @AutoMigration(from = 1, to = 2)
//            @AutoMigration(from = 2, to = 3)
//        }
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract IncidentDao incidentDao();

    public static Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE User ADD COLUMN password TEXT");
            database.execSQL("ALTER TABLE User ADD COLUMN auth_timestamp INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE User ADD COLUMN disability_category TEXT");
            database.execSQL("ALTER TABLE User ADD COLUMN only_SMS INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS Incident (" +
                    "id TEXT PRIMARY KEY NOT NULL," +
                    "viewedMessageCount INTEGER NOT NULL," +
                    "canWriteChat INTEGER NOT NULL" + // boolean is not supported by room
                ")");
        }
    };

    public static Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE User ADD COLUMN surname TEXT");
            database.execSQL("ALTER TABLE User ADD COLUMN name TEXT");
            database.execSQL("ALTER TABLE User ADD COLUMN midname TEXT");
            database.execSQL("ALTER TABLE User ADD COLUMN email TEXT");
            database.execSQL("ALTER TABLE User ADD COLUMN carBrand TEXT");
            database.execSQL("ALTER TABLE User ADD COLUMN carNumber TEXT");
            database.execSQL("ALTER TABLE User ADD COLUMN osago TEXT");
            database.execSQL("ALTER TABLE User ADD COLUMN snils TEXT");
            database.execSQL("ALTER TABLE User ADD COLUMN medPolis TEXT");
            database.execSQL("ALTER TABLE User ADD COLUMN birthday TEXT");
        }
    };

//    public static class AutoMigration_1_to_2 implements AutoMigrationSpec {};
}
