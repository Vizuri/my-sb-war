package com.vizuri.demo.rest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vizuri.demo.entity.Person;
import com.vizuri.demo.service.PersonService;

@RestController
@RequestMapping("/person")
public class PersonResource {
	private static final Logger logger = LoggerFactory.getLogger(PersonResource.class);
	
	
	@Autowired
	PersonService personService;
	
    @GetMapping()
    public Iterable<Person> findAllPeople() throws Exception {
    	logger.info("In findAllPeople:");

    	return personService.findAllPeople();
    }

    
    @PostMapping()
    public Person savePerson(@RequestBody Person person) {
    	logger.info("In savePerson:" + person.getFirstName() + ":" + person.getLastName() + ":" + person.getWebsite());
    	return personService.savePerson(person);
    	
    }
    

}
