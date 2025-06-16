# UNO Backend Deployment Guide

This guide explains how to deploy the UNO backend application to various environments.

## Prerequisites

- Java Development Kit (JDK) 21
- Maven 3.6+ installed
- Git installed
- A MariaDB database server
- SMTP server access for email functionality (optional, but recommended)

## Local Deployment

### 1. Clone the Repository

```bash
git clone https://github.com/mruchann/CENG453_20242_Group1_backend.git
cd CENG453_20242_Group1_backend
```

### 2. Configure Application Properties

Create or edit the `src/main/resources/application.properties` file with your local configuration:

```properties
# Database Configuration
spring.datasource.url=jdbc:mariadb://localhost:3306/uno_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# Server Configuration
server.port=8080

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update

# Email Configuration (Optional)
spring.mail.protocol=smtp
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 3. Build the Application

```bash
mvn clean package
```

### 4. Run the Application

```bash
java -jar target/uno-0.0.1-SNAPSHOT.jar
```

The application will be available at `http://localhost:8080`.

## Docker Deployment

### 1. Build a Docker Image

Make sure Docker is installed on your system. Then build the Docker image:

```bash
docker build -t uno-backend .
```

### 2. Run the Docker Container

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mariadb://your-db-host:3306/uno_db \
  -e SPRING_DATASOURCE_USERNAME=your_username \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  -e SPRING_MAIL_USERNAME=your_email@gmail.com \
  -e SPRING_MAIL_PASSWORD=your_app_password \
  uno-backend
```

## Cloud Deployment (Render)

The UNO backend is currently deployed on Render. Here's how to deploy it to Render:

### 1. Create a Render Account

Sign up at [render.com](https://render.com) if you don't have an account already.

### 2. Create a New Web Service

1. Click "New" and select "Web Service"
2. Connect your GitHub repository
3. Select the repository containing the UNO backend code
4. Configure the service:
   - Name: `uno-backend` (or your preferred name)
   - Environment: `Docker`
   - Branch: `main` (or your deployment branch)
   - Region: Choose the closest to your users
   - Plan: Select an appropriate plan

### 3. Configure Environment Variables

In the Render dashboard, add the following environment variables:

```
SPRING_DATASOURCE_URL=jdbc:mariadb://your-db-host:3306/uno_db
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
```

### 4. Deploy

Click "Create Web Service" to deploy the application. Render will automatically build and deploy your application.

## Database Setup

### 1. Create the Database

```sql
CREATE DATABASE uno_db;
```

### 2. Create a Database User

```sql
CREATE USER 'your_username'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON uno_db.* TO 'your_username'@'%';
FLUSH PRIVILEGES;
```

The application will automatically create the necessary tables on startup since `spring.jpa.hibernate.ddl-auto=update` is set.

## SSL Configuration (HTTPS)

For production deployment, it's recommended to enable HTTPS. If deploying on Render, HTTPS is provided automatically with a free SSL certificate.

For custom domain configuration:

1. In the Render dashboard, go to your web service
2. Navigate to "Settings" > "Custom Domain"
3. Add your domain and follow the instructions to set up DNS records

## Monitoring and Logging

### Accessing Logs on Render

1. Go to your web service in the Render dashboard
2. Click on "Logs" to view application logs

### Setting Up Log Aggregation (Optional)

For production deployments, consider setting up log aggregation using services like:
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Papertrail
- Datadog

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   - Verify database credentials
   - Check if the database server is accessible from the deployment environment
   - Ensure the database user has appropriate permissions

2. **Application Startup Failure**
   - Check application logs for detailed error messages
   - Verify that all environment variables are correctly set
   - Ensure the Java version is 21

3. **Email Sending Issues**
   - Verify SMTP credentials
   - If using Gmail, ensure you're using an app password
   - Check if the SMTP server is accessible from the deployment environment

## Scaling Considerations

For high traffic scenarios, consider:

1. **Database Scaling**
   - Use connection pooling (Hikari is configured by default in Spring Boot)
   - Consider read replicas for read-heavy workloads

2. **Application Scaling**
   - Deploy multiple instances of the application
   - Use a load balancer to distribute traffic
   - Consider containerization with Kubernetes for advanced scaling

## Backup Strategy

Implement a regular backup strategy for your database:

```bash
# Example MariaDB backup command
mysqldump -u your_username -p your_password uno_db > uno_backup_$(date +%Y%m%d).sql
```

Consider automating backups and storing them in a secure location. 