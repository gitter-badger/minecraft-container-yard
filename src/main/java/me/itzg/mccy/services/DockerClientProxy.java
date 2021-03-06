package me.itzg.mccy.services;

import com.google.common.base.Optional;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import me.itzg.mccy.config.MccySettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

/**
 * @author Geoff Bourne
 * @since 12/21/2015
 */
@Service
public class DockerClientProxy {
    @Autowired
    private MccySettings mccySettings;

    public interface Consumer<T> {
        T use(DockerClient dockerClient) throws DockerException, InterruptedException;
    }

    public <T> T access(Consumer<T> consumer) throws DockerException, InterruptedException {

        DefaultDockerClient dockerClient = null;

        try {
            dockerClient = createClient(dockerClient);

            return consumer.use(dockerClient);
        }
        finally {
            if (dockerClient != null) {
                dockerClient.close();
            }
        }
    }

    private DefaultDockerClient createClient(DefaultDockerClient dockerClient) {
        Optional<DockerCertificates> certs = Optional.absent();
        if (mccySettings.getDockerCertPath() != null) {
            try {
                certs = DockerCertificates.builder()
                        .dockerCertPath(Paths.get(mccySettings.getDockerCertPath())).build();
            } catch (DockerCertificateException e) {
                throw new IllegalArgumentException("Unable to initialize Docker certificates with given configuration");
            }
        }

        final DefaultDockerClient.Builder clientBuilder = DefaultDockerClient.builder();

        if (certs.isPresent()) {
            clientBuilder
                    .dockerCertificates(certs.get());
        }

        dockerClient = clientBuilder
                .uri(mccySettings.getDockerHostUri())
                .build();
        return dockerClient;
    }
}
