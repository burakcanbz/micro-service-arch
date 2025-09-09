import app from './app';
import { connectDB } from './config/db';

const PORT = process.env.PORT || 3000;

import { connectRabbitMQ } from './messaging/rabbitmq';
import { startOrderConsumer } from './messaging/consumer';

(async () => {
  await connectRabbitMQ();

  await startOrderConsumer();
})();

connectDB().then(() => {
  app.listen(PORT, () => {
    console.log(`🚀 Product Service running at http://localhost:${PORT}`);
  });
});
