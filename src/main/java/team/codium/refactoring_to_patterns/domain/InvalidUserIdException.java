package team.codium.refactoring_to_patterns.domain;

public class InvalidUserIdException extends Exception {
    public InvalidUserIdException(String msg) {
        super(msg);
    }
}
