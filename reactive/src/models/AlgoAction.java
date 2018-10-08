package models;

import logist.topology.Topology;

public class AlgoAction {

    private final Topology.City city;

    public AlgoAction(Topology.City city) {
        if (city == null) {
            throw new IllegalArgumentException("An Action cannot be created with a null city");
        }
        this.city = city;
    }

    public Topology.City getCity() {
        return city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlgoAction that = (AlgoAction) o;

        return city.equals(that.city);
    }

    @Override
    public int hashCode() {
        return city.hashCode();
    }

}
