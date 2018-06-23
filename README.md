# Front Controller for OneGuard Micro-Service Architecture

[![GitHub license](https://img.shields.io/github/license/OneGuardSolutions/msa-service-front-controller.svg)](https://github.com/OneGuardSolutions/msa-service-front-controller/blob/master/LICENSE)
[![Maintainability](https://api.codeclimate.com/v1/badges/a1b4c94c7e34c5732aae/maintainability)](https://codeclimate.com/github/OneGuardSolutions/msa-service-front-controller/maintainability)

Micro-Service that acts as a front controller to allow client to consume
other services APIs.

## Requirements:

- JDK 9 or above
- Maven
- RabbitMQ server

## How to run this example

First install [RabbitMQ Server](http://www.rabbitmq.com/download.html).
For purpose of this example we assume the server is running on local host and default port.

If your environment differs, override the configuration in `src/main/resources/application-default.properties`.
See `src/main/resources/application.properties` for reference. 

Clone this repository:

```bash
git clone git@github.com:OneGuardSolutions/msa-service-front-controller.git
```

Start the app from the cloned directory:

```bash
mvn spring-boot:run
```

## Message format

The message MUST by json encoded.

```json
{
  "type": "echo.request",
  "payload": {},
  "occurredAt": 1514761200000, 
  "reference": "UUID"
}
```

- **`type`** `string` - message type used to determine handler
- **`payload`** `object` - actual contents of the message processed by the handler, 
                           structure depends on the handler
- **`occurredAt`** `date` *optional* - time of message occurrence in milliseconds since the epoch 
                                      (1970-01-01 00:00:00.000); defaults to current time when received
- **`reference`** `uuid` *optional* - used by client to match any potential response to the request

## Used libraries

- [OneGuard Micro-Service Architecture Core library](https://github.com/OneGuardSolutions/msa-core)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [JSON Web Token Support For The JVM](https://jwt.io/)

## Client libraries

- [SockJS](https://github.com/sockjs/sockjs-client)
