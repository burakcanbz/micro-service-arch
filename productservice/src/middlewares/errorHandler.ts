import { Request, Response, NextFunction } from 'express';

export function errorHandler(err: any, _: Request, res: Response, __: NextFunction) {
  console.error(err);

  if (err.name === 'ZodError') {
    return res.status(400).json({ message: 'Validation failed', details: err.errors });
  }

  res.status(500).json({ message: 'Internal Server Error' });
}
