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

# CONSUMIDORES

Creamos una nueva clase java ConsumerDemo.java, con un main

```java
public class ConsumerDemo {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(ConsumerDemo.class);

    }
}
```

Creamos las properties y consultamos el manual de properties


se usa `auto.offset.reset` para definir el comportamiento del consumidor cuando no hay una posición comprometida (que sería el caso cuando el grupo se inicializa por primera vez o cuando un desplazamiento está fuera de rango). Puede elegir restablecer la posición al desplazamiento "más antiguo (earliest)" o al desplazamiento "más reciente (latest)" (el valor predeterminado). También se puede seleccionar "ninguno (none)" si se prefiere establecer el desplazamiento inicial usted mismo y está dispuesto a manejar los errores fuera de rango manualmente.

```java
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_DOC, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    // al final un grupo es una aplicación en cierto sentido. profundizar en este punto
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "mi_aplicacion");
    // para definir, cuando levantamos un consumidor por primera vez, cual es su offset
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
```

Creamos el consumidor

```java
    // Crear el consumidor
    KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String,String>(properties); 

    // Suscribir a los topics ( a un array de topics)
    kafkaConsumer.subscribe(Arrays.asList("partitions", "primer-topic"));
```

Ahora el `poll` 


```java
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
```

# Consumer groups

Podemos comprobar en este momento que a pesar del earliest, si no cambiamos de grupo ya el offset del grupo ya está...
Comentar que es el equivalente a --from-beginning del CLI


Revisamos los grupos

```
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
mi_aplicacion
```

Y posteriormente los offsets de cada grupo
```shell
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group mi_aplicacion --describe

GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID                                     HOST            CLIENT-ID
mi_aplicacion   partitions      0          0               0               0               consumer-1-5f29404b-6f54-4557-9243-25ee0bf8c1bd /172.24.0.1     consumer-1
mi_aplicacion   primer-topic    0          22              22              0               consumer-1-5f29404b-6f54-4557-9243-25ee0bf8c1bd /172.24.0.1     consumer-1
mi_aplicacion   partitions      1          12              12              0               consumer-1-5f29404b-6f54-4557-9243-25ee0bf8c1bd /172.24.0.1     consumer-1
mi_aplicacion   partitions      2          18              18              0               consumer-1-5f29404b-6f54-4557-9243-25ee0bf8c1bd /172.24.0.1     consumer-1

```

Echar un vistazo a los offsets de cada uno, al lag (diferencia) , el consumirod, el grupo...

Ahora:

> Si cambiamos el grupo los offsets son otros... claro... probarlo, otra opción para volver a empezar desde el principio sería resetear los offsets

# Rebalancing

Vamos a ver algo sobre rebalancing.

Teniendo un terminal con un consumidor (en un grupo) arrancamos otro. Tenemos algo parecido a esto:

```log
[main] INFO org.apache.kafka.common.utils.AppInfoParser - Kafka version: 2.3.1
[main] INFO org.apache.kafka.common.utils.AppInfoParser - Kafka commitId: 18a913733fb71c01
[main] INFO org.apache.kafka.common.utils.AppInfoParser - Kafka startTimeMs: 1574886754605
[main] INFO org.apache.kafka.clients.consumer.KafkaConsumer - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Subscribed to topic(s): partitions, primer-topic
[main] INFO org.apache.kafka.clients.Metadata - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Cluster ID: mcjk0dLgTsuMTBvndD_wjQ
[main] INFO org.apache.kafka.clients.consumer.internals.AbstractCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Discovered group coordinator 172.24.0.4:9092 (id: 2147483646 rack: null)
[main] INFO org.apache.kafka.clients.consumer.internals.ConsumerCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Revoking previously assigned partitions []
[main] INFO org.apache.kafka.clients.consumer.internals.AbstractCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] (Re-)joining group
[main] INFO org.apache.kafka.clients.consumer.internals.AbstractCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Successfully joined group with generation 2
[main] INFO org.apache.kafka.clients.consumer.internals.ConsumerCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Setting newly assigned partitions: partitions-2
[main] INFO org.apache.kafka.clients.consumer.internals.ConsumerCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Setting offset for partition partitions-2 to the committed offset FetchPosition{offset=18, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=172.24.0.4:9092 (id: 1 rack: null), epoch=-1}}
```

