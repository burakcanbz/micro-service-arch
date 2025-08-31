import mongoose, { Schema, HydratedDocument } from 'mongoose';
import { Product } from '../types/product.types';
import { productSchema } from '../validations/product.schema';

export type ProductDocument = HydratedDocument<Product>;

const productMongooseSchema = new Schema<Product>(
  {
    name: { type: String, required: true },
    description: { type: String },
    price: { type: Number, required: true },
    currency: { type: String, default: 'USD' },
    stock: { type: Number, required: true },
    category: { type: String },
    image: { type: String },
  },
  { timestamps: true }
);

productMongooseSchema.pre('save', function (next) {
  try {
    productSchema.parse(this.toObject());
    next();
  } catch (error) {
    next(error as Error);
  }
});

productMongooseSchema.pre('findOneAndUpdate', function (next) {
  try {
    productSchema.partial().parse(this.getUpdate() as Partial<Product>);
    next();
  } catch (error) {
    next(error as Error);
  }
});

productMongooseSchema.pre('insertMany', function (next, docs: any[]) {
  try {
    docs.forEach(doc => productSchema.partial().parse(doc));
    next();
  } catch (error) {
    next(error as Error);
  }
});

const ProductModel = mongoose.model('Product', productMongooseSchema);

export default ProductModel;
