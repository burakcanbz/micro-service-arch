export interface Product {
  name: string;
  description?: string;
  price: number;
  currency: string;
  stock: number;
  category?: string;
  image: string;
  createdAt?: Date;
  updatedAt?: Date;
}

