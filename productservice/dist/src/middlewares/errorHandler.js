"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.errorHandler = errorHandler;
function errorHandler(err, _, res, __) {
    console.error(err);
    if (err.name === 'ZodError') {
        return res.status(400).json({ message: 'Validation failed', details: err.errors });
    }
    res.status(500).json({ message: 'Internal Server Error' });
}
