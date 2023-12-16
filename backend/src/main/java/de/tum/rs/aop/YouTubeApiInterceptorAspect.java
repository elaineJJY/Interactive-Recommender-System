package de.tum.rs.aop;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import de.tum.rs.controller.APIController;
import de.tum.rs.controller.YouTubeController;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Aspect
@Component
public class YouTubeApiInterceptorAspect {

	@Autowired
	private APIController apiController;

	@Around("execution(* de.tum.rs.controller.YouTubeController.*(..))")
	public Object aroundYouTubeControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
		try {
			return joinPoint.proceed();
		} catch (GoogleJsonResponseException e) {
			log.error("GoogleJsonResponseException", e.getMessage());
			if (isApiKeyExhausted(e)) {
				apiController.switchToNextKey();
				log.warn("API key exhausted, switching to next key: {}", apiController.getCurrentApiKey());
				return joinPoint.proceed();
			}
			throw e;
		}
	}

	private boolean isApiKeyExhausted(Exception e) {
		return e.getMessage()
			.contains("The request cannot be completed because you have exceeded your");
	}

}
