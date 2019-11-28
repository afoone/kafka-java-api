package com.iprocuratio.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConsumerDemo
 */
public class ConsumerDemo {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(ConsumerDemo.class);

        // Creamos las propiedades
        Properties properties = new Properties();

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // al final un grupo es una aplicación en cierto sentido. profundizar en este
        // punto
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "mi_aplicacion");
        // para definir, cuando levantamos un consumidor por primera vez, cual es su
        // offset
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        public class ConsumerThread implements Runnable {

            private CountDownLatch latch = null;

            private KafkaConsumer<String, String> kafkaConsumer;

            Logger logger = LoggerFactory.getLogger(ConsumerThread.class);

            // CountDownLatch bloquea todos los hilos hasta que el conteo llegue a cero.
            // Cuando el conteo llega a cero, todos los hilos de espera se vuelven a
            // habilitar.
            public ConsumerThread(CountDownLatch latch, Properties properties) {
                this.latch = latch;
                // Crear el consumidor
                this.kafkaConsumer = new KafkaConsumer<String, String>(properties);

                // Suscribir a los topics ( a un array de topics)
                this.kafkaConsumer.subscribe(Arrays.asList("partitions", "primer-topic"));

            }

            public void run() {
                // Buscamos nuevos datos
                while (true) {
                    ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));
                    for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                        logger.info("key", consumerRecord.key());
                        logger.info("value", consumerRecord.value());
                        logger.info("offset", consumerRecord.offset());
                        logger.info("partition", consumerRecord.partition());

                    }
                }
            }

            public void shutdown()

        }

    }
}