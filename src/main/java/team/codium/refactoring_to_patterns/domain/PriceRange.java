package team.codium.refactoring_to_patterns.domain;

public class PriceRange {
    private Integer minimumPrice;
    private Integer maximumPrice;

    public PriceRange(Integer minimumPrice, Integer maximumPrice) throws InvalidPriceException {
        if (!(minimumPrice == null || maximumPrice == null || minimumPrice <= maximumPrice)) {
            throw new InvalidPriceException("The minimum price should be bigger than the maximum price");
        }

        this.maximumPrice = maximumPrice;
        this.minimumPrice = minimumPrice;
    }

    public boolean isInRange(Property property) {
        return (minimumPrice == null || property.getPrice() >= minimumPrice) &&
                (maximumPrice == null || property.getPrice() <= maximumPrice);
    }
}
