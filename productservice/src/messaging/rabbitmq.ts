import amqplib from 'amqplib';

let channel: any;

export async function connectRabbitMQ() {
  const connection = await amqplib.connect('amqp://localhost');
  channel = await connection.createChannel();
  console.log('[✓] Connected to RabbitMQ');
}

export function getChannel() {
  if (!channel) throw new Error('Channel not initialized');
  return channel;
}
