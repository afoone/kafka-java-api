package com.iprocuratio.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    logger.info("key", consumerRecord.key());
                    logger.info("value", consumerRecord.value());
                    logger.info("offset", consumerRecord.offset());
                    logger.info("partition", consumerRecord.partition());

                }
            }
            
        } catch (WakeupException e) {
            logger.error("Recibida señal de shutdown", e);
        } finally {
            // Aquí cerraremos el consumidor, tras la señan de shutdown
            kafkaConsumer.close();
            // le cominicamos a nuestro main code que hemos terminado con el consumidor
            latch.countDown();
        }
       
    }

    public void shutdown(){
        // El método wakeup lo que hace es interrumpir el poll, esto es el while(true)
        kafkaConsumer.wakeup();
    }

}