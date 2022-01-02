git pull
mvn clean install
docker build -t giejay/senseair-s8-java .
docker-compose -f /home/pi/dockerized/docker-compose.yml up -d
