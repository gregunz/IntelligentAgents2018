package algo;

import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;
import print.PrintHandler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskImportanceEstimator {

    private final Topology topology;
    private final TaskDistribution distribution;
    private final Map<CityPair, Double> posImportanceMap; // values are between 0 and 1 (one being the more useful)
    private final Map<CityPair, Double> probImportanceMap; // values are between 0 and 1 (one being the more useful)
    private final double maxCap;

    private double minWeight = Double.POSITIVE_INFINITY;
    private double maxWeight = Double.NEGATIVE_INFINITY;

    private double posWeight = 1. / 3.;
    private double probWeight = 1. / 3.;
    private double weightWeight = 1. / 3.;

    public TaskImportanceEstimator(Agent agent, Topology topology, TaskDistribution distribution) {
        this.topology = topology;
        this.distribution = distribution;
        this.posImportanceMap = new HashMap<>();
        this.probImportanceMap = new HashMap<>();
        double maxCap = 0;
        for (Vehicle v : agent.vehicles()) {
            if (v.capacity() > maxCap) {
                maxCap = v.capacity();
            }
        }
        this.maxCap = maxCap;
        init();
    }

    public double get(Task task) {
        if (task.weight > maxCap) { // impossible to deliver
            return 0;
        }

        // to keep weight normalized (though past weight have wrong values then) // can be improved with an empirical estimation approach
        minWeight = Math.min(task.weight, minWeight);
        maxWeight = Math.max(task.weight, maxWeight);
        if (maxWeight == minWeight) { // to avoid nan values at start
            maxWeight += 1;
        }
        // normalized
        double weightImportance = (maxWeight - task.weight) / (maxWeight - minWeight); // 1 = most important

        CityPair cp = new CityPair(task);

        if (cp.from.equals(cp.to)) { // could take it and deliver it directly, what's better ?
            return 1 * (posWeight + probWeight) + weightImportance * weightWeight;
        }

        PrintHandler.println("importances: position = " + posImportanceMap.get(cp) + ", probability = " +
                probImportanceMap.get(cp) + ", weight = " + weightImportance, 2);

        return posWeight * posImportanceMap.get(cp) +
                probWeight * probImportanceMap.get(cp) +
                weightWeight * weightImportance;
    }

    public void setWeights(double posWeight, double probWeight, double weightWeight) {
        double sum = posWeight + probWeight + weightWeight;
        if (sum > 0.9999 && sum < 1.0001) {
            this.posWeight = posWeight;
            this.probWeight = probWeight;
            this.weightWeight = weightWeight;
        } else {
            PrintHandler.println("weights ignored, they should sum up to 1!");
        }
    }

    private Map<City, Double> initProbOfDeliveryCity() {
        final Map<City, Double> probOfDeliveryCityMap = new HashMap<>();
        double probOfPickupCity = 1. / topology.cities().size();

        for (City to : topology.cities()) {
            double probOfDeliveryCity = 0;
            for (City from : topology.cities()) {
                probOfDeliveryCity += probOfPickupCity * this.distribution.probability(from, to);
            }
            probOfDeliveryCityMap.put(to, probOfDeliveryCity);
        }
        return probOfDeliveryCityMap;
    }

    private Map<City, Double> initCityToAllCitiesDistance() {
        Map<City, Double> cityToAllCitiesDistance = new HashMap<>();
        for (City from : topology.cities()) {
            double distanceToAllCities = 0;
            for (City to : topology.cities()) {
                distanceToAllCities += from.distanceTo(to);
            }
            cityToAllCitiesDistance.put(from, distanceToAllCities);
        }
        return cityToAllCitiesDistance;
    }

    private void init() {
        List<City> cities = topology.cities();
        if (cities.size() > 0) {
            Map<City, Double> cityToAllCitiesDistance = initCityToAllCitiesDistance();
            Map<City, Double> probOfDeliveryCityMap = initProbOfDeliveryCity();
            double maxDistance = cityToAllCitiesDistance.values().stream().max(Comparator.naturalOrder()).get();

            double minPosImp = Double.POSITIVE_INFINITY;
            double minProbImp = Double.POSITIVE_INFINITY;

            double maxPosImp = Double.NEGATIVE_INFINITY;
            double maxProbImp = Double.NEGATIVE_INFINITY;

            for (City from : cities) {
                for (City to : cities) {
                    CityPair cp = new CityPair(from, to);
                    double bothDistances = cityToAllCitiesDistance.get(from) + cityToAllCitiesDistance.get(to);
                    double positionImportance = maxDistance - bothDistances; // the more centered (less distant to most cities), the more important
                    double probImportance = probOfDeliveryCityMap.get(to); // the most common delivery city will be given more importance than one we rarely go

                    this.posImportanceMap.put(cp, positionImportance);
                    this.probImportanceMap.put(cp, probImportance);

                    maxPosImp = Math.max(maxPosImp, positionImportance);
                    maxProbImp = Math.max(maxProbImp, probImportance);

                    minPosImp = Math.min(minPosImp, positionImportance);
                    minProbImp = Math.min(minProbImp, probImportance);
                }
            }

            // normalization
            for (City from : cities) {
                for (City to : cities) {
                    CityPair cp = new CityPair(from, to);
                    this.posImportanceMap.put(cp, (this.posImportanceMap.get(cp) - minPosImp) / (maxPosImp - minPosImp));
                    this.probImportanceMap.put(cp, (this.probImportanceMap.get(cp) - minProbImp) / (maxProbImp - minProbImp));
                }
            }
        }
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
