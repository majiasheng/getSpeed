package example.com.trackme.model;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import example.com.trackme.R;
import example.com.trackme.util.HistoryConverter;

public class TrackFinishDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getArguments().getString("msg_key"))
                .setTitle(R.string.end_tracking_title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.out.println("clicked yes");
                        String historyString = getArguments().getString("history_key");
                        History currentHistory = HistoryConverter.stringToHistory(historyString);
                        //TODO: save trail to history
                        System.out.println("currentHistory"+currentHistory.toString());

                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.out.println("clicked no");
                        //TODO: do nothing
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
