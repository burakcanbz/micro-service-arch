import { getProductById, updateProduct } from '../services/product.service';
import logger from '../utils/logger';

interface OrderItem {
    id: number;
    productId: string;
    quantity: number;
    order: any;
}

interface Order {
    id: number;
    userId: string;
    status: string;
    items: OrderItem[];
}

export const handleOrderCreated = async (order: Order): Promise<void> => {
    try{
        for (const item of order.items) {
          const product = await getProductById(item.productId);
          if (!product) {
            logger.error(`Product not found: ${item.productId}`)
            throw new Error(`Product not found: ${item.productId}`);
          }
          if (product.stock < item.quantity) {
            logger.error(`Insufficient stock for product ${product.name}`)
            throw new Error(`Insufficient stock for product ${product.name}`);
          }
          const {id, ...itemData} = item;
          const updatedProduct = await updateProduct(itemData.productId, { "stock": product.stock - item.quantity});
          console.log("product updated => ", updatedProduct);
        }
    }
    catch(err) {
        const error = err as Error;
        logger.error(error.message);
    }
};
