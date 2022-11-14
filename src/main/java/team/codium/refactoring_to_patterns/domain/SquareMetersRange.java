package team.codium.refactoring_to_patterns.domain;

public class SquareMetersRange {
    private Integer minimumSquareMeters;
    private Integer maximumSquareMeters;
    public SquareMetersRange(Integer minimumSquareMeters, Integer maximumSquareMeters) {
        this.minimumSquareMeters = minimumSquareMeters;
        this.maximumSquareMeters = maximumSquareMeters;
    }

    public boolean isInRange(Property property) {
        return (minimumSquareMeters == null || property.getSquareMeters() >= minimumSquareMeters) &&
                (maximumSquareMeters == null || property.getSquareMeters() <= maximumSquareMeters);
    }
}
