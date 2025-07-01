# Introduction 
TODO: Below step cover only insertion of record record to record synchronization process from internal system(CRM) to external system(Marketing).

# Prerequisite:
1. Install docker in you local machine.
2. Run docker-compose file added in root of project file using below command in terminal
    a. docker-compose pull
    b. docker-compose up -d
3.  Install Java with spring boot.
4.  This is POC hence both internal and external table created in single schema but table are different.

# Steps:
1.  In docker instance of mysql created respective table and grant permissionas per below steps.

    a. Login to root user and provide full privalage
        mysql -u root -p
        # password: debezium

        GRANT ALL PRIVILEGES ON *.* TO 'debezium'@'%' WITH GRANT OPTION;
        FLUSH PRIVILEGES;

    b. Login to debezium
        mysql -u debezium -p
        # password: dbz

        USE inventory;
        CREATE TABLE crm_customers (
            crm_customer_id VARCHAR(20) PRIMARY KEY,
            first_name VARCHAR(100),
            last_name VARCHAR(100),
            email VARCHAR(255),
            phone_number VARCHAR(20),
            last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        );

        CREATE TABLE marketing_contacts (
            mkt_contact_id VARCHAR(20) PRIMARY KEY,
            full_name VARCHAR(200),
            email_address VARCHAR(255),
            mobile VARCHAR(20),
            last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        );

        CREATE TABLE synced_records (
            syn_id VARCHAR(50) PRIMARY KEY,
            crm_record_id VARCHAR(255),
            mkt_record_id VARCHAR(255),
            source_system VARCHAR(50),
            sync_direction VARCHAR(20),
            last_synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            last_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            retry_count INT,
            sync_status VARCHAR(20),
            error_message TEXT
        );

2. Create connector in "debezium" with mysql server. Here any chnage in crm_customers then its sync to kafka.

    a. Create api for making connector.
        curl -X POST http://localhost:8083/connectors \
        -H "Content-Type: application/json" \
        -d '{
            "name": "mysql-inventory-connector",
            "config": {
            "connector.class": "io.debezium.connector.mysql.MySqlConnector",
            "database.hostname": "mysql",
            "database.port": "3306",
            "database.user": "debezium",
            "database.password": "dbz",
            "database.server.id": "184054",
            "database.server.name": "dbserver1",
            "database.include.list": "inventory",
            "table.include.list": "inventory.crm_customers,inventory.synced_records",
            "schema.history.internal.kafka.bootstrap.servers": "kafka:9092",
            "schema.history.internal.kafka.topic": "schema-changes.inventory",
            "topic.prefix": "mysql-inventory",
            "snapshot.mode": "initial",
            "snapshot.locking.mode": "none"
            }
        }'

    b. Delete connector if any changes neede and again create it.
        curl -X DELETE http://localhost:8083/connectors/mysql-inventory-connector

3. Insert record in crm_customers table then its capture by kafka topic name as "mysql-inventory.inventory.crm_customers" then it call rest api of external system to syn record here "syn_record" table centrally place to managed synchronization.
    
# Run java:
1. Clone code from repository given below.
https://github.com/ajaynimbolkar/SB_SAMPLE

2. Take pull from develoment branch.

3. Once code setting run below command to run java application.
- mvn clean install
- mvn spring-boot:run

4. Once above command exceuted, Your application start.

# Test Case:
Case 1: Sync data from crm to maketing.
    a. Insert record to crm table.
    INSERT INTO inventory.crm_customers
    (crm_customer_id, first_name, last_name, email, phone_number, last_updated_at)
    VALUES
    ('crm_99999', 'test', 'john', 'john@example.com', '99999999999', '2025-06-09 15:56:09');

    b. Record will be available in sync table for balance check as well as in marketing table.


Case 2. Sync data from maketing to crm.

    a. Run below rest api using curl command.
        curl --location 'http://localhost:8020/sb-sample/api/v1/marketing-contacts' \
        --header 'Content-Type: application/json' \
        --data-raw '{"marketingContactId":"mkt_55555", "fullName":"test test", "emailAddress":"test@example.com", "mobileNumber":"99999999999"}'

    b. Record will be available in sync table for balance check as well as in crm table.
 





