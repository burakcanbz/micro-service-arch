import asyncio
import asyncpg
import sys, os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from utils.password import hash_password 


DB_URL = "postgresql://admin:admin123@localhost:5432/users_db"

async def add_user(name, email, password, role):
    conn = await asyncpg.connect(DB_URL)
    await conn.execute(
        """
        INSERT INTO users (name, email, password, role)
        VALUES ($1, $2, $3, $4)
        """,
        name, email, password, role
    )
    await conn.close()
    print(f"User {name} added!")

async def delete_all_users():
    conn = await asyncpg.connect(DB_URL)
    await conn.execute("DELETE FROM users")  # tüm satırları siler
    await conn.close()
    print("All users deleted!")

async def main():
    await delete_all_users()
    await add_user("Burak Canbaz", "burak@gmail.com", hash_password("1234"), "admin")
    await add_user("John Doe", "doe@gmail.com", hash_password("1234"), "user")

asyncio.run(main())