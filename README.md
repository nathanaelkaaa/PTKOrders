# Spring Boot + PostgreSQL Dockerized

### Creating a docker network
``` bash
docker network create ptk_orders
```

### Spring Boot docker setup
``` bash
#Build Spring boot image (From the position of the Dockerfile)
docker build . -t ptk_orders_service

#Run backend service and hos tit on localhost port 8083
docker run -d -p 8083:8080 --name ptk_orders_backend --network ptk_orders ptk_orders_service

```

### Access To RabbitMq
``` 
http://localhost:15672/
user : guest
password : guest
```