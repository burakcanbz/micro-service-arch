import { ConsumeMessage } from 'amqplib';
import { getChannel } from './rabbitmq';

export const startOrderConsumer = async (): Promise<void> => {
  const channel = getChannel();
  const exchange = 'amq.topic'; // the exchange where order messages are published
  const queue = 'product_order_consumer'; // your queue name

  // 1️⃣ Assert the exchange
  await channel.assertExchange(exchange, 'topic', { durable: true });

  // 2️⃣ Assert a queue for this service
  const q = await channel.assertQueue(queue, { durable: true });

  // 3️⃣ Bind the queue to the exchange with the routing key "order.created"
  await channel.bindQueue(q.queue, exchange, 'order.created');

  // 4️⃣ Consume messages
  channel.consume(q.queue, (msg: ConsumeMessage | null) => {
    if (msg) {
      try {
        const data = JSON.parse(msg.content.toString());
        console.log(`[←] Received order.created:`, data);

        // Handle the order message here
        // e.g., update inventory, notify product service, etc.

        // Acknowledge message
        channel.ack(msg);
      } catch (err) {
        console.error('❌ Failed to process message', err);
        // Reject and do not requeue
        channel.nack(msg, false, false);
      }
    }
  });

  console.log('[✓] Order consumer started and listening for order.created');
};
