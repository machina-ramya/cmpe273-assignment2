package edu.sjsu.cmpe.library.config;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

public class LibraryServiceConfiguration extends Configuration {
    @NotEmpty
    @JsonProperty
    private String libraryInstanceName;

    @NotEmpty
    @JsonProperty
    private String stompQueueName;

    @NotEmpty
    @JsonProperty
    private String stompTopicName;

    @NotEmpty
    @JsonProperty
    private String apolloUser;

    @NotEmpty
    @JsonProperty
    private String apolloPassword;

    @NotEmpty
    @JsonProperty
    private String apolloHost;

    @JsonProperty
    private int apolloPort;

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

    /**
     * @return the stompTopicName
     */
    public String getStompTopicName() {
	return stompTopicName;
    }

    /**
     * @param stompTopicName
     *            the stompTopicName to set
     */
    public void setStompTopicName(String stompTopicName) {
	this.stompTopicName = stompTopicName;
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

    public String getLibraryInstanceName() {
        return this.libraryInstanceName;
    }

    /**
     * @param libraryName
     *            the libraryName to set
     */
    public void setLibraryName(String libraryName) {
	this.libraryInstanceName = libraryName;
    }
}
