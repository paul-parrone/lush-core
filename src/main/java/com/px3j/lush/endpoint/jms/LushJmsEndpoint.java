package com.px3j.lush.endpoint.jms;

/**
 * Use this annotation to allow Lush to apply behavior to receiver method.  Will typically be used with @JmsListener
 * annotation.
 *
 * <code>
 *     @LushJmsEndpoint
 *     @JmsListener(destination = "jms.message.endpoint")
 *     public void receiveMessage(Message msg) throws JMSException {
 * </code>
 */
public @interface LushJmsEndpoint {
}
