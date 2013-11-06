package edu.sjsu.cmpe.procurement.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONArray;
import org.json.JSONObject;


import de.spinscale.dropwizard.jobs.Job;
import de.spinscale.dropwizard.jobs.annotations.Every;
import edu.sjsu.cmpe.procurement.ProcurementService;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.DeliveryMode;


import java.util.*;
import java.lang.String;

import edu.sjsu.cmpe.procurement.config.ProcurementServiceConfiguration;
import edu.sjsu.cmpe.procurement.domain.BookArrivals;
import edu.sjsu.cmpe.procurement.domain.Book;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jackson.map.ObjectMapper;


/**
 * This job will run at every 5 second.
 */

@Every("60s")
public class ProcurementSchedulerJob extends Job {
    private final Logger log = LoggerFactory.getLogger(getClass());
    ArrayList<String> bookList = new ArrayList<String>();

    private final String STUDENT_ID = "34375";
    private final int PORT_NUM      = 61613;
    private final String USERNAME   = "admin";
    private final String PWD        = "password";
    private final String HOST 		= "54.215.210.214";//"localhost";

    @Override
    public void doJob() 
    {
		String aQueueName = "/queue/" + STUDENT_ID + ".book.orders";
	    
	    try{
    		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
    		factory.setBrokerURI("tcp://" + HOST + ":" + PORT_NUM);

	        Connection connection = factory.createConnection(USERNAME, PWD);
	        connection.start();
	        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	        Destination dest = new StompJmsDestination(aQueueName);

	        MessageConsumer consumer = session.createConsumer(dest);
	        System.out.println("Waiting for messages from " + aQueueName + "...");
       
	        long waitUntil = 5 * 1000; // wait for 5 sec
        	String body = "";
        
        	while(true) {
	            Message msg = consumer.receive(waitUntil);
	            if( msg instanceof  TextMessage ) {
	                   body = ((TextMessage) msg).getText();
	                   System.out.println("Received message = " + body);
	                   String[] bookIsbn = body.split(":");
	                   bookList.add(bookIsbn[1]);
	            } else if (msg == null) {
	                  System.out.println("No new messages.Exiting due to timeout - " + waitUntil / 1000 + " sec");
	                  break;
	            } else {
	                 System.out.println("Unexpected message type: " + msg.getClass());
	            }
        	} // end while loop
        
        	if(!body.equals("")){
	        	// POST method to the publisher
	        	SendOrderBookMsgToPublisher(bookList);

	        	//GET books from publisher
	        	ReceiveBooksFromPublisher();
	        }

        	connection.close();
        	System.out.println("Done");
	    } catch (Exception e) {
	    	System.out.println("Exception = "+e.getMessage());
	    }
    }

    private void SendOrderBookMsgToPublisher(ArrayList<String> aBookList) throws Exception
    {
    	String orderBookApiUrl = "http://54.215.210.214:9000/orders";

    	try {
			Client client = Client.create();
	 
			WebResource webResource = client.resource(orderBookApiUrl);

			String input = "{\"id\":\"34375\", \"order_book_isbns\": "+ "["+ aBookList+"]}";

			ClientResponse response = webResource.type("application/json")
				   					  .post(ClientResponse.class, input);
		 
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}
	 
			System.out.println("Output from Server .... \n");
			String output = response.getEntity(String.class);
			System.out.println(output);
 
	  	} catch (Exception e) {
 			e.printStackTrace();
	  	}
    }


    private void ReceiveBooksFromPublisher() throws Exception
    {
    	String receiveOrderApiUrl = "http://54.215.210.214:9000/orders/" + STUDENT_ID;
    	try {
 
			Client client = Client.create();
	 
			WebResource webResource = client.resource(receiveOrderApiUrl);
	 
			ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
	 
			if (response.getStatus() != 200) {
			   throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}
	 
	 		System.out.println("Output from Server .... \n");
			ObjectMapper mapper = new ObjectMapper();
			BookArrivals bookArrivals = mapper.readValue(response.getEntity(String.class), BookArrivals.class);
			System.out.println(bookArrivals);

			// Send messages for each book.
			ListIterator<Book> li = bookArrivals.getShippedBooks().listIterator();
			while(li.hasNext()) {
				sendTopicMsgToSubscribers(li.next());
			}

	 	} catch (Exception e) {
	 
			e.printStackTrace();
	 
		}
    }

    private void sendTopicMsgToSubscribers(Book aBook) throws JMSException {

    	if( aBook == null ) {
    		return;
    	}

    	String destination = "/topic/"+STUDENT_ID+".book."+aBook.getCategory();

		StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
		factory.setBrokerURI("tcp://" + HOST + ":" + PORT_NUM);

		Connection connection = factory.createConnection(USERNAME, PWD);
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination dest = new StompJmsDestination(destination);
		MessageProducer producer = session.createProducer(dest);
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);

		TextMessage msg = session.createTextMessage(aBook.format());
		msg.setLongProperty("id", System.currentTimeMillis());
		producer.send(msg);

		connection.close();
    }
}
