package de.tum.rs.handler;

import jakarta.servlet.http.HttpServletRequest;
import java.net.ConnectException;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ConnectException.class)
	public void handleConnectException(ConnectException ex, HttpServletRequest request)
		throws ConnectException {
		String url = request.getRequestURL().toString();
		log.error("Connection error occurred while accessing: " + url + ". Error: " + ex.getMessage());
		throw ex;
	}

}
