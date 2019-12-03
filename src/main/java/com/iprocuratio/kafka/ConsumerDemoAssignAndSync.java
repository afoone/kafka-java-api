package com.iprocuratio.kafka;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerConfig;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConsumerDemo
 */
public class ConsumerDemoAssignAndSync {

    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(ConsumerDemo.class);

        // Creamos las propiedades
        Properties properties = new Properties();

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // al final un grupo es una aplicación en cierto sentido. profundizar en este
        // punto

        // ASSIGN AND SEEK: NO TENGO GROUPO ID
       // properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "mi_aplicacion");
        // para definir, cuando levantamos un consumidor por primera vez, cual es su
        // offset
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // el latch para cuando tenemos múltiples threads
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        // creamos el runnable del consumidor
        final Runnable kafkaConsumer = new ConsumerThread(countDownLatch, properties);

        // Arrancamos el thread
        Thread miThread = new Thread(kafkaConsumer);
        logger.debug("creando el thread");
        miThread.start();

        // creamos el shutdown hook con una función anónima
        // addShutdownHook() will register some actions which is to be performed on a
        // Program's termination. The program that you start ends in two ways:

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("gracefully stopping...");
                ((ConsumerThread) kafkaConsumer).shutdown();
                // ESperamos que termine el shutdown
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    logger.info("terminado.");
                }
            }

        });

        // aquí esperamos que termine
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            // La aplicación se esta
            logger.info("la aplicación ha sido interrumpida");
        } finally {
            logger.info("la aplicación se ha cerrado");
        }

    }
}