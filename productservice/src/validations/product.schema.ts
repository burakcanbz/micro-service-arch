import { z } from 'zod';

// Zod doğrulama şeması
export const productSchema = z.object({
  id: z.string().min(10).max(24).regex(/^[a-zA-Z0-9_-]+$/, 'ID must be alphanumeric and 10-12 characters long.').optional(),
  name: z.string().min(3, 'Product name must be at least 3 characters long.').max(100, 'Product name cannot exceed 100 characters.'),
  description: z.string().max(500, 'Description cannot exceed 500 characters.').optional(),
  price: z.number().min(0, 'Price must be a positive number.'),
  currency: z.enum(['USD', 'EUR', 'GBP', 'TRY'], 'Currency must be one of: USD, EUR, GBP, TRY.').default('USD'),
  stock: z.number().min(0, 'Stock cannot be negative.'),
  category: z.enum(['Electronics', 'Clothing', 'Home', 'Books'], 'Category must be one of: Electronics, Clothing, Home, Books.').optional(),
  createdAt: z.date().default(new Date()),
  updatedAt: z.date().default(new Date()),
});
