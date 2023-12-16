# Interactive Recommender System

## Description
This repository contains the codebase for the thesis project titled "Human-centric Recommender Systems: Investigating Interaction for Transparency and User Control". In the modern digital era, recommender systems play a pivotal role in shaping online behaviors. This project aims to explore the extent to which recommendations influence user decisions and how users would exercise their right to influence automated recommendations.

## Features
- Frontend built with Vue.js
- Backend developed using Spring Boot
- Elasticsearch as the database
- Integration with Google's YouTube Data API to fetch video collections and video details

## Thesis Objectives
This project is designed to:

+ **Understand User Influence**: Investigate how users would utilize their newfound ability to influence automated recommendations.
+ **Enhance Transparency**: With the European Union's Digital Services Act as a backdrop, this system seeks to bring transparency to recommendation algorithms, allowing users to understand and even modify the parameters driving their content suggestions.
+ **Simulate Real-world Scenarios**: Through an interface-based simulation of recommender systems, users can interact with various settings, mimicking real-world algorithms used by major online platforms.


## Setup and Installation

This project is containerized using Docker and orchestrated using Docker Compose, which makes it easy to deploy and run on any machine that has Docker and Docker Compose installed. Here are the steps to get it up and running:

### Prerequisites
- Ensure you have [Docker](https://docs.docker.com/get-docker/) installed on your machine.
- Ensure you have [Docker Compose](https://docs.docker.com/compose/install/) installed on your machine.

### Steps

1. **Clone the Repository**:

   ```bash
   cd Interactive-Recommender-System
   ```

2. **Configure your Google API Keys in docker-compose.yml:**

   In the `docker-compose.yml` file, locate the `backend` service configuration. Add your Google API keys to the `environment` section as shown below. Replace `APIKEY1, APIKEY2, APIKEY3` with your actual Google API keys, separating each key with a comma.

   ```dockerfile
   backend:
       build:
         context: ./backend
       ports:
         - 8081:8081
       environment:
         - SPRING_ELASTICSEARCH_URIS=http://es:9200
         - API_KEYS="APIKEY1,APIKEY2,APIKEY3"
   ```

3. **Build and Run the Docker Compose Stack**:

   ```bash
   docker-compose up --build
   ```

   This command will build the Docker images for the frontend, backend, Elasticsearch, and Kibana services defined in your `docker-compose.yml` file, and start all the services.

4. **Access the Application**:

   - Frontend: Open a web browser and navigate to [http://localhost:8080](http://localhost:8080)
   - Backend API Documentation: Navigate to [http://localhost:8081/docs](http://localhost:8081/docs)
   - Elasticsearch: Accessible at [http://localhost:9200](http://localhost:9200)
   - Kibana: Navigate to [http://localhost:5601](http://localhost:5601)
   	- NOTE: The visualisation panel needs to be configured by yourself.

5. **Stopping the Services**:
   ```bash
   docker-compose down
   ```
   This command will stop all the running services and remove the containers.


## Common Issues
#### Error: Invalid or corrupt jarfile my-app.jar
Deploying the cluster to a virtual machine sometimes results in file corruption, leading to errors like Invalid or corrupt jarfile my-app.jar. This can be due to unreliable transfer methods.

Recommended Solution:
- Use SCP (Secure Copy Protocol) for transferring files to ensure integrity. SCP, which relies on SSH (Secure Shell), provides a secure and reliable way to transfer files over a network using:
```bash
scp /path/to/local/Interactive-Recommender-System/backend/target/rs-0.0.1-SNAPSHOT.jar  user@vm-address:/path/to/remote/Interactive-Recommender-System/backend/target
```

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](https://chat.openai.com/c/LICENSE) file for details.