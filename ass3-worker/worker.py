import os
import sys
import signal
import time
import random
import json
import pika
import redis
from haversine import haversine, Unit


def handle_sigterm(signum, frame):
    print("SIGTERM signal received. Shutting down gracefully...")
    sys.exit(0)


def calculate_distance(driver_location, pickup_location):
    driver_lat, driver_lon = map(float, driver_location.split())
    pickup_lat = pickup_location['latitude']
    pickup_lon = pickup_location['longitude']
    return haversine((driver_lat, driver_lon), (pickup_lat, pickup_lon), unit=Unit.METERS)


def get_random_workload(region):
    if region == "at_linz":
        return random.randint(1, 2)
    elif region == "at_vienna":
        return random.randint(3, 5)
    elif region == "de_berlin":
        return random.randint(8, 11)
    else:
        return random.randint(3, 5)  # Default workload if region is unknown


def match_drivers_to_location(available_drivers, pickup_location):
    closest_driver_id = None
    closest_distance = float('inf')

    for driver_id in available_drivers.keys():
        driver_location = available_drivers[driver_id]
        distance = calculate_distance(driver_location, pickup_location)
        if distance < closest_distance:
            closest_driver_id = driver_id
            closest_distance = distance

    return closest_driver_id


def process_trip_request(channel, redis_client, region):
    method_frame, header_frame, body = channel.basic_get(queue=f'dst.{region}', auto_ack=True)
    if body:
        trip_request = json.loads(body.decode('utf-8'))
        pickup_location = trip_request['pickup']

        start_time = time.time()
        drivers_key = f"drivers:{region}"
        available_drivers = redis_client.hgetall(drivers_key)

        is_delete_successful = False
        while (not is_delete_successful):
            if (len(available_drivers.keys()) <= 0):
                result = {'driver_id': '', 'processing_time': 0}
                break

            closest_driver_id = match_drivers_to_location(available_drivers, pickup_location)

            is_delete_successful = redis_client.hdel(f"drivers:{region}", closest_driver_id)
            if not is_delete_successful:
                print(f"Driver {closest_driver_id} already deleted from {region} drivers.")
                start_time = time.time()  # Reset start time
                continue

            random_workload = get_random_workload(region)
            time.sleep(random_workload)
            end_time = time.time()
            processing_time = (end_time - start_time) + random_workload

            result = {'driver_id': closest_driver_id, 'processing_time': processing_time}

        print(f"Trip request processed: {result}")
        channel.basic_publish(exchange=f'dst.workers', routing_key=f'requests.{region}', body=json.dumps(result))


def main(region):
    signal.signal(signal.SIGTERM, handle_sigterm)

    rabbitmq_host = "rabbit"
    rabbitmq_port = 5672
    rabbitmq_user = "dst"
    rabbitmq_pass = "dst"

    redis_host = "redis"
    redis_port = 6379

    redis_client = redis.Redis(host=redis_host, port=redis_port, decode_responses=True)
    connection = pika.BlockingConnection(pika.ConnectionParameters(
        host=rabbitmq_host,
        port=rabbitmq_port,
        credentials=pika.PlainCredentials(rabbitmq_user, rabbitmq_pass)
    ))
    channel = connection.channel()

    channel.queue_declare(queue=f'dst.{region}')
    channel.exchange_declare(exchange=f'dst.workers', exchange_type='topic')

    while True:
        process_trip_request(channel, redis_client, region)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python worker.py <region>")
        sys.exit(1)

    region = sys.argv[1]
    region = region.lower()
    main(region)
