# Dutch Parking Application API's

This is a simple Java / Maven / Spring Boot (version 3.1.5) based application that can be used to register and de-register the Parking of Vehicle based on there licence number. 
And also administrator can also upload list of vehicle they monitored with monitoring vehicle.

## Tech Stack used
- Java - 17
- SpringBoot - 3.1.2
- Maven - 3.9.5
- Embedded Tomcat 10.x 

## Step to setup

This application is packaged as a war which has Tomcat 10 embedded. No Tomcat or JBoss installation is necessary. You run it using the ```java -jar``` command.

* Clone this repository - https://github.com/PaapiCoder/DutchParkingApis.git
* Make sure you are using JDK 17 and Maven 3.9.x
* You can build the project and run the tests by running ```mvn clean package```
* Once successfully built, you can run the service by one of these two methods:
```
java -jar target/dutch-parking-api-0.0.1-SNAPSHOT.war
OR
mvn spring-boot:run
```

Once the application runs you should see something like this

```
2023-11-19T13:57:01.791+05:30  INFO 6190 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2023-11-19T13:57:01.800+05:30  INFO 6190 --- [           main] c.dutch.parking.DutchParkingApplication  : Started DutchParkingApplication in 2.224 seconds (process running for 2.401)
```

## Capability used in Application

* Full integration with the latest **Spring** Framework: inversion of control, dependency injection, etc.
* Packaging as a single war with embedded container (tomcat 10): No need to install a container separately on the host just run using the ``java -jar`` command
* Demonstrates how to set up health endpoints automatically on a configured port. Inject your own health info with a few lines of code.
* Writing a RESTful service using annotation: supports JSON request / response.
* Exception mapping from application exceptions to the right HTTP response with exception details in the body
* *Spring Data* Integration with JPA/Hibernate with just a few lines of configuration and familiar annotations.
* Automatic CRUD functionality against the data source using Spring *Repository* pattern
* Demonstrates MockMVC test framework with associated libraries
* All APIs are "self-documented" by Swagger2 using annotations

## Endpoints of application:

### System health.

```
http://localhost:8080/actuator/health
```

### Registering vehicle using licence number.

```
POST /api/register
Accept: application/json
Content-Type: application/json

Request
{
    "licenceNumber":1900,
    "streetName":"Java"
}

RESPONSE: HTTP 201 (Created)
{
    "id": 202,
    "licenceNumber": "1900",
    "streetName": "Java",
    "registerDatetime": "2023-11-19T14:41:02.68666",
    "unregisterDatetime": null,
    "parkingStatus": "Registered"
}

```

### De-Registering vehicle using licence number

```
POST /api/unregister
Accept: application/json
Content-Type: application/json

Request
{
    "licenceNumber":1900,
}

RESPONSE: HTTP 200 (OK)
{
    "message": "You have successfully De-Registered you vehicle. Total Time : 2 min",
    "parkingAmount": 30.00
}
```

### Load List of vehicle during monitoring

```
POST /api/loadParkingRecordList
Accept: application/json
Content-Type: application/json

Request
[
     {
        "licenceNumber":1400,
        "streetName":"Jakarta"
    },
    {
        "licenceNumber":1900,
        "streetName":"Java"
    }
]

RESPONSE: HTTP 200 (OK)
[
    {
        "id": 102,
        "licenceNumber": "1400",
        "streetName": "Jakarta",
        "recordingDate": "2023-11-19T14:47:56.20114"
    },
    {
        "id": 103,
        "licenceNumber": "1900",
        "streetName": "Java",
        "recordingDate": "2023-11-19T14:47:56.201175"
    }
]
```
# Database
## Steps to create Database schema

As we are using Mysql CE database for this application. Please Download the MySql Data base from online and follow below steps

- Install the MySql on local Machine.
- Start the MySql Database instance 
- Create Schema using ```CREATE SCHEMA DutchParking```
- Table will be automatically created once code run on the machine.

# Swagger link
Start application and then click on below link to see swagger page.

http://localhost:8080/swagger-ui/index.html