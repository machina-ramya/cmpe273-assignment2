package edu.sjsu.cmpe.library;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

import edu.sjsu.cmpe.library.api.resources.BookResource;
import edu.sjsu.cmpe.library.api.resources.RootResource;
import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;
import edu.sjsu.cmpe.library.ui.resources.HomeResource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;

public class LibraryService extends Service<LibraryServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ExecutorService executor;
    public static String topicName = "";

    public static void main(String[] args) throws Exception {
    	new LibraryService().run(args);
    }

    @Override
    public void initialize(Bootstrap<LibraryServiceConfiguration> bootstrap) {
		bootstrap.setName("library-service");
		bootstrap.addBundle(new ViewBundle());
		bootstrap.addBundle(new AssetsBundle());
    }

    @Override
    public void run(LibraryServiceConfiguration configuration,
	    			Environment environment) throws Exception {
    	executor = Executors.newFixedThreadPool(1);

    	// This is how you pull the configurations from library_x_config.yml
		String queueName = configuration.getStompQueueName();
		topicName = configuration.getStompTopicName();
		log.debug("{} - Queue name is {}. Topic name is {}",
			configuration.getLibraryInstanceName(), queueName,
			topicName);
		
		// TODO: Apollo STOMP Broker URL and login

		/** Root API */
		environment.addResource(RootResource.class);
		/** Books APIs */
		BookRepositoryInterface bookRepository = new BookRepository();
		environment.addResource(new BookResource(bookRepository, configuration));

		/** UI Resources */
		environment.addResource(new HomeResource(bookRepository));

		Runnable backgroundTask = new Runnable() {
     		@Override
    	    public void run() {
    	    	final String STUDENT_ID = "34375";
			    final int PORT_NUM      = 61613;
			    final String USERNAME   = "admin";
			    final String PWD        = "password";
			    final String HOST 		= "54.215.210.214";//"localhost";
			    final String TOPIC_NAME = LibraryService.topicName;
			    System.out.println(TOPIC_NAME);

    			try{
    				StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
		    		factory.setBrokerURI("tcp://" + HOST + ":" + PORT_NUM);

			        Connection connection = factory.createConnection(USERNAME, PWD);
			        connection.start();
			        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			        Destination dest = new StompJmsDestination(TOPIC_NAME);

			        MessageConsumer consumer = session.createConsumer(dest);
			        
			        String body = "";
        
		        	while(true) {
			            Message msg = consumer.receive();
			            if( msg instanceof  TextMessage ) {
		                    body = ((TextMessage) msg).getText();
		                    System.out.println("Received message = " + body);
			            } else if (msg == null) {
		                  	System.out.println("No new messages.");
		                  	break;
			            } else {
		                	System.out.println("Unexpected message type: " + msg.getClass());
			            }
		        	} // end while loop
		        
		        	if(!body.equals("")){
			        	
			        }

		        	connection.close();
		        	System.out.println("Done");
			    } catch (Exception e) {
			    	System.out.println("Exception = "+e.getMessage());
			    }
    	    }
    	};
 
    	executor.execute(backgroundTask);
    }
}
