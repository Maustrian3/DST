package dst.ass3.elastic.impl;

import dst.ass3.elastic.ContainerException;
import dst.ass3.elastic.ContainerInfo;
import dst.ass3.elastic.IContainerService;
import dst.ass3.elastic.IElasticityController;
import dst.ass3.messaging.IWorkloadMonitor;
import dst.ass3.messaging.Region;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElasticityController implements IElasticityController {

    private final double scaleOutThreshold = 0.1;
    private final double scaleDownThreshold = 0.05;
    private final Map<Region, Double> rmaxValues = Map.of(
            Region.AT_LINZ, 30.0 * 1000, // 30 seconds
            Region.AT_VIENNA, 30.0 * 1000, // 30 seconds
            Region.DE_BERLIN, 120.0 * 1000 // 120 seconds
    );

    private final IContainerService containerService;
    private final IWorkloadMonitor workloadMonitor;


    public ElasticityController(IContainerService containerService, IWorkloadMonitor workloadMonitor) {
        this.containerService = containerService;
        this.workloadMonitor = workloadMonitor;
    }


    @Override
    public void adjustWorkers() throws ContainerException {
        Map<Region, Long> requestCounts = workloadMonitor.getRequestCount();
        Map<Region, Long> workerCounts = workloadMonitor.getWorkerCount();
        Map<Region, Double> avgProcessingTimes = workloadMonitor.getAverageProcessingTime();

        for (Region region : requestCounts.keySet()) {
            double rmax = rmaxValues.get(region);
            double totalWaitingTime = ((double) avgProcessingTimes.get(region) * requestCounts.get(region));
            double rexp = totalWaitingTime / workerCounts.get(region);

            long optimalWorkerCount = workerCounts.get(region);

            // Check if scaling is necessary
            if (rexp > rmax * (1 + scaleOutThreshold)) {
                while (totalWaitingTime / optimalWorkerCount > rmax) {
                    optimalWorkerCount++;
                }
            } else if (rexp < rmax * (1 - scaleDownThreshold)) {
                while (totalWaitingTime / optimalWorkerCount < rmax) {
                    optimalWorkerCount--;
                }
            }

            long workerDifference = optimalWorkerCount - workerCounts.get(region);
            if (workerDifference > 0) {
                scaleOut(region, workerDifference);
            } else if (workerDifference < 0) {
                scaleDown(region, workerDifference);
            }
        }
    }

    private void scaleOut(Region region, long workerDifference) throws ContainerException {
        System.out.println(workerDifference + " workers will be added in region " + region);
        for (int i = 0; i < workerDifference; i++) {
            containerService.startWorker(region);
        }
    }

    private void scaleDown(Region region, long workerDifference) throws ContainerException {
        System.out.println(workerDifference + " workers will be removed in region " + region);
        List<ContainerInfo> allContainers = containerService.listContainers();
        List<ContainerInfo> regionContainers = allContainers.stream()
                .filter(container -> container.getWorkerRegion().equals(region))
                .collect(Collectors.toList());

        for (int i = 0; i < Math.abs(workerDifference); i++) {
            containerService.stopContainer(regionContainers.get(i).getContainerId());
        }
    }
}
