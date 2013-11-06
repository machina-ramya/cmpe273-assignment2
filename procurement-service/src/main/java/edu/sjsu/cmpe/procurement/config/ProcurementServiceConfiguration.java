package edu.sjsu.cmpe.procurement.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;

public class ProcurementServiceConfiguration extends Configuration {
    @NotEmpty
    @JsonProperty
    private String stompQueueName;

    @NotEmpty
    @JsonProperty
    private String stompTopicPrefix;

    @NotEmpty
    @JsonProperty
    public String apolloUser;

    @NotEmpty
    @JsonProperty
    private String apolloPassword;

    @NotEmpty
    @JsonProperty
    private String apolloHost;

    @JsonProperty
    private int apolloPort;
	
	@NotNull
    @JsonProperty
    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

    /**
     * @return the stompQueueName
     */
    public String getStompQueueName() {
	return stompQueueName;
    }

    /**
     * @param stompQueueName
     *            the stompQueueName to set
     */
    public void setStompQueueName(String stompQueueName) {
	this.stompQueueName = stompQueueName;
    }

    public String getStompTopicPrefix() {
	return stompTopicPrefix;
    }
	
	

    public void setStompTopicPrefix(String stompTopicPrefix) {
	this.stompTopicPrefix = stompTopicPrefix;
    }

	

    /**
     * 
     * @return
     */
    public JerseyClientConfiguration getJerseyClientConfiguration() {
	return httpClient;
    }
    public String getApolloUser() {
        return this.apolloUser;
    }

    public void setApolloUser(String aApolloUser) {
        this.apolloUser = aApolloUser;
    }

    public String getApolloPassword() {
        return this.apolloPassword;
    }

    public void setApolloPassword(String aApolloPassword) {
        this.apolloPassword = aApolloPassword;
    }

    public String getApolloHost() {
        return this.apolloHost;
    }

    public void setApolloHost(String aApolloHost) {
        this.apolloHost = aApolloHost;
    }

    public int getApolloPort() {
        return this.apolloPort;
    }

    public void setApolloPort(int aApolloPort) {
        this.apolloPort = aApolloPort;
    }
}