Primero revocará las particiones asignadas:

`Revoking previously assigned partitions []` (no tenía ninguna, claro, es nuevo)

Siguiente se une al grupo:

`Successfully joined group with generation 2`

Y por último hace el rebalance de las particiones:

`Setting newly assigned partitions: partitions-2`

Y por último los offsets de la partición:

`Setting offset for partition partitions-2 to the committed offset FetchPosition{offset=18, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=172.24.0.4:9092 (id: 1 rack: null), epoch=-1}}`


Mientras tanto, en el primer proceso ha ocurrido lo equivalente:

```log
[main] INFO org.apache.kafka.clients.consumer.internals.AbstractCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] (Re-)joining group
[main] INFO org.apache.kafka.clients.consumer.internals.AbstractCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Successfully joined group with generation 2
[main] INFO org.apache.kafka.clients.consumer.internals.ConsumerCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Setting newly assigned partitions: partitions-1, partitions-0, primer-topic-0
[main] INFO org.apache.kafka.clients.consumer.internals.ConsumerCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Setting offset for partition partitions-1 to the committed offset FetchPosition{offset=12, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=172.24.0.4:9092 (id: 1 rack: null), epoch=-1}}
[main] INFO org.apache.kafka.clients.consumer.internals.ConsumerCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Setting offset for partition primer-topic-0 to the committed offset FetchPosition{offset=22, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=172.24.0.4:9092 (id: 1 rack: null), epoch=-1}}
[main] INFO org.apache.kafka.clients.consumer.internals.ConsumerCoordinator - [Consumer clientId=consumer-1, groupId=mi_aplicacion] Setting offset for partition partitions-0 to the committed offset FetchPosition{offset=0, offsetEpoch=Optional.empty, currentLeader=LeaderAndEpoch{leader=172.24.0.4:9092 (id: 1 rack: null), epoch=-1}}
```

Comprobamos las nuevas particiones de cada uno:

Primer consumidor:

`Setting newly assigned partitions: partitions-1, partitions-0, primer-topic-0`

SEgundo consumidor:

`Setting newly assigned partitions: partitions-2` 


Si arrancamos un tercer proceso podemos continuar viendo las consecuencias. Luego eliminar uno y ver el rebalancing en sentido contrario, o poner un cuarto (con tres particiones) para ver que no se asigna nada. Llegar hasta cero y volver a levantar...

# Hilos en el consumidor

El while(true) no es muy elegante y de alguna forma está bloqueando la ejecución de java en sí, a no ser que levantemos un hilo.

Para ello creamos una nueva clase que implemente Runnable, ConsumeThread por ejemplo. Puede estar dentro de la clase ConsumerDemo.

```java
    public class ConsumerThread implements Runnable {
    }
```

implementamos los métodos.

```java
        public class ConsumerThread implements Runnable {



            public void run() {
                // TODO Auto-generated method stub

            }
```

Creo un constructor para la clase

```java
    private CountDownLatch latch = null;

    // CountDownLatch bloquea todos los hilos hasta que el conteo llegue a cero. 
    // Cuando el conteo llega a cero, todos los hilos de espera se vuelven a habilitar.
    public ConsumerThread(CountDownLatch latch) {
        this.latch = latch;

    }
```

Luego en el `run` pongo toda la iteración infinita (el `while(true)`)

Y crearé el Kafka consumer en el constructor, esperando properties por ejemplo:

```java
     // habilitar.
    public ConsumerThread(CountDownLatch latch, Properties properties) {
        this.latch = latch;
        // Crear el consumidor
        this.kafkaConsumer = new KafkaConsumer<String, String>(properties);

        // Suscribir a los topics ( a un array de topics)
        this.kafkaConsumer.subscribe(Arrays.asList("partitions", "primer-topic"));
            }
```




















