package team.codium.refactoring_to_patterns.domain;

public class PriceRange {
    public PriceRange(Integer minimumPrice, Integer maximumPrice) throws InvalidPriceException {
        if (!(minimumPrice == null || maximumPrice == null || minimumPrice <= maximumPrice)) {
            throw new InvalidPriceException("The minimum price should be bigger than the maximum price");
        }
    }
}
