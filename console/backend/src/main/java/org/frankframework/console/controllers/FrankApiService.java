/*
   Copyright 2024 WeAreFrank!

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.frankframework.console.controllers;

import java.util.UUID;

import jakarta.annotation.Nonnull;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import lombok.Getter;

import org.frankframework.console.ApiException;
import org.frankframework.console.configuration.ClientSession;
import org.frankframework.console.configuration.DeprecationInterceptor;
import org.frankframework.console.controllers.socket.MessageCacheStore;
import org.frankframework.console.util.RequestMessageBuilder;
import org.frankframework.console.util.ResponseUtils;
import org.frankframework.management.bus.OutboundGateway;

@Service
public class FrankApiService implements ApplicationContextAware, InitializingBean {

	private @Getter ApplicationContext applicationContext;
	private @Getter Environment environment;

	private final ClientSession session;
	private final MessageCacheStore messageCacheStore;

	public FrankApiService(ClientSession session, MessageCacheStore messageCacheStore) {
		this.session = session;
		this.messageCacheStore = messageCacheStore;
	}

	protected final OutboundGateway getGateway() {
		return getApplicationContext().getBean("outboundGateway", OutboundGateway.class);
	}

	@Nonnull
	protected Message<?> sendSyncMessage(RequestMessageBuilder input) {
		Message<?> message = getGateway().sendSyncMessage(input.build(getMemberTarget()));
		if (message == null) {
			StringBuilder errorMessage = new StringBuilder("did not receive a reply while sending message to topic [" + input.getTopic() + "]");
			if (input.getAction() != null) {
				errorMessage.append(" with action [");
				errorMessage.append(input.getAction());
				errorMessage.append("]");
			}
			throw new ApiException(errorMessage.toString());
		}
		return message;
	}

	public ResponseEntity<?> callSyncGateway(RequestMessageBuilder input) throws ApiException {
		Message<?> response = sendSyncMessage(input);
		// Build the response or do some final checks / return a different response
		return ResponseUtils.convertToSpringResponse(response);
	}

	/**
	 * Overload of the normal callSyncGateway but can `prefill` the cache (for WebSockets).
	 * The response message must be an instance of String else it won't be cached.
	 */
	public ResponseEntity<?> callSyncGateway(RequestMessageBuilder input, boolean preFillCache) throws ApiException {
		Message<?> response = sendSyncMessage(input);

		if (preFillCache && response.getPayload() instanceof String result) {
			String topic = input.getTopic().name();
			String cachedMessage = messageCacheStore.get(getMemberTarget(), topic);
			if (cachedMessage == null) {
				messageCacheStore.put(getMemberTarget(), topic, result);
			}
		}

		// Build the response or do some final checks / return a different response
		return ResponseUtils.convertToSpringResponse(response);
	}

	public ResponseEntity<?> callAsyncGateway(RequestMessageBuilder input) {
		OutboundGateway gateway = getGateway();
		gateway.sendAsyncMessage(input.build(session.getMemberTarget()));
		return ResponseEntity.ok().build();
	}

	@Override
	public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public final void afterPropertiesSet() {
		environment = applicationContext.getEnvironment();
	}

	private UUID getMemberTarget() {
		return session.getMemberTarget();
	}

	/** Get a property from the Spring Environment. */
	@SuppressWarnings("unchecked")
	protected <T> T getProperty(String key, T defaultValue) {
		return environment.getProperty(key, (Class<T>) defaultValue.getClass(), defaultValue);
	}

	protected final boolean allowDeprecatedEndpoints() {
		return getProperty(DeprecationInterceptor.ALLOW_DEPRECATED_ENDPOINTS_KEY, false);
	}
}
