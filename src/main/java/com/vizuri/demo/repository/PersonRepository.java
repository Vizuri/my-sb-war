package com.vizuri.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import com.vizuri.demo.entity.Person;

public interface PersonRepository extends CrudRepository<Person, Integer> {

}
