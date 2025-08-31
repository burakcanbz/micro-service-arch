import express from 'express';
import dotenv from 'dotenv';
import productRoutes from './routes/product.routes';
import rateLimit from 'express-rate-limit';
import { errorHandler } from './middlewares/errorHandler';

dotenv.config();

const app = express();

app.use(express.json());
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, 
  max: 100,
  message: 'Too many requests, please try again later.'
});

app.use(limiter);

app.use('/api/products', productRoutes);
app.use(errorHandler);

export default app;
