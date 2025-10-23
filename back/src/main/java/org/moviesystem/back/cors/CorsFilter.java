package org.moviesystem.back.cors;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // Удаляем существующие CORS заголовки, если они есть
        responseContext.getHeaders().remove("Access-Control-Allow-Origin");
        responseContext.getHeaders().remove("Access-Control-Allow-Methods");
        responseContext.getHeaders().remove("Access-Control-Allow-Headers");
        responseContext.getHeaders().remove("Access-Control-Allow-Credentials");
        responseContext.getHeaders().remove("Access-Control-Max-Age");
        
        // Добавляем наши CORS заголовки
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, x-requested-with");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        responseContext.getHeaders().add("Access-Control-Max-Age", "1209600");
    }
}
