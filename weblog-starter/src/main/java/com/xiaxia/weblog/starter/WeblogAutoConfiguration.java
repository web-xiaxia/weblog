package com.xiaxia.weblog.starter;


import com.xiaxia.weblog.core.properties.WeblogConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@ComponentScan({"com.xiaxia.weblog.core", "com.xiaxia.weblog.starter"})
@EnableWebSocketMessageBroker
public class WeblogAutoConfiguration implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WeblogConfig weblogConfig;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint(weblogConfig.getEndpoint())
                .setAllowedOrigins("*")
                .addInterceptors()
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    List<String> clientIds = accessor.getNativeHeader("weblogClientId");
                    if (!CollectionUtils.isEmpty(clientIds)) {
                        accessor.setUser(() -> clientIds.get(0));
                    }
                }
                return message;
            }

        });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker(weblogConfig.getTopicPrefix(), weblogConfig.getUserDestinationPrefix());
        config.setUserDestinationPrefix(weblogConfig.getUserDestinationPrefix());
        config.setApplicationDestinationPrefixes(weblogConfig.getApplicationDestinationPrefix());
    }
}
