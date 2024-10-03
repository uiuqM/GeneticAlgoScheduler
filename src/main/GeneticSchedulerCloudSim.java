package main;
import org.cloudbus.cloudsim.*;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class GeneticSchedulerCloudSim {

    public static void main(String[] args) {
        int numUsers = 1;
        Calendar calendar = Calendar.getInstance();
        boolean traceFlag = false;
        CloudSim.init(numUsers, calendar, traceFlag);

        Datacenter datacenter0 = createDatacenter("Datacenter_0", 0);
        Datacenter datacenter1 = createDatacenter("Datacenter_1", 1);      

        DatacenterBroker broker = createBroker();
        int brokerId = broker.getId();

        List<Vm> vmList = new ArrayList<>();
        int vmid = 0;
        int mips = 1000;
        long size = 1000;
        int ram = 256;
        long bw = 500;
        int pesNumber = 1;
        String vmm = "Xen";

        for (int i = 0; i < 5; i++) {
            Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmList.add(vm);
            vmid++;
        }
        
        vmid = 11;
        mips = 100;
        size = 1000;
        ram = 512;
        bw = 1000;
        pesNumber = 2;
        vmm = "Xen";

        for (int i = 0; i < 5; i++) {
            Vm vm = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmList.add(vm);
            vmid++;
        }

        List<Cloudlet> cloudletList = new ArrayList<>();
        int id = 0;
        long length = 1000;
        long fileSize = 3000;
        long outputSize = 3000;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < 10; i++) {
            Cloudlet cloudlet = new 
            		Cloudlet(
            		id, length, pesNumber, fileSize, 
            		outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
            id++;
        }

        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);
        
        //GeneticAlgorithmScheduler geneticScheduler = new GeneticAlgorithmScheduler(vmList, cloudletList, broker);
        //geneticScheduler.schedule();

        CloudSim.startSimulation();

        List<Cloudlet> newList = broker.getCloudletReceivedList();

        CloudSim.stopSimulation();

        printCloudletList(newList);
        //printRam(vmList);
        //printEnergyConsumption(datacenter0);
        //printEnergyConsumption(datacenter1);
        //printCost(newList);
        printMakespan(newList);
        WriteFileResults(newList);
    }

    private static Datacenter createDatacenter(String name, int id) {
        List<Host> hostList = new ArrayList<>();

        int mips = 10000;
        int ram = 2048;
        long storage = 1000000;
        long bw = 10000;

        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(id, new PeProvisionerSimple(mips)));
        hostList.add(new Host(id, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList)));

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 10.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.002;

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return broker;
    }

    private static void printCloudletList(List<Cloudlet> list) {
    	int size = list.size();
    	Cloudlet cloudlet;
    	String indent = "    ";
		System.out.println();
		System.out.println("========== OUTPUT ==========");
		System.out.println("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet value : list) {
			cloudlet = value;
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);
				Log.print("SUCCESS");

				System.out.println(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) +
						indent + indent + dft.format(cloudlet.getFinishTime()));
			if (cloudlet.getStatus() != Cloudlet.SUCCESS) {
				System.out.println("Houve falha na tarefa");
			}
		}
		
		/*
        System.out.println();
        System.out.println("=========== RESULTADO DA SIMULAÇÃO ============");
        System.out.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        for (Cloudlet cloudlet : list) {
            System.out.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                System.out.println("SUCCESS" + indent + indent + cloudlet.getResourceId() + indent + indent + cloudlet.getVmId() + indent + indent + cloudlet.getActualCPUTime() + indent + indent + cloudlet.getExecStartTime() + indent + indent + cloudlet.getFinishTime());
            }
        }
        */
    }
    private static void printRam(List<Vm> vmList) {
        System.out.println("VM ID | Total Execution Time");
        for (Vm vm : vmList) {
            System.out.println(vm.getId() + " | " + vm.getRam());
        }
    }

    private static void printEnergyConsumption(Datacenter datacenter) {
        if (datacenter instanceof PowerDatacenter) {
            PowerDatacenter powerDatacenter = (PowerDatacenter) datacenter;
            System.out.println("Total Energy Consumption: " + powerDatacenter.getPower());
        }
    }

    private static void printCost(List<Cloudlet> cloudletList) {
        System.out.println("VM ID | Total Cost");
        for (Cloudlet cloudlet : cloudletList) {
            System.out.println(cloudlet.getProcessingCost());
        }
    }

    private static void printMakespan(List<Cloudlet> cloudletList) {
        double makespan = 0.0;
        for (Cloudlet cloudlet : cloudletList) {
            makespan = Math.max(makespan, cloudlet.getFinishTime());
        }
        System.out.println("Makespan: " + makespan);
    }
    private static void WriteFileResults(List<Cloudlet> cloudletList) {
		try {
			FileWriter writter = new FileWriter("/home/uiuq/Documentos/result.csv");
			PrintWriter pW = new PrintWriter(writter);
			StringBuilder sb = new StringBuilder();
		    sb.append("CloudletID,StartTime,FinishTime,ExecutionTime\n");
		    for (Cloudlet cloudlet : cloudletList) {
		        sb.append(cloudlet.getCloudletId()).append(",");
		        sb.append(cloudlet.getExecStartTime()).append(",");
		        sb.append(cloudlet.getFinishTime()).append(",");
		        sb.append(cloudlet.getActualCPUTime()).append("\n");
		    }
		    pW.write(sb.toString());
		    pW.close();
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
}
