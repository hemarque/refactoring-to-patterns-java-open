package team.codium.refactoring_to_patterns.domain;

public class InvalidPriceException extends Exception {
    public InvalidPriceException(String message) {
        super(message);
    }
}
