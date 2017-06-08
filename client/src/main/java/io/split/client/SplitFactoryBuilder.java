package io.split.client;

import io.split.grammar.Treatments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

/**
 * Builds an instance of SplitClient.
 */
public class SplitFactoryBuilder {
    private static final Logger _log = LoggerFactory.getLogger(SplitFactoryBuilder.class);

    /**
     * Instantiates a SplitFactory with default configurations
     *
     * @param apiToken the API token. MUST NOT be null
     * @return a SplitFactory
     * @throws IOException                           if the SDK was being started in 'localhost' mode, but
     *                                               there were problems reading the override file from disk.
     * @throws java.lang.InterruptedException        if you asked to block until the sdk was
     *                                               ready and the block was interrupted.
     * @throws java.util.concurrent.TimeoutException if you asked to block until the sdk was
     *                                               ready and the timeout specified via config#ready() passed.
     */
    public static SplitFactory build(String apiToken) throws IOException, InterruptedException, TimeoutException, URISyntaxException {
        return build(apiToken, SplitClientConfig.builder().build());
    }

    /**
     * @param apiToken the API token. MUST NOT be null
     * @param config   parameters to control sdk construction. MUST NOT be null.
     * @return a SplitFactory
     * @throws java.io.IOException                   if the SDK was being started in 'localhost' mode, but
     *                                               there were problems reading the override file from disk.
     * @throws InterruptedException                  if you asked to block until the sdk was
     *                                               ready and the block was interrupted.
     * @throws java.util.concurrent.TimeoutException if you asked to block until the sdk was
     *                                               ready and the timeout specified via config#ready() passed.
     */
    public static synchronized SplitFactory build(String apiToken, SplitClientConfig config) throws IOException, InterruptedException, TimeoutException, URISyntaxException {
        if (LocalhostSplitFactory.LOCALHOST.equals(apiToken)) {
            return LocalhostSplitFactory.createLocalhostSplitFactory();
        } else {
            return new SplitFactoryImpl(apiToken, config);

        }
    }

    /**
     * Instantiates a local Off-The-Grid SplitFactory
     *
     * @return a SplitFactory
     * @throws IOException if there were problems reading the override file from disk.
     */
    public static SplitFactory local() throws IOException {
        return LocalhostSplitFactory.createLocalhostSplitFactory();
    }

    /**
     * Instantiates a local Off-The-Grid SplitFactory
     *
     * @param home A directory containing the .split file from which to build treatments. MUST NOT be null
     * @return a SplitFactory
     * @throws IOException if there were problems reading the override file from disk.
     */
    public static SplitFactory local(String home) throws IOException {
        return new LocalhostSplitFactory(home);
    }

    public static void main(String... args) throws IOException, InterruptedException, TimeoutException, URISyntaxException {
        if (args.length != 1) {
            System.out.println("Usage: <api_token>");
            System.exit(1);
            return;
        }

        SplitClientConfig config = SplitClientConfig.builder().build();
        SplitClient client = SplitFactoryBuilder.build("API_KEY", config).client();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if ("exit".equals(line)) {
                    System.exit(0);
                }
                String[] userIdAndSplit = line.split(" ");

                if (userIdAndSplit.length != 2) {
                    System.out.println("Could not understand command");
                    continue;
                }

                boolean isOn = client.getTreatment(userIdAndSplit[0], userIdAndSplit[1]).equals("on");

                System.out.println(isOn ? Treatments.ON : Treatments.OFF);
            }
        } catch (IOException io) {
            _log.error(io.getMessage(), io);
        }
    }
}
