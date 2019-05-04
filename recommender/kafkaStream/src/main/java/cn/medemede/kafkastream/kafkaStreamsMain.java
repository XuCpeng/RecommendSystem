package cn.medemede.kafkastream;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class kafkaStreamsMain {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: kafkaStream <brokers> <zookeepers> <from> <to>\n" +
                    "  <brokers> is a list of one or more Kafka brokers\n" +
                    "  <zookeepers> is a list of one or more Zookeeper nodes\n" +
                    "  <from> is a topic to consume from\n" +
                    "  <to> is a topic to product to\n\n");
            System.exit(1);
        }
        String brokers = args[0];
        String zookeepers = args[1];
        String from = args[2];
        String to = args[3];

        Properties settings = new Properties();
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "logFilter");
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
//        settings.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, zookeepers);


        Topology builder = new Topology();

        builder.addSource("SOURCE", from)
                .addProcessor("PROCESS", LogProcessor::new, "SOURCE")
                .addSink("SINK", to, "PROCESS");

        KafkaStreams streams = new KafkaStreams(builder, settings);

        final CountDownLatch latch = new CountDownLatch(1);
        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
