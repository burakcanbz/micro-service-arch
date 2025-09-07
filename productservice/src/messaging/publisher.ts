// src/messaging/publisher.ts
import { getChannel } from './rabbitmq';

export const publishProductCreated = async (product: any) => {
  const channel = getChannel();
  const exchange = 'product_exchange';
  const routingKey = 'product.created';

  await channel.assertExchange(exchange, 'topic', { durable: true });
  channel.publish(exchange, routingKey, Buffer.from(JSON.stringify(product)));

  console.log(`[→] Sent product.created: ${product.name}`);
};
