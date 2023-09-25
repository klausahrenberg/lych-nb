package com.ka.lych.util;

import com.ka.lych.LBase;
import com.ka.lych.event.LErrorEvent;
import com.ka.lych.event.LEventHandler;
import com.ka.lych.event.LNotificationEvent;
import com.ka.lych.exception.LException;
import com.ka.lych.observable.LString;
import com.ka.lych.util.LReflections.LMethod;

/**
 *
 * @author klausahrenberg
 */
public abstract class LLog {

    public static String APP_TAG;
    public static LLogLevel LOG_LEVEL = LLogLevel.DEBUGGING;
    private static final Class[] ANDROID_LOG_PARAMS = new Class[]{String.class, String.class, Throwable.class};
    private static LEventHandler<LErrorEvent> onError;
    private static LEventHandler<LNotificationEvent> onNotification;

    public static enum LLogLevel {
        DEBUGGING, RUNTIME
    };

    public static boolean printAndroidLogger(String methodName, String message, Throwable ex) {
        try {
            Class result = Class.forName("android.util.Log");
            //(String tag, String msg, Throwable tr)
            Object[] values = new Object[]{APP_TAG, message, ex};
            LMethod m = LReflections.getMethod(result, methodName, ANDROID_LOG_PARAMS);
            //Method m = result.getDeclaredMethod(methodName, ANDROID_LOG_PARAMS);
            m.invoke(null, values);
            return true;
        } catch (Exception cnfe) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public static LEventHandler<LErrorEvent> onErrorProperty() {
        if (onError == null) {
            onError = new LEventHandler();
        }
        return onError;
    }

    public static void setOnError(ILHandler<LErrorEvent> errorHandler) {
        onErrorProperty().set(errorHandler);
    }

    public static void removeOnError(ILHandler<LErrorEvent> errorHandler) {
        if (onError != null) {
            onErrorProperty().remove(errorHandler);
        }
    }

    @SuppressWarnings("unchecked")
    public static LEventHandler<LNotificationEvent> onNotificationProperty() {
        if (onNotification == null) {
            onNotification = new LEventHandler();
        }
        return onNotification;
    }

    public static void setOnNotification(ILHandler<LNotificationEvent> notificationHandler) {
        onNotificationProperty().set(notificationHandler);
    }

    public static void removeOnNotification(ILHandler<LNotificationEvent> notificationHandler) {
        if (onNotification != null) {
            onNotificationProperty().remove(notificationHandler);
        }
    }

    public static void error(String message) {
        error(message, null, false);
    }

    public static void error(String message, Throwable ex) {
        error(message, ex, false);
    }

    @SuppressWarnings("unchecked")
    public static void error(LException exception, boolean promptUser) {
        String message = exception.getMessage();
        try {
            message = LBase.getResources().localize(exception, ILConstants.EXCEPTION + ILConstants.DOT + exception.getMessage());
            message = (message == null ? (exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage()) : message);
        } catch (Exception ex) {
            
        }
        if (!printAndroidLogger("e", message, exception)) {
            printToConsole(APP_TAG + " (error) " + getCallerInfo() + ": " + message + " (" + exception.getMessage() + ")", true);
            if (LOG_LEVEL == LLogLevel.DEBUGGING) {
                exception.printStackTrace();
            }
        }
        if ((LLog.onError != null) && (promptUser)) {
            LLog.onError.get().handle(new LErrorEvent(exception, message, exception.getCause(), false));
        }
    }
    
    @SuppressWarnings("unchecked")
    public static void error(final String message, final Throwable ex, boolean promptUser) {
        if (!printAndroidLogger("e", message, ex)) {
            printToConsole(APP_TAG + " (error) " + getCallerInfo() + ": " + message + (ex != null ? " / " + ex.getClass().getName() + ": " + ex.getMessage() : ""), true);
            if ((ex != null) && (LOG_LEVEL == LLogLevel.DEBUGGING)) {
                ex.printStackTrace();
            }
        }
        if ((LLog.onError != null) && (promptUser)) {
            LLog.onError.get().handle(new LErrorEvent(ex, message, ex, false));
        }
    }

    @SuppressWarnings("unchecked")
    public static void notification(Throwable ex) {
        notification(new LErrorEvent(ex, ex.getMessage(), ex, false));
    }
    
    public static void notification(String message) {
        LLog.notification(message, (Object) null);
    }
    
    public static void notification(String message, Object... arguments) {        
        String m = message;
        try {
            m = String.format(m, arguments);
        } catch (Exception ex2) {}
        notification(new LNotificationEvent(LLog.class, m, 10000));
    }

    public static void notification(LNotificationEvent notification) {
        if (!printAndroidLogger("i", notification.getMessage(), null)) {
            printToConsole(APP_TAG + ": " + notification.getMessage(), false);
        }
        if (LLog.onNotification != null) {
            try {
                LLog.onNotification.fireEvent(notification);
            } catch (Exception e) {
                if (notification instanceof LErrorEvent) {
                    LErrorEvent ev = (LErrorEvent) notification;
                    LLog.error(ev.getMessage(), ev.getException());
                } else {
                    LLog.debug(notification.getMessage());
                }
            }    
        }
    }

    public static void debug(String message, Object... arguments) {
        debug("debug", false, message, arguments);
    }

    private static String getCallerInfo() {
        try {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            int count = stackTraceElements.length;
            for (int i = 0; i < count; i++) {
                StackTraceElement ste = stackTraceElements[i];
                String callerClass = ste.getClassName();
                if ((!callerClass.equals(Thread.class.getName())) && (!callerClass.equals(LLog.class.getName()))) {
                    return callerClass.substring(callerClass.lastIndexOf(".") + 1) + "." + ste.getMethodName() + ":" + ste.getLineNumber();     
                }
            }
        } catch (Exception ex) {
            return "n.a.";
        }
        return "<unknown>";
    }

    private static void debug(final String dMode, final boolean printAsError, final String message, final Object... arguments) {
        Throwable ex = ((arguments != null) && (arguments.length > 0) && (arguments[arguments.length - 1] instanceof Throwable) ? (Throwable) arguments[arguments.length - 1] : null);
        String m = LString.format(message, arguments);
        if (!printAndroidLogger("d", m, ex)) {
            if (LOG_LEVEL == LLogLevel.DEBUGGING) {
                m = APP_TAG + " (" + dMode + ") " + getCallerInfo() + ": " + m;                
                if (ex != null) {
                    m += " / " + ex.getClass().getName() + ": " + ex.getMessage();
                }
                printToConsole(m, ((printAsError) || (ex != null)));
            }
            if (ex != null) {
                ex.printStackTrace();
            }
        }
    }

    private static void printToConsole(final String message, final boolean printAsError) {
        if (printAsError) {
            System.err.println(message);
        } else {
            System.out.println(message);
        }
    }

    public static void test(String message, Object... arguments) {
        debug("test", false, message, arguments);
    }

}
