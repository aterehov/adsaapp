package ru.anoadsa.adsaapp.models.daos;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Function;
import ru.anoadsa.adsaapp.models.data.RouteUserPoint;

@Dao
public abstract class RouteUserPointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract Single<List<Long>> insertRups(List<RouteUserPoint> rups);

    @Update
    protected abstract Single<Integer> updateRups(List<RouteUserPoint> rups);

    @Delete
    protected abstract Single<Integer> deleteRups(List<RouteUserPoint> rups);

    @Query("SELECT * FROM routeuserpoint WHERE route_id = :route_id")
    protected abstract Single<List<RouteUserPoint>> selectRupsForRouteId(int route_id);

    @Query("SELECT * FROM routeuserpoint WHERE route_id = :route_id AND `order` = :order")
    protected abstract Single<List<RouteUserPoint>> selectRup(int route_id, int order);

    @Query("SELECT COUNT(*) FROM routeuserpoint WHERE route_id = :route_id")
    protected abstract Single<Integer> countRupsForRouteId(int route_id);

    public Single<List<RouteUserPoint>> getRupsForRouteId(int route_id) {
        return selectRupsForRouteId(route_id);
    }

    public Single<Boolean> setRupsForRouteId(int route_id, @NonNull List<RouteUserPoint> rups) {
        rups.sort(RouteUserPoint::sortByOrder);
        for (int i = 0; i < rups.size(); ++i) {
            if (rups.get(i).getRouteId() != route_id
                || rups.get(i).getOrder() != i) {
                return Single.just(false);
            }
        }
//        Single<Pair<Boolean, Integer>> task = countRupsForRouteId(route_id).map(new Function<Integer, Pair<Boolean, Integer>>() {
//            @Override
//            public Pair<Boolean, Integer> apply(Integer count) throws Throwable {
//                return new Pair<Boolean, Integer>(true, count);
//            }
//        });

        return countRupsForRouteId(route_id).concatMap(new Function<Integer, Single<Boolean>>() {
            @Override
            public Single<Boolean> apply(Integer count) throws Throwable {
                Single<Boolean> task = insertRups(rups).map(new Function<List<Long>, Boolean>() {
                    @Override
                    public Boolean apply(List<Long> rows) throws Throwable {
                        return rows.size() == rups.size();
                    }
                });

                if (count > rups.size()) {
                    ArrayList<RouteUserPoint> delrups = new ArrayList<RouteUserPoint>();
                    for (int i = rups.size(); i < count; ++i) {
                        RouteUserPoint delrup = new RouteUserPoint();
                        delrup.setRouteId(route_id);
                        delrup.setOrder(i);
                        delrups.add(delrup);
                    }
                    task = task.concatMap(new Function<Boolean, Single<Boolean>>() {
                        @Override
                        public Single<Boolean> apply(Boolean success) throws Throwable {
                            if (success) {
                                return deleteRups(delrups).map(new Function<Integer, Boolean>() {
                                    @Override
                                    public Boolean apply(Integer count) throws Throwable {
                                        return count == delrups.size();
                                    }
                                });
                            } else {
                                return Single.just(false);
                            }
                        }
                    });
                }
                return task;
            }
        });
    }
}
