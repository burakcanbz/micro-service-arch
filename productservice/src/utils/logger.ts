import fs from 'fs';
import path from 'path';
import winston from "winston";

const logDir = path.join(__dirname, "..", 'logs');

if (!fs.existsSync(logDir)) {
  fs.mkdirSync(logDir); 
}


const logger = winston.createLogger({
    level: 'info',
    format: winston.format.combine(
        winston.format.timestamp(),
        winston.format.printf(({ timestamp, level, message }) => `${timestamp} [${level.toUpperCase()}]: ${message}`)
    ),
    transports: [
        new winston.transports.Console(),
        new winston.transports.File({ filename: 'logs/error.log', level: 'error' }),
        new winston.transports.File({ filename: 'logs/combined.log', level: 'info' }),
    ],
});

export default logger;