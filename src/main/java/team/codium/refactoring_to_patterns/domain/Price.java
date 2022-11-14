package team.codium.refactoring_to_patterns.domain;

public class Price {
    public Price(Integer price) throws InvalidPriceException {
        if (!(price == null || price >= 0)) {
            throw new InvalidPriceException("Price cannot be negative");
        }
    }
}
