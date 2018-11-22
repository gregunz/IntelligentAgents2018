package algo;

import logist.agent.Agent;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;
import print.PrintHandler;

import java.util.*;

public class TaskImportanceEstimator {

    private final Topology topology;
    private final TaskDistribution distribution;
    private final Map<CityPair, Double> posImportanceMap; // values are between -1 and 1
    private final Map<CityPair, Double> probImportanceMap; // values are between -1 and 1
    private final List<Double> marginalCostsDif;

    private int minWeight = Integer.MAX_VALUE;
    private int maxWeight = Integer.MIN_VALUE;

    private final double posWeight;
    private final double probWeight;
    private final double weightWeight;
    private final double marginalWeight;

    public TaskImportanceEstimator(Agent agent, Topology topology, TaskDistribution distribution, double posWeight,
                                   double probWeight, double weightWeight, double marginalWeight) {
        this.topology = topology;
        this.distribution = distribution;
        this.posImportanceMap = new HashMap<>();
        this.probImportanceMap = new HashMap<>();
        this.marginalCostsDif = new ArrayList<>();
        this.marginalCostsDif.add(100d); // in order to have values for max and min when computing dif
        this.marginalCostsDif.add(-100d);

        double dif = Math.abs(posWeight + probWeight + weightWeight + marginalWeight - 1);
        if (dif > 0.01) {
            throw new IllegalArgumentException("weights must sum to 1");
        }
        this.posWeight = posWeight;
        this.probWeight = probWeight;
        this.weightWeight = weightWeight;
        this.marginalWeight = marginalWeight;
        init();
    }

    public double get(Task task, double marginalDif) {

        minWeight = Math.min(task.weight, minWeight);
        maxWeight = Math.max(task.weight, maxWeight);
        if (maxWeight == minWeight) { // to avoid nan values at start
            maxWeight++;
            minWeight--;
        }
        double weightImportance = 2. * (maxWeight - task.weight) / (maxWeight - minWeight) - 1;
        double marginalImportance = marginCostDifNormalized(marginalDif);

        CityPair cp = new CityPair(task);

        PrintHandler.println("[GET] importances: position = " + posImportanceMap.get(cp) + ", probability = " +
                probImportanceMap.get(cp) + ", weight = " + weightImportance + ", marginalImportance = " + marginalImportance, 2);

        return posWeight * posImportanceMap.get(cp) +
                probWeight * probImportanceMap.get(cp) +
                weightWeight * weightImportance +
                marginalWeight * marginalImportance;
    }

    public boolean mustComputeMarginalDif() {
        return marginalWeight > 0.01;
    }

    private double marginCostDifNormalized(double dif) {
        marginalCostsDif.add(dif);
        double maxDif = Math.max(0, marginalCostsDif.stream().max(Comparator.naturalOrder()).get());
        double minDif = Math.min(0, marginalCostsDif.stream().min(Comparator.naturalOrder()).get());
        maxDif = Math.max(maxDif, -minDif);
        minDif = Math.min(minDif, -maxDif);
        return 2 * ((dif - minDif) / (maxDif - minDif)) - 1;
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
                    double probImportance = probOfDeliveryCityMap.get(from); // if the city of pickup is likely to be a deliver city, the more important

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
                    this.posImportanceMap.put(cp, 2 * (this.posImportanceMap.get(cp) - minPosImp) / (maxPosImp - minPosImp) - 1);
                    this.probImportanceMap.put(cp, 2 * (this.probImportanceMap.get(cp) - minProbImp) / (maxProbImp - minProbImp) - 1);
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
