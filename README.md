# Crear el maven project

mvn archetype:generate \
	-DgroupId=com.iprocuratio.kafka \
	-DartifactId=java-api \
	-DarchetypeArtifactId=maven-archetype-quickstart \
	-DinteractiveMode=false

# abrir el visual studio

cd java-api

code .

# Insertar las dependencias de kafka

Primero la dependencia del kafka-clients
```xml
<!-- https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients -->
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>2.3.1</version>
</dependency>
```

Logger para java

```xml
<!-- https://mvnrepository.com/artifact/org.slf4j/slf4j-simple -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.29</version>
</dependency>
```

# Primer productor

Creamos la clase ProducerDemo.java (Copia de app.java)

```java
package com.iprocuratio.kafka;

/**
 * Hello world!
 *
 */
public class ProducerDemo 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
```

La probamos con Run... en Visual Studio Code.

# Esquema de las necesidades

```java
 // Crear properties del producer
 // Crear el productor 
 // Enviar datos
```

# Repasar las propiedades

EStán en 
https://docs.confluent.io/current/installation/configuration/producer-configs.html

# CRear las propiedades

```java
   // Crear properties del producer
        Properties properties = new Properties();
        // properties.setProperty("bootstrap.servers", "127.0.0.1:9092");
        // properties.setProperty("key.serializer", StringSerializer.class.getName());
        // properties.setProperty("value.serializer",StringSerializer.class.getName());

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
```

# Crear el productor

```java
    KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String,String>(properties);
```

# Enviar datos

```java
  ProducerRecord<String, String> producerRecord = new ProducerRecord<String,String>("primer-topic", "primer mensaje");
        kafkaProducer.send(producerRecord);
        // hace el flush de los datos
        kafkaProducer.flush();

        // flush y cierra
        kafkaProducer.close();
```

- Ahora probar a enviar mensajes con un Consumidor activo

# Callbacks

El producer.send TIENE una posibilidad de método con procuder record y callback

Tenemos que iniciar el logger:

```java
        final Logger logger = LoggerFactory.getLogger(ProducerDemo.class);
```

y será con callback:

```java
   kafkaProducer.send(producerRecord, new Callback() {

            public void onCompletion(RecordMetadata metadata, Exception exception) {
                // Se ejecuta cuando se devuelve ha terminado de enviar o bien hay una excepcion
                if (exception == null) {
                    logger.info("enviado mensaje");
                    logger.info("topic "+metadata.topic()+"\n offset"+metadata.offset());
                } else {
                    logger.error(exception.getLocalizedMessage(), exception);
                }

            }
        });
```

# Enviar múltiples mensajes

Quedará así:

```java
        ProducerRecord<String, String> producerRecord = null;
        for (int i = 0; i < 10; i++) {
            producerRecord = new ProducerRecord<String, String>("partitions", " mensaje " + i);
            kafkaProducer.send(producerRecord, new Callback() {

                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    // Se ejecuta cuando se devuelve ha terminado de enviar o bien hay una excepcion
                    if (exception == null) {
                        logger.info("enviado mensaje");
                        logger.info("topic " + metadata.topic() + "\n offset" + metadata.offset());
                    } else {
                        logger.error(exception.getLocalizedMessage(), exception);
                    }

                }
            });
        }
```

- Comprobar cómo se dividen en particiones diferentes
- Comprobar cómo el orden de los consumidores no se mantiene

# keys

Para garantizar el orden hay que usar keys. Para ello hay que crear un topic con varias particiones:
```sh
bin/kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --topic partitions --create --partitions 3 --replication-factor 1
```

Ojo con el producer record:
```
            producerRecord = new ProducerRecord<String, String>("partitions", " mensaje " + i);
```

Crearemos keys con el mod:

```java
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
```

- Comprobar que las keys aparecen en la misma particion




