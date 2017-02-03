package com.nn.roomx.view;

import android.graphics.Point;
import android.os.CountDownTimer;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nn.roomx.DataExchange;
import com.nn.roomx.MainActivity;
import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.R;
import com.nn.roomx.RoomxUtils;
import com.nn.roomx.Setting;
import com.nn.roomx.view.seekbar.IRangeBarFormatter;
import com.nn.roomx.view.seekbar.RangeBar;

/**
 * Created by user on 2017-01-15.
 */

public class DialogueHelper {

    private static final String TAG = "RoomX";

    public static CreateAppointmentDialog getCreateAppointmnetDialogue(final MainActivity context, final DialogueHelperButtonAction buttonAction, final Appointment currentAppointment) {

        final CreateAppointmentDialog dialog = new CreateAppointmentDialog(context, currentAppointment, buttonAction);
        dialog.init();
        return dialog;

    }

    public static FinishAppointmentDialog getFinishAppointmnetDialogue(final MainActivity context, final Appointment currentAppointment, DataExchange dataExchange, Setting setting) {

        final FinishAppointmentDialog dialog = new FinishAppointmentDialog(context, currentAppointment, dataExchange, setting);
        dialog.init();
        return dialog;

    }

    public interface DialogueHelperButtonAction {
        public void action();
    }
}
