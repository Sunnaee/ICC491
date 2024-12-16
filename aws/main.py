from flask import Flask, render_template, request, jsonify
from database import insertar_datos, obtener_ultimo_estado
from correo import enviar_correo

app = Flask(__name__)

@app.route('/estacionamiento', methods=['POST'])
def recibir_datos():
    data = request.json
    print(f"Datos recibidos: {data}")  # Agregar esto para depuración
    insertar_datos(data)
    
    if data['status'] == 0:  # No hay disponibilidad
        print("Estacionamiento lleno, enviando correo...")
        enviar_correo("Estacionamiento lleno", "Su estacionamiento está actualmente ocupado. Si fue usted, bienvenid@ y, porfavor, ignore este mensaje.")
        
    return jsonify({"mensaje": "Datos recibidos correctamente"}), 200


@app.route('/estacionamiento', methods=['GET'])
def consultar_disponibilidad():
    status = obtener_ultimo_estado()
    return jsonify({"disponible": status == 1})

@app.route('/')
def interfaz_web():
    try:
        return render_template('index.html')
    except Exception as e:
        return f"Error al cargar la página: {e}", 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8081)
