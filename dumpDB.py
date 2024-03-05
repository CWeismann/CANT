# Python script to dump login and message databases. 
import sqlite3

print("All of Messages DB:")
conn = sqlite3.connect('messages.db')
cursor = conn.cursor()
c = cursor.execute("SELECT * FROM messages")
data = list(c)
for msg in data:
    print(msg)
cursor.close()

print("\nAll of Login DB:")
conn = sqlite3.connect('login.db')
cursor = conn.cursor()
c = cursor.execute("SELECT * FROM login")
data = list(c)
for msg in data:
    print(msg)
cursor.close()
