# 🔀 Java Virtual Thread Load Balancer

A lightweight, high-concurrency TCP load balancer written in **Java 21**, powered by **virtual threads**, supporting pluggable backend selection strategies, health checking and rate limiting.

---

## Brief Description

When the load balancer starts up it spins up the Health Checker which just tries to connect to each backend in turn and if it can't then it marks that backend as unhealthy.  
The load balancer then listens for connections and if a connection is made to the load balancer it will spin up two virtual threads, one to transfer data from client to backend and one vice versa.  
This is the basic core design of this load balancer.

---


## 🚀 Features

- ⚡ **Virtual Threads** for scalable connection handling
- 🔁 **Multiple Backend Selection Strategies**
    - Round Robin
    - Least Connections
    - First Available
- ✅ **Health Checking** to detect and exclude unhealthy backends
- 🚫 Configurable per-client IP rate limiting to manage connection bursts and prevent abuse
- ⚙️ Runtime configuration via **environment variables**
- 🧪 Built-in **mock echo servers** for integration testing
- 📦 Docker-friendly deployment
 

---

## 🛠️ Getting Started
### ✅ Requirements

- Java 21+
- Maven
- (Optional) Docker

## ▶️ How to Run
  The load balancer is configured via environment variables.

## ✅ Required Environment Variable

| Variable	  | Description	                                            | Example |
| ----------- | ------------------------------------------------------- |--------|
| LB_BACKENDS |	Comma-separated list of backend servers in host:port format	| 192.168.15.72:9001,203.0.113.57:9002 |

- Note: if you don't set any backends the load balancer will simply exit execution.

## ⚙️ Optional Environment Variables
| Variable                     | Description                                                                     | Default      | Example           |
| ---------------------------- | ------------------------------------------------------------------------------- | ------------ |-------------------|
| LB_PORT	                   | Port the load balancer listens on	                                             | 8080         | 9000              |
| LB_STRATEGY          	       | Backend selection strategy: ROUND_ROBIN, LEAST_CONNECTIONS, or FIRST_AVAILABLE	 | ROUND_ROBIN	| LEAST_CONNECTIONS |
| LB_HEALTH_CHECK_INTERVAL_MS  | Interval (in milliseconds) between health checks	                             | 5000	        | 2000              | 
| LB_HEALTH_CHECK_TIMEOUT_MS   | Timeout (in milliseconds) for each health check socket connection	             | 1000	        | 1500              |
| LB_RATE_LIMIT_MAX            | Maximum number of connections a client can have before being throttled         | 100           | 50                |
| LB_RATE_LIMIT_WINDOW_MS      | This is the window in which the maximum number of connections per client is active| 60000      | 10000             |
| LB_MAX_CONNECTIONS           | This is the max total connections that can be made to the load balancer at any one time | 50000 | 10000 |
---

### ▶️ Example: Minimal Run
`export LB_BACKENDS=localhost:9001,localhost:9002`  
`java -jar target/loadbalancer-jar-with-dependencies.jar`  

### ▶️ Example: Configuration with docker
`docker build -t load-balancer .`  
`docker run -e LB_BACKENDS=localhost:9001 -e LB_PORT=1234 -e LB_STRATEGY=FIRST_AVAILABLE -e LB_HEALTH_CHECK_INTERVAL_MS=10000 -e LB_HEALTH_CHECK_TIMEOUT_MS=3000 -p 8080:1234 load-balancer`
- Note: The LB_PORT is the mapping to the docker containers internal port (so not really needed unless in some very special case)
- Note: Also using localhost backends won't work as they would need to be in the docker container, you'd need to have the connections being made from the docker to an external service
💡 If LB_BACKENDS is not set, the load balancer will fail to start.

### ▶️ Intellij - And for the examiners

For the purposes of just having a look at how this works for yourselves quickly you can just create a run config in intellij. You could use ncat to host some backends locally.   
Then you can use putty instances or alternative to send raw data to the load balancer and manually checked which ncat instance picked up the input. This isn't realistic as the nature of TCP connections and layer 4 load balancers are that the connect won't remain open for so long.