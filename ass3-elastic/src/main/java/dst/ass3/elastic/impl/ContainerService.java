package dst.ass3.elastic.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.transport.DockerHttpClient;
import dst.ass3.elastic.ContainerException;
import dst.ass3.elastic.ContainerInfo;
import dst.ass3.elastic.ContainerNotFoundException;
import dst.ass3.elastic.IContainerService;
import dst.ass3.messaging.Region;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class ContainerService implements IContainerService {

    private final DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("tcp://localhost:2375")
            .build();

    private static Region getWorkerRegion(Container container) {
        String image = container.getImage();
        if (StringUtils.isNotBlank(image) && image.contains("dst/ass3-worker")) {
            // Extract region out of python start command
            Region region = Region.valueOf(container.getCommand().split(" ")[2].toUpperCase());
            return region;
        }
        return null;
    }

    @Override
    public List<ContainerInfo> listContainers() throws ContainerException {
        try (DockerClient dockerClient = DockerClientBuilder.getInstance(config).build()) {

            List<Container> containers = dockerClient.listContainersCmd().exec();
            List<ContainerInfo> containerInfoList = new ArrayList<>();
            for (Container container : containers) {
                ContainerInfo containerInfo = new ContainerInfo();
                containerInfo.setImage(container.getImage());
                containerInfo.setContainerId(container.getId());
                containerInfo.setRunning(container.getState().equals("running"));
                containerInfo.setWorkerRegion(getWorkerRegion(container));
                containerInfoList.add(containerInfo);
            }
            return containerInfoList;
        } catch (Exception e) {
            throw new ContainerException("Failed to list containers", e);
        }
    }

    @Override
    public void stopContainer(String containerId) throws ContainerException {
        try (DockerClient dockerClient = DockerClientBuilder.getInstance(config).build()) {

            dockerClient.stopContainerCmd(containerId).exec();
        } catch (NotFoundException e) {
            throw new ContainerNotFoundException("Container with id " + containerId + " not found", e);
        } catch (Exception e) {
            throw new ContainerException("Failed to stop container", e);
        }
    }

    @Override
    public ContainerInfo startWorker(Region region) throws ContainerException {
        try (DockerClient dockerClient = DockerClientBuilder.getInstance(config).build()) {

            CreateContainerResponse container = dockerClient
                    .createContainerCmd("dst/ass3-worker")
                    .withHostConfig(HostConfig
                            .newHostConfig()
                            .withAutoRemove(true)
                            .withNetworkMode("dst"))
                    .withCmd(region.toString().toLowerCase())
                    .exec();
            dockerClient.startContainerCmd(container.getId()).exec();

            ContainerInfo containerInfo = new ContainerInfo();
            containerInfo.setImage("dst/ass3-worker");
            containerInfo.setContainerId(container.getId());
            containerInfo.setWorkerRegion(region);

            InspectContainerResponse inspectContainer = dockerClient.inspectContainerCmd(container.getId()).exec();
            try {
                containerInfo.setRunning(inspectContainer.getState().getRunning());
            } catch (NullPointerException e) {
                containerInfo.setRunning(false);
            }

            System.out.println("Started container " + containerInfo.getContainerId() + " in region " + region);
            return containerInfo;
        } catch (Exception e) {
            throw new ContainerException("Failed to start worker", e);
        }
    }
}
