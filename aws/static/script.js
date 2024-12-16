// script.js
function actualizarEstado() {
    fetch('http://54.210.65.125:8081/estacionamiento', {
        method: 'GET',
    })
    .then(response => response.json())
    .then(data => {
        const statusText = document.getElementById('status-text');
        const statusIndicator = document.getElementById('status-indicator');
        
        if (data.disponible) {
            // Estacionamiento disponible
            statusText.textContent = 'Disponible';
            statusIndicator.className = 'available';  // Cambiar a verde
        } else {
            // Estacionamiento ocupado
            statusText.textContent = 'Ocupado';
            statusIndicator.className = 'occupied';  // Cambiar a rojo
        }
    })
    .catch(error => {
        console.error('Error al obtener el estado del estacionamiento:', error);
    });
}

// Actualiza el estado cada 1 segundo
setInterval(actualizarEstado, 1000);

// Llamada inicial para obtener el estado de inmediato
actualizarEstado();
