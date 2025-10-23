package org.moviesystem.back.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.moviesystem.back.dao.PersonDAO;
import org.moviesystem.back.model.Person;

import java.util.List;

@Path("/persons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {
    
    @Inject
    private PersonDAO personDAO;
    
    @GET
    public Response getAllPersons() {
        try {
            List<Person> persons = personDAO.findAll();
            return Response.ok(persons).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving persons: " + e.getMessage()))
                    .build();
        }
    }
    
    @GET
    @Path("/{id}")
    public Response getPersonById(@PathParam("id") Long id) {
        try {
            Person person = personDAO.findById(id);
            if (person == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Person not found"))
                        .build();
            }
            return Response.ok(person).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving person: " + e.getMessage()))
                    .build();
        }
    }
    
    @POST
    public Response createPerson(Person person) {
        try {
            Person createdPerson = personDAO.create(person);
            return Response.status(Response.Status.CREATED)
                    .entity(createdPerson)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error creating person: " + e.getMessage()))
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
