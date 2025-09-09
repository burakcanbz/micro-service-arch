import { ConsumeMessage } from 'amqplib';
import { getChannel } from './rabbitmq';
import { handleOrderCreated } from '../services/orderHandler';

export const startOrderConsumer = async (): Promise<void> => {
  const channel = getChannel();
  const exchange = 'amq.topic'; 
  const queue = 'product_order_consumer';

  await channel.assertExchange(exchange, 'topic', { durable: true });
  const q = await channel.assertQueue(queue, { durable: true });
  await channel.bindQueue(q.queue, exchange, 'order.created');

  channel.consume(q.queue, (msg: ConsumeMessage | null) => {
    if (msg) {
      try {
        const data = JSON.parse(msg.content.toString());
        console.log(`[←] Received order.created:`, data);
        console.log(msg);
        handleOrderCreated(data);
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
