package team.codium.refactoring_to_patterns.domain;

public class InvalidPostalCodeException extends Exception {
    public InvalidPostalCodeException(String exceptionMessage) {
        super(exceptionMessage);
    }
}
