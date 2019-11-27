package com.iprocuratio.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

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
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_DOC, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // al final un grupo es una aplicación en cierto sentido. profundizar en este punto
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "mi aplicacion");
        // para definir, cuando levantamos un consumidor por primera vez, cual es su offset
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Crear el consumidor
        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String,String>(properties); 

        // Suscribir a los topics ( a un array de topics)
        kafkaConsumer.subscribe(Arrays.asList("partitions", "primer-topic"));

        // Buscamos nuevos datos
        while (true) {
            ConsumerRecords<String, String> consumerRecords =  kafkaConsumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                logger.info("key", consumerRecord.key());
                logger.info("value", consumerRecord.value());
                logger.info("offset", consumerRecord.offset());
                logger.info("partition", consumerRecord.partition());
                
                
            }
        }


    }
}