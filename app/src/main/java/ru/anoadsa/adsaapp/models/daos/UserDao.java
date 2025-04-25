package ru.anoadsa.adsaapp.models.daos;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.anoadsa.adsaapp.models.data.User;

@Dao
public abstract class UserDao {
    private static List<Integer> userIdArr = Collections.singletonList(1);
    private static List<User> userArr = Collections.singletonList(new User());

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract Single<List<Long>> insertUsers(List<User> users);

    @Update
    protected abstract Single<Integer> updateUsers(List<User> users);

    @Delete
    protected abstract Single<Integer> deleteUsers(List<User> users);

    @Query("SELECT * FROM user WHERE id IN (:ids)")
    protected abstract Single<List<User>> selectUsersById(List<Integer> ids);

    public Single<Boolean> newUser() {
//        return insertUsers(userArr).size() == 1;
//        Single.fromCallable(new Callable<Object>() {
//        })
//        insertUsers(userArr).subscribeOn(Schedulers.io()).observeOn()
        return insertUsers(userArr).map(new Function<List<Long>, Boolean>() {
            @Override
            public Boolean apply(List<Long> ids) throws Throwable {
                return ids.size() == 1;
            }
        });
    }

//    @Nullable
    public Single<User> getUser() {
//        try {
//            return selectUsersById(userIdArr).get(0);
//        } catch (IndexOutOfBoundsException e) {
//            return null;
//        }
        return selectUsersById(userIdArr).concatMap(new Function<List<User>, SingleSource<User>>() {
            @Override
            public SingleSource<User> apply(List<User> users) throws Throwable {
                if (users.isEmpty()) {
                    return newUser().map(new Function<Boolean, User>() {
                        @Override
                        public User apply(Boolean success) throws Throwable {
                            return new User();
                        }
                    });
                } else {
                    return Single.just(users.get(0));
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

    public Single<Boolean> editUser(User newUser) {
//        return updateUsers(Collections.singletonList(newUser)) == 1;
        return updateUsers(Collections.singletonList(newUser)).map(new Function<Integer, Boolean>() {
            @Override
            public Boolean apply(Integer count) throws Throwable {
                return count == 1;
            }
        });
    }

    public Single<Boolean> clearUser() {
//        deleteUsers(userArr);
//        return newUser();
//        deleteUsers(userArr).subscribeOn(Schedulers.io()).subscribe(new Consumer<Integer>() {
//            @Override
//            public void accept(Integer integer) throws Throwable {
//
//            }
//        });
        return deleteUsers(userArr).concatMap(new Function<Integer, SingleSource<Boolean>>() {
            @Override
            public SingleSource<Boolean> apply(Integer count) throws Throwable {
                if (count == 1) {
                    return newUser();
                } else {
                    return Single.just(false);
                }
            }
        });
    }
}
