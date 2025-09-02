import app from './app';
import { connectDB } from './config/db';

const PORT = process.env.PORT || 3000;

connectDB().then(() => {
  app.listen(PORT, () => {
    console.log(`🚀 Product Service running at http://localhost:${PORT}`);
  });
});
