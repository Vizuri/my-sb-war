package com.vizuri.demo.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vizuri.demo.entity.Person;
import com.vizuri.demo.repository.PersonRepository;

@Service
public class PersonService {
	private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

	@Autowired
	PersonRepository personRepository;
	
	public Person savePerson(Person person) {
    	logger.info("In savePerson:" + person.getFirstName() + ":" + person.getLastName() + ":" + person.getWebsite());
		return personRepository.save(person);
	}
	
	public Iterable<Person> findAllPeople() {
		return personRepository.findAll();
	}
}
