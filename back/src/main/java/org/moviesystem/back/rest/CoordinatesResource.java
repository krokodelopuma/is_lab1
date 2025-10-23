package org.moviesystem.back.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.moviesystem.back.dao.CoordinatesDAO;
import org.moviesystem.back.model.Coordinates;

import java.util.List;

@Path("/coordinates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoordinatesResource {
    
    @Inject
    private CoordinatesDAO coordinatesDAO;
    
    @GET
    public Response getAllCoordinates() {
        try {
            List<Coordinates> coordinates = coordinatesDAO.findAll();
            return Response.ok(coordinates).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving coordinates: " + e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/{id}")
    public Response getCoordinatesById(@PathParam("id") Long id) {
        try {
            Coordinates coordinates = coordinatesDAO.findById(id);
            if (coordinates == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Coordinates not found"))
                        .build();
            }
            return Response.ok(coordinates).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving coordinates: " + e.getMessage()))
                    .build();
        }
    }
    
    @POST
    public Response createCoordinates(Coordinates coordinates) {
        try {
            Coordinates createdCoordinates = coordinatesDAO.create(coordinates);
            return Response.status(Response.Status.CREATED)
                    .entity(createdCoordinates)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error creating coordinates: " + e.getMessage()))
                    .build();
        }
    }
    
    public static class ErrorResponse {
        public String error;
        
        public ErrorResponse() {}
        
        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
