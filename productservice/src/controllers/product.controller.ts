import { Request, Response, NextFunction } from 'express';
import * as productService from '../services/product.service';
import logger from '../utils/logger'; // Assuming you've set up a logger file
import { z } from 'zod';

const paginationSchema = z.object({
  page: z.preprocess((val) => Number(val ?? 1), z.number().int().min(1)),
  limit: z.preprocess((val) => Number(val ?? 10), z.number().int().min(1)),
});

export const getAllProducts = async (req: Request, res: Response, next: NextFunction) => {
    try {
        const { page, limit } = paginationSchema.parse(req.query);

        logger.info('GET /products in all products - Request received');
        const products = await productService.
        getAllProducts(page, limit);
        res.status(200).json(products);
        logger.info('GET /products in all products - Response sent');
    } catch (error) {
        if (error instanceof Error) { 
            logger.error(`GET /products in all products- Error: ${error.message}`);
        }
        else {
            logger.error('GET /products in all products - Unknown error');
        }
        next(error as Error);
    }
};

export const getProductById = async (req: Request, res: Response, next: NextFunction) => {
    try {
        const product = await productService.getProductById(req.params.id);
        if (!product) {
            logger.warn(`GET /products/${req.params.id} - Product not found`);
            return res.status(404).json({ message: 'Product not found' });
        }
        res.status(200).json(product);
        logger.info(`GET /products/${req.params.id} - Response sent`);
    } catch (error) {
        if (error instanceof Error) {
            logger.error(`GET /products/${req.params.id} - Error: ${error.message}`);
        } else {
            logger.error(`GET /products/${req.params.id} - Unknown error`);
        }
        next(error as Error);
    }
};

export const createProduct = async (req: Request, res: Response, next: NextFunction) => {
    try {
        logger.info('POST /products - Request received');
        const newProduct = await productService.createProduct(req.body);  
        res.status(201).json(newProduct);
        logger.info('POST /products - New product created');
    } catch (error) {
        if (error instanceof Error) {
            logger.error(`POST /products - Error: ${error.message}`);
        } else {
            logger.error('POST /products - Unknown error');
        }
        next(error as Error);
    }
};

export const createManyProducts = async (req: Request, res: Response, next: NextFunction) => {
    try {
        if (!Array.isArray(req.body)) {
            logger.warn('POST /products - Invalid request body, expected an array');
            return res.status(400).json({ message: 'Request body must be an array of products' });
        }
        logger.info('POST /products - Request to create multiple products received');
        const newProducts = await productService.createManyProducts(req.body); 
        res.status(201).json(newProducts);
        logger.info('POST /products - Multiple products created');
    } catch (error) {
        if (error instanceof Error) {
            logger.error(`POST /products - Error: ${error.message}`);
        } else {
            logger.error('POST /products - Unknown error');
        }   
        next(error as Error);
    }
};

export const updateProduct = async (req: Request, res: Response, next: NextFunction) => {
    try {
        logger.info(`PUT /products/${req.params.id} - Request received`);
        const updatedProduct = await productService.updateProduct(req.params.id, req.body);  
        if (!updatedProduct) {
            logger.warn(`PUT /products/${req.params.id} - Product not found`);
            return res.status(404).json({ message: 'Product not found' });
        }
        res.status(200).json(updatedProduct);
        logger.info(`PUT /products/${req.params.id} - Product updated`);
    } catch (error) {
        if (error instanceof Error) {
            logger.error(`PUT /products/${req.params.id} - Error: ${error.message}`);
        } else {
            logger.error(`PUT /products/${req.params.id} - Unknown error`);
        }   
        next(error as Error);
    }
};

export const replaceProduct = async (req: Request, res: Response, next: NextFunction) => {
  try {
    logger.info(`PUT /products/${req.params.id} - Replace request received`);
    const replaced = await productService.replaceProduct(req.params.id, req.body);  
    if (!replaced) {
      logger.warn(`PUT /products/${req.params.id} - Product not found`);
      return res.status(404).json({ message: 'Product not found' });
    }
    res.status(200).json(replaced);
    logger.info(`PUT /products/${req.params.id} - Product replaced`);
  } catch (error) {
    if (error instanceof Error) {
        logger.error(`PUT /products/${req.params.id} - Error: ${error.message}`);
    } else {
        logger.error(`PUT /products/${req.params.id} - Unknown error`);
    }
    next(error as Error);
  }
};

export const deleteProduct = async (req: Request, res: Response, next: NextFunction) => {
    try {
        logger.info(`DELETE /products/${req.params.id} - Request received`);
        const deleted = await productService.deleteProduct(req.params.id);
        if (!deleted) {
            logger.warn(`DELETE /products/${req.params.id} - Product not found`);
            return res.status(404).json({ message: 'Product not found' });
        }
        res.status(204).send();
        logger.info(`DELETE /products/${req.params.id} - Product deleted`);
    } catch (error) {
        if (error instanceof Error) {
            logger.error(`DELETE /products/${req.params.id} - Error: ${error.message}`);
        } else {
            logger.error(`DELETE /products/${req.params.id} - Unknown error`);
        }
        next(error as Error);
    }
};
