import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import ssl

def enviar_correo(subject, body):
    print("Enviando correo...")  # Agregar esto para verificar si se ejecuta
    config = {
        "cuenta_gmail": "s.lopez15@ufromail.cl",
        "contraseña": "*"
    }
    smtp_server = "smtp.gmail.com"
    port = 587  # Puerto para StartTLS
    
    sender_email = config["cuenta_gmail"]
    receiver_email = config["cuenta_gmail"]  # Puedes usar otro destinatario si lo deseas
    password = config["contraseña"]
    
    # Crear el mensaje
    mensaje = MIMEMultipart()
    mensaje['From'] = sender_email
    mensaje['To'] = receiver_email
    mensaje['Subject'] = subject
    mensaje.attach(MIMEText(body, 'plain'))
    
    # Enviar el correo
    context = ssl.create_default_context()
    try:
        with smtplib.SMTP(smtp_server, port) as server:
            server.starttls(context=context)  # Iniciar conexión segura
            server.login(sender_email, password)
            server.sendmail(sender_email, receiver_email, mensaje.as_string())
            print("Correo enviado correctamente")
    except Exception as e:
        print(f"No se pudo enviar el correo. Error: {e}")
