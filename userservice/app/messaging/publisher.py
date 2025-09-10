import aio_pika
import json
import os

RABBITMQ_URL = os.getenv("RABBITMQ_URL")

async def send_user_registered_event(user_data: dict):
    connection = await aio_pika.connect_robust(RABBITMQ_URL)
    async with connection:
        channel = await connection.channel()
        queue = await channel.declare_queue(
            "user.registered",
            durable=True  
        )
        await queue.bind(exchange="amq.topic", routing_key="user.registered")
        await channel.default_exchange.publish(
            aio_pika.Message(
                body=json.dumps(user_data).encode(),
                content_type="application/json",
                delivery_mode=aio_pika.DeliveryMode.PERSISTENT  
            ),
            routing_key="user.registered"
        )
        print(f"✅ Sent user_registered event: {user_data}")

async def send_user_deleted_event(user_data: dict):
    connection = await aio_pika.connect_robust(RABBITMQ_URL)
    async with connection:
        channel = await connection.channel()
        queue = await channel.declare_queue(
            "user.deleted",
            durable=True  
        )
        await queue.bind(exchange="amq.topic", routing_key="user.deleted")
        await channel.default_exchange.publish(
            aio_pika.Message(
                body=json.dumps(user_data).encode(),
                content_type="application/json",
                delivery_mode=aio_pika.DeliveryMode.PERSISTENT  
            ),
            routing_key="user.registered"
        )
        print(f"✅ Sent user_deleted event: {user_data}")