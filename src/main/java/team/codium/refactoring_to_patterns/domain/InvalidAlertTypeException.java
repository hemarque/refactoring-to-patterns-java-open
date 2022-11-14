package team.codium.refactoring_to_patterns.domain;

public class InvalidAlertTypeException extends Exception {
    public InvalidAlertTypeException(String message) {
        super(message);
    }
}
