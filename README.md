stack: Akka Actors, Akka Persistence, Akka HTTP, Event Sourcing, Cassandra, Cats


## Run cassandra on Docker Compose
```bash
cd /home/robert/sandbox/projects/mini-bank-akka-cassandra-cats
sudo docker-compose up
```

## Check cassandra tables (in other terminal)
```bash
cd /home/robert/sandbox/projects/mini-bank-akka-cassandra-cats

sudo docker ps
sudo docker exec -it <container_name_from_the_ps_command_above> cqlsh

cqlsh> describe tables;
cqlsh> exit;
```

## How this works ?