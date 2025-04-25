package ru.anoadsa.adsaapp.models.daos;

import android.util.Pair;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import ru.anoadsa.adsaapp.models.data.Incident;
import ru.anoadsa.adsaapp.models.data.User;

@Dao
public abstract class IncidentDao {
    private static List<Integer> userIdArr = Collections.singletonList(1);
    private static List<User> userArr = Collections.singletonList(new User());

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract Single<List<Long>> insertIncidents(List<Incident> incidents);

    @Update
    protected abstract Single<Integer> updateIncidents(List<Incident> incidents);

    @Delete
    protected abstract Single<Integer> deleteIncidents(List<Incident> incidents);

    @Query("SELECT * FROM incident WHERE id IN (:ids)")
    protected abstract Single<List<Incident>> selectIncidentsById(List<String> ids);

    @Query("SELECT * FROM incident")
    protected abstract Single<List<Incident>> selectAllIncidents();
//    public Single<Boolean> newUser() {
////        return insertUsers(userArr).size() == 1;
////        Single.fromCallable(new Callable<Object>() {
////        })
////        insertUsers(userArr).subscribeOn(Schedulers.io()).observeOn()
//        return insertUsers(userArr).map(new Function<List<Long>, Boolean>() {
//            @Override
//            public Boolean apply(List<Long> ids) throws Throwable {
//                return ids.size() == 1;
//            }
//        });
//    }

    public Single<List<Incident>> getAllIncidents() {
        return selectAllIncidents();
    }

//    @Nullable
    public Single<Pair<Boolean, Incident>> getIncident(String id) {
//        try {
//            return selectUsersById(userIdArr).get(0);
//        } catch (IndexOutOfBoundsException e) {
//            return null;
//        }
        return selectIncidentsById(
//                userIdArr
                Collections.singletonList(id)
        ).concatMap(new Function<List<Incident>, Single<Pair<Boolean, Incident>>>() {
            @Override
            public Single<Pair<Boolean, Incident>> apply(List<Incident> incidents) throws Throwable {
                if (incidents.isEmpty()) {
//                    return newUser().map(new Function<Boolean, User>() {
//                        @Override
//                        public User apply(Boolean success) throws Throwable {
//                            return new User();
//                        }
//                    });
                    return Single.just(new Pair<Boolean, Incident>(false, null));
                } else {
                    return Single.just(new Pair<Boolean, Incident>(true, incidents.get(0)));
                }
            }
        });
//                .map(new Function<List<User>, User>() {
//            @Override
//            public User apply(List<User> users) throws Throwable {
//                if (users.isEmpty()) {
//
//                }
//                return users.get(0);
//            }
//        });
    }

    public Single<Boolean> saveIncident(Incident incident) {
//        return updateUsers(Collections.singletonList(newUser)) == 1;
        return
//                updateIncidents
                insertIncidents
                (Collections.singletonList(incident)).map(
                        new Function<List<Long>, Boolean>() {
                            @Override
                            public Boolean apply(List<Long> rows) throws Throwable {
                                return rows.size() == 1 && rows.get(0) != -1;
                            }
                        }
//                        new Function<Integer, Boolean>() {
//            @Override
//            public Boolean apply(Integer count) throws Throwable {
//                return count == 1;
//            }
//    }
        );
    }

    public Single<Boolean> clearIncidents() {
        return selectAllIncidents().concatMap(new Function<List<Incident>, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(List<Incident> incidents) throws Throwable {
                if (incidents != null) {
                    return deleteIncidents(incidents).map(new Function<Integer, Boolean>() {
                        @Override
                        public Boolean apply(Integer count) throws Throwable {
                            return count == incidents.size();
                        }
                    });
                } else {
                    return Single.just(false);
                }
            }
        });
    }

//    public Single<Boolean> clearUser() {
////        deleteUsers(userArr);
////        return newUser();
////        deleteUsers(userArr).subscribeOn(Schedulers.io()).subscribe(new Consumer<Integer>() {
////            @Override
////            public void accept(Integer integer) throws Throwable {
////
////            }
////        });
//        return deleteUsers(userArr).concatMap(new Function<Integer, SingleSource<Boolean>>() {
//            @Override
//            public SingleSource<Boolean> apply(Integer integer) throws Throwable {
//                return newUser();
//            }
//        });
//    }
}
