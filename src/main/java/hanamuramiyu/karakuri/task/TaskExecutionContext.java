package hanamuramiyu.karakuri.task;

public final class TaskExecutionContext {
    public static final long NO_SESSION = -1L;

    private static final ThreadLocal<Long> SESSION_ID =
        ThreadLocal.withInitial(
            () -> NO_SESSION
        );

    private TaskExecutionContext() {
    }

    public static long currentSessionId() {
        return SESSION_ID.get();
    }

    static void run(
        long sessionId,
        Runnable action
    ) {
        long previousSessionId =
            SESSION_ID.get();

        SESSION_ID.set(sessionId);

        try {
            action.run();
        } finally {
            SESSION_ID.set(previousSessionId);
        }
    }
}