package ec.edu.espe.coworkingapp.web.advice;

public class BusinessConflictException extends RuntimeException {
    public BusinessConflictException(String message) {
        super(message);
    }
}
