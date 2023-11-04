# HyperSkill: Recipe Project

## Technology / External Libraries
- Spring boot 3
- Spring Security
- Spring Web
- Lombok
- JUnit 5
- Mockito
- H2 Database
- MySQL Database
- Gradle
- JPA / Hibernate

## Project Description
Get to know the backend development. Use Spring Boot to complete this project. Learn about JSON, REST API,
Spring Boot Security, H2 database, LocalDateTime, Project Lombok, and other concepts useful for the backend. This repo
will also add in other changes for practice that are not part of the project requirements.


## Project Progress Journal

# Stage 1/5

*==Primary tasks==*

Create basic REST API to post a recipe, and get a recipe.

**==Notes==**

Pretty basic stuff, did not take much time. Whipped up an entity, repository, service, and controller, etc.

=========================================================================

# Stage 2/5

*==Primary tasks==*

Rearrange the existing endpoints; the service should support the following:

POST /api/recipe/new receives a recipe as a JSON object and returns a JSON object with one id field. This is a 
uniquely generated number by which we can identify and retrieve a recipe later. The status code should be 200 (Ok).
GET /api/recipe/{id} returns a recipe with a specified id as a JSON object (where {id} is the id of a recipe). 
The server should respond with the 200 (Ok) status code. If a recipe with a specified id does not exist, the server 
should respond with 404 (Not found).

**==Notes==**

Also pretty easy, just had to add a get method by id, change some entity stuff. A database was not in use yet, so I
used a yucky id increment thing. Then some basic response entity stuff.

=========================================================================

# Stage 3/5:

*==Primary tasks==*

- This is the obligatory implement a database stage, an H2 db is used. The same endpoints from before, they just need to 
persist in a file now. 
- Add a delete end point.
- Add bean validation for entity fields.

*==Additional Changes==*

- Added delete endpoint.
- Changed directions, and ingredients to be @ElementCollection'd along with some other changes to the Recipe entity.
- Made a DTO and mapper for the Recipe entity using a record and MapStruct.

**==Notes==**

Here is where things start getting a bit interesting. The database stuff I have done before, but now I got to practice
more with JPA. I learned about things like @ElementCollection, @OneToMany, @ManyToOne, @JoinColumn, etc. I also got to
practice more with bean validation stuff.

=========================================================================

# Stage 4/5

**==Primary tasks==**

- Add a category field, and a date field that will update if the entry gets updated, to the entity. 
- Add a new search endpoint to find all recipes by either category or name. 
- Add a new endpoint to update a recipe.

**==Additional Changes==**

- Non-notable refactoring and cleanup.
- Minor http response stuff.

**==Notes==**

Finding out there is a handy @UpdateTimestamp annotation for the date field which made all the date stuff easy.
The search endpoint was slightly more difficult, but more in a how to do it effectively way. I ended up making a nice
little method that uses a supplier function to make search work, but also be efficient.

=========================================================================

# Stage 5/5

**==Primary tasks==**

- Add spring security to the project, and configure it to allow only authenticated users to access the endpoints.
- Add a new endpoint to register a new user.
- Add user entity, repository, controller, etc.
- Add the user that made a recipe to the recipe entity.
- Add checks so that only the user that made a recipe can update or delete it.
- Add user email and password validation.

**==Notes==**

This stage was a bit of a dozy. I did a bit of my own security practice in my previous project, but this was harder.
Mostly from me over complicating some stuff, and also doing tiny mistakes that caused big bugs. One of which was technically
not my fault. A spring security update caused an issue with the way I was doing the filter chain, and I had to change it.

One of the main issues I ran into was getting stuck on a failed Hyperskill test for a while not knowing why. Some log 
statements, manual testing, and desk violence later I found out the user part of the recipe kept getting overwritten to 
null during the update. I used some MapStruct magic so that all null values are ignored when updating. There is still
a bunch of JPA and security stuff to learn, but I think I got the basics down.

=========================================================================

# Post Project Changelog

Since there won't be any major upkeep or changes I will just make a changelog here, instead of a separate file.
Also, the completed project will be considered version 1.0.0.

## [1.1.0]

### Added
- Switched from H2 to MySQL database.