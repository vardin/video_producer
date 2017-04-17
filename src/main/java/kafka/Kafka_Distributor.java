package kafka;

import camera.*;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.*;  
import org.apache.kafka.common.*;

import org.opencv.core.Core;
import org.opencv.core.Size;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.Highgui;


public class Kafka_Distributor implements Runnable {

	private String Broker_URI;
	private String Kafka_Topic;
	private String cameraUrl;
	private int count;
	private Producer<String, byte[]> producer;

	// new Size(640, 480), 16

	public Kafka_Distributor() {

		Broker_URI = "163.152.174.73:9092";
		Kafka_Topic = "supercom";
//		cameraUrl = "2";
		count =0;
	}

	public void run()

	{

		boolean isOpen = false;

		VideoCapture cap = null;

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		int count = 0;
		while (!isOpen) {
			cap = new VideoCapture(0);
			isOpen = cap.open(0);
			System.out.println("Try to open times: " + ++count);
		}

		if (!isOpen) {
			System.out.println("not open the stream!");
			return;
		}

		Mat frame = new Mat();
		
	
		Properties props = new Properties();

		props.put("bootstrap.servers", Broker_URI);
	//	props.put("metadata.broker.list", Broker_URI);
		props.put("acks", "all");
		props.put("client.id", "super-group");
		
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		// props.put("serializer.class", "kafka.serializer.StringEncoder");
		
			  
        producer = new KafkaProducer<String, byte[]>(props);  
       
		  Future<RecordMetadata> a = null;  
		  
	      long frameCount = 0;  
		  

		while (true) {

			cap.read(frame);

			byte[] frameArray = new byte[((int) frame.total() * frame.channels())];
			frame.get(0, 0, frameArray);

//			System.out.println("FrameSize:" + frameArray.length);
	//		System.out.println("Mat:height " + frame.height());
	//		System.out.println("Mat: width " + frame.width());
	//		System.out.println("channels: " + frame.channels());
	//		System.out.println("type: " + frame.type());

			// KeyedMessage<String, byte[]> message = new KeyedMessage<String,
			// byte[]>(Kafka_Topic, frameArray);
			//KeyedMessage<String, byte[]> message = new KeyedMessage<String, byte[]>(Broker_URI, frameArray);
			
			ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<String, byte[]>(Kafka_Topic,Long.toString(frameCount),frameArray);

            a = producer.send(producerRecord);
                           
	          count++;
            System.out.println("Send one frame");  

                      
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
                        
		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException {

		Kafka_Distributor Distributor = new Kafka_Distributor();
		// distributor.Init(URLDecoder.decode(args[0], "UTF-8"), args[1],
		// args[2]);
		// distributor.Init(args[0], args[1], args[2]);
		Thread producerProcess = new Thread(Distributor);
		producerProcess.start();
	}

}
