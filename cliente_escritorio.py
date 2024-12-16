import tkinter as tk
import requests
import threading

# Función para obtener el estado del estacionamiento
def obtener_estado():
    try:
        # Hacemos una solicitud GET al servidor Flask
        response = requests.get('http://54.210.65.125:8081/estacionamiento')
        if response.status_code == 200:
            data = response.json()
            return data['disponible']
        else:
            print("Error al obtener estado del servidor.")
            return None
    except requests.exceptions.RequestException as e:
        print(f"Error en la conexión: {e}")
        return None

# Función para actualizar la interfaz gráfica
def actualizar_estado():
    estado = obtener_estado()
    
    if estado is not None:
        if estado:
            # Estacionamiento disponible (verde)
            indicador.config(bg="green")
            estado_texto.config(text="Disponible", fg="white")
        else:
            # Estacionamiento ocupado (rojo)
            indicador.config(bg="red")
            estado_texto.config(text="Ocupado", fg="white")
    
    # Actualizamos cada 1 segundo
    ventana.after(1000, actualizar_estado)

# Crear la ventana principal
ventana = tk.Tk()
ventana.title("Monitoreo de Estacionamiento")

# Crear un rectángulo que servirá de indicador
indicador = tk.Label(ventana, width=20, height=5, relief="solid")
indicador.pack(pady=20)

# Crear un texto para mostrar el estado
estado_texto = tk.Label(ventana, text="Cargando...", font=("Arial", 16))
estado_texto.pack()

# Iniciar la actualización del estado cada 1 segundo
actualizar_estado()

# Iniciar la interfaz gráfica
ventana.mainloop()