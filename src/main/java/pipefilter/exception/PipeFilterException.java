package pipefilter.exception;

/**
 * @author Nardos Tessema
 *
 * Custom exception class
 *
 * Every exception that occurs is re-thrown as a
 * PipeFilterException with a specified detailed
 * message but without a cause.
 *
 * Note: an Exception can have a cause. See constructor:
 *       public Exception(String message, Throwable cause)
 */
public class PipeFilterException extends RuntimeException {
    public PipeFilterException(String message) {
        super(message);
    }
}
