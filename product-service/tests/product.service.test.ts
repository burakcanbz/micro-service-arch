import * as productService from '../src/services/product.service';
import mongoose from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';
import { Product } from '../src/types/product.types';

let mongoServer: MongoMemoryServer;

beforeAll(async () => {
    mongoServer = await MongoMemoryServer.create();
    const uri = mongoServer.getUri();

    await mongoose.connect(uri, {
        useNewUrlParser: true,
        useUnifiedTopology: true,
    } as mongoose.ConnectOptions);
});

afterAll(async () => {
    await mongoose.disconnect();
    await mongoServer.stop();
});

beforeEach(async () => {
  const collections = mongoose.connection.collections;
  for (const key in collections) {
    await collections[key].deleteMany({});
  }
});


const productData: Product = {
    name: 'Product 1',
    description: 'A product for testing',
    price: 100,
    currency: 'USD',
    stock: 10,
    category: 'Electronics',
    image: 'http://example.com/image.png',
};

describe('Product Service - getProductById', () => {
    it('should return a product when given a valid ID', async () => {

        const createdProduct = await productService.createProduct(productData as Product);
        const productId = createdProduct.id.toString();
        const fetchedProduct = await productService.getProductById(productId);

        expect(fetchedProduct).not.toBeNull();
        expect(fetchedProduct?.name).toBe(productData.name);
        expect(fetchedProduct?.price).toBe(productData.price);
    });
});

describe('Product Service - createProduct', () => {
    it('should create and return a new product', async () => {
        const newProduct = await productService.createProduct(productData as Product);

        expect(newProduct).not.toBeNull();
        expect(newProduct).toHaveProperty('id');
        expect(newProduct.name).toBe(productData.name);
        expect(newProduct.price).toBe(productData.price);
        expect(newProduct.stock).toBe(productData.stock);
    });
});

describe('Product Service - getAllProducts', () => {
    it('should return a list of products with pagination', async () => {
        for (let i = 0; i < 15; i++) {
            await productService.createProduct({ ...productData, name: `Product ${i + 1}`, createdAt: new Date(Date.now() + i * 1000)} as Product);
        }

        const page = 2;
        const limit = 5;
        const products = await productService.getAllProducts(page, limit);

        expect(products.length).toBe(limit);
        expect(products[0].name).toBe('Product 6');
        expect(products[4].name).toBe('Product 10');
    });
});

describe('Product Service - updateProduct', () => {
    it('should update and return the modified product', async () => {
        const createdProduct = await productService.createProduct(productData as Product);
        const productId = createdProduct.id.toString();

        const updatedData = { price: 150, stock: 5 };
        const updatedProduct = await productService.updateProduct(productId, updatedData);

        expect(updatedProduct).not.toBeNull();
        expect(updatedProduct?.price).toBe(updatedData.price);
        expect(updatedProduct?.stock).toBe(updatedData.stock);
    });
});

describe('Product Service - deleteProduct', () => {
    it('should delete the product and return null when fetching again', async () => {
        const createdProduct = await productService.createProduct(productData as Product);
        const productId = createdProduct.id.toString();

        const deletedProduct = await productService.deleteProduct(productId);
        expect(deletedProduct).not.toBeNull();
        expect(deletedProduct?.id.toString()).toBe(productId);

        const fetchAfterDelete = await productService.getProductById(productId);
        expect(fetchAfterDelete).toBeNull();
    });
});

describe('Product Service - createManyProducts', () => {
    it('should create and return multiple products', async () => {
        const productsToCreate = [
            { ...productData, name: 'Bulk Product 1' },
            { ...productData, name: 'Bulk Product 2' },
            { ...productData, name: 'Bulk Product 3' },
        ];

        const createdProducts = await productService.createManyProducts(productsToCreate as Product[]);

        expect(createdProducts.length).toBe(productsToCreate.length);
        expect(createdProducts[0].name).toBe('Bulk Product 1');
        expect(createdProducts[1].name).toBe('Bulk Product 2');
        expect(createdProducts[2].name).toBe('Bulk Product 3');
    });
});

describe('Product Service - replaceProduct', () => {
    it('should replace and return the new product data', async () => {
        const createdProduct = await productService.createProduct(productData as Product);
        const productId = createdProduct.id.toString();

        const newProductData = {
            name: 'Replaced Product',
            description: 'This product has been replaced',
            price: 200,
            currency: 'EUR',
            stock: 20,
            category: 'Books',
            image: 'http://example.com/new-image.png',
        };

        const replacedProduct = await productService.replaceProduct(productId, newProductData as Product);

        expect(replacedProduct).not.toBeNull();
        expect(replacedProduct?.name).toBe(newProductData.name);
        expect(replacedProduct?.price).toBe(newProductData.price);
        expect(replacedProduct?.currency).toBe(newProductData.currency);
        expect(replacedProduct?.stock).toBe(newProductData.stock);
        expect(replacedProduct?.category).toBe(newProductData.category);
    });
});
