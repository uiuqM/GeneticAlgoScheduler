package main;

import java.util.*;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

public class GeneticAlgorithmScheduler {
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private DatacenterBroker broker;
    private static final int POPULATION_SIZE = 10;
    private static final int GENERATIONS = 100;

    public GeneticAlgorithmScheduler(List<Vm> vmList, List<Cloudlet> cloudletList, DatacenterBroker broker) {
        this.vmList = vmList;
        this.cloudletList = cloudletList;
        this.broker = broker;
    }

    public void schedule() {
        List<List<Integer>> population = initializePopulation();

        for (int gen = 0; gen < GENERATIONS; gen++) {
            Map<List<Integer>, Double> fitnessMap = evaluatePopulation(population);

            List<List<Integer>> newPopulation = new ArrayList<>();
            while (newPopulation.size() < POPULATION_SIZE) {
                List<Integer> parent1 = selectParent(fitnessMap);
                List<Integer> parent2 = selectParent(fitnessMap);

                List<Integer> offspring = crossover(parent1, parent2);

                mutate(offspring);

                newPopulation.add(offspring);
            }
            population = newPopulation;
        }
        List<Integer> bestSolution = selectBest(population);
        System.out.println(bestSolution);
        assignCloudletsToVMs(bestSolution);
    }

    private List<List<Integer>> initializePopulation() {
        Random rand = new Random();
        List<List<Integer>> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<Integer> individual = new ArrayList<>();
            for (int j = 0; j < cloudletList.size(); j++) {
                individual.add(rand.nextInt(vmList.size()));
            }
            population.add(individual);
        }
        return population;
    }

    private Map<List<Integer>, Double> evaluatePopulation(List<List<Integer>> population) {
        Map<List<Integer>, Double> fitnessMap = new HashMap<>();
        for (List<Integer> individual : population) {
            double fitness = calculateFitness(individual);
            fitnessMap.put(individual, fitness);
        }
        return fitnessMap;
    }

    private double calculateFitness(List<Integer> individual) {
        double makespan = 0.0;
        Map<Integer, Double> vmCompletionTimes = new HashMap<>();

        for (int i = 0; i < cloudletList.size(); i++) {
            int vmIndex = individual.get(i);
            Cloudlet cloudlet = cloudletList.get(i);
            Vm vm = vmList.get(vmIndex);

            double execTime = cloudlet.getCloudletLength() / vm.getMips();
            double startTime = vmCompletionTimes.getOrDefault(vmIndex, 0.0);
            double finishTime = startTime + execTime;

            vmCompletionTimes.put(vmIndex, finishTime);
            makespan = Math.max(makespan, finishTime);
        }
        return makespan;
    }

    private List<Integer> selectParent(Map<List<Integer>, Double> fitnessMap) {
        Random rand = new Random();
        List<List<Integer>> population = new ArrayList<>(fitnessMap.keySet());
        List<Integer> parent1 = population.get(rand.nextInt(population.size()));
        List<Integer> parent2 = population.get(rand.nextInt(population.size()));
        return fitnessMap.get(parent1) < fitnessMap.get(parent2) ? parent1 : parent2;
    }

    private List<Integer> crossover(List<Integer> parent1, List<Integer> parent2) {
        Random rand = new Random();
        List<Integer> offspring = new ArrayList<>();
        for (int i = 0; i < parent1.size(); i++) {
            offspring.add(rand.nextBoolean() ? parent1.get(i) : parent2.get(i));
        }
        return offspring;
    }

    private void mutate(List<Integer> individual) {
        Random rand = new Random();
        int cloudletIndex = rand.nextInt(individual.size());
        int newVmIndex = rand.nextInt(vmList.size());
        individual.set(cloudletIndex, newVmIndex);
    }

    private List<Integer> selectBest(List<List<Integer>> population) {
        return population.stream().min(Comparator.comparingDouble(this::calculateFitness)).orElse(null);
    }

    private void assignCloudletsToVMs(List<Integer> bestSolution) {
        for (int i = 0; i < cloudletList.size(); i++) {
            int vmIndex = bestSolution.get(i);
            Cloudlet cloudlet = cloudletList.get(i);
            Vm vm = vmList.get(vmIndex);
            broker.bindCloudletToVm(cloudlet.getCloudletId(), vm.getId());
            System.out.println(cloudlet.getCloudletId() + "," + vm.getId());
        }
    }
}
