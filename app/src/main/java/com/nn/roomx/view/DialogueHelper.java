package com.nn.roomx.view;

import com.nn.roomx.DataExchange;
import com.nn.roomx.MainActivity;
import com.nn.roomx.ObjClasses.Appointment;
import com.nn.roomx.ObjClasses.ServiceResponse;
import com.nn.roomx.Setting;

import java.util.List;

/**
 * Created by user on 2017-01-15.
 */

public class DialogueHelper {

    private static final String TAG = "RoomX";

    public static CreateAppointmentDialog getCreateAppointmnetDialogue(MainActivity context, Appointment currentAppointment, DataExchange dataExchange, Setting setting, DialogueHelperAction dialogueHelperAction) {

        final CreateAppointmentDialog dialog = new CreateAppointmentDialog(context, currentAppointment, dataExchange, setting, dialogueHelperAction);
        dialog.init();
        return dialog;

    }

    public static FinishAppointmentDialog getFinishAppointmnetDialogue(final MainActivity context, final Appointment currentAppointment, DataExchange dataExchange, Setting setting, DialogueHelperAction dialogueHelperAction) {

        final FinishAppointmentDialog dialog = new FinishAppointmentDialog(context, currentAppointment, dataExchange, setting, dialogueHelperAction);
        dialog.init();
        return dialog;

    }

    public static CancelAppointmentDialog getCancelAppointmnetDialogue(final MainActivity context, final Appointment currentAppointment, DataExchange dataExchange, Setting setting, DialogueHelperAction dialogueHelperAction) {

        final CancelAppointmentDialog dialog = new CancelAppointmentDialog(context, currentAppointment, dataExchange, setting, dialogueHelperAction);
        dialog.init();
        return dialog;

    }

    public static ConfirmAppointmentDialog getConfirmAppointmnetDialogue(final MainActivity context, final Appointment currentAppointment, DataExchange dataExchange, Setting setting, DialogueHelperAction dialogueHelperAction) {
        final ConfirmAppointmentDialog dialog = new ConfirmAppointmentDialog(context, currentAppointment, dataExchange, setting, dialogueHelperAction);
        dialog.init();
        return dialog;

    }



    public interface DialogueHelperAction {
        public void refreshAppoitnments(ServiceResponse<List<Appointment>> response);
        public void onFinish();
        public void onError(Throwable err);
    }
}
