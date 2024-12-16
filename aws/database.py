from pymongo import MongoClient
from datetime import datetime

# Configuración de MongoDB
client = MongoClient("mongodb://localhost:27017/")
db = client["estacionamientos"]
collection = db["sensores"]

def insertar_datos(data):
    data['timestamp'] = datetime.utcnow()
    collection.insert_one(data)

def obtener_ultimo_estado():
    ultimo_registro = collection.find_one(sort=[("timestamp", -1)])
    return ultimo_registro['status'] if ultimo_registro else None