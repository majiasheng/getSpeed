package example.com.trackme.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import example.com.trackme.TrackMe;
import example.com.trackme.model.History;

@Dao
public interface HistoryDao {
    long HR_IN_MS = 86400000L;
    @Query("SELECT * FROM history")
    List<History> getAll();

    @Query("SELECT * FROM history WHERE id IN (:historyIds)")
    List<History> loadAllByIds(int[] historyIds);

    @Query("SELECT * FROM history WHERE id = :id LIMIT 1")
    History findById(int id);

    @Insert
    void insertAll(History... histories);

    @Delete
    void delete(History history);

    @Query("DELETE FROM history")
    void deleteAll();

    //FIXME: query was assuming new lo and hi are time of day...
//    @Query("SELECT * FROM history WHERE startTime >= (:newStartTime-" + TrackMe.T + ") AND startTime <= (:newStartTime+" + TrackMe.T+")")
    @Query("SELECT * FROM history WHERE (:newStartTime - startTime)%" + HR_IN_MS + " >= -" + TrackMe.T + " AND (:newStartTime - startTime)%" + HR_IN_MS + "<= " + TrackMe.T)
    List<History> getCandidatePredictions(long newStartTime);


//    @Query("SELECT * FROM history WHERE startTime % " + HR_IN_MS + ":lo AND startTime <= :hi")
//    List<History> getCandidatePredictions(long lo, long hi);
}
