package example.com.trackme.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import example.com.trackme.R;
import example.com.trackme.persistence.TrackMeDatabase;
import example.com.trackme.util.DbConnConverter;
import example.com.trackme.util.HistoryConverter;

public class TrackFinishDialog extends DialogFragment {

    private TrackMeDatabase db;
    private Activity context;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context=activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (db == null) {
            db = Room.databaseBuilder(context,
                    TrackMeDatabase.class, "trackMe")
                    .allowMainThreadQueries()
                    .build();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getArguments().getString("msg_key"))
                .setTitle(R.string.end_tracking_title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // retrieve history from argument
                        String historyString = getArguments().getString("history_key");
                        History currentHistory = HistoryConverter.stringToHistory(historyString);

                        System.out.println("currentHistory: "+currentHistory.toString());

                        // save trail to history
                        System.out.println("Saving data to history...");
                        db.historyDao().insertAll(currentHistory);
                        System.out.println("Done");
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                        System.out.println("Clicked no. No saving data to history.");
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
