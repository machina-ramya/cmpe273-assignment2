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
import edu.sjsu.cmpe.library.domain.Book;

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

import java.net.URL;
import java.net.MalformedURLException;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;

public class LibraryService extends Service<LibraryServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ExecutorService executor;
    public  static String topicName = "";
    private static BookRepositoryInterface bookRepository;

    public static void main(String[] args) throws Exception {
    	new LibraryService().run(args);
    }

    @Override
    public void initialize(Bootstrap<LibraryServiceConfiguration> bootstrap) {
		bootstrap.setName("library-service");
		bootstrap.addBundle(new ViewBundle());
		bootstrap.addBundle(new AssetsBundle());
    }

    public static Book convertMessage(String aMessage) {
    	if(aMessage == null || aMessage.length() <= 0) {
    		return null;
    	}

    	String[] info = aMessage.split(":");
    	if(info == null || info.length <= 0) {
    		return null;
    	}

    	Book b = new Book();
    	b.setIsbn(Integer.parseInt(info[0]));
    	b.setTitle(info[1].split("\"")[1]);
    	b.setCategory(info[2].split("\"")[1]);
    	
    	try {
    		String http = info[3].split("\"")[1];
    		String urlBody = info[4].split("\"")[0];
    		String url = http + ":" + urlBody;
    		b.setCoverimage(new URL(url));
    	} catch (MalformedURLException e) {
    		System.out.println("MalformedURLException");
    	}

    	return b;
    }

    public static void updateRepository(Book aBook) {
    	if(aBook == null) {
    		return;
    	}

    	// If the book is already present, then update it.
    	Book b = bookRepository.getBookByISBN(aBook.getIsbn());
    	if(b != null) {
    		b.setStatus(aBook.getStatus());
    		b.setCategory(aBook.getCategory());
    		b.setTitle(aBook.getTitle());
    		b.setCoverimage(aBook.getCoverimage());
    	}
    	// If absent, then add the book to the repo.
    	else {
    		bookRepository.addBook(aBook);
    	}
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
		bookRepository = new BookRepository();
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
		                    Book bookReceived = LibraryService.convertMessage(body);
		                    LibraryService.updateRepository(bookReceived);
			            } else if (msg == null) {
		                  	System.out.println("No new messages.");
		                  	break;
			            } else {
		                	System.out.println("Unexpected message type: " + msg.getClass());
			            }
		        	} // end while loop
		        
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
