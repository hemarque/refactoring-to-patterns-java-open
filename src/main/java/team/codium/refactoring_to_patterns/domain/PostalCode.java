package team.codium.refactoring_to_patterns.domain;

import java.util.regex.Pattern;

public class PostalCode {
    public PostalCode(String postalCode) throws InvalidPostalCodeException {
        if (!Pattern.matches("^\\d{5}$", postalCode)) {
            throw new InvalidPostalCodeException(postalCode + " is not a valid postal code");
        }
    }
}
