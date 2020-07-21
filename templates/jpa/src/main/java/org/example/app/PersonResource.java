/*
 * Copyright (c) 2019 IBM Corporation and others
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.example.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;

import javax.sql.DataSource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.inject.Inject;
import javax.enterprise.context.RequestScoped;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.example.app.Person;

@Path("/")
@RequestScoped
//@ApplicationScoped
@Transactional(Transactional.TxType.REQUIRES_NEW)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    @PersistenceContext(name = "jpa-unit")
    EntityManager em;

    @Resource UserTransaction userTran;

	@PostConstruct
	public void initPeople() {
		System.out.println("Seeding database with sample data");
		createPerson("Sample Person A", 25);
		createPerson("Sample Person B", 26);   
    }

    @GET
    public Collection<Person> getAllPeople(){
        //Set<Person> allPeople = new HashSet<>();
        return em.createNamedQuery("Person.findAll", Person.class).getResultList();	
    }

    @GET
    @Path("/{personId}")
    public Person getPerson(@PathParam("personId") long id) {
	Person person = (Person)em.find(Person.class, id);
        return person;
    }

    @POST
    public Long createPerson(@QueryParam("name") @NotEmpty @Size(min = 2, max = 50) String name,
                             @QueryParam("age") @PositiveOrZero int age){
            try{
		Person p = new Person(name, age);
                System.out.println("AJM: trying to create a person in db via jpa");
                userTran.begin();
                em.persist(p);
                userTran.commit();
                System.out.println("AJM: used persistence manager to persist to db");
                return p.id;
            }
            catch (Exception e){
                e.printStackTrace();
                return null;
            }

    }

    @POST
    @Path("/{personId}")
    public void updatePerson(@PathParam("personId") long id, @Valid Person p) {
         Person person = em.find(Person.class, id);
         person.setName(p.name);
         person.setAge(p.age);
         em.merge(person);

   }

    @DELETE
    @Path("/{personId}")
    public void removePerson(@PathParam("personId") long id) {
         em.remove(id);
    }

}
