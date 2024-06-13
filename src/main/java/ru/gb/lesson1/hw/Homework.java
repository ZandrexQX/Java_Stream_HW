package ru.gb.lesson1.hw;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Homework {
  public static void main(String[] args) {
    List<Person> people = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      Person person = new Person();
      person.setName("Person_" + i);
      person.setAge(i+20);
      person.setSalary(i*1000 + 20000);
      Department department = new Department();
      if(i % 2 == 0){
        department.setName("depart_1");
      }
      else department.setName("depart_2");

      person.setDepart(department);
      people.add(person);
    }

    System.out.println(people);

    System.out.println(findMostYoungestPerson(people));
    System.out.println("-----------------");
    System.out.println(findMostExpensiveDepartment(people));
    System.out.println("-----------------");
    System.out.println(groupByDepartment(people));
    System.out.println("-----------------");
    System.out.println(groupByDepartmentName(people));
    System.out.println("-----------------");
    System.out.println(getDepartmentOldestPerson(people));
    System.out.println("-----------------");
    System.out.println(cheapPersonsInDepartment(people));
  }
  @Getter
  @Setter
  @ToString
  private static class Department {
    private String name;

    // TODO: геттеры, сеттеры
  }
  @Getter
  @Setter
  @ToString
  private static class Person {
    private String name;
    private int age;
    private double salary;
    private Department depart;

    public String getNameDepartment(){
      return depart.getName();
    }
  }

  /**
   * Найти самого молодого сотрудника
   */
  static Optional<Person> findMostYoungestPerson(List<Person> people) {
    Optional<Person> person = people.stream()
            .min(Comparator.comparingInt(Person::getAge));
    return person;
  }

  /**
   * Найти департамент, в котором работает сотрудник с самой большой зарплатой
   */
  static Optional<Department> findMostExpensiveDepartment(List<Person> people) {
    Optional<Department> department = people.stream()
            .max(Comparator.comparingDouble(Person::getSalary))
            .flatMap(a -> Optional.ofNullable(a.getDepart()));
    return department;
  }

  /**
   * Сгруппировать сотрудников по департаментам
   */
  static Map<Department, List<Person>> groupByDepartment(List<Person> people) {
    return people.stream()
            .collect(Collectors.groupingBy(Person::getDepart));
  }

  /**
   * Сгруппировать сотрудников по названиям департаментов
   */
  static Map<String, List<Person>> groupByDepartmentName(List<Person> people) {
    return people.stream()
            .collect(Collectors.groupingBy(Person::getNameDepartment));
  }

  /**
   * В каждом департаменте найти самого старшего сотрудника
   */
  static Map<String, Person> getDepartmentOldestPerson(List<Person> people) {
    return people.stream()
            .collect(Collectors.toMap(
                    Person::getNameDepartment,
                    Function.identity(),
                    (a, b) ->{
                      if (a.getAge() > b.getAge()) return a;
                      return b;
                    }
            ));
  }

  /**
   * *Найти сотрудников с минимальными зарплатами в своем отделе
   * (прим. можно реализовать в два запроса)
   */
  static List<Person> cheapPersonsInDepartment(List<Person> people) {
    return people.stream()
            .collect(Collectors.toMap(
                    Person::getNameDepartment,
                    Function.identity(),
                    (a, b) ->{
                      if (a.getSalary() < b.getSalary()) return a;
                      return b;
                    }
            )).values().stream().toList();
  }
}
