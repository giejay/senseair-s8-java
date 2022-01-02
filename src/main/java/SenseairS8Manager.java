import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SenseairS8Manager {
    private static final Logger LOGGER = Logger.getLogger(SenseairS8Manager.class.getSimpleName());
    private static MqttClient mqttClient;

    public static void main(String[] args) {
        final SenseairS8Driver driver = new SenseairS8Driver();
        try {
            mqttClient = new MqttClient("tcp://192.168.2.15:1883", "senseair");
        } catch (MqttException e) {
            LOGGER.severe("Could not initialize client: " + e.getMessage());
            throw new RuntimeException("Could not initialize client", e);
        }

        ScheduledFuture<?> future = Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(
                        () -> {
                            final Integer measure = driver.measure();
                            LOGGER.info("CO2: " + measure + " ppm");
                            try {
                                publish(measure);
                            } catch (MqttException e) {
                                if (e.getMessage().contains("Client is not connected")) {
                                    LOGGER.info("Reconnecting client");
                                    connect();
                                    try {
                                        publish(measure);
                                    } catch (MqttException ex) {
                                        LOGGER.severe("Retry: Could not publish to MQTT: " + e.getMessage());
                                    }
                                } else {
                                    LOGGER.severe("Could not publish to MQTT: " + e.getMessage());
                                }
                            }
                        },
                        0L,
                        10,
                        TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!future.isDone())
                future.cancel(true);
        }));
    }

    private static void publish(Integer measure) throws MqttException {
        mqttClient.publish("senseair-s8", new MqttMessage(String.valueOf(measure).getBytes(StandardCharsets.UTF_8)));
    }

    private static void connect() {
        try {
            mqttClient.connect();
        } catch (MqttException e) {
            throw new RuntimeException("Could not connect to mqtt broker: " + e.getMessage());
        }
    }

}
