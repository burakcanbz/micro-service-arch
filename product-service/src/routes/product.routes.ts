import { Router } from 'express';
import * as controller from '../controllers/product.controller';

const router = Router();

router.get('/', controller.getAllProducts);
router.get('/:id', controller.getProductById);
router.post('/', controller.createProduct);
router.post('/create-many', controller.createManyProducts);
router.patch('/:id', controller.updateProduct);
router.put('/:id', controller.replaceProduct);
router.delete('/:id', controller.deleteProduct);

export default router;
