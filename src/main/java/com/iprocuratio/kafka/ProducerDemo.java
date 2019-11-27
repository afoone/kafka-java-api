package com.iprocuratio.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class ProducerDemo {
    public static void main(String[] args) {

        final Logger logger = LoggerFactory.getLogger(ProducerDemo.class);

        // Crear properties del producer
        Properties properties = new Properties();
        // properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        // properties.setProperty("key.serializer", StringSerializer.class.getName());
        // properties.setProperty("value.serializer",StringSerializer.class.getName());

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Crear el productor

        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);

        // Enviar datos

        ProducerRecord<String, String> producerRecord = null;
        for (int i = 0; i < 10; i++) {
            final String  key = "key" + i % 3;

            producerRecord = new ProducerRecord<String, String>("partitions", key,  " mensaje " + i);
            
            kafkaProducer.send(producerRecord, new Callback() {

                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    // Se ejecuta cuando se devuelve ha terminado de enviar o bien hay una excepcion
                    if (exception == null) {
                        logger.info("enviado mensaje");
                        logger.info("topic " + metadata.topic() + "\n offset" + metadata.offset());
                        logger.info("key"+ key);
                        logger.info("partition "+metadata.partition());
                        
                    } else {
                        logger.error(exception.getLocalizedMessage(), exception);
                    }

                }
            });
        }

        // hace el flush de los datos
        kafkaProducer.flush();

        // flush y cierra
        kafkaProducer.close();
    }
}
