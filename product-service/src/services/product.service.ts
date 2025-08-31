import { HydratedDocument } from 'mongoose';
import ProductModel from '../models/product.model';
import { Product } from '../types/product.types'; 

export const getAllProducts = async (page: number, limit: number): Promise<HydratedDocument<Product>[]> => { 
    return await ProductModel.find().sort({createdAt: 1}).skip((page - 1) * limit).limit(limit).exec();   
};

export const getProductById = async (id: string): Promise<HydratedDocument<Product> | null> => await ProductModel.findById(id);

export const createProduct = async (data: Product): Promise<HydratedDocument<Product>> => await ProductModel.create(data);

export const createManyProducts = async (data: Product[]): Promise<HydratedDocument<Product>[]> => await ProductModel.insertMany(data);

export const updateProduct = async (id: string, data: Partial<HydratedDocument<Product>>): Promise<Product | null> => 
  await ProductModel.findByIdAndUpdate(id, data, { new: true, runValidators: true });

export const replaceProduct = async (id: string, data: Product): Promise<HydratedDocument<Product> | null> => 
  await ProductModel.findOneAndReplace({ _id: id }, data, { new: true });

export const deleteProduct = async (id: string): Promise<HydratedDocument<Product> | null> => await ProductModel.findByIdAndDelete(id);
