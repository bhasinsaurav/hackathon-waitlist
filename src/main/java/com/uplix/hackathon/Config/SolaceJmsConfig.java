package com.uplix.hackathon.Config;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
public class SolaceJmsConfig {

    private final SolaceJmsProperties props;

    public SolaceJmsConfig(SolaceJmsProperties props) {
        this.props = props;
    }

    @Bean
    public ConnectionFactory solaceConnectionFactory() throws Exception {
        SolConnectionFactory cf = SolJmsUtility.createConnectionFactory();
        cf.setHost(props.getHost());
        cf.setVPN(props.getVpn());
        cf.setUsername(props.getUsername());
        cf.setPassword(props.getPassword());
        return (ConnectionFactory) cf;
    }

    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPubSubDomain(true);
        factory.setConcurrency("5"); // you can tune this
        return factory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

}
