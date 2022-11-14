package team.codium.refactoring_to_patterns.domain;

public class Price {
    public Price(int price) throws InvalidPriceException {
        if (!(price >= 0)) {
            throw new InvalidPriceException("Price cannot be negative");
        }
    }
}
