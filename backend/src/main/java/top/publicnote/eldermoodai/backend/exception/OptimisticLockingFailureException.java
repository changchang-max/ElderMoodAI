package top.publicnote.eldermoodai.backend.exception;

public class OptimisticLockingFailureException extends RuntimeException {

    public OptimisticLockingFailureException(String message) {
        super(message);
    }
}
