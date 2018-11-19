package algo;

import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsefulnessEstimator {

    private Topology topology;
    private TaskDistribution distribution;
    private Map<CityPair, Double> usefulnessMap; // values are between 0 and 1

    public UsefulnessEstimator(Topology topology, TaskDistribution distribution) {
        this.topology = topology;
        this.distribution = distribution;
        this.usefulnessMap = new HashMap<>();
    }


    public void computeEstimates() {
        List<City> cities = topology.cities();

        Map<City, Double> cityToAllCitiesDistance = new HashMap<>();
        for (City from : cities) {
            double distanceToAllCities = 0;
            for (City to : cities) {
                distanceToAllCities += from.distanceTo(to);
            }
            cityToAllCitiesDistance.put(from, distanceToAllCities);
        }

        double maxDistance = cityToAllCitiesDistance.values().stream().max(Double::compareTo).get();
        for (City from : cities) {
            for (City to : cities) {
                CityPair cp = new CityPair(from, to);
                if (from.equals(to)) {
                    this.usefulnessMap.put(cp, 1d);
                } else {
                    double usefulness = (cityToAllCitiesDistance.get(from) + cityToAllCitiesDistance.get(to)) / (2 * maxDistance);
                    this.usefulnessMap.put(cp, usefulness);
                }
            }
        }
    }

    public double getUsefulness(Task task) {
        CityPair cp = new CityPair(task);
        if (usefulnessMap.containsKey(cp)) {
            return usefulnessMap.get(cp);
        }
        return 0;
    }

    private class CityPair {
        City from;
        City to;

        private CityPair(Task task) {
            this.from = task.pickupCity;
            this.to = task.deliveryCity;
        }

        private CityPair(City from, City to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CityPair cityPair = (CityPair) o;

            if (!from.equals(cityPair.from)) return false;
            return to.equals(cityPair.to);
        }

        @Override
        public int hashCode() {
            int result = from.hashCode();
            result = 31 * result + to.hashCode();
            return result;
        }
    }
}
